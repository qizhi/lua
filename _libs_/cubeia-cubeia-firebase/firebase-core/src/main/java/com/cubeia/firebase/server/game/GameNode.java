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
package com.cubeia.firebase.server.game;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.commands.TableRemoved;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.game.activation.ActivationManager;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.node.BaseNode;
import com.cubeia.firebase.server.node.GameNodeContext;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.ChannelMoveContainer;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.util.ChannelPartitionFilter;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;

public class GameNode extends BaseNode<GameNodeContext> implements GameNodeMBean, Haltable {

    // private static final long MAX_MBUS_WAIT = 5000;

	/// --- INSTANCE MEMBERS --- ///
    
	private final Object promotionLock = new Object();
	
	private boolean isCoord;
	// private GameEventDaemon eventConsumer;
	private ReceivingGameEventDaemon daemon;
	private TableRemoveListener listener;
	private GameConfig config;

	private ActivationManager activationManager;
	
	public GameNode(String id) {
		super(id, ClusterRole.GAME_NODE);
	}

    public void destroy() {  
    	// destroyEventConsumer();
    	stopListenForRemoval();
    	if(daemon != null) {
    		daemon.destroy();
    	}
    	destroyJmx();
    	super.destroy();
    }

	public void init(GameNodeContext con) throws SystemException { 
        /*
         * Ticket #632 - Register a listener on the MBUS here that captures and stores relevant
         * events during the handshake. We might receive important MBus commands during startup 
         * and we must not lose them.
         */
		TemporaryMBusListener tempListener = new TemporaryMBusListener();
		MBusContract mbus = con.getServices().getServiceInstance(MBusContract.class);
		mbus.addMBusListener(tempListener);
        // End #632
		
        super.init(con); // Triggers handshake...
        setupConf();
        daemon = new ReceivingGameEventDaemon(getId(), config, con.getDeploymentClassLoader());
        daemon.init(con);
        
        /*
         * Ticket #632 - Hand over captured events to the ChannelChangeListener contained within
         * the receiving game event daemon to trigger re-scheduling of events
         */
        ChannelPartitionFilter filter = daemon.getChannelPartitionFilter();
        ChannelMoveContainer move = tempListener.getChannelMoved();
        if (move != null) {
        	filter.channelMoved(move.getFromPartition(), move.getToPartition(), move.getChannels());
        }
        // End #632
        
        initListenForRemval();
        initJmx();
    }
	
	public void halt() {
		if(daemon != null) {
			daemon.halt();
		}
	}
	
	public boolean isHalted() {
		if(daemon != null) {
			return daemon.isHalted();
		} else {
			return false;
		}
	}
	
	public void resume() {
		if(daemon != null) {
			// log.info("JMXSCH - Resume");
			daemon.resume();
		}
	}

	public void start() {
    	/*GameNodeRouter route = con.getNodeRouter();
        ServiceRegistry reg = con.getServices();
        MBeanServer mbs = con.getMBeanServer();
        TableSpaceServiceContract inst = reg.getServiceInstance(TableSpaceServiceContract.class);
        eventConsumer = new GameEventDaemon(getId(), inst.getObjectSpace(FirebaseTable.class, GameAction.class), route, mbs, reg);
        eventConsumer.start();*/
		daemon.start();
        synchronized(promotionLock) {
        	if(isCoord) setupActivators();
        	super.start();
        }
    }

	public void stop() {
        daemon.stop();
		// stopEventConsumer();
        synchronized(promotionLock) {
        	if(isCoord) destroyActivators();
        	super.stop();
        }
    }
	
	
	/// --- PROTECTED METHODS --- ///
	
	@Override
	protected boolean listenForPromotions() {
		return true;
	}
	
	@Override
	protected void promoted() {
        synchronized(promotionLock) {
        	isCoord = true;
        	if(isStarted()) {
        		setupActivators();
        	}
        }
        // We need to init all game and tournament deployments with a base lobby node.
        // This is OK to apply on an existing lobby tree so we just run it for every
        // promotion, which should not be that many.
        setInitalLobbyTree();
	}

	
	/// --- PRIVATE METHODS --- ///
	
