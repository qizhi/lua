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
package com.game.server.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class CubeiaURLClassLoader extends URLClassLoader {

	public CubeiaURLClassLoader(URL[] urls) {
		super(urls);
	}

	public CubeiaURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public CubeiaURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> cl = super.loadClass(name);
		if(cl != null) {
			ClassLoadingDebug.classLoaded(cl);
		}
		return cl;
	}
}
