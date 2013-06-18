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
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;

public class WatchResponseAction extends AbstractPlayerAction {

    private Enums.WatchResponseStatus status;

    public WatchResponseAction(int tableId, WatchResponseStatus status) {
        super(-1, tableId);
        this.status = status;
    }

    private static final long serialVersionUID = 1341892920537244783L;

    public String toString() {
    	return "Watch response - pid["+getPlayerId()+"] tid["+getTableId()+"] status["+status+"]";
    }
    
    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }

    public WatchResponseStatus getStatus() {
        return status;
    }

}
