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

package com.cubeia.poker.blinds;

import java.io.Serializable;

/**
 * Class for describing the blinds info of a hand.
 *
 * @author viktor
 */
public class BlindsInfo implements Serializable {

    private int dealerSeatId;

    private int smallBlindSeatId;

    private int smallBlindPlayerId;

    private int bigBlindSeatId;

    private int bigBlindPlayerId;

    /** Indicates that this blinds info belongs to a hand that was canceled. */
    private boolean handCanceled = false;

    public BlindsInfo() {
        // Empty constructor.
    }

    public BlindsInfo(int dealerSeatId, int smallBlindSeatId, int bigBlindSeatId, int bigBlindPlayerId) {
        this.dealerSeatId = dealerSeatId;
        this.smallBlindSeatId = smallBlindSeatId;
        this.bigBlindSeatId = bigBlindSeatId;
        this.bigBlindPlayerId = bigBlindPlayerId;
    }

    public int getDealerSeatId() {
        return dealerSeatId;
    }

    /**
     * Gets the small blind seat id of this hand. Note that every hand has a small blind seat id,
     * even if the small blind was dead.
     *
     * @return
     */
    public int getSmallBlindSeatId() {
        return smallBlindSeatId;
    }

    public int getBigBlindSeatId() {
        return bigBlindSeatId;
    }

    public boolean isHeadsUpLogic() {
        return smallBlindSeatId == dealerSeatId;
    }

    public boolean isDefined() {
        // If dealer, sb and bb are 0, the blinds are undefined.
        return !(smallBlindSeatId == 0 && bigBlindSeatId == 0 && dealerSeatId == 0);
    }

    public void setDealerSeatId(int seatId) {
        dealerSeatId = seatId;
    }

    public void setSmallBlindSeatId(int seatId) {
        smallBlindSeatId = seatId;
    }

    public void setBigBlindSeatId(int seatId) {
        bigBlindSeatId = seatId;
    }

    /**
     * Gets the player id of the player who pays the small blind, or -1 if no player pays the small blind (it's dead).
     * @return
     */
    public int getSmallBlindPlayerId() {
        return smallBlindPlayerId;
    }

    @Override
    public String toString() {
        return String.format("dealer=%s small=%s big=%s " + "smallpid=%s bigpid=%s", dealerSeatId, smallBlindSeatId, bigBlindSeatId, smallBlindPlayerId, bigBlindPlayerId);
    }

    public void setSmallBlindPlayerId(int playerId) {
        this.smallBlindPlayerId = playerId;
    }

    public int getBigBlindPlayerId() {
        return bigBlindPlayerId;
    }

    public void setBigBlindPlayerId(int bigBlindPlayerId) {
        this.bigBlindPlayerId = bigBlindPlayerId;
    }

    public boolean handCanceled() {
        return handCanceled;
    }

    public void setHandCanceled() {
        handCanceled = true;
    }
}
