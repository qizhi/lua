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
package com.cubeia.firebase.server.util;

import com.cubeia.firebase.api.util.Arguments;

/**
 * This class handles id generation for game objects. It caches
 * objects ids 20 at the time.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 27
 */
public final class GameObjectIdSysStateMapper {

	private int lastId, nextCache;
	private final IdSysStateMapperMemory state;
	// private final String fqn;
	
	/**
	 * 
	 * @param state
	 * @param fqn, base node that will hold the id number
	 */
	public GameObjectIdSysStateMapper(IdSysStateMapperMemory state/*, String fqn*/) {
		//this.fqn = fqn;
		Arguments.notNull(state, "state");
		this.state = state;
		lastId = -1;
	}
	
	public synchronized int generateNewObjectId() {
		if(lastId == -1) resetId(); // INIT
		lastId++;
		int id = lastId;
		if(lastId == nextCache) {
			resetId();
		}
		return id;
	}
	
	// --- PRIVATE METHODS --- //
	
	private void resetId() {
		int next = state.get();
		if(next == -1) {
			lastId = 0;
		} else {
			lastId = next;
		}
		nextCache = lastId + 20;
		next = new Integer(nextCache);
		state.set(next);
	}
}
