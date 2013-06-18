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
package com.cubeia.firebase.api.action;

import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * Response to a request for detailed information about a table.
 * 
 */
public class TableQueryResponseAction extends AbstractPlayerAction {
    
    private static final long serialVersionUID = -2824279361177906228L;

	private List<SeatInfoAction> seatInfos;
	
	/** Status is defaulted to OK */
    private Status status = Status.OK; 

	public TableQueryResponseAction(int playerId, int tableId, List<SeatInfoAction> seatInfos) {
		super(playerId, tableId);
		this.seatInfos = seatInfos;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public List<SeatInfoAction> getSeatInfos() {
		return seatInfos;
	}
	
	 public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
