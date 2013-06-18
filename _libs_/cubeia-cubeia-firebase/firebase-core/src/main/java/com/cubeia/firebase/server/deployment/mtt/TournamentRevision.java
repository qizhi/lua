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

import com.cubeia.firebase.api.mtt.MTTLogic;
import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public interface TournamentRevision {

	public abstract int getVersion();

	public abstract DeploymentResource getResource();

	public abstract TournamentDeploymentLayout getLayout();

	public abstract ClassLoader getRevisionClassLoader();

	public abstract TournamentDefinition getTournamentDefinition();

	public abstract Class<?> loadTournamentClass()
			throws ClassNotFoundException;

	public abstract MTTLogic newTournamentInstance()
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException;

}