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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.cubeia.firebase.api.action.AbortActionException;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.processing.ReceiverEventDaemonBase;
import com.cubeia.firebase.server.node.GameNodeContext;
import com.cubeia.firebase.server.processor.ActionGameRegistry;
import com.cubeia.firebase.server.processor.TableActionProcessor;
import com.cubeia.firebase.server.processor.TableActionScheduler;
import com.cubeia.firebase.server.routing.GameNodeRouter;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;
import com.cubeia.firebase.service.messagebus.util.ChannelPartitionFilter;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.SpaceObjectNotFoundException;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.transaction.TransactionException;
import com.cubeia.firebase.util.executor.JmxExecutor;

public class ReceivingGameEventDaemon extends ReceiverEventDaemonBase implements GameEventLoopback, Initializable<GameNodeContext> {

	private static final int LOG_SUPPRESSION_TIMEOUT = 1000;
    private static final String SCHEDULER_NAME = "ReceivingGameEventDaemon";
	private static final long PROCESSING_WARN_LIMIT_MS = 1000;

	private GameObjectSpace<FirebaseTable, GameAction> tableSpace;
	
	private GameNodeContext con;
	private JmxExecutor tableMover;
	private TableActionScheduler actionScheduler;
	private TableActionProcessor<GameAction> tableProcessor;
	private ChannelPartitionFilter filter;
	private TableMoveChangeListener handler;
	private JndiProvider eventContext;
	
	private final Logger log = Logger.getLogger(getClass());
	private final GameConfig config;
    private long warningTimestamp;
	private GameOrphanMBusListener orphanListener;
	private final ClassLoader deploymentClassLoader;
	
	ReceivingGameEventDaemon(String nodeId, GameConfig config/*, GameObjectSpace<FirebaseTable, GameAction> tableSpace*/, ClassLoader deploymentClassLoader) {
		super(nodeId);
		this.deploymentClassLoader = deploymentClassLoader;
		this.config = config;
	}
	
	// --- INITIALIZABLE --- //
	
	public void init(GameNodeContext con) throws SystemException {
		this.con = con;
		initEventContext(con);
		doSuperInit(con);
		initTableSpace();
		initTableMover();
		initScheduler();
		initProcessor();
		initListener();	
	}

	public void destroy() {
		destroyListener();
		destroyTableMover();
		super.destroy();
	}

	
	// --- BASE METHODS --- //
	
	@Override
	protected String getSchedulerName() {
		return SCHEDULER_NAME;
	}
	
	@Override
	protected void dispatch(RouterEvent event) {
		if (event.getRoutedEvent() instanceof GameEvent) {
			unwrapAndDispatch(event);	
		} else {
			log.warn("I was asked to process an unknown event type. That shouldn't be. Event: " + event);
		}
	}
	
	
	// --- GAME EVENT LOOP BACK --- //
	
	public boolean dispatch(GameEvent event) {
		long start = System.currentTimeMillis();
		MDC.put(MDC_TABLE_ID, event.getTableId());
		MDC.put(MDC_PLAYER_ID, event.getPlayerId());
		checkDispatchTrace("Dispatch entrance", event);
		try {
			/*
			 * START: HACKING IN NEW TRANSACTION SCHEME 
			 */
	    	CoreTransactionManager man = con.getServices().getServiceInstance(CoreTransactionManager.class);
	    	CoreTransaction trans = man.newTransaction(ContextType.GAME_EVENT);
	    	boolean ok = false; 
	    	try {
	    		ok = tableSpace.handle(event, tableProcessor);
	    		checkDispatchTrace("Dispatch pre-commit", event, Boolean.valueOf(ok));
	    		trans.commit();
	    		checkDispatchTrace("Dispatch post-commit", event);
	    	} catch(TransactionException e) {
	    		log.error("Could not commit transaction!", e);
	    	} catch(SpaceObjectNotFoundException e) {
	    		log.debug("Table " + e.getId() + " not found, forwarding to orphan listener.");
	    		orphanListener.orphanedEvent(event);
	    		trans.rollback();
	    	} catch(Throwable th) {
	    		Throwable rootException = ExceptionUtils.getRootCause(th);
	    		if ( rootException instanceof AbortActionException ) {
	    			// Action aborted, don't show stack trace, just the message
		    		log.info(rootException.getMessage());
		    		trans.rollback();
	    		} else {
	    			log.error("XXXError when handling action, rolling back transaction", th);
	    			trans.rollback();
	    		}
	    	}
			/*
			 * END: HACKING IN NEW TRANSACTION SCHEME
			 */
	        registerDispatch(event, start, ok);
			return ok;
		
		} finally {
			checkDispatchTrace("Dispatch exit", event);
    		MDC.remove(MDC_TABLE_ID);
			MDC.remove(MDC_PLAYER_ID);
		}
	}
    

	// --- STARTABLE --- // 

    public void start() {
		tableProcessor.start();
		super.start();
	}

	public void stop() {
		super.stop();
		tableProcessor.stop();
	}
	
	
	// --- HALTABLE --- //
	
	@Override
	public void halt() {
		tableMover.halt();
		super.halt();
	}
	
