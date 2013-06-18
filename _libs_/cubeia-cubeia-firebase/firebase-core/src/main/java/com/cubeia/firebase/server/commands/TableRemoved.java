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
package com.cubeia.firebase.server.commands;

import com.cubeia.firebase.api.command.Command;

public class TableRemoved extends Command<TableCommandData> {

	private static final long serialVersionUID = 8359641893969261399L;
	
	private final int mttId;

	public TableRemoved(int mttId) {
		super(Types.TABLE_REMOVED.ordinal());
		this.mttId = mttId;
	}
	
	public TableRemoved(TableCommandData data, int mttId) {
		super(Types.TABLE_REMOVED.ordinal());
		this.mttId = mttId;
		setAttachment(data);
	}
	
	public boolean isMtt() {
		return mttId != -1;
	}
	
	public int getMttId() {
		return mttId;
	}
}
