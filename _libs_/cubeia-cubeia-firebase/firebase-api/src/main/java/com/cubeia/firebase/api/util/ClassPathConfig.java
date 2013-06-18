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
package com.cubeia.firebase.api.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A very simple config source that takes a string argument
 * and attempt to load the source from the class path. It uses
 * the thread context class loader first (if available) and the 
 * current class loader afterwards.
 * 
 * @author lars.j.nilsson
 */
public class ClassPathConfig implements ConfigSource {

	private final String source, name;
	
	/**
	 * @param name Config name, must not be null
	 * @param path Class path name, must not be null
	 * @throws IOException If the path cannot be found in any available class loader
	 */
	public ClassPathConfig(String name, String path) throws IOException {
		Arguments.notNull(name, "name");
		Arguments.notNull(path, "path");
		this.name = name;
		this.source = path;
		checkSource(path);
	}
	
	public String getName() {
		return name;
	}

	public InputStream getInputStream() throws IOException {
		InputStream s = doGetStream(source, Thread.currentThread().getContextClassLoader());
		if(s == null) s = doGetStream(source, getClass().getClassLoader());
		if(s == null) throw new FileNotFoundException("Could not find resource '" + source + "' in class path.");
		return s;
	}
	
	@Override
	public String toString() {
		return "classpath://" + source + " [" + name + "]";
	}

	
	// --- PRIVATE METHODS --- //
	
	private InputStream doGetStream(String path, ClassLoader loader) {
		return (loader == null ? null : loader.getResourceAsStream(path));
	}
	
	private void checkSource(String path) throws IOException {
		InputStream s = getInputStream();
		try {
			s.close();
		} catch(IOException e) { }
	}
}
