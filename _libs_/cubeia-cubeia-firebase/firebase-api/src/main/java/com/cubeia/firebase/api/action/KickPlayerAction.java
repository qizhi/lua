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

/**
 * Action indicating that a player has been kicked from a table.
 * 
 *
 */
public class KickPlayerAction extends AbstractPlayerAction {

    private static final long serialVersionUID = 4593853205349281927L;

    private short reasonCode;
    
    public KickPlayerAction(int playerId, int tableId, short reasonCode) {
        super(playerId, tableId);
        this.reasonCode = reasonCode;
    }

    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }
    
    public short getReasonCode() {
        return reasonCode;
    }
}
