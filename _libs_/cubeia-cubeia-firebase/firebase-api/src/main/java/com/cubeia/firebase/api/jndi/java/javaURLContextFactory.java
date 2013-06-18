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
package com.cubeia.firebase.api.jndi.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.jndi.FirebaseContextFactory;

/**
 * This context factory creates {@link JavaContext} for accessing the
 * "java:" namespace.
 * 
 * @author Lars J. Nilsson
 */
public class javaURLContextFactory implements ObjectFactory {
	
	private static final ThreadLocal<Entry> WRAP = new ThreadLocal<Entry>();

	public static void enter(Context con) {
		WRAP.set(new Entry(WRAP.get(), con));
	}
	
	public static void exit() {
		Entry e = WRAP.get();
		if(e != null) {
			WRAP.set(e.previous);
		}
	}
	
    @SuppressWarnings("rawtypes")
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env) throws NamingException {
        Entry entry = WRAP.get();
        Context c = (entry == null ? null : entry.con);
        if(c == null) {
        	Logger.getLogger(getClass()).debug("Java context created outside of JNDI provider; Will be created with a new, empty memory context!");
        	c = new FirebaseContextFactory().getInitialContext(env);
        }
    	return new JavaContext(c);
    }
    
    private static class Entry {
    	
    	private final Entry previous;
		private final Context con;
    	
    	private Entry(Entry prev, Context con) {
    		this.previous = prev;
			this.con = con;
    	}
    }
}
