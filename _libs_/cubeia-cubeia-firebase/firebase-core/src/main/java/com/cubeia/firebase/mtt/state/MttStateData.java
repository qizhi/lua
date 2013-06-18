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
package com.cubeia.firebase.mtt.state;

import java.io.Serializable;

import com.cubeia.firebase.api.common.Identifiable;

public abstract class MttStateData implements Serializable, Identifiable {
	
	private static final long serialVersionUID = -3725554307215129492L;
	
	private byte[] realState;
	private final int mttId;
	//private int[] tables;
	
	protected byte[] scheduledActions; // serialized map UUID -> scheduled action
	private final int mttLogicId;
	
	public MttStateData(int mttLogicId, int mttId, byte[] realState) {
		this.mttLogicId = mttLogicId;
		this.realState = realState;
		this.mttId = mttId;
	}
	
	public int getMttLogicId() {
		return mttLogicId;
	}
	
	@Deprecated
	public int[] getTables() {
		return null; // tables;
	}
	
	@Deprecated
	public void setTables(int[] tables) {
		// this.tables = tables;
	}
	
	public byte[] getState() {
		return realState;
	}

	public void setState(byte[] state) {
		this.realState = state;
	}
	
	public int getId() {
		return mttId;
	}
	
	public String toString() {
		return "mttId["+mttId+"]";
	}
	
	public void setScheduledActions(byte[] scheduledActions) {
		this.scheduledActions = scheduledActions;
	}
	
	public byte[] getScheduledActions() {
		return scheduledActions;
	}
}
