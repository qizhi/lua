/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker;

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;


public class MockPlayer extends DefaultPokerPlayer implements PokerPlayer {

    private static final long serialVersionUID = 1L;
    private boolean allIn;
    private boolean forceAllIn = false;

    public MockPlayer(int id) { 
        super(id + 100);
        seatId = id;
    }

    public boolean isActionPossible(PokerActionType actionType) {
        return getActionRequest().isOptionEnabled(actionType);
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    public void setPlayerId(int id) {
        super.playerId = id;
    }

    public void setSeatId(int i) {
        seatId = i;
    }

    public void setPocketCards(Hand pocketCards) {
        this.pocketCards = pocketCards;
    }

    public void forceAllIn(boolean b) {
        forceAllIn = true;
        allIn = b;
    }

    @Override
    public boolean isAllIn() {
        if (!forceAllIn) {
            return super.isAllIn();
        }
        return allIn;
    }
}
