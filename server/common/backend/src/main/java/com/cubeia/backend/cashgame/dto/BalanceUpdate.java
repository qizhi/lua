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

@SuppressWarnings("serial")
public class BalanceUpdate implements Serializable {

    private final PlayerSessionId playerSessionId;
    private final Money balance;
    private final long balanceVersionNumber;

    public BalanceUpdate(PlayerSessionId playerSessionId, Money balance, long balanceVersionNumber) {
        this.playerSessionId = playerSessionId;
        this.balance = balance;
        this.balanceVersionNumber = balanceVersionNumber;
    }

    public PlayerSessionId getPlayerSessionId() {
        return playerSessionId;
    }

    public Money getBalance() {
        return balance;
    }

    public long getBalanceVersionNumber() {
        return balanceVersionNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BalanceUpdate");
        sb.append("{playerSessionId=").append(playerSessionId);
        sb.append(", balance=").append(balance);
        sb.append(", balanceVersionNumber=").append(balanceVersionNumber);
        sb.append('}');
        return sb.toString();
    }
}
