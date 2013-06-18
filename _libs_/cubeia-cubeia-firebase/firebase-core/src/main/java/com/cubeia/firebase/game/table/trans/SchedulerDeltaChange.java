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

import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.server.processor.TableActionScheduler;

public class SchedulerDeltaChange implements DeltaChange {

	private static transient Logger log = Logger.getLogger(SchedulerDeltaChange.class);
	
	private final TableActionScheduler scheduler;
	private final Type type;
	private final ScheduledAction<GameAction> action;
	private final UUID id;
	private final Table table;

	public SchedulerDeltaChange(TableActionScheduler scheduler, Type type, UUID id, ScheduledAction<GameAction> action, Table table) {
		this.scheduler = scheduler;
		this.type = type;
		this.id = id;
		this.action = action;
		this.table = table;
	}

	public void commit() {
		if(scheduler == null) {
			log.debug("Ommitting commit in scheduler delta changer; This should only happend when tables are created.");
			return; // THIS IS WHEN TABLES ARE CREATED, IT IS ACTUALLY LEGAL
		}
		if(type == Type.REM) {
			scheduler.cancelSceduledLoopback(id);
		} else if(type == Type.ADD || type == Type.SET) {
			if (action.getAction() instanceof GameAction) {
				scheduler.scheduleLoopback(action.getAction(), action.delay, table, id);
			} else {
				log.warn("Bad Action type for Scheduler Delta: "+action.getAction()+". Expected type: GameAction");
			}
		}
	}
}
