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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

/**
 * This is a context adapter that is read-only and which performs
 * name checks and modifications for the "java:" namespace. In particular,
 * it 1) enforces read-only; 2) checks that all access is within "java:"; and
 * 3) removes the namespace before accessing the wrapped context.
 * 
 * @author Lars J. Nilsson
 */
public class JavaContext implements Context {

	private Context wrapped;

	public JavaContext(Context context) throws NamingException {
		if(context == null) {
			throw new OperationNotSupportedException("Context cannot be null");
		}
		this.wrapped = context;
	}

	@Override
	public Object lookup(Name name) throws NamingException {
		name = verifyNameInJavaContext(name);
		Object o = wrapped.lookup(name);
		if(o == null) {
			throw new NameNotFoundException();
		}
		return o;
	}

	@Override
	public Object lookup(String name) throws NamingException {
		return lookup(parse(name));
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void unbind(Name name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void unbind(String name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		name = verifyNameInJavaContext(name);
		return wrapped.list(name);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		return list(parse(name));
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		name = verifyNameInJavaContext(name);
		return wrapped.listBindings(name);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		return listBindings(parse(name));
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Context is read-only");
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		name = verifyNameInJavaContext(name);
		Object o = wrapped.lookupLink(name);
		if(o == null) {
			throw new NameNotFoundException();
		}
		return o;
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		return lookupLink(parse(name));
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		return wrapped.getNameParser(name);
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		return wrapped.getNameParser(name);
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		return wrapped.composeName(name, prefix);
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException {
		return wrapped.composeName(name, prefix);
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		throw new UnsupportedOperationException("Context is read-only");
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		throw new UnsupportedOperationException("Context is read-only");
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return wrapped.getEnvironment();
	}

	@Override
	public void close() throws NamingException {
		wrapped.close();
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		return wrapped.getNameInNamespace();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private Name verifyNameInJavaContext(Name name) throws NamingException {
		String first = (name.size() == 0 ? "" : name.get(0));
		if(!first.startsWith("java:")) {
			throw new NamingException("Incorrect context: name does not start with 'java:'");
		}
		Name tmp = (Name) name.clone();
		first = first.substring(5); // remove 'java:'
		tmp.remove(0);
		tmp.add(0, first);
		return tmp;
	}
	
	private Name parse(String name) throws NamingException {
		return getNameParser("").parse(name);
	}
}
