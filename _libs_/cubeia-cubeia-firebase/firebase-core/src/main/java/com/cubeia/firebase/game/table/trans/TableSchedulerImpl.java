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
import java.util.Map;
import java.util.UUID;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.MapDeltaChange;
import com.cubeia.firebase.game.DeltaChange.Type;
import com.cubeia.firebase.server.processor.TableActionScheduler;
import com.cubeia.firebase.server.scheduler.AbstractScheduler;

public class TableSchedulerImpl extends AbstractScheduler<GameAction> implements TableScheduler {

	private final StandardTable table;

	TableSchedulerImpl(StandardTable table) {
		this.table = table;
	}
	
	// --- PROTECTED METHODS --- //
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void deltaAdd(UUID id, ScheduledAction next) {
		getDelta().add(new MapDeltaChange<UUID, ScheduledAction<?>>(getMap(), Type.ADD, id, next));
		getDelta().add(new SchedulerDeltaChange(getScheduler(), Type.ADD, id, next, table));
	}
	
	@Override
	protected void deltaRem(UUID id) {
		getDelta().add(new MapDeltaChange<UUID, ScheduledAction<?>>(getMap(), Type.REM, id, null));
		getDelta().add(new SchedulerDeltaChange(getScheduler(), Type.REM, id, null, null));
	}
	
	protected List<DeltaChange> getDelta() {
		return table.getDeltaChanges();
	}
	
	private TableActionScheduler getScheduler() {
		return table.getInternalScheduler();
	}
	
	@Override
	protected Map<UUID, ScheduledAction<?>> getMap() {
		return table.getCurrentSchedule().getNext();
	}
}