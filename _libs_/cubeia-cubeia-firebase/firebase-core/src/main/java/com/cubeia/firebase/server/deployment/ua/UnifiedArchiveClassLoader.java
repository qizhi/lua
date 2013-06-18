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

import java.io.File;
import java.net.URL;

import se.xec.commons.resource.Resource;

import com.cubeia.firebase.server.deployment.resources.FileSetResource;
import com.cubeia.firebase.util.Files;
import com.cubeia.util.ArrayUtil;
import com.game.server.bootstrap.CubeiaURLClassLoader;

public class UnifiedArchiveClassLoader extends CubeiaURLClassLoader {

	public UnifiedArchiveClassLoader(UnifiedArchiveRevision rev) {
		super(new URL[0], rev.getParentLoader());
		addLibraries(rev.getResource().getRoot());
	}
	
	public UnifiedArchiveClassLoader(ClassLoader parent, FileSetResource res) {
		super(new URL[0], parent);
		addLibraries(res.getRoot());
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
	
	public void addResource(Resource res) {
		URL url = res.getURL();
		if(url != null) {
			addURL(res.getURL());
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void addLibraries(File resourceRoot) {
		UnifiedArchiveDeploymentLayout lay = DefaultArchiveLayout.getInstance();
		URL[] libUrls = Files.toUrls(lay.getUtilityLibraries(resourceRoot));
		URL classes = Files.toUrls(lay.getOpenClassFolder(resourceRoot));
		URL[] combined = ArrayUtil.concat(libUrls, new URL[] { classes });
		for (URL u : combined) {
			super.addURL(u);
		}
	}
}
