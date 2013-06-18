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
package com.game.server.service.systemstate.local.impl;

import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.service.systemstate.cache.AbstractSystemStateService;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

/**
 * Implementation of the System State interface.
 * 
 * @author Fredrik
 *
 */
public class SystemStateServiceImpl extends AbstractSystemStateService {
	
	@Override
	protected SystemStateCacheHandler createSystemStateHandler(ServiceContext con) {
		return new SystemStateCacheHandler(con, true);
	}
}
