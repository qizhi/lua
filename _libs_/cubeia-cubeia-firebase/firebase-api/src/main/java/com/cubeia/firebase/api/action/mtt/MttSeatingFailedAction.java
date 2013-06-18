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
package com.cubeia.firebase.api.action.mtt;

import com.cubeia.firebase.api.action.visitor.MttActionVisitor;

/**
 * A tournament seating has failed. 
 * 
 * This player does not have a seat reserved at the designated table
 * and needs to be seated elsewhere. 
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class MttSeatingFailedAction extends MttPlayerAction {

	/** Version ID */
	private static final long serialVersionUID = 1L;
	
	private int tableId = -1;

    private int seatId = -1;

	public MttSeatingFailedAction(int mttId, int playerId, int tableId, int seatId) {
		super(mttId, playerId);
        this.tableId = tableId;
        this.seatId = seatId;
	}

	public void accept(MttActionVisitor visitor) {
		visitor.visit(this);
	}

	public String toString() {
		return "MttSeatingFailedAction - "+super.toString();
	}

    public int getTableId() {
        return tableId;
    }

    public int getSeatId() {
        return seatId;
    }
	
}
