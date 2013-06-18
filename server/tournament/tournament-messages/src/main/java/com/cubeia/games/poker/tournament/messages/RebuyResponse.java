/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.messages;

import java.io.Serializable;
import java.math.BigDecimal;

public class RebuyResponse implements Serializable {

    private final int playerId;
    private final BigDecimal chipsAtHandFinish;
    private final boolean answer;
    private int tableId;

    /**
     * @param chipsAtHandFinish the amount of chips when the last hand finished (if no hands are finished, pass the starting stack)
     * @param answer true if player wants to perform a rebuy
     */
    public RebuyResponse(int tableId, int playerId, BigDecimal chipsAtHandFinish, boolean answer) {
        this.tableId = tableId;
        this.playerId = playerId;
        this.chipsAtHandFinish = chipsAtHandFinish;
        this.answer = answer;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean getAnswer() {
        return answer;
    }

    public int getTableId() {
        return tableId;
    }

    public BigDecimal getChipsAtHandFinish() {
        return chipsAtHandFinish;
    }
}
