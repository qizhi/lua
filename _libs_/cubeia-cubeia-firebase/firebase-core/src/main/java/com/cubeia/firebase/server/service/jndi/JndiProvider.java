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
package com.cubeia.firebase.server.service.jndi;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * This service sets up the JNDI context used by Firebase and has
 * the ability to wrap invocations in a runtime context.
 * 
 * @author Lars J. Nilsson
 */
public interface JndiProvider extends Contract {

	/**
	 * Wrap the given invocation in a JNDI context. The context will be
	 * thread-local and only accessible within the invocation.
	 * 
	 * @param facade Invocation to perform, must not be null
	 * @return The return value of the invocation
	 * @throws T Any invocation error
	 */
	public <T extends Throwable> Object wrapInvocation(InvocationFacade<T> facade) throws T;
	
}
