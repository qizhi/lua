/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.server.mtt;

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttDestroyedAction;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.mtt.comm.MttFinalized;
import com.cubeia.firebase.mtt.comm.MttRemoved;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.mtt.activator.MttActivationManager;
import com.cubeia.firebase.server.mtt.event.ReceivingMttEventDaemon;
import com.cubeia.firebase.server.node.BaseNode;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.ChannelMoveContainer;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.messagebus.util.ChannelPartitionFilter;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.SpaceObjectNotFoundException;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

public final class MttNode extends BaseNode<MttNodeContext> implements MttNodeMBean {
	
	// private static final long MAX_MBUS_WAIT = 5000;
    private static final int THREAD_COUNT = 1;

	private final Object promotionLock = new Object();
	private boolean isCoord;
	
    private MttNodeConfig config;
	private ReceivingMttEventDaemon daemon;
	// private CommandContext commandContext;
	private JmxExecutor executor;

	private GameObjectSpace<TransactionalMttState, MttAction> objectSpace;
	private MttActivationManager activationManager;
	
	private MttRemoveListener listener;

	
	
	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/
	
	
	public MttNode(String id) {
		super(id, ClusterRole.MTT_NODE);
	}

	
	
	/*------------------------------------------------
	 
	 	LIFE CYCLE METHODS
	  
	 ------------------------------------------------*/

	
	@Override
	public void init(MttNodeContext con) throws SystemException {
		
        /* Ticket #634 - Register a listener on the MBUS here that captures and stores relevant
        * events during the handshake. We might receive important MBus commands during startup 
        * and we must not lose them.
        */
		TemporaryMBusListener tempListener = new TemporaryMBusListener();
		MBusContract mbus = con.getServices().getServiceInstance(MBusContract.class);
		mbus.addMBusListener(tempListener);
       // End #632
		
		super.init(con);
		setupConf();
		// waitForMBus();
        daemon = new ReceivingMttEventDaemon(getId(), config);
		daemon.init(con);
		initConn();
		
        /*
         * Ticket #634 - Hand over captured events to the ChannelChangeListener contained within
         * the receiving mtt event daemon to trigger re-scheduling of events
         */
        ChannelPartitionFilter filter = daemon.getChannelPartitionFilter();
        ChannelMoveContainer move = tempListener.getChannelMoved();
        if (move != null) {
        	filter.channelMoved(move.getFromPartition(), move.getToPartition(), move.getChannels());
        }
        // End #632
        
        initListenForRemval();
		initJmx();
		initMttCreationListener();
	}

    @Override
	public void start() {
		ServiceRegistry reg = con.getServices();
		TableSpaceServiceContract inst = reg.getServiceInstance(TableSpaceServiceContract.class);
        objectSpace = inst.getObjectSpace(TransactionalMttState.class, MttAction.class);
		daemon.start();
        synchronized(promotionLock) {
        	log.debug("Starting - Before activation; isCord: " + isCoord);
        	if(isCoord) setupActivators();
        	super.start();
        }
	}
    
	private boolean isStarted() {
		return super.state == State.STARTED;
	}
    
    @Override
    public void stop() {
    	if(daemon != null) {
    		daemon.stop();
    	}
        synchronized(promotionLock) {
        	if(isCoord) destroyActivators();
        	super.stop();
        }
    }
	
    @Override
	public void destroy() {
		stopListenForRemoval();
		daemon.destroy();
		destroyConn();
		destroyJmx();
		super.destroy();
	}
	
	@Override
	protected boolean listenForPromotions() {
		return true;
	}
	
	@Override
	protected void promoted() {
        synchronized(promotionLock) {
        	isCoord = true;
        	log.debug("Promoting - Before activation; isStarted(): " + isStarted());
        	if(isStarted()) {
        		setupActivators();
        	}
        }
	}
	

	
	
	
	/*------------------------------------------------
	 
	 	PRIVATE METHODS
	  
	 ------------------------------------------------*/
	
