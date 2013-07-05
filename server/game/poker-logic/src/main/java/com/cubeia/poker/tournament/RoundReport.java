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

package com.cubeia.poker.tournament;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RoundReport {

    private Map<Integer, BigDecimal> balanceMap = new HashMap<Integer, BigDecimal>();

    private BigDecimal smallBlindAmount;

    private BigDecimal bigBlindAmount;

    private BigDecimal anteAmount;

    public RoundReport(BigDecimal smallBlindAmount, BigDecimal bigBlindAmount, BigDecimal anteAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.anteAmount = anteAmount;
    }

    public BigDecimal getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public BigDecimal getBigBlindAmount() {
        return bigBlindAmount;
    }

    public BigDecimal getAnteAmount() {
        return anteAmount;
    }

    public void setSetBalance(int playerId, BigDecimal balance) {
        balanceMap.put(playerId, balance);
    }

    public Map<Integer, BigDecimal> getBalanceMap() {
        return balanceMap;
    }

    @Override
    public String toString() {
        return "RoundReport: " + balanceMap;
    }
}
