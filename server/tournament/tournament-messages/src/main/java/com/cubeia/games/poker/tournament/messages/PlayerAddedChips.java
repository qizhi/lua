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

/**
 * This is sent from the tournament to a table when a player has added chips via a rebuy or an add-on.
 */
public class PlayerAddedChips implements Serializable {
    public enum Reason { REBUY, ADD_ON }
    private final int playerId;
    private final BigDecimal chipsToAdd;
    private final Reason reason;

    public PlayerAddedChips(int playerId, BigDecimal chipsToAdd, Reason reason) {
        this.playerId = playerId;
        this.chipsToAdd = chipsToAdd;
        this.reason = reason;
    }

    public int getPlayerId() {
        return playerId;
    }

    public BigDecimal getChipsToAdd() {
        return chipsToAdd;
    }

    public Reason getReason() {
        return reason;
    }
}
