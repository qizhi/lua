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
package com.cubeia.firebase.game;

import java.util.Map;


public class MapDeltaChange<K, V> implements DeltaChange {
	
	
	// --- INSTANCE MEMBERS --- //

	private final Type type;
	private final K key;
	private final V value;
	private final Map<K, V> target;
	
	public MapDeltaChange(Map<K, V> target, Type type, K key, V value) {
		this.target = target;
		this.type = type;
		this.key = key;
		this.value = value;
	}
	

	public void commit() {
		if(type == Type.ADD || type == Type.SET) {
			target.put(key, value);
		} else if(type == Type.REM) {
			target.remove(key);
		} else if(type == Type.CLEAR) {
			target.clear();
		}
	}
}
