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

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

public class DealerAction extends AbstractGameAction {

	private static final long serialVersionUID = -743015323825797475L;
	private int dealerSeatId;

	public DealerAction(int dealerSeatId, int tableId) {
		super(tableId);
		this.dealerSeatId = dealerSeatId;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return Returns the dealerSeatId.
	 */
	public int getDealerSeatId() {
		return dealerSeatId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "DealerAction: dealerSeatId: " + dealerSeatId;
	}    
}
