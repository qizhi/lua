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
import java.util.HashSet;
import java.util.Set;

public class ClassLoadingDebug {

	private static final Set<String> debugSet = new HashSet<String>();
	
	static {
		String tmp = System.getProperty("com.cubeia.firebase.findClassSource");
		if(tmp != null && tmp.length() > 0) {
			String[] arr = tmp.split(",");
			for (String name : arr) {
				System.out.println("com.cubeia.firebase.findClassSource -> " + name);
				debugSet.add(name);
			}
		}
	}
	
	private ClassLoadingDebug() { }
	
	public static final void classLoaded(Class<?> cl) {
		if(debugSet.contains(cl.getName())) {
			URL location = cl.getProtectionDomain().getCodeSource().getLocation();
			System.out.println(" !!! findClassSource !!! " + cl.getName() + " == " + location.toString());
			debugSet.remove(cl.getName());
		}
	}
}
