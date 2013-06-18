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


/**
 * Models a deployment resource.
 * The deployment indetifier will be a combination of the 
 * resource name as well as the resource type.
 * 
 * Examples:
 * 
 * Datasource
 * game-ds.xml -> game.datasource
 *
 * Game Archive
 * game.gar -> game.game
 * 
 *  
 *  
 * @author Fredrik
 *
 */
public interface Deployment {
	
	/**
	 * Get the enum type of resource.
	 */
	public DeploymentType getType();
	
	/**
	 * Get the resource name.
	 * I.e.
	 * game-ds.xml -> game
	 * 
	 * @return the name wihtout resource specification
	 */
	public String getName();
	
	/**
	 * This method should return the configured name for the
	 * resource if available. If the resource has no configured name,
	 * it should return the identifier.
	 * 
	 * @return The configured name, never null
	 */
	public String getArtifactName();
	
	/**
	 * This method should return the configured version for the
	 * resource if available. If the resource has no configured version,
	 * it should return the latest deployment version.
	 * 
	 * @return The configured version, never null
	 */
	public String getArtifactVersion();
	
	/**
	 * This method should return the configured id for the
	 * resource if available. If the resource has no configured id,
	 * it should return a passable string.
	 * 
	 * @return The configured id, never null
	 */
	public String getArtifactId();
	
	/**
	 * The identifier is the name and resource type of 
	 * a deployment resource. It is  derived from the 
	 * resource filename and combined with the type.
	 * 
	 * I.e. definitions:
	 * Datasource: mysql-ds.xml -> mysql.datasource
	 * Game Archive: game.gar -> game.game
	 * 
	 * 
	 */
	public String getIdentifier();
	
	/**
	 * Returns the latest deployement version.
	 * Versions are always started at revision 1 by
	 * the Deployment Manager.
	 * 
	 * Not applicable for all resources, in which case
	 * the version will be statically 1.
	 * 
	 * @return
	 */
	public int getLatestVersion();
	
}
