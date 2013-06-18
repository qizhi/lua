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
package com.cubeia.firebase.server.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;

/**
 * This is a class loader switcher which check the return value: if it is a 
 * one of several nested classes that object will be proxied before returned. 
 * 
 * <p>The nested classes are, and their origin class/proxy:
 * 
 * <pre>
 * 	ServiceRegistry - Service, game and mtt context
 *  ResourceLocator - Service, game and mtt context
 *  TableScheduler - Table
 *  TournamentNotifier - Table 
 *  GameNotifier - Table
 *  TablePlayerSet - Table
 *  TableWatcherSet - Table
 *  LobbyTableAttributeAccessor - Table
 *  Scheduler - Mtt Instance
 *  TournamentPlayerRegistry - Mtt Instance
 *  MttTableCreator - Mtt Instance
 *  MttNotifier - Mtt Instance
 * </pre>
 * 
 * To be clear (?): this invocation handler should be used in a proxy given to a game, service or
 * tournament implementation, where the proxied object gives access to objects that must be 
 * proxied <em>in turn</em>. As such, the nested classes represents those secondary objects. 
 * 
 * @author Lars J. Nilsson
 */
public class InternalComponentInvocationHandler extends ContextClassLoaderInvocationHandler {
	
	private static final Class<?>[] FACES = { 
		ServiceRegistry.class, 
		ResourceLocator.class, 
		TableScheduler.class, 
		TournamentNotifier.class, 
		GameNotifier.class, 
		TablePlayerSet.class, 
		TableWatcherSet.class, 
		LobbyTableAttributeAccessor.class,
		Scheduler.class,
		TournamentPlayerRegistry.class,
		MttTableCreator.class, 
		MttNotifier.class };

	/**
	 * We'll cache all "hits" here. We can't do it immediately as we first
	 * need to look, not for concrete classes but also for subclasses. But if
	 * we've found a match we can reuse it. 
	 * 
	 * This map is between implementetsions -> iface
	 */
	private static final Map<Class<?>, Class<?>> cache = new ConcurrentHashMap<Class<?>, Class<?>>();
	
	/**
	 * This method checks if the given object's class matches at least
	 * one of the listed internal components, and return the component
	 * interface if it does. 
	 * 
	 * @param o The object to check
	 * @return The internal component interface or null
	 */
	private static Class<?> getProxyFace(Object o) {
		if(o == null) {
			return null; // SANITY CHECK
		} 
		Class<?> check = o.getClass();
		if(cache.containsKey(check)) {
			return cache.get(check);
		} else {
			for (Class<?> cl : FACES) {
				if(cl.isAssignableFrom(o.getClass())) {
					cache.put(check, cl);
					return cl;
				}
			}
			return null;
		}
	}
	
	
	// --- INSTANCE MEMBERS --- //
	
	public InternalComponentInvocationHandler(ClassLoader contextClassLoader, InvocationHandler nested) {
		super(contextClassLoader, nested);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object o = super.invoke(proxy, method, args);
		Class<?> iFace = getProxyFace(o);
		if(iFace != null && !Proxy.isProxyClass(o.getClass())) {
			return getProxy(iFace, o);
		} else {
			return o;
		}
	}

	private Object getProxy(Class<?> iFace, Object o) {
		InvocationHandlerAdapter root = new InvocationHandlerAdapter(o);
		return  Proxy.newProxyInstance(
					super.contextClassLoader, 
					new Class[] { iFace }, 
					new ContextClassLoaderInvocationHandler(contextClassLoader, root)
				);
	}
}
