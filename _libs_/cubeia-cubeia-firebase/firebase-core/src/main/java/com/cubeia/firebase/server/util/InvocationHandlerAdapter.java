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

/**
 * This adapter takes an inner invocation handler and does this: if 
 * the inner handler is an instance of InvocationHandler the call will be
 * forwarded, otherwise the method will be invoked on the object directly.
 * 
 * @author Lars J. Nilsson
 */
public class InvocationHandlerAdapter implements InvocationHandler {

	protected final Object receiver;

	protected InvocationHandlerAdapter(InvocationHandlerAdapter copy) {
		this.receiver = copy.receiver;
	}
	
	public InvocationHandlerAdapter(Object receiver) {
		this.receiver = receiver;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(receiver instanceof InvocationHandler) {
			return ((InvocationHandler)receiver).invoke(proxy, method, args);
		} else {
			try {
				return method.invoke(receiver, args);
			} catch(Throwable th) {
				// Trac ticket #790
				Throwable t = th.getCause();
                // Check to see if it's an expected exception
                for( Class<?> c : method.getExceptionTypes() ) {
                    if (t.getClass().isAssignableFrom(c)) {
                        throw t;
                    }
                }
                throw th;
			}
		}
	}
}
