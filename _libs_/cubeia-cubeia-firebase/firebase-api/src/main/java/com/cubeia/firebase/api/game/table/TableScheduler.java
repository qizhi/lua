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
package com.cubeia.firebase.api.game.table;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.scheduler.Scheduler;

/**
 * This object manages scheduled actions for a particular table. Using this
 * interface scheduled actions can be associated with a table for later processing.
 * It is important to remember to remove the scheduled action if it is cancelled.
 * 
 * @author Lars J. Nilsson
 */
public interface TableScheduler extends Scheduler<GameAction> { }