	/**
	 * Create an mbus listener on created mtt channels.
	 */
    private void initMttCreationListener() {
        executor = new JmxExecutor(1, THREAD_COUNT, "MTT-InitExecutor");
        ConnectionServiceContract conn = con.getServices().getServiceInstance(ConnectionServiceContract.class);
        conn.getSharedConnection().getCommandReceiver().addCommandListener(Constants.MTT_CREATION_COMMAND_CHANNEL, new CommandListener() {
		
			public Object commandReceived(CommandMessage c) {
				if(c.command instanceof MttFinalized) {
					PartitionMap map = getMBus().getCurrentPartitionMap();
					List<Integer> list = new LinkedList<Integer>();
					for (int id : ((MttFinalized)c.command).getAttachment()) {
						Partition p = map.getPartitionForChannel(EventType.MTT, id);
						if(MttNode.this.con.getNodeRouter().getLocalPartition().equals(p)) {
							list.add(id);
						}
					}
					log.debug("Scheduling init on emerged for MTTs: " + list);
					scheduleMttInitialization(list);
				} 
				return null;
			}
		});
        
		/*MBusContract mbus = getMBus();
		mbus.addMBusListener(new MBusListenerAdapter() {
            @Override
            public void channelAdded(Partition part, Channel[] channels) {
                if(part.equals(MttNode.this.con.getNodeRouter().getLocalPartition())) {
                    scheduleMttInitialization(channels);
                }
            }
        });*/
    }

    /**
     * Schedule an asynchronous sender of mtt created actions to the new mtt:s.
     * @param channels the mtt:s created
     */
	protected void scheduleMttInitialization(List<Integer> channels) {
        for (int id : channels) {
            final int mttId = id; // ch.getId();
            executor.submit(new SafeRunnable() {
                @Override
                protected void innerRun() {
                    processCreatedAction(mttId);
                }
            });
        }
    }

    private void processCreatedAction(int mttId) {
    	CoreTransactionManager man = con.getServices().getServiceInstance(CoreTransactionManager.class);
    	CoreTransaction trans = man.newTransaction(ContextType.TOURNAMENT_EVENT);
    	boolean done = false;
    	try {
    		objectSpace.handle(new MttEvent(-1, mttId, new MttCreatedAction(mttId)), daemon.getMttActionProcessor());
    		trans.commit();
    		done = true;
    	} catch (SpaceObjectNotFoundException e) {
			log.error("Could not find tournament to init!", e);
		} finally {
    		if(!done) {
    			trans.rollback();
    		}
    	}
    	/*objectSpace.handle(mttId, new Processor<TransactionalMttState>() {
            public boolean process(TransactionalMttState txMttState) {
                MttActionProcessor proc = eventConsumer.getMttActionProcessor();
                proc.handleAction(txMttState, new MttCreatedAction(txMttState.getId()));
                return true;
            }
        });*/
    }
    
	private void processDestroyedAction(int mttId) {
    	CoreTransactionManager man = con.getServices().getServiceInstance(CoreTransactionManager.class);
    	CoreTransaction trans = man.newTransaction(ContextType.TOURNAMENT_EVENT);
    	boolean done = false;
    	try {
    		objectSpace.handle(new MttEvent(-1, mttId, new MttDestroyedAction(mttId)), daemon.getMttActionProcessor());
    		trans.commit();
    		done = true;
    	} catch (SpaceObjectNotFoundException e) {
			log.error("Could not find tournament to destroy!", e);
		} finally {
    		if(!done) {
    			trans.rollback();
    		}
    	}
    	/*objectSpace.handle(mttId, new Processor<TransactionalMttState>() {
            public boolean process(TransactionalMttState txMttState) {
                MttActionProcessor proc = eventConsumer.getMttActionProcessor();
                proc.handleAction(txMttState, new MttDestroyedAction(txMttState.getId()));
                return true;
            }
        });*/
    }
	
	private void initListenForRemval() {
		ClusterConnection con = getClusterConnection();
		if(con == null) return; // SANITY CHECK
		CommandReceiver rec = con.getCommandReceiver();
		listener = new MttRemoveListener();
		String id = Constants.MTT_CREATION_COMMAND_CHANNEL;
		rec.addCommandListener(id, listener);
	}

