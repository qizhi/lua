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
package com.cubeia.firebase.api.util;

import java.util.Map;

public class ActorMap<E> {

	private final Object nullLock = new Object();
	private final Map<Integer, E> map;

	public ActorMap(Map<Integer, E> map) {
		this.map = map;
	}
	
	public Map<Integer, E> getMap() {
		return map;
	}
	
	public boolean act(int object, MapActor<E> actor) {
		E o = map.get(object);
		synchronized(o == null ? nullLock : o) {
			return actor.act(object, o);
		}
	}
 }
