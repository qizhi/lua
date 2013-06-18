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
/**
 * 
 */
package com.cubeia.firebase.server.game.activation;

import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.activator.RequestAwareActivator;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.server.deployment.game.GameRevision;

class ActivationBean {
	
	final String deploymentName;
	final GameActivator act;
	final GameDefinition def;
	final GameRevision rev;
	
	ActivatorContext context;
	
	ActivationBean(String deploymentName, GameActivator act, GameDefinition def, GameRevision rev) {
		this.deploymentName = deploymentName;
		this.act = act;
		this.def = def;
		this.rev = rev;
	}

	public boolean isMttAware() {
		return (act instanceof MttAwareActivator);
	}
	
	public boolean isRoutable() {
		return (act instanceof RoutableActivator);
	}

	public boolean isCreationAware() {
		return (act instanceof RequestAwareActivator);
	}
}