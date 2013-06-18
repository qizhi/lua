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

import com.cubeia.firebase.server.util.JarFileFilter;
import com.cubeia.util.ArrayUtil;

/*
 * Default GAR layout:
 * 
 * ./*.jar
 * ./META-INF/classes/**
 * ./META-INF/lib/*.jar
 */
public class DefaultArchiveLayout implements UnifiedArchiveDeploymentLayout {

	private static final String LIB_FOLDER = "META-INF" + File.separator + "lib";
	private static final String CLASS_FOLDER = "META-INF" + File.separator + "classes";
	// private static final String ARCHIVE_FILE = "META-INF" + File.separator + "game.xml";
	
	private static final DefaultArchiveLayout LAY = new DefaultArchiveLayout();
	
	public static UnifiedArchiveDeploymentLayout getInstance() {
		return LAY;
	}

	private DefaultArchiveLayout() { }

	public File[] getUtilityLibraries(File root) {
		File[] two = getJars(root);
		File[] one = getJars(new File(root, LIB_FOLDER));
		return ArrayUtil.concat(notNull(one), notNull(two));
	}

	public File getOpenClassFolder(File root) {
		return new File(root, CLASS_FOLDER);
	}
	
	
	
	// --- PRIVATE METHODS ---- //

	private File[] notNull(File[] two) {
		return two == null ? new File[0] : two;
	}
	
	private File[] getJars(File base) {
		return base.listFiles(new JarFileFilter());
	}
}
