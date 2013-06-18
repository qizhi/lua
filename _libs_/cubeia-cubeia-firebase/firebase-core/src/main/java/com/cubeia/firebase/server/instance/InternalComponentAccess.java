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
package com.cubeia.firebase.server.instance;

import java.util.concurrent.atomic.AtomicReference;

import com.cubeia.firebase.server.deployment.ResourceManager;
import com.cubeia.firebase.server.service.InternalServiceRegistry;

public class InternalComponentAccess {

	private static final AtomicReference<InternalServiceRegistry> REG = new AtomicReference<InternalServiceRegistry>();
	private static final AtomicReference<ResourceManager> RESOURCES = new AtomicReference<ResourceManager>();
	
	public static InternalServiceRegistry getRegistry() {
		return REG.get();
	}
	
	public static ResourceManager getDeploymentResources() {
		return RESOURCES.get();
	}
	
	static void setRegistry(InternalServiceRegistry reg) {
		REG.set(reg);
	}
	
	static void setDeploymentResources(ResourceManager man) {
		RESOURCES.set(man);
	}
}
