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
package com.cubeia.firebase.server.deployment.game;

import java.util.Map;

import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public interface GameDeployment {
	
	public abstract String getName();

	public abstract String getArtifactId();

	public abstract String getArtifactName();

	public abstract String getArtifactVersion();

	public abstract void unregisterClassLoaders();

	public abstract void registerClassLoaders();

	/**
	 * Get all defined revisions.
	 * 
	 * @return 
	 */
	public abstract Map<Integer, GameRevision> getAllRevisions();

	/**
	 * Get the supplied version
	 * 
	 * @param version
	 * @return null if not found
	 */
	public abstract GameRevision getGameRevision(int version);

	/**
	 * Get the latest and greatest revision of the game.
	 * 
	 * @return
	 */
	public abstract GameRevision getLatestRevision();

	/**
	 * Explode file to version specific location.
	 * The attribute workFolder must have been set
	 * before calling this method.
	 * 
	 * @param resource
	 * @return File to the exploded revision folder
	 */
	public abstract GameRevision newRevision(DeploymentResource resource, boolean registerClassLoader) throws DeploymentFailedException;

}