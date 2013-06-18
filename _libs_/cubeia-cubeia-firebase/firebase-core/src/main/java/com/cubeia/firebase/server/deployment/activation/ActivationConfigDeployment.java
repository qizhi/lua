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
package com.cubeia.firebase.server.deployment.activation;

import java.io.IOException;

import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.server.deployment.DeploymentImpl;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.util.ResourceConfigSource;
import com.cubeia.firebase.service.activation.ActivationType;

public class ActivationConfigDeployment extends DeploymentImpl {
	
	private ConfigSource config;
	private final ActivationType activationType;

	public ActivationConfigDeployment(String name, ActivationType type) {
		super(name, (type == ActivationType.GAR ? DeploymentType.GAME_ACTIVATOR_CONF : DeploymentType.TOURNAMENT_ACTIVATOR_CONF));
		activationType = type;
	}
	
	@Override
	public String getArtifactName() {
		return getIdentifier();
	}
	
	@Override
	public String getArtifactVersion() {
		return String.valueOf(getLatestVersion());
	}
	
	void configure(DeploymentResource res) throws IOException {
		config = new ResourceConfigSource(getName(), res);
	}
	
	public ActivationType getActivationType() {
		return activationType;
	}

	public ConfigSource getConfigSource() {
		return config;
	}
}
