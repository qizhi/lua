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

package com.cubeia.backend.cashgame.dto;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.games.poker.common.money.Money;

/**
 * This is a request for transferring money between two session accounts.
 *
 */
public class TransferMoneyRequest {

    public final Money amount;
    public final PlayerSessionId fromSession;
    public final PlayerSessionId toSession;
    public final String comment;

    public TransferMoneyRequest(Money amount, PlayerSessionId fromSession, PlayerSessionId toSession, String comment) {
        this.amount = amount;
        this.fromSession = fromSession;
        this.toSession = toSession;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "TransferMoneyRequest{" +
                "amount=" + amount +
                ", fromSession=" + fromSession +
                ", toSession=" + toSession +
                ", comment='" + comment + '\'' +
                '}';
    }
}
