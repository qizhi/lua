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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.cubeia.firebase.api.action.AbstractPlayerAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.google.inject.Injector;

/**
 * This proxy handler is used by the Guice game to enter and
 * exit the event scope for each method invocation.
 * 
 * @author larsan
 */
public class ScopeProcessor implements InvocationHandler {

	private final EventScope scope;
	private final Class<?> clazz;
	private final Injector injector;
	
	private final ScopeListenerProvider listeners;

	/**
	 * @param clazz Class to proxy, must not be null
	 * @param injector Injector to use, must not be null
	 * @param listeners Scope listeners, must not be null
	 */
	public ScopeProcessor(Class<?> clazz, Injector injector, ScopeListenerProvider listeners) {
		this.listeners = listeners;
		this.scope = injector.getInstance(EventScope.class);
		this.injector = injector;
		this.clazz = clazz;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		scope.enter(getPlayerId(args), getTableInstance(args));
		enterListeners();
		try {
			Object target = injector.getInstance(clazz);
			if(target == null) {
				throw new IllegalStateException("Missing binding for the " + clazz.getName() + ", has it been removed from the Injector?");
			}
			return method.invoke(target, args);
		} finally {
			exitListeners();
			scope.exit();
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void enterListeners() {
		for (ScopeListener l : listeners.getListeners()) {
			try {
				l.enter();
			} catch(Throwable e) { }
		}
	}

	private void exitListeners() {
		for (ScopeListener l : listeners.getListeners()) {
			try {
				l.exit();
			} catch(Throwable e) { }
		}
	}

	private Integer getPlayerId(Object[] args) {
		for (Object o : args) {
			/*
			 * Oooh, this is ugly, but will actually work. The only *bare* ints in the
			 * interfaces are player ids... /LJN
			 */
			if(o instanceof Integer) {
				return (Integer) o;
			} else if(o instanceof SeatRequest) {
				return ((SeatRequest)o).getPlayerId();
			} else if(o instanceof GenericPlayer) {
				return ((GenericPlayer)o).getPlayerId();
			} else if(o instanceof AbstractPlayerAction) {
				return ((AbstractPlayerAction)o).getPlayerId();
			}
		}
		return -1;
	}

	private Table getTableInstance(Object[] args) {
		for (Object o : args) {
			if(o instanceof Table) {
				return (Table) o;
			}
		}
		throw new IllegalStateException("Table not found in arguments");
	}	
}
