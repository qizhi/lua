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
package com.cubeia.firebase.server.scheduler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.api.util.UnmodifiableSet;

public abstract class AbstractScheduler<T extends Action> implements Scheduler<T>{

	public void cancelScheduledAction(UUID id) {
		getMap().remove(id);
		deltaRem(id);
	}
	
	public void cancelAllScheduledActions() {
		Set<UUID> set = new HashSet<UUID>(getMap().keySet());
		for (UUID id : set) {
			cancelScheduledAction(id);
		}
	}

	public boolean hasScheduledGameAction(UUID id) {
		return getMap().containsKey(id);
	}
	
	public UnmodifiableSet<UUID> getAllScheduledGameActions() {
		final Set<UUID> set = new HashSet<UUID>(getMap().keySet());
		return new UnmodifiableSet<UUID>() {
		
			public Iterator<UUID> iterator() {
				return set.iterator();
			}
		
			public boolean contains(UUID id) {
				return set.contains(id);
			}
		};
	}
	
	public Action getScheduledGameAction(UUID id) {
		ScheduledAction<?> action = getMap().get(id);
		return (action == null ? null : action.getAction());
	}
	
	public long getScheduledGameActionDelay(UUID id) {
		ScheduledAction<?> action = getMap().get(id);
		return (action == null ? -1 : action.delay);
	}

	public UUID scheduleAction(T action, long delay) {
		UUID id = UUID.randomUUID();
		ScheduledAction<T> next = new ScheduledAction<T>(delay, action);
		getMap().put(id, next);
		deltaAdd(id, next);
		return id;
	}

	protected abstract void deltaAdd(UUID id, ScheduledAction<T> next);
	
	protected abstract void deltaRem(UUID id);
	
	protected abstract Map<UUID, ScheduledAction<?>> getMap();
	
}
