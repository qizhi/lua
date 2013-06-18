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

package com.cubeia.poker.blinds.utils;

import com.cubeia.poker.blinds.BlindsPlayer;
import com.cubeia.poker.blinds.MissedBlindsStatus;

public class MockPlayer implements BlindsPlayer {

    private final int playerId;
    private boolean hasPostedEntry;
    private MissedBlindsStatus missedBlindsStatus = MissedBlindsStatus.NOT_ENTERED_YET;
    private boolean isSittingIn;

    public MockPlayer(int playerId) {
        super();
        this.playerId = playerId;
    }

    @Override
    public int getId() {
        return playerId;
    }

    @Override
    public int getSeatId() {
        return playerId;
    }

    @Override
    public boolean isSittingIn() {
        return isSittingIn;
    }

    public void setSittingIn(boolean isSittingIn) {
        this.isSittingIn = isSittingIn;
    }

    @Override
    public boolean hasPostedEntryBet() {
        return hasPostedEntry;
    }

    @Override
    public void setHasPostedEntryBet(boolean hasPostedEntry) {
        this.hasPostedEntry = hasPostedEntry;
    }

    public void setMissedBlindsStatus(MissedBlindsStatus status) {
        missedBlindsStatus = status;
    }

    @Override
    public MissedBlindsStatus getMissedBlindsStatus() {
        return missedBlindsStatus;
    }

    @Override
    public String toString() {
        return "seatId=" + playerId;
    }

}
