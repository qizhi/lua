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

import java.io.File;
import java.net.URL;

import com.cubeia.firebase.server.util.JarFileFilter;
import com.cubeia.firebase.util.Files;
import com.game.server.bootstrap.CubeiaURLClassLoader;

public class DeploymentClassLoader extends CubeiaURLClassLoader {

	/*DeploymentClassLoader(File libDir) { 
		this(libDir, DeploymentClassLoader.getSystemClassLoader());
	}*/
	
	public DeploymentClassLoader(File libDir, ClassLoader parent) {
		super(DeploymentClassLoader.getJars(libDir), parent);
	}

	
	// --- PRIVATE METHODS --- //
	
	private static URL[] getJars(File libDir) {
		if(libDir == null) return new URL[0];
		File[] arr = libDir.listFiles(new JarFileFilter());
		return Files.toUrls(arr);
	}
}
