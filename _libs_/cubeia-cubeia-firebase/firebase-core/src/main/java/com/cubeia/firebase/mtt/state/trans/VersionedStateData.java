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
package com.cubeia.firebase.mtt.state.trans;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.mtt.state.MttStateData;

public class VersionedStateData extends MttStateData {

	private static final long serialVersionUID = 214317913947814183L;
	
	private transient Object cachedState;
	private transient Lock transientLock = new ReentrantLock();
	private transient final long version;
	
	private transient Map<UUID, ScheduledAction<?>> cachedScheduledActions;
	
	public VersionedStateData(int mttLogicId, int mttId, long version) {
		super(mttLogicId, mttId, null);
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
	
	public Object getCachedState() {
		return cachedState;
	}
	
	public void setCachedState(Object cachedGameState) {
		this.cachedState = cachedGameState;
	}
	
	public String toString() {
		return super.toString()+" "+cachedState;
	}
	
	public void setCachedScheduledActions(Map<UUID, ScheduledAction<?>> cachedScheduledActions) {
		this.cachedScheduledActions = cachedScheduledActions;
	}
	
	public Map<UUID, ScheduledAction<?>> getCachedScheduledActions() {
		return cachedScheduledActions;
	}
}
