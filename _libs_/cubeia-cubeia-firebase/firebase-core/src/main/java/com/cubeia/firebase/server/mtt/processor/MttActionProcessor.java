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
package com.cubeia.firebase.server.mtt.processor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.mtt.MTTLogic;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.mtt.MttNotifierImpl;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.mtt.MttCommandContext;
import com.cubeia.firebase.server.mtt.MttInstanceImpl;
import com.cubeia.firebase.server.mtt.tables.MttTableCreatorImpl;
import com.cubeia.firebase.server.processor.GameObjectProcessor;
import com.cubeia.firebase.server.routing.MttNodeRouter;
import com.cubeia.firebase.server.service.PublicServiceRegistry;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.util.InternalComponentInvocationHandler;
import com.cubeia.firebase.server.util.InvocationHandlerAdapter;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.executor.JmxScheduler;

public class MttActionProcessor implements GameObjectProcessor<MttAction> {
    
	private State state = State.STOPPED;
	
	/*
	 * The command context is used for sending and receiving
	 * commands regarding table creations.
	 */
	// private MttCommandContext commandContext;
	
	private MttRegistry registry;
    // private MttNotifier mttNotifier;
    private MttActionScheduler scheduler;
    private JmxScheduler tableCreatorScheduler;
	private SystemStateServiceContract sysState;
	private MttTableCreatorImpl tableCreator;

	private final ServiceRegistry services;
    private WrappingSender<GameEvent> gameEventSender;
    private WrappingSender<ClientEvent<?>> clientEventSender;
 
	private final JndiProvider jndiProvider;
    
	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/

	/**
	 * Empty constructor
	 * @param mttNodeRouter 
	 */
    public MttActionProcessor(ServiceRegistry services, MttNodeRouter mttNodeRouter, MttActionScheduler scheduler) {
    	this.services = services;
		this.scheduler = scheduler;
		registry = new MttRegistry(services);
        gameEventSender = mttNodeRouter.getGameEventSender();
        clientEventSender = mttNodeRouter.getClientEventSender();
        sysState = services.getServiceInstance(SystemStateServiceContract.class);
        jndiProvider = services.getServiceInstance(JndiProvider.class);
    }
	
	
	
	
	/*------------------------------------------------
	 
	 	LIFE CYCLE METHODS
	  
	 ------------------------------------------------*/
	
	public void start() {
		tableCreatorScheduler = new JmxScheduler(1, "MttTableCreatorScheduler");
		tableCreator = new MttTableCreatorImpl(services, tableCreatorScheduler, sysState);
		tableCreator.init();
        scheduler.start();
		tableCreatorScheduler.start();
		state = State.STARTED;
	}

	public void stop() {
		tableCreator.destroy();
	    tableCreatorScheduler.stop();
		state = State.STOPPED;
	}

	
	
	/*------------------------------------------------
	 
		PROCESSOR 
		
	 ------------------------------------------------*/
	
	/**
	 * Process the action for the given state.
	 * 
	 * @param mttState, state for the tournament
	 * @param action, the action to process
	 */
	public void handleAction(final TransactionalMttState mttState, final MttAction action) {
		final MttInstanceImpl mttInstance = new MttInstanceImpl(mttState, sysState);
		if (state.equals(State.STARTED)) {
			jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
		    	@Override 
		    	public Object invoke() throws RuntimeException {
		    		handleInJndiContext(mttState, action, mttInstance);
		    		return null;
		    	}
			});
		}
	}


    /*------------------------------------------------
		 
		PUBLIC ACCESSORS AND MUTATORS
	
	 ------------------------------------------------*/

	@Deprecated
	public MttCommandContext getCommandContext() {
		// return commandContext;
		return null;
	}

	@Deprecated
	public void setCommandContext(MttCommandContext commandContext) {
		// this.commandContext = commandContext;
	}
	
	
	
	
	/*------------------------------------------------
	 
		PRIVATE METHODS

	 ------------------------------------------------*/

	private MTTLogic getMttLogicInstance(int id) {
		MTTLogic mtt = registry.getMttInstance(id);
		// mtt.setTableCreator(tableCreator);
		return mtt;
	}
	
	/*
	 * This call should only be made after a JNDI context have been mounted. 
	 */
	private void handleInJndiContext(TransactionalMttState mttState, final MttAction action, MttInstanceImpl mttInstance) {
		mttState.setActionScheduler(scheduler);
		setDependencies(mttState, mttInstance);
		final MTTLogic mtt = getMttLogicInstance(mttInstance.getState().getMttLogicId());
		
		// FIXME: Thread-local hack for #566
		// setThreadLocalNotifier(mttInstance, mtt);
		// FIXME: Thread-local hack for #568
		// setThreadLocalTableCreator(mttInstance, mtt);

		/*
		 * PROXY THE MTT INSTANCE IN ORDER TO RESET THE CLASS
		 * LOADER FOR ANY CALLBACK TO THE PLATFORM
		 */
		InvocationHandler root = new InvocationHandlerAdapter(mttInstance);
		InvocationHandler switcher = new InternalComponentInvocationHandler(getClass().getClassLoader(), root);
		final MttInstance proxy = (MttInstance) Proxy.newProxyInstance(MttInstance.class.getClassLoader(), new Class[] { MttInstance.class }, switcher);
		
		/*
		 * HANDLE ACTION WITHIN THE RIGHT CLASS LOADER CONTEXT
		 */
		Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
			
			@Override
			public Object invoke() throws RuntimeException {
				mtt.handle(action, proxy);
				return null;
			}
		}, mtt.getClass().getClassLoader());
		
		mttState.setActionScheduler(null);
		
		// cleanupThreadLocals(mtt);
		
		clearDependencies(mttInstance);
	}

	private void checkTransaction(MttNotifierImpl notifier) {
		CoreTransactionManager manager = services.getServiceInstance(CoreTransactionManager.class);
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new NotifierResource(notifier));
		}
	}

	private void setDependencies(TransactionalMttState mttState, MttInstanceImpl mttInstance) {
		mttInstance.setScheduler(mttState.getScheduler());
		// DefaultLobbyMutator mutator = new DefaultLobbyMutator();
		ServiceRegistry serviceRegistry = new PublicServiceRegistry(InternalComponentAccess.getRegistry());
		mttInstance.setServiceRegistry(serviceRegistry);
		mttInstance.setMttCreator(tableCreator);
		MttNotifierImpl notifier = new MttNotifierImpl(gameEventSender, clientEventSender, true);
		mttInstance.setMttNotifier(notifier);
        checkTransaction(notifier);
	}
	
	private void clearDependencies(MttInstanceImpl mttInstance) {
		mttInstance.setScheduler(null);
		mttInstance.setServiceRegistry(null);
		mttInstance.setMttCreator(null);
		mttInstance.setMttNotifier(null);
	}
}
