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
package com.cubeia.firebase.service.deploy.conf.impl;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.activation.ActivationConfigDeploymentHandler;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.activation.ActivationType;

public class GarActivationConfigDeploymentHandler extends ActivationConfigDeploymentHandler {

	public GarActivationConfigDeploymentHandler(ServiceRegistry registry) throws SystemCoreException {
		super(registry);
	}
	
	@Override
	public String getTail() {
		return Constants.GAR_ACTIVATION_CONFIG_DEF_TAIL;
	}
	
	@Override
	public ActivationType getType() {
		return ActivationType.GAR;
	}
}
