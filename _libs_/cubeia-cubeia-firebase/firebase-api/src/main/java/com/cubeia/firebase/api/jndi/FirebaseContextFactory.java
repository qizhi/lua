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
package com.cubeia.firebase.api.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.osjava.sj.memory.MemoryContext;

/**
 * This context factory creates an in-memory context with a "left to right"
 * syntax direction and a '/' separator (for names). The created context does not
 * share state with any runtime context created within Firebase, this can only
 * be accessed via the "java:" namespace. So to access, for example, the runtime
 * transaction manager, do like this:
 * 
 * <pre>
 *   InitialContext ic = new InitialContext();
 *   TransactionManager man = (TransactionManager) ic.lookup("java:comp/env/TransactionManager");
 * </pre>
 * 
 * But again, the above will only work in an event execution context within Firebase. 
 * 
 * @author Lars J. Nilsson
 */
public class FirebaseContextFactory implements InitialContextFactory {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Context getInitialContext(Hashtable environment) throws NamingException {
        Hashtable clone = new Hashtable(environment);
		clone.put("jndi.syntax.direction", "left_to_right");
		clone.put("jndi.syntax.separator", "/");
		MemoryContext context = new MemoryContext(clone);
        return context;
	}
}
