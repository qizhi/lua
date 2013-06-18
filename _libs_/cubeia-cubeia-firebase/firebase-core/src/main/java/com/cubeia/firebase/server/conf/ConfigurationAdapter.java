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
package com.cubeia.firebase.server.conf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.firebase.server.util.StringListArray;


/**
 * This adapted fulfills interface declaration based on 
 * a configuration. It maps configuration properties by
 * namespace (declared in the interface by annotation) and 
 * properties to "get"/"is"/"has"/"do" methods. Return types are checked and
 * will be fulfilled if 1) they are primitives; 2) they
 * have a constructor taking a single string; or 3) can be
 * resolved to an enum value (by matching the enum toString 
 * with the string property). It also handle the special case 
 * {@link StringList} and InetAddress.
 * 
 * <p>Capitalized methods names (getMyProperty) will be matched
 * against either hyphen (my-property) or underscore (my_property).
 * 
 * <p>More info coming...
 * 
 * @author lars.nilsson
 */

public class ConfigurationAdapter {
	
	private static final Byte NULL_BYTE = new Byte((byte)-1);
	private static final Short NULL_SHORT = new Short((short)-1);
	private static final Integer NULL_INT = new Integer((int)-1);
	private static final Long NULL_LONG = new Long((long)-1);
	private static final Float NULL_FLOAT = new Float((float)-1);
	private static final Double NULL_DOUBLE = new Double((double)-1);
	private static final Boolean NULL_BOOL = Boolean.FALSE;
	private static final Character NULL_CHAR = new Character((char)0);
	
	
	/// --- INSTANCE MEMBERS --- ///

	private final Configuration conf;
	private final Map<IFaceHandle, Meta> cache;
	
	private final Logger log;

	public ConfigurationAdapter(Configuration conf) {
		log = Logger.getLogger(getClass());
		cache = new ConcurrentHashMap<IFaceHandle, Meta>();
		Arguments.notNull(conf, "conf");
		this.conf = conf;
	}

	public <T extends Configurable> T implement(Class<T> iface) {
		return implement(iface, null);
	}
		
	@SuppressWarnings("unchecked")
	public <T extends Configurable> T implement(Class<T> iface, Namespace forcedNs) {
		Arguments.notNull(iface, "interface");
		checkReturnTypes(iface);
		Meta meta = collectMeta(iface, forcedNs);
		return (T)createProxy(meta);
	}
	
	
	/// --- PRIVATE METHODS --- ///

	private void checkReturnTypes(Class<? extends Configurable> iface) {
		Method[] methods = iface.getMethods();
		for (Method method : methods) {
			checkReturnType(method);
		}
	}

	private void checkReturnType(Method m) {
		Class<?> type = m.getReturnType();
		if(!isPrimitive(type) && 
		   !haveStringConstructor(type)) {
			
			throw new IllegalStateException("Method '" + m.getName() + "' returns an invalid type; type is not primitive and does not have a string constructor");
		}
	}

