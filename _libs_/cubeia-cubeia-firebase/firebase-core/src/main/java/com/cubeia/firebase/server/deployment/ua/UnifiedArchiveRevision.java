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
package com.cubeia.firebase.server.deployment.ua;

import com.cubeia.firebase.server.deployment.UnifiedDeploymentCallback;
import com.cubeia.firebase.server.deployment.resources.FileSetResource;

public class UnifiedArchiveRevision {

	private final int version;
	private final ClassLoader classLoader;
	private final FileSetResource clone;
	private final ClassLoader parentLoader;
	
	public UnifiedArchiveRevision(int version, FileSetResource clone, ClassLoader parentLoader) {
		this.clone = clone;
		this.version = version;
		this.parentLoader = parentLoader;
		ClassLoader test = (ClassLoader)clone.getAttachment();
		if(test != null) {
			classLoader = test;
		} else {
			classLoader = new UnifiedArchiveClassLoader(this);
		}
	}
	
	public FileSetResource getResource() {
		return clone;
	}

	public ClassLoader getRevisionClassLoader() {
		return classLoader;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void deploy(UnifiedDeploymentCallback callback) {
		callback.deploy(clone, classLoader);
	}
	
	public UnifiedArchiveDeploymentLayout getLayout() {
		return DefaultArchiveLayout.getInstance();
	}

	public ClassLoader getParentLoader() {
		return parentLoader;
	}
}
