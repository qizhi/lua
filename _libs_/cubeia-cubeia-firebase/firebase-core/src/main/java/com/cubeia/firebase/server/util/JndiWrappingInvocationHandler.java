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

import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * This handler wraps execution in a JNDI context as per the given
 * event thread context class.
 * 
 * @author Lars J. Nilsson
 */
public class JndiWrappingInvocationHandler extends InvocationHandlerAdapter {

	private final JndiProvider con;

	public JndiWrappingInvocationHandler(InvocationHandler nested, JndiProvider con) {
		super(nested);
		this.con = con;
	} 

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		return con.wrapInvocation(new InvocationFacade<Throwable>() {
			@Override
			public Object invoke() throws Throwable {
				return JndiWrappingInvocationHandler.super.invoke(proxy, method, args);
			}
		});
	}
}