	private boolean haveStringConstructor(Class<?> type) {
		if(type == Character.class) return true; // SPECIAL CASE
		if(type.isEnum()) return true; // SPECIAL CASE
		if(type.equals(StringList.class)) return true; // SPECIAL CASE
		if(type.equals(InetAddress.class)) return true;
		try {
			return type.getConstructor(new Class[] { String.class }) != null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}

	private Object createProxy(Meta meta) {
		Handler h = new Handler(meta);
		ClassLoader load = getClass().getClassLoader();
		Class<?>[] classes = { meta.iface };
		return Proxy.newProxyInstance(load, classes, h);
	}

	private Meta collectMeta(Class<? extends Configurable> iface, Namespace forcedNs) {
		IFaceHandle h = new IFaceHandle(forcedNs, iface);
		if(cache.containsKey(h)) return cache.get(h);
		else {
			Meta m = new Meta(iface, forcedNs);
			cache.put(h, m);
			return m;
		}
	}
	
	
	/// --- INNER CLASSES --- ///
	
	private class Handler implements InvocationHandler {
		
		private final Meta meta;

		private Handler(Meta meta) {
			this.meta = meta;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String val = matchProperty(method);
			if(val == null) {
				return toNull(method);
			}
			else return toObject(method, val);
		}
		
		
		/// --- PRIVATE METHODS --- ///

		private Object toObject(Method method, String val) {
			Class<?> type = method.getReturnType();
			if(isPrimitive(type)) return toPrimitive(type, val);
			else if(type == Character.class) {
				// SPECIAL CASE
				if(val.length() == 0) return null;
				else return new Character(val.charAt(0));
			} else if(type.isEnum()) {
				return matchEnum(type, val);
			} else if(type.equals(StringList.class)) {
				return toStringList(val);
			} else if(type.equals(InetAddress.class)) {
				try {
					return InetAddress.getByName(val);
				} catch(Exception e) {
					log.warn("Failed to construct return type '" + type.getName() + "' for method '" + method.getName() + "' in class '" + meta.iface.getName() + "'", e);
					return null;
				}
			} else {
				try {
					Constructor<?> cons = type.getConstructor(new Class[] { String.class });
					return cons.newInstance(new Object[] { val });
				} catch (Exception e) {
					log.warn("Failed to construct return type '" + type.getName() + "' for method '" + method.getName() + "' in class '" + meta.iface.getName() + "'", e);
					return null;
				} 
			}
		}

		private Object toStringList(String val) {
			final String[] arr = val.split(",");
			return new StringListArray(arr);
		}

		private Object matchEnum(Class<?> type, String val) {
			String tmp = val.toLowerCase();
			for(Object o : type.getEnumConstants()) {
				String s = o.toString().toLowerCase();
				if(tmp.equals(s)) return o;
			}
			return null;
		}
		
		private Object toPrimitive(Class<?> type, String val) {
			if(type == Byte.TYPE) return new Byte(val);
			if(type == Short.TYPE) return new Short(val);
			if(type == Integer.TYPE) return new Integer(val);
			if(type == Long.TYPE) return new Long(val);
			if(type == Character.TYPE) return (val.length() == 0 ?  new Character((char)0) : new Character(val.charAt(0)));
			if(type == Float.TYPE) return new Float(val);
			if(type == Double.TYPE) return new Double(val);
			if(type == Boolean.TYPE) return new Boolean(val);
			return null;
		}

		private Object toNull(Method method) {
			Property p = meta.getAnnotation(method);
			if(p == null || p.defaultValue().length() == 0) {
				Class<?> type = method.getReturnType();
				if(isPrimitive(type)) return toPrimitiveNull(type);
				else return null;
			} else {
				return toObject(method, p.defaultValue());
			}
		}

		private Object toPrimitiveNull(Class<?> type) {
			if(type == Byte.TYPE) return NULL_BYTE;
			if(type == Short.TYPE) return NULL_SHORT;
			if(type == Integer.TYPE) return NULL_INT;
			if(type == Long.TYPE) return NULL_LONG;
			if(type == Character.TYPE) return NULL_CHAR;
			if(type == Float.TYPE) return NULL_FLOAT;
			if(type == Double.TYPE) return NULL_DOUBLE;
			if(type == Boolean.TYPE) return NULL_BOOL;
			return null;
		}

		private String matchProperty(Method method) {
			if(meta.methCache.containsKey(method)) return getCached(method); 
			else {
				String[] alts = getNameAlts(method);
				Namespace ns = getNamespace();
				boolean inherit = isInherited(method);
				PropertyKey prop = recursiveFind(alts, ns, inherit);
				if(prop == null) {
					// search on fall back if possible
					String fallback = getFallback(method);
					if(fallback != null) {
						prop = recursiveFind(new String[] { fallback }, ns, inherit);
					}
				}
				return returnProperty(prop);
			}
		}

		private String returnProperty(PropertyKey prop) {
			if(prop != null) {
				return conf.getProperty(prop).trim();
			} else return null;
		}

		private boolean isInherited(Method method) {
			Property p = meta.annCache.get(method);
			if(p == null) {
				p = method.getAnnotation(Property.class);
				if(p != null) meta.annCache.put(method, p);
			}
			if(p != null && p.inheritance() != Inheritance.NAN) return (p.inheritance() == Inheritance.ALLOW);
			else {
				if(meta.annotation != null) return (meta.annotation.inheritance() == Inheritance.ALLOW);
				else return false;
			}
		}

		private PropertyKey recursiveFind(String[] alts, Namespace ns, boolean inherit) {
			String[] props = conf.getProperties(ns);
			String match = getMatch(props, alts);
			if(match != null) return new PropertyKey(ns, match);
			else {
				if(!inherit) return null;
				else {
					Namespace next = ns.getParent();
					if(next == null) return null; // ROOT NAMESPACE, BREAK
					else return recursiveFind(alts, next, inherit);
				}
			}
		}

		private String getMatch(String[] props, String[] alts) {
			for (String alt : alts) {
				for (String prop : props) {
					if(alt.equals(prop)) {
						return prop;
					}
				}
			}
			return null;
		}

		private Namespace getNamespace() {
			return meta.ns;
		}

		private String getCached(Method method) {
			PropertyKey k = meta.methCache.get(method);
			return conf.getProperty(k);
		}

		private String[] getNameAlts(Method method) {
			if(hasPropertyAnnotation(method)) return getPropertyName(method);
			else {
				String[] arr = new String[2];
				String name = prepareName(method.getName());
				StringBuilder hyphen = new StringBuilder(name.length() + 3);
				StringBuilder under = new StringBuilder(name.length() + 3);
				for(int i = 0; i < name.length(); i++) {
					char ch = name.charAt(i);
					// if first, only go to lower case
					if(i == 0 && Character.isUpperCase(ch)) {
						ch = Character.toLowerCase(ch);
					}
					// if upper case add delimiter and convert
					if(Character.isUpperCase(ch)) {
						hyphen.append("-");
						under.append("_");
						ch = Character.toLowerCase(ch);
					}
					hyphen.append(ch);
					under.append(ch);
				}
				arr[0] = hyphen.toString();
				arr[1] = under.toString();
				if(arr[0].equals(arr[1])) return new String[] { arr[0] };
				else return arr;
			}
		}

		private String[] getPropertyName(Method method) {
			return new String[] { meta.getAnnotation(method).property() };
		}

		private boolean hasPropertyAnnotation(Method method) {
			Property p = meta.getAnnotation(method);
			return (p == null ? false : p.property().length() > 0);
		}

		private String getFallback(Method method) {
			Property p = meta.getAnnotation(method);
			String over = (p == null ? "" : p.fallback());
			return (over == null || over.length() == 0 ? null : over);
		}
		
		private String prepareName(String name) {
			if(name.startsWith("get") && isCapitalizedAt(name, 3)) return name.substring(3);
			else if(name.startsWith("is") && isCapitalizedAt(name, 2)) return name.substring(2);
			else if(name.startsWith("has") && isCapitalizedAt(name, 3)) return name.substring(3);
			else if(name.startsWith("do") && isCapitalizedAt(name, 2)) return name.substring(2);
			else return name;
		}

		private boolean isCapitalizedAt(String name, int i) {
			if(name.length() <= i) return false;
			else return Character.isUpperCase(name.charAt(i));
		}
	}
	
	private static class Meta {
		
		private final Configurated annotation;
		private final Class<? extends Configurable> iface;
		private final Namespace ns;
		
		private final Map<Method, PropertyKey> methCache;
		private final Map<Method, Property> annCache;
		
		private Meta(Class<? extends Configurable> iface, Namespace forcedNs) {
			annCache = new ConcurrentHashMap<Method, Property>(3);
			methCache = new ConcurrentHashMap<Method, PropertyKey>(3);
			annotation = iface.getAnnotation(Configurated.class);
			if(forcedNs != null) ns = forcedNs;
			else ns = doGetNamespace();
			this.iface = iface;
		}

		private Namespace doGetNamespace() {
			String s = (annotation == null ? null : annotation.namespace());
			if(s == null || s.length() == 0) return Namespace.NULL;
			else return new Namespace(s);
		}
		
		public Property getAnnotation(Method meth) {
			Property p = annCache.get(meth);
			if(p == null) {
				p = meth.getAnnotation(Property.class);
				if(p != null) annCache.put(meth, p);
			}
			return p;
		}
		 
		@Override
		public int hashCode() {
			return iface.hashCode() ^ ns.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Meta)) return false;
			Meta m = (Meta)obj;
			return m.iface.equals(iface) && m.ns.equals(ns);
		}
	}
	

	private static class IFaceHandle {
		
		private final Namespace ns;
		private final Class<? extends Configurable> iface;
		
		private IFaceHandle(Namespace ns, Class<? extends Configurable> iface) {
			this.iface = iface;
			this.ns = ns;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof IFaceHandle)) return false;
			IFaceHandle h = (IFaceHandle)o;
			return h.hashCode() == hashCode();
		}
		
		@Override
		public int hashCode() {
			int pre = (ns == null ? 1 : ns.hashCode());
			return pre ^ iface.hashCode();
		}
	}
}