	@Override
	public void resume() {
		tableMover.resume();
		super.resume();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkDispatchTrace(String msg, Object...data) {
		if(log.isTraceEnabled()) {
			StringBuilder b = new StringBuilder(msg);
			b.append("; ");
			if(data != null) {
				for (int i = 0; i < data.length; i++) {
					b.append(data[i]);
					if(i + 1 < data.length) {
						b.append(", ");
					}
				}
			}
			log.trace(b.toString());
 		}
	}
	
	private void initEventContext(GameNodeContext con) {
		eventContext = con.getServices().getServiceInstance(JndiProvider.class);
	}
	
	private void registerDispatch(GameEvent event, long start, boolean ok) {
		long elapsed = System.currentTimeMillis() - start;
        stats.registerRawExecution(elapsed);
        stats.registerDispatchedEvent();
        if (elapsed > PROCESSING_WARN_LIMIT_MS && log.isDebugEnabled()) {
        	GameAction a = event.getAction();
        	String actString = (a == null ? null : a.toString());
        	if(a instanceof GameDataAction) {
        		actString = ((GameDataAction)a).toString(log.isTraceEnabled());
        	}
        	if (log.isDebugEnabled() && checkLogSuppression()) {
        	    log.debug("Long event processing time detected (ok: "+ok+"): " + elapsed + " ms. tid: "+event.getTableId()+", pid: "+event.getPlayerId()+", Action: " + actString);
        	    reportLongEventLog();
        	}
        }
	}
	
	private boolean checkLogSuppression() {
        if (System.currentTimeMillis() - warningTimestamp > LOG_SUPPRESSION_TIMEOUT) {
            return true;
        } else {
            return false;
        }
    }

    private void reportLongEventLog() {
        warningTimestamp = System.currentTimeMillis();
    }
	
    private void unwrapAndDispatch(RouterEvent ev) {
    	GameEvent event = (GameEvent)ev.getRoutedEvent();    	
    	try {
    		if (dispatch(event)) {
    			stats.registerExecutedEvent();
    		}
    	} finally {
    		ev.acknowledge();
    	}
    }
	
	private void doSuperInit(GameNodeContext con) {
		MBeanServer mbs = con.getMBeanServer();
		GameNodeRouter router = con.getNodeRouter();
		Receiver<RouterEvent> receiver = router.getGameEventReceiver();
		super.init(getSchedulerSize(), mbs, receiver);
	}
	
	private int getSchedulerSize() {
		return config.getEventDaemonThreads();
	}

	private void initTableSpace() {
		ServiceRegistry reg = con.getServices();
		TableSpaceServiceContract inst = reg.getServiceInstance(TableSpaceServiceContract.class);
		tableSpace = inst.getObjectSpace(FirebaseTable.class, GameAction.class);
	}
	
	private void destroyListener() {
		ServiceRegistry reg = con.getServices();
		MBusContract mbus = reg.getServiceInstance(MBusContract.class);
		ConnectionServiceContract conn = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		CommandReceiver rec = conn.getSharedConnection().getCommandReceiver();
		rec.removeCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, handler);
    	mbus.removeMBusListener(filter);
    	receiver.setOrphanEventListener(null);
	}
	
	private void initListener() {
		GameNodeRouter router = con.getNodeRouter();
		ServiceRegistry reg = con.getServices();
		MBusContract mbus = reg.getServiceInstance(MBusContract.class);
		ConnectionServiceContract conn = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		CommandReceiver rec = conn.getSharedConnection().getCommandReceiver();
		handler = new TableMoveChangeListener(tableMover, actionScheduler, tableSpace, router.getLocalPartition(), mbus);
		filter = new ChannelPartitionFilter(router.getLocalPartition(), handler);
		rec.addCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, handler);
		mbus.addMBusListener(filter);
		intiOrphanListening();
	}
	
	public ChannelPartitionFilter getChannelPartitionFilter() {
		return filter;
	}
	
	private void intiOrphanListening() {
		orphanListener = new GameOrphanMBusListener(con.getNodeRouter(), deploymentClassLoader);
		receiver.setOrphanEventListener(orphanListener);
	}

	private void initProcessor() {
		GameNodeRouter router = con.getNodeRouter();
		ServiceRegistry reg = con.getServices();
		tableProcessor = new TableActionProcessor<GameAction>(router.getClientEventSender(), router.getMttSender(), reg, getGameCreatorManager(reg), actionScheduler, config);
	}
	
    private ActionGameRegistry getGameCreatorManager(ServiceRegistry reg) {
		return new GameRegistry(getDeploymentManager(reg), eventContext);
	}
    
    private DeploymentManager getDeploymentManager(ServiceRegistry reg) {
		DeploymentServiceContract ser = reg.getServiceInstance(DeploymentServiceContract.class);
    	return ser.getDeploymentManager();
	}
	
	private void initScheduler() {
		actionScheduler = new TableActionScheduler(exec, stats);
        actionScheduler.setEventLoopBack(this);
	}
	
	private void initTableMover() {
		tableMover = new JmxExecutor(1, "GameEventDeamon-TableMover");  
	}
	
	private void destroyTableMover() {
		tableMover.stop();
	}
}
