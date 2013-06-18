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

import com.cubeia.firebase.api.action.mtt.AbstractMttAction;
import com.cubeia.firebase.api.action.visitor.MttActionVisitor;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;

public class MttRegisterResponseAction extends AbstractMttAction {

    private static final long serialVersionUID = 9192561303309808705L;
    private final int mttId;
    private final MttRegisterResponse status;
    private final int playerId;

    public MttRegisterResponseAction(int playerId, int mttId, MttRegisterResponse status) {
        super(mttId);
        this.mttId = mttId;
        this.status = status;
        this.playerId = playerId;
    }

    public void accept(MttActionVisitor visitor) {
        visitor.visit(this);
    }

    public String toString() {
        return "mtt register response action: pid[" + getPlayerId() + "] mttId[" + mttId + "] status[" + status;
    }

    public MttRegisterResponse getStatus() {
        return status;
    }
    
    public int getPlayerId() {
        return playerId;
    }
}
