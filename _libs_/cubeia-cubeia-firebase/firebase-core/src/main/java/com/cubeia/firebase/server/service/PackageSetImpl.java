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
package com.cubeia.firebase.server.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class PackageSetImpl implements PackageSet {

	private final Logger log = Logger.getLogger(getClass());
	
	private Set<Path> paths = new TreeSet<Path>();
	private final String name;

	public PackageSetImpl(String name) {
		this.name = name;
	}
	
	public void addResource(String name) {
		Path path = new Path(name);
		paths.add(path);
	}

	public boolean impliesResource(String name) {
		Path test = new Path(name);
		for(Iterator<Path> it = paths.iterator(); it.hasNext(); ) {
			Path tmp = it.next();
			if(tmp.implies(test)) {
				log.trace("Service archive '" + this.name + "' implication: " + tmp.path + " > " + name);
				return true; // EARLY RETURN
			} 
		}
		return false;
	}

	public Iterator<String> iterator() {
		final Iterator<Path> nest = paths.iterator();
		return new Iterator<String>() {
			public boolean hasNext() {
				return nest.hasNext();
			}

			public String next() {
				Path tmp = nest.next();
				return tmp.path;
			}

			public void remove() {
				nest.remove();
			}
		};
	}
	
	
	/// --- PACKAGE ACCESS --- ///
	
	/**
	 * Get hold of the internal path object set.
	 */
	
	Set<Path> getPathObjectSet() {
		return paths;
	}

	
	/// --- INNER CLASSES --- ///
	
	static class Path implements Comparable<Path> {
		
		private String[] parts;
		private String path;
		
		private Path(String p) {
			if(p == null) throw new IllegalArgumentException("null path");
			this.path = p;
			String[] tmp = p.split("\\.");
			List<String> list = new ArrayList<String>(tmp.length);
			for(int i = 0; i < tmp.length; i++) {
				String s = tmp[i];
				if(s.length() > 0) {
					list.add(s);
				}
			}
			parts = new String[list.size()];
			list.toArray(parts);
		}
		
		public int compareTo(Path o) {
			return o.path.compareTo(path);
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof Path)) return false;
			return ((Path)obj).path.equals(path);
		}
		
		public int hashCode() {
			return path.hashCode();
		}
		
		public String toString() {
			return path;
		}
		
		private boolean implies(Path other) {
			String[] tmp = other.parts;
			if(tmp.length < parts.length) return false;
			for(int i = 0; i < parts.length; i++) {
	        	String my = parts[i];
	            String his = tmp[i];
	            if(my.equals("-")) return true; // EARLY RETURN
	            if(my.equals("*")) {
	                boolean isLast = (i + 1 == tmp.length);
	                if(isLast) return true; // EARLY RETURN
	                isLast = (i + 1 == parts.length);
	                if(isLast) return false;
	            } else {
	                if(!my.equals(his)) return false;
	            }
	        }
			return true;
		}
	}
}