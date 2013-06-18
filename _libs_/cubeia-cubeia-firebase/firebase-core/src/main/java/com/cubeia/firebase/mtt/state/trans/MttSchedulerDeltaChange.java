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

import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;

public class MttSchedulerDeltaChange implements DeltaChange {

	private static transient Logger log = Logger.getLogger(MttSchedulerDeltaChange.class);
	
	private final MttActionScheduler scheduler;
	private final Type type;
	private final ScheduledAction<MttAction> action;
	private final UUID id;

	public MttSchedulerDeltaChange(MttActionScheduler scheduler, Type type, UUID id, ScheduledAction<MttAction> action) {
		this.scheduler = scheduler;
		this.type = type;
		this.id = id;
		this.action = action;
	}

	public void commit() {
		if(type == Type.REM) {
			scheduler.cancelSceduledLoopback(id);
		} else if(type == Type.ADD || type == Type.SET) {
			if (action.getAction() instanceof MttAction) {
				scheduler.scheduleLoopback(action.getAction(), action.delay, id);
			} else {
				log.warn("Bad Action type for Scheduler Delta: "+action.getAction()+". Expected type: GameAction");
			}
		}
	}
}
