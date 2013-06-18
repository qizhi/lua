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
package com.cubeia.firebase.server.mtt.event;

import javax.management.MBeanServer;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.cubeia.firebase.api.action.AbortActionException;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.event.processing.ReceiverEventDaemonBase;
import com.cubeia.firebase.server.mtt.MttNodeConfig;
import com.cubeia.firebase.server.mtt.MttNodeContext;
import com.cubeia.firebase.server.mtt.processor.MttActionProcessor;
import com.cubeia.firebase.server.processor.GameObjectProcessor;
import com.cubeia.firebase.server.routing.MttNodeRouter;
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

public class ReceivingMttEventDaemon extends ReceiverEventDaemonBase  implements MttEventLoopback, Initializable<MttNodeContext> {
	
	private static final String SCHEDULER_NAME = "ReceivingMttEventDaemon";

	
	// --- INSTANCE MEMBERS --- //
	
	private final Logger log = Logger.getLogger(getClass());
	private final MttNodeConfig config;
	
	private MttNodeContext con;	
	private GameObjectSpace<TransactionalMttState, MttAction> space;
	private MttActionScheduler actionScheduler;
	private MttActionProcessor processor;
	private JmxExecutor mttMover;
	private ChannelPartitionFilter filter;
	private MttChangeHandler handler;

	public ReceivingMttEventDaemon(String nodeId, MttNodeConfig config) {
		super(nodeId);
		this.config = config;
	}
	
	public GameObjectProcessor<MttAction> getMttActionProcessor() {
		return processor;
	}
	
	@Override
	public void destroy() {
		destroyMBusListener();
		destroyTableMover();
		super.destroy();
	}
	
	public void init(MttNodeContext con) throws SystemException {
		this.con = con;
		doSuperInit(con);
		initTableMover();
		initMttSpace();
		initScheduler();
		initProcessor();
		initMBusListener();
	}

	private void initMttSpace() {
		ServiceRegistry reg = con.getServices();
		TableSpaceServiceContract inst = reg.getServiceInstance(TableSpaceServiceContract.class);
		space = inst.getObjectSpace(TransactionalMttState.class, MttAction.class);
	}

	@Override
	protected void dispatch(RouterEvent event) {
		Event<?> evt = event.getRoutedEvent();
		if (evt instanceof MttEvent) {
			MttEvent mttEvent = (MttEvent) evt;
			try {
				dispatch(mttEvent);
			} finally {
				event.acknowledge();
			}
		} else {
			log.warn("I was asked to process an unknown event type. That shouldn't be. Event: " + event);
		}
	}
	
	public void dispatch(MttEvent event) {
		MDC.put(MDC_TOURNAMENT_ID, event.getMttId());
		MDC.put(MDC_PLAYER_ID, event.getPlayerId());
		try {
			/*
			 * START: HACKING IN NEW TRANSACTION SCHEME
			 */
	    	CoreTransactionManager man = con.getServices().getServiceInstance(CoreTransactionManager.class);
	    	CoreTransaction trans = man.newTransaction(ContextType.TOURNAMENT_EVENT);
	    	try {
	    		space.handle(event, processor);
	    		trans.commit();
	    	} catch(TransactionException e) {
	    		log.error("Could not commit transaction!", e);
	    	} catch(SpaceObjectNotFoundException e) {
	    		log.warn("Received event for unreachable tournament, id: " + event.getMttId());
	    		trans.rollback();
	    	} catch(Throwable th) {
	    		Throwable rootException = ExceptionUtils.getRootCause(th);
	    		if ( rootException instanceof AbortActionException ) {
	    			// Action aborted, don't show stack trace, just the message
		    		log.info(rootException.getMessage());
		    		trans.rollback();
	    		} else {
	    			log.error("Error when handling action, rolling back transaction", th);
	    			trans.rollback();
	    		}
	    	}
			/* 
			 * END: HACKING IN NEW TRANSACTION SCHEME
			 */
		} finally {
			MDC.remove(MDC_TOURNAMENT_ID);
			MDC.remove(MDC_PLAYER_ID);
		}
	}
	
	public ChannelPartitionFilter getChannelPartitionFilter() {
		return filter;
	}

	@Override
	protected String getSchedulerName() {
		return SCHEDULER_NAME;
	}
	
	
	// --- STARTABLE --- // 

	public void start() {
		processor.start();
		super.start();
	}

	public void stop() {
		super.stop();
		processor.stop();
	}
	
	
	// --- HALTABLE --- //
	
	@Override
	public void halt() {
		mttMover.halt();
		super.halt();
	}
	
	@Override
	public void resume() {
		mttMover.resume();
		super.resume();
	}
	
	
	// --- PRIVATE METHODS --- //
	
    /*
     * This is where we listen to the message bus in order to catch MTT
     * moves. This is needed for rescheduling of actions etc. /LJN
     */
	private void initMBusListener() {
		ConnectionServiceContract conn = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		CommandReceiver rec = conn.getSharedConnection().getCommandReceiver();
		MBusContract mbus = con.getServices().getServiceInstance(MBusContract.class);
		handler = new MttChangeHandler(space, mttMover, actionScheduler, con.getNodeRouter().getLocalPartition(), con.getServices().getServiceInstance(MBusContract.class));
		filter = new ChannelPartitionFilter(con.getNodeRouter().getLocalPartition(), handler);
		rec.addCommandListener(Constants.MTT_CREATION_COMMAND_CHANNEL, handler);
		mbus.addMBusListener(filter);
	}
	
	private void initTableMover() {
		mttMover = new JmxExecutor(1, "MttEventDeamon-MttMover");  
	}
	
	private void destroyTableMover() {
		mttMover.stopNow();
	}
	
    private void destroyMBusListener() {
    	ConnectionServiceContract conn = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		CommandReceiver rec = conn.getSharedConnection().getCommandReceiver();
    	MBusContract mbus = con.getServices().getServiceInstance(MBusContract.class);
    	rec.removeCommandListener(Constants.MTT_CREATION_COMMAND_CHANNEL, handler);
    	mbus.removeMBusListener(filter);
	}
	
	private void initProcessor() {
		processor = new MttActionProcessor(con.getServices(), con.getNodeRouter(), actionScheduler);
	}
	
	private void initScheduler() {
		actionScheduler = new MttActionScheduler();
		actionScheduler.setEventLoopBack(this);
	}
	
	private void doSuperInit(MttNodeContext con) {
		MBeanServer mbs = con.getMBeanServer();
		MttNodeRouter router = con.getNodeRouter();
		Receiver<RouterEvent> receiver = router.getMttEventReceiver();
		super.init(getSchedulerSize(), mbs, receiver);
	}
	
	private int getSchedulerSize() {
		return config.getEventDaemonThreads();
	}
}
