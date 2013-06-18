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
package com.cubeia.firebase.server.deployment;

import java.net.URL;

import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveClassLoader;
import com.game.server.bootstrap.CubeiaURLClassLoader;

public class RevisionClassLoader extends CubeiaURLClassLoader {

	// private final File[] gameLibs;

	public RevisionClassLoader(URL[] libs, ClassLoader parent) {
		super(new URL[0], parent);
		if(parent instanceof UnifiedArchiveClassLoader) {
			UnifiedArchiveClassLoader uacl = (UnifiedArchiveClassLoader)parent;
			for (URL u : libs) {
				uacl.addURL(u);
			}
		} else {
			for (URL u : libs) {
				super.addURL(u);
			}
		}
		// Arguments.notNull(gameLibs, "game libraries");
		// this.gameLibs = gameLibs;
	}
	
	/*public Class[] getEntityClasses() throws IOException, ClassNotFoundException {
		JarFile[] jarFiles = libsToJarsFiles();
		List<Class> list = new LinkedList<Class>();
		for (JarFile file : jarFiles) {
			findEntities(file, list);
		}
		return list.toArray(new Class[list.size()]);
	}

	
	// --- PRIVATE METHODS --- ///
	
	private void findEntities(JarFile file, List<Class> list) throws ClassNotFoundException {
		for (Enumeration<JarEntry> en = file.entries(); en.hasMoreElements(); ) {
			JarEntry e = en.nextElement();
			String name = e.getName();
			if(name.endsWith(".class")) {
				name = name.substring(0, name.length() - 6);
				name = name.replace('/', '.').replace('\\', '.');
				Class cl = this.loadClass(name);
				if(isEntity(cl)) list.add(cl);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isEntity(Class cl) {
		return cl.getAnnotation(Entity.class) != null;
	}

	private JarFile[] libsToJarsFiles() throws IOException {
		JarFile[] arr = new JarFile[gameLibs.length];
		for(int i = 0; i < arr.length; i++) {
			arr[i] = new JarFile(gameLibs[i]);
		}
		return arr;
	}*/
}
