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

public class DeploymentType {

	public static final DeploymentType GAME_ARCHIVE = new DeploymentType(".gar", "firebase/game-archive", true, false);
	
	public static final DeploymentType GAME_ACTIVATOR_CONF = new DeploymentType("-ga.xml", "firebase/game-activator-conf", false, false);
	public static final DeploymentType TOURNAMENT_ARCHIVE = new DeploymentType(".tar", "firebase/tournament-archive", true, false);
	public static final DeploymentType TOURNAMENT_ACTIVATOR_CONF = new DeploymentType("-ta.xml", "firebase/tournament-activator-conf", false, false);
	public static final DeploymentType SERVICE_ARHIVE = new DeploymentType(".sar", "firebase/service-arcive", true, false);
	public static final DeploymentType DATA_SOURCE = new DeploymentType("-ds.xml", "firebase/data-source", false, false);
	// PERSISTENCE_ARCHIVE(".par", "firebase/persistence-archive", true, false),
	public static final DeploymentType UNIFIED_ARCHIVE = new DeploymentType(".uar", "firebase/unified-archive", true, true);
	
	/**
	 * @param file File to get type for, or null
	 * @return The file type, or null if not known
	 */
	public static DeploymentType parse(File file) {
		return (file == null ? null : parse(file.getName()));
	}
	
	/**
	 * @param file Resource name to get type for, or null
	 * @return The resource type, or null if not known
	 */
	public static DeploymentType parse(String name) {
		if(GAME_ARCHIVE.matches(name)) {
			return GAME_ARCHIVE;
		} else if(GAME_ACTIVATOR_CONF.matches(name)) {
			return GAME_ACTIVATOR_CONF;
		} else if(TOURNAMENT_ARCHIVE.matches(name)) {
			return TOURNAMENT_ARCHIVE;
		} else if(TOURNAMENT_ACTIVATOR_CONF.matches(name)) {
			return TOURNAMENT_ACTIVATOR_CONF;
		} else if(SERVICE_ARHIVE.matches(name)) {
			return SERVICE_ARHIVE;
		} else if(DATA_SOURCE.matches(name)) {
			return DATA_SOURCE;
		/*} else if(PERSISTENCE_ARCHIVE.matches(name)) {
			return PERSISTENCE_ARCHIVE;*/
		} else if(UNIFIED_ARCHIVE.matches(name)) {
			return UNIFIED_ARCHIVE;
		} else {
			return null;
		}
	}
	
	
	// --- INSTANCE MEMBERS --- //
	
	private final String extension;
	private final boolean compressed;
	private final boolean nested;
	private final String contentType;
	
	private DeploymentType(String extension, String contentType, boolean compressed, boolean nested) {
		this.extension = extension;
		this.contentType = contentType;
		this.compressed = compressed;
		this.nested = nested;
	}
	
	
	/**
	 * @return True if the type is a compressed archive, false otherwise
	 */
	public boolean isCompressed() {
		return compressed;
	}
	
	/**
	 * This method checks if the deployment type may contain
	 * other deployment types. Currently this is only true for 
	 * the unified archives.
	 * 
	 * @return True If the type may contain nested types, false otherwise
	 */
	public boolean isNested() {
		return nested;
	}
	
	/**
	 * @param file File to match against type extension, may be null
	 * @return True if the file matches, false if not (or null)
	 */
	public boolean matches(File file) {
		return (file == null ? false : matches(file.getName()));
	}
	
	/**
	 * @param file Resource name to match against type extension, may be null
	 * @return True if the file matches, false if not (or null)
	 */
	public boolean matches(String name) {
		return (name == null ? false : name.toLowerCase().endsWith(extension));
	}

	/**
	 * @return The content type, never null
	 */
	public String getContentType() {
		return contentType;
	}
}