	/**
	 * Iterate all game and tournament deployments and set an initial root path for the given deployments.
	 * 
	 */
	private void setInitalLobbyTree() {
		ServiceRegistry services = con.getServices();
		DeploymentServiceContract service = services.getServiceInstance(DeploymentServiceContract.class);
		DeploymentManager deploymentManager = service.getDeploymentManager();
		// SystemStateServiceContract systemState = services.getServiceInstance(SystemStateServiceContract.class);
		Lobby lobby = services.getServiceInstance(LobbyServiceContract.class).getLobby();
		
		for (GameDeployment game : deploymentManager.getAllGameDeployments().values()) {
			lobby.addPath(SystemStateConstants.TABLE_ROOT_FQN+game.getLatestRevision().getGameDefinition().getId());
			// systemState.setAttribute(SystemStateConstants.TABLE_ROOT_FQN+game.getLatestRevision().getGameDefinition().getId(), null, null);
		}
		
		for (TournamentDeployment tourn : deploymentManager.getAllTournamentDeployments().values()) {
			lobby.addPath(SystemStateConstants.TOURNAMENT_ROOT_FQN+tourn.getLatestRevision().getTournamentDefinition().getId());
			// systemState.setAttribute(SystemStateConstants.TOURNAMENT_ROOT_FQN+tourn.getLatestRevision().getTournamentDefinition().getId(), null, null);
		}
	}

	private void initListenForRemval() {
		ClusterConnection con = getClusterConnection();
		if(con == null) return; // SANITY CHECK
		CommandReceiver rec = con.getCommandReceiver();
		listener = new TableRemoveListener();
		String id = Constants.TABLE_CREATION_COMMAND_CHANNEL;
		rec.addCommandListener(id, listener);
	}

	private void stopListenForRemoval() {
		ClusterConnection con = getClusterConnection();
		if(con == null) return; // SANITY CHECK
		CommandReceiver rec = con.getCommandReceiver();
		String id = Constants.TABLE_CREATION_COMMAND_CHANNEL;
		rec.removeCommandListener(id, listener);
	}
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=GameNode");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=GameNode");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	/*private void stopEventConsumer() {
		if(eventConsumer != null) {
        	eventConsumer.stop();
        }
	}
	
	private void destroyEventConsumer() {
		if(eventConsumer != null) {
			eventConsumer.destroy();
		}
	}*/
	
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

	private void setupActivators() {
		activationManager = new ActivationManager(getActivatorHaltOnInit());
		try {
			activationManager.init(this.con);
		} catch (SystemException e) {
			log.fatal("Failed to initiate game activation manager! Recevied message: " + e.getMessage(), e);
		}
	}
	
	private boolean getActivatorHaltOnInit() {
		return config.getActivatorHaltOnInitError();
	}

	private void destroyActivators() {
		activationManager.destroy();
		activationManager = null;
	}

	private boolean isStarted() {
		return super.state == State.STARTED;
	}
	
	private void setupConf() throws SystemException {
		ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		config = contr.getConfiguration(GameConfig.class, NodeRoles.getNodeNamespace(ClusterRole.GAME_NODE, getId()));
		log.info("Configuration found for node: " + getId() + " (" + config.getName() + ")");
	}
	
	
	// --- PRIVATE CLASSES --- ///
	
	private class TableRemoveListener implements CommandListener {

		private final Partition part = con.getNodeRouter().getLocalPartition();
		private final ClientRegistry reg = con.getServices().getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
		private final GameObjectSpace<FirebaseTable, GameAction> space;
		
		private TableRemoveListener() {
			TableSpaceServiceContract serv = con.getServices().getServiceInstance(TableSpaceServiceContract.class);
			space = serv.getObjectSpace(FirebaseTable.class, GameAction.class);
		}
		
		public Object commandReceived(CommandMessage c) {
			if(c.command instanceof TableRemoved) {
				process((TableRemoved)c.command);
			}
			return null;
		}

		private void process(TableRemoved com) {
			TableCommandData att = com.getAttachment();
			if(isMyPartition(att)) {
				FirebaseTable table = space.peek(att.getId());
				if(table != null) {
					// Ticket #366, remove trailing players...
					checkRemoveSittingPlayers(table);
				}
				space.remove(att.getId());
			}
		}

		private void checkRemoveSittingPlayers(FirebaseTable table) {
			for (GenericPlayer p : table.getPlayerSet().getPlayers()) {
				reg.removeClientTable(p.getPlayerId(), table.getId());
			}
		}

		private boolean isMyPartition(TableCommandData att) {
			return part.equals(att.getPartition());
		}
	}
	
	/**
	 * This internal class only exists to provide a fix for #632
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

