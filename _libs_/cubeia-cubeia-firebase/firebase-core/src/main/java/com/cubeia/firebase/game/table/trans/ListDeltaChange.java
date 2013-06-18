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
package com.cubeia.firebase.game.table.trans;

import java.util.List;

import com.cubeia.firebase.game.DeltaChange;

public class ListDeltaChange<K> implements DeltaChange {

	// --- INSTANCE MEMBERS --- //

	private final Type type;
	private final int index;
	private final K value;
	private final List<K> target;
	
	ListDeltaChange(List<K> target, Type type, int index, K value) {
		this.target = target;
		this.type = type;
		this.index = index;
		this.value = value;
	}
	

	public void commit() {
		if(type == Type.ADD) {
			target.add(index, value);
		} else if(type == Type.SET) {
			target.set(index, value);
		} else if(type == Type.REM) {
			target.remove(index);
		} else if(type == Type.CLEAR) {
			target.clear();
		}
	}
}
