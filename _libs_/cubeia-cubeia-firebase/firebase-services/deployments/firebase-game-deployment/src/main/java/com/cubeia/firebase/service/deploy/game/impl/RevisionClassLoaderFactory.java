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
package com.cubeia.firebase.service.deploy.game.impl;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.deployment.RevisionClassLoader;
import com.cubeia.firebase.server.deployment.game.GameDeploymentLayout;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.util.Files;

public class RevisionClassLoaderFactory {

	private final ClassLoader parent;
	
	public RevisionClassLoaderFactory(final ClassLoader parent) {
		Arguments.notNull(parent, "deployment class loader");
		this.parent = parent;
	}

	public RevisionClassLoader createClassLoader(GameRevision rev) {
		GameDeploymentLayout layout = rev.getLayout();
		URL[] libUrls = Files.toUrls(layout.getUtilityLibraries(rev));
		File[] gameLibs = layout.getGameLibraries(rev);
		URL[] gameUrls = Files.toUrls(gameLibs);
		URL classes = Files.toUrls(layout.getOpenClassFolder(rev));
		URL[] combined = combine(libUrls, gameUrls, classes);
		return new RevisionClassLoader(combined, parent);
	}

	
	// --- PRIVATE METHODS --- //
	
	private URL[] combine(URL[] arr1, URL[] arr2, URL url) {
		List<URL> list = new LinkedList<URL>();
		if(arr1 != null) addUrls(arr1, list);
		if(arr2 != null) addUrls(arr2, list);
		if(url != null) list.add(url);
 		return list.toArray(new URL[list.size()]);
	}

	private void addUrls(URL[] arr, List<URL> list) {
		for (URL url : arr) {
			list.add(url);
		}
	}
}
