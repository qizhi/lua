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

public class TableCreated extends Command<TableCommandData[]> {

	private static final long serialVersionUID = 6081467175665864623L;

	private int mttId = -1;
	
	public TableCreated(int mttId) {
		super(Types.TABLE_CREATED.ordinal());
		this.mttId = mttId;
	}
	
	public TableCreated(TableCommandData[] data, int mttId) {
		super(Types.TABLE_CREATED.ordinal());
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
