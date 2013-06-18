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
import com.cubeia.firebase.api.game.player.PlayerStatus;

public class SeatInfoAction extends AbstractPlayerAction {

    private static final long serialVersionUID = 2174187062177583916L;

    private int seatId;
    
    private PlayerInfoAction playerInfo;

    private PlayerStatus status;
    
    public SeatInfoAction(int playerId, int tableId) {
        super(playerId, tableId);
    }

    public String toString() {
    	return "SeatInfoAction - sid["+seatId+"] pi["+playerInfo+"] ps["+status+"]";
    }
    
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
    
    public int getSeatId() {
        return seatId;
    }

    public PlayerInfoAction getPlayerInfo() {
        return playerInfo;
    }
    
    public void setPlayerInfo(PlayerInfoAction playerInfo) {
        this.playerInfo = playerInfo;
    }

    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public PlayerStatus getStatus() {
        return status;
    }
}
