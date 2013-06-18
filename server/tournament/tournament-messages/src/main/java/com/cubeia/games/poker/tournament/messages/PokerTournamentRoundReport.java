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

package com.cubeia.games.poker.tournament.messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents the result after a poker hand has been played in a tournament.
 */
public class PokerTournamentRoundReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Integer, BigDecimal> balanceMap = new HashMap<Integer, BigDecimal>();

    private Level currentBlindsLevel;

    public PokerTournamentRoundReport(Level blindsLevel) {
        currentBlindsLevel = blindsLevel;
    }

    public PokerTournamentRoundReport(Map<Integer, BigDecimal> balanceMap, Level currentLevel) {
        this.balanceMap = balanceMap;
        this.currentBlindsLevel = currentLevel;
    }

    public void setBalance(int playerId, BigDecimal balance) {
        balanceMap.put(playerId, balance);
    }

    public Set<Entry<Integer, BigDecimal>> getBalances() {
        return balanceMap.entrySet();
    }

    public Level getCurrentBlindsLevel() {
        return currentBlindsLevel;
    }

    public void setCurrentBlindsLevel(Level currentBlindsLevel) {
        this.currentBlindsLevel = currentBlindsLevel;
    }

    public static class Level implements Serializable {

        private final BigDecimal smallBlindAmount;

        private final BigDecimal bigBlindAmount;

        private final BigDecimal anteAmount;

        public Level(BigDecimal smallBlindAmount, BigDecimal bigBlindAmount, BigDecimal anteAmount) {
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

        @Override
        public String toString() {
            return "Level{" +
                    "smallBlindAmount=" + smallBlindAmount +
                    ", bigBlindAmount=" + bigBlindAmount +
                    ", anteAmount=" + anteAmount +
                    '}';
        }
    }
}
