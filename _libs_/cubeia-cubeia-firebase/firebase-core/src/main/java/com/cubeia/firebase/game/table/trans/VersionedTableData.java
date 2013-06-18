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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.StandardTableData;

public final class VersionedTableData extends StandardTableData {

	private static final long serialVersionUID = 648785906316241435L;

	
	// --- INSTANCE MEMBERS --- //

	private transient Object cachedGameState;
	private transient Map<UUID, ScheduledAction<?>> cachedScheduledActions;
	private transient Lock transientLock = new ReentrantLock();
	private transient final long version;

	VersionedTableData(InternalMetaData metaData, long version, int numSeats) {
		super(metaData, true, numSeats);
		this.version = version;
	}
	
	long getVersion() {
		return version;
	}

	Lock getTransientLock() {
		return transientLock;
	}
	
	void setTransientLock(Lock transientLock) {
		this.transientLock = transientLock;
	}
	
	public Object getCachedGameState() {
		return cachedGameState;
	}
	
	public void setCachedGameState(Object cachedGameState) {
		this.cachedGameState = cachedGameState;
	}
	
	public void setCachedScheduledActions(Map<UUID, ScheduledAction<?>> cachedScheduledActions) {
		this.cachedScheduledActions = cachedScheduledActions;
	}
	
	public Map<UUID, ScheduledAction<?>> getCachedScheduledActions() {
		return cachedScheduledActions;
	}
	
	@Override
	public String toString() {
	    return metaData + " " + cachedGameState;
	}
}
