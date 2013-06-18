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
package com.cubeia.firebase.server.game;

import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.PublicServiceRegistry;

/**
 * Holds system contextual references.
 * 
 * @author Fredrik
 *
 */
public class DefaultGameContext implements GameContext {
	
	private final ResourceLocator locator;
	private final PublicServiceRegistry reg;

	public DefaultGameContext(ResourceLocator locator) {
		reg = new PublicServiceRegistry(InternalComponentAccess.getRegistry());
		this.locator = locator;
	}

	public ServiceRegistry getServices() {
		return reg;
	}
	
	public ResourceLocator getResourceLocator() {
		return locator;
	}
}
