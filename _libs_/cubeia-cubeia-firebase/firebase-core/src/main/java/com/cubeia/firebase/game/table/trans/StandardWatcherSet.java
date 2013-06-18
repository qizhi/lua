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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.DeltaChange.Type;

public final class StandardWatcherSet implements TableWatcherSet {

	private boolean isDirty;
	private final StandardTable table;

	StandardWatcherSet(StandardTable table) {
		this.table = table;
	}
	
	public void addWatcher(int playerId) {
		isDirty = true;
		Set<Integer> clone = getCheckClone();
		clone.add(playerId);
		doDeltaAdd(playerId);
	}

	public int getCountWatchers() {
		if(isDirty) return getCheckClone().size();
		else return getReal().size();
	}

	public UnmodifiableSet<Integer> getWatchers() {
		return new UnmodifiableSet<Integer>() {
		
			Set<Integer> set = (isDirty ? getCheckClone() : getReal());
			
			public Iterator<Integer> iterator() {
				return set.iterator();
			}
		
			public boolean contains(Integer o) {
				return set.contains(o);
			}
		};
	}

	public void removeWatcher(int playerId) {
		isDirty = true;
		Set<Integer> clone = getCheckClone();
		clone.remove(playerId);
		doDeltaRem(playerId);
	}
	
	public boolean isWatching(int playerId) {
		if(isDirty) return getCheckClone().contains(playerId);
		else return getReal().contains(playerId);
	}
	
	public boolean getIsDirty() {
		return isDirty;
	}
	
	// --- PRIVATE METHODS --- //
	
	private void doDeltaAdd(int playerId) {
		addDelta(new CollectionDeltaChange<Integer>(getReal(), Type.ADD, playerId));
	}
	
	private void doDeltaRem(int playerId) {
		addDelta(new CollectionDeltaChange<Integer>(getReal(), Type.REM, playerId));
	}
	
	private void addDelta(DeltaChange change) {
		table.getDeltaChanges().add(change);
	}
	
	private Set<Integer> getReal() {
		return table.getRealData().getWatchingPlayers();
	}
	
	private Set<Integer> getCheckClone() {
		Set<Integer> tmp = table.getDataClone().getWatchingPlayers();
		if(tmp == null) {
			Set<Integer> real = table.getRealData().getWatchingPlayers();
			tmp = new TreeSet<Integer>(real);
			table.getDataClone().setWatchingPlayers(tmp);
		}
		return tmp;
	}	
}
