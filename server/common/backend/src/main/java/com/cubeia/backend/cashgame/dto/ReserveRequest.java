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

package com.cubeia.backend.cashgame.dto;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.games.poker.common.money.Money;

import java.io.Serializable;

/**
 * This is request for reserving money at a table.
 */
public class ReserveRequest implements Serializable {

    private static final long serialVersionUID = -5456254904252608864L;

    public final PlayerSessionId playerSessionId;
    public final Money amount;

    public ReserveRequest(PlayerSessionId playerSessionId, Money amount) {
        this.playerSessionId = playerSessionId;
        this.amount = amount;
    }

    public PlayerSessionId getPlayerSessionId() {
        return playerSessionId;
    }

    public Money getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ReserveRequest");
        sb.append("{playerSessionId=").append(playerSessionId);
        sb.append(", amount=").append(amount);
        sb.append('}');
        return sb.toString();
    }
}
