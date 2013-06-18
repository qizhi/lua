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

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.game.Transactional;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;

/**
 * The mtt action scheduler must be set before any action is executed. And
 * preferably nulled when the action is finished.
 * 
 * @author Larsan
 */
public interface TransactionalMttState extends Transactional, Identifiable { 
	
	public MTTState getMttState();

	public void setMttState(MTTState state);

	public void setActionScheduler(MttActionScheduler actionScheduler);

	public Scheduler<MttAction> getScheduler();

	// public MttTableMap getMttTables();
	
}
