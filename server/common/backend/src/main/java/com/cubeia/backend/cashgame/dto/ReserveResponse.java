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
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ReserveResponse implements Serializable {

    private final BalanceUpdate balanceUpdate;
    private final Money amountReserved;
    private final Map<String, String> reserveProperties;

    public ReserveResponse(BalanceUpdate balanceUpdate, Money amountReserved) {
        this.balanceUpdate = balanceUpdate;
        this.amountReserved = amountReserved;
        this.reserveProperties = new HashMap<String, String>();
    }

    public PlayerSessionId getPlayerSessionId() {
        return getBalanceUpdate().getPlayerSessionId();
    }

    public void setProperty(String key, String value) {
        reserveProperties.put(key, value);
    }

    public BalanceUpdate getBalanceUpdate() {
        return balanceUpdate;
    }

    public Money getAmountReserved() {
        return amountReserved;
    }

    public Map<String, String> getReserveProperties() {
        return new HashMap<String, String>(reserveProperties);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ReserveResponse");
        sb.append("{balanceUpdate=").append(balanceUpdate);
        sb.append(", amountReserved=").append(amountReserved);
        sb.append(", reserveProperties=").append(reserveProperties);
        sb.append('}');
        return sb.toString();
    }
}
