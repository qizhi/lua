/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.game;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TableInterceptorProvider;
import com.cubeia.firebase.api.game.TableListenerProvider;
import com.cubeia.firebase.api.game.TournamentGame;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TournamentTableListener;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This is a game with built in support for Guice injected processing. 
 * Implementors should override the {@link #getConfigurationHelp()} help method
 * to configure the injector context.
 * 
 * <p>This game is meant to be used for event processing only. It binds the game
 * processor and associated interfaces to the {@link EventScoped event scope} which is
 * only valid within the context of an executed event.
 * 
 * <p>For custom configuration, override the {@link #preInjectorCreation(List)}
 * method to add custom modules prior to the injection context creation, but be aware
 * that objects should be bound in {@link EventScoped} if not truly global
 * 
 * <p>This activator adds a {@link FirebaseModule}, an {@link EventModule} and
 * a {@link GameModule} to the injector configuration by default.
 * 
 * @author larsan
 */
public abstract class GuiceGame implements Game, TableListenerProvider, TableInterceptorProvider, TournamentGame {

	private final static NullTableProcessor NULL_PROCESSOR = new NullTableProcessor();
	
	protected GameContext context;
	private Injector injector;
	private GameProcessor processor;
	private TableListener listener;
	private TableInterceptor interceptor;
	private TournamentProcessor mttProcessor;
	
	private final List<ScopeListener> listeners = new CopyOnWriteArrayList<ScopeListener>();
	
	
	// --- GAME --- ///
	
	@Override
	public void destroy() { }

	@Override
	public void init(GameContext context) throws SystemException {
		this.context = context;
		createInjector();
		createMttProcessor();
		createProcessor();
		createInterceptor();
		createListener();
	}
	
	@Override
	public TournamentProcessor getTournamentProcessor() {
		return mttProcessor;
	}

	@Override
	public GameProcessor getGameProcessor() {
		return processor;
	}
	
	
	// --- INTERCEPTOR PROVICER --- //
	
	@Override
	public TableInterceptor getTableInterceptor(Table table) {
		return interceptor;
	}
	
	
	// --- LISTENER PROVIDER --- //
	
	@Override
	public TableListener getTableListener(Table arg0) {
		return listener;
	}


	// --- PACKAGE ACCESS --- //
	
	GameContext getContext() {
		return context;
	}

	
	// --- CONFIG METHDOS --- //
	
	/**
	 * @return The configuration to use, must not return null
	 */
	public abstract Configuration getConfigurationHelp();

	/**
	 * Override this method to add you own modules to the 
	 * injection context.
	 * 
	 * @param modules List of modules for the context, never null
	 */
	protected void preInjectorCreation(List<Module> modules) { }

	/**
	 * This method is called when the injector is created. Use if
	 * you need to access bound instances.
	 * 
	 * @param injector The game injector, never null
	 */
	protected void postInjectorCreation(Injector injector) { }
	
	
	/**
	 * Add a scope listener. Scope listeners are updated for 
	 * each event execution. Use {@link #postInjectorCreation(Injector)} to
	 * initiate with bound members.
	 * 
	 * @param listener Listener to add, must not be null
	 */
	protected void addScopeListener(ScopeListener listener) {
		if(listener != null) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a scope listener. Scope listeners are updated for 
	 * each event execution. Use {@link #postInjectorCreation(Injector)} to
	 * initiate with bound members.
	 * 
	 * @param listener Listener to remove, must not be null
	 */
	protected void removeScopeListener(ScopeListener listener) {
		if(listener != null) {
			listeners.remove(listener);
		}
	}
	
	
	// --- PRIVATE METHODS --- //

	private void createInjector() {
		List<Module> list = new ArrayList<Module>(5);
		addStandardModules(list);
		preInjectorCreation(list);
		injector = Guice.createInjector(list);
		postInjectorCreation(injector);
	}

	private void addStandardModules(List<Module> list) {
		list.add(new FirebaseModule(context.getServices()));
		list.add(new GameModule(this));
		// list.add(new Jsr250Module());
		list.add(new EventModule());
	}
	
	private void createProcessor() throws SystemException {
		if(!isBound(GameProcessor.class)) {
			throw new SystemException("No GameProcessor class bound! Consider using the Configuration class.");
		} else {
			processor = createScopeProxy(GameProcessor.class);
		}
	}
	
	private void createMttProcessor() throws SystemException {
		if(getConfigurationHelp().getTournamentProcessorClass() == null) {
			mttProcessor = NULL_PROCESSOR;
		} else {
			mttProcessor = createScopeProxy(TournamentProcessor.class);
		}
	}

	private boolean isBound(Class<?> cl) {
		return injector.getProvider(cl) != null;
	}
	
	private void createListener() {
		Class<? extends TableListener> cl = getConfigurationHelp().getTableListenerClass();
		if(cl != null) {
			// #663: Check for tournament listener...
			if(TournamentTableListener.class.isAssignableFrom(cl)) {
				listener = createScopeProxy(TournamentTableListener.class);
			} else {
				listener = createScopeProxy(TableListener.class);
			}
		} else {
			listener = NULL_PROCESSOR;
		}
	}
	
	private void createInterceptor() {
		if(getConfigurationHelp().getTableInterceptorClass() != null) {
			interceptor = createScopeProxy(TableInterceptor.class);
		} else {
			interceptor = NULL_PROCESSOR;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T createScopeProxy(Class<T> face) {
		ScopeProcessor handler = new ScopeProcessor(face, injector, new ScopeListenerProvider() {
			
			@Override
			public List<ScopeListener> getListeners() {
				return listeners;
			}
		});
		return (T) Proxy.newProxyInstance(
							getClass().getClassLoader(), 
							new Class<?>[] { face }, 
							handler);
	}
}
