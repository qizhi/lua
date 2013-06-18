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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.MapDeltaChange;
import com.cubeia.firebase.game.DeltaChange.Type;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;
import com.cubeia.firebase.server.scheduler.AbstractScheduler;

/**
 * FIXME: Does not work.
 * 
 * Right now I would rather climb Mount Everest dressed in lederhosen 
 * than try to refactor and work with the transactional objects.
 * 
 * The tx objects are tightly coupled with table and game actions
 * due to historic reasons. Coupled with a complex hierarchy we really
 * need to look through this and preferably refactor. 
 * 
 * In short: I GIVE UP!
 *
 * @author Fredrik
 */
public class MttSchedulerImpl extends AbstractScheduler<MttAction> {

	private final TransactionalMttStateImpl state;

	public MttSchedulerImpl(TransactionalMttStateImpl state) {
		this.state = state;
	}

	
	// --- PROTECTED METHODS --- //
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void deltaAdd(UUID id, ScheduledAction next) {
		getDelta().add(new MapDeltaChange<UUID, ScheduledAction<?>>(getMap(), Type.ADD, id, next));
		getDelta().add(new MttSchedulerDeltaChange(getScheduler(), Type.ADD, id, next));
	}
	
	@Override
	protected void deltaRem(UUID id) {
		getDelta().add(new MapDeltaChange<UUID, ScheduledAction<?>>(getMap(), Type.REM, id, null));
		getDelta().add(new MttSchedulerDeltaChange(getScheduler(), Type.REM, id, null));
	}
	
	protected List<DeltaChange> getDelta() {
		return state.getDeltaChanges();
	}
	
	private MttActionScheduler getScheduler() {
		return state.getActionScheduler();
	}
	
	@Override
	protected Map<UUID, ScheduledAction<?>> getMap() {
		return state.getCurrentSchedule().getNext();
	}
}
