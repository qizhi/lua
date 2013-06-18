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

import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * This invocation handler sets the given context class loader on
 * each invocation, and resets it afterwards. 
 * 
 * @author Lars J. Nilsson
 */
public class ContextClassLoaderInvocationHandler extends InvocationHandlerAdapter {

	protected final ClassLoader contextClassLoader;

	public ContextClassLoaderInvocationHandler(ClassLoader contextClassLoader, InvocationHandler nested) {
		super(nested);
		this.contextClassLoader = contextClassLoader;
	}
	
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Throwable>() {
			
			@Override
			public Object invoke() throws Throwable {
				return ContextClassLoaderInvocationHandler.super.invoke(proxy, method, args);
			}
		}, contextClassLoader);
		
	}
}