	private void stopListenForRemoval() {
		ClusterConnection con = getClusterConnection();
		if(con == null) return; // SANITY CHECK
		CommandReceiver rec = con.getCommandReceiver();
		String id = Constants.MTT_CREATION_COMMAND_CHANNEL;
		rec.removeCommandListener(id, listener);
	}
	
	private void initConn() {
		// commandContext = new CommandContext(getClusterConnection());
	}

    private void destroyConn() {
		// commandContext.destroy();
	}

	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=MttNode");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void destroyJmx() {
		if(con == null) return; // sanity check
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=MttNode");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void setupConf() throws SystemException {
		ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		config = contr.getConfiguration(MttNodeConfig.class, NodeRoles.getNodeNamespace(ClusterRole.MTT_NODE, getId()));
		log.info("Configuration found for node: " + getId() + " (" + config.getName() + ")");
	}
	
	private void setupActivators() {
		activationManager = new MttActivationManager();
		try {
			activationManager.init(this.con);
		} catch (SystemException e) {
			log.fatal("Failed to initiate tournament activation manager! Recevied message: " + e.getMessage(), e);
		}
	}
	
	private void destroyActivators() {
		activationManager.destroy();
		activationManager = null;
	}
	
	/*
	 * This method is a major hack. It forcibly halts the 
	 * startup channel to make sure the message bus, which is updating
	 * asynchronously, is in the right state. 
	 */
	/*private void waitForMBus() throws SystemException {
		Partition p = null;
		MBusContract serv = con.getServices().getServiceInstance(MBusContract.class);
		long next = System.currentTimeMillis() + MAX_MBUS_WAIT;
		while(next > System.currentTimeMillis()) {
			p = serv.getCurrentPartitionMap().getPartition(getId());
			if(p != null) {
				break; // LOOP BREAK
			} else {
				try {
					Thread.sleep(10);
				} catch(InterruptedException e) {
					break; // LOOP BREAK
				}
			}
		}
		if(p == null) {
			throw new SystemCoreException("Cannot contiue Game node startup; The message bus has not been updated correctly.");
		}
	}*/
	
	
	// --- PRIVATE CLASSES --- //
	
	/*private static final class CommandContext implements MttCommandContext {
		
		private final ClusterConnection con;
		// private final MttCommandFilter filter;
		
		private CommandContext(ClusterConnection con) {
			// filter = new MttCommandFilter();
			// con.getCommandReceiver().addCommandListener(filter);
			this.con = con;
		}
		
		private void destroy() {
			// con.getCommandReceiver().removeCommandListener(filter);
			// filter.clear();
		}

		public CommandDispatcher getCommandDispatcher() {
			return con.getCommandDispatcher();
		}
		
		public CommandReceiver getCommandReceiver() {
			return con.getCommandReceiver();
		}
	}*/
	
	private class MttRemoveListener implements CommandListener {

		private final Partition part = con.getNodeRouter().getLocalPartition();
		
		private MttRemoveListener() { }
		
		public Object commandReceived(CommandMessage c) {
			if(c.command instanceof MttRemoved) {
				process((MttRemoved)c.command);
			}
			return null;
		}

		private void process(MttRemoved com) {
			if(isMyPartition(com)) {
				if(com.isPre()) {
					log.debug("Destroying MTT: " + com.getMttId());
					processDestroyedAction(com.getMttId());
				} else {
					log.debug("Removing MTT: " + com.getMttId());
					objectSpace.remove(com.getMttId());
				}
			}
		}

		private boolean isMyPartition(MttRemoved com) {
			return part.equals(com.getPartition());
		}
	}
	
	/**
	 * This internal class only exists to provide a fix for #634
	 * 
	 * @author Fredrik
	 */
	private class TemporaryMBusListener implements MBusListener {

		private ChannelMoveContainer lastMove;
		
		public ChannelMoveContainer getChannelMoved() {
			return lastMove;
		}

		@Override
		public void channelMoved(Partition from, Partition to, Channel[] channels) {
			lastMove = new ChannelMoveContainer(from, to, channels);
		}

		public void channelAdded(Partition part, Channel[] channels) {}
		public void channelRemoved(Partition part, Channel[] channels) {}
		public void partitionCreated(Partition part, Channel[] channels) {}
		public void partitionDropped(Partition part) {}
	}
}
