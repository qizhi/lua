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
package com.cubeia.firebase.server.deployment.mtt;

import java.util.Map;

import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public interface TournamentDeployment {
	
	public abstract String getName();

	public abstract String getArtifactId();

	public abstract String getArtifactName();

	public abstract String getArtifactVersion();

	public abstract void unregisterClassLoaders();

	public abstract void registerClassLoaders();

	public abstract Map<Integer, TournamentRevision> getAllRevisions();

	public abstract TournamentRevision getTournamentRevision(int version);

	public abstract TournamentRevision getLatestRevision();

	public abstract TournamentRevision newRevision(DeploymentResource resource,
			boolean registerClassLoader) throws DeploymentFailedException;

}