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

package com.cubeia.games.poker.adapter;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates min and max buy ins depending on the player's balance and
 * the rules of the table.
 *
 * @author w
 */
public class BuyInCalculator {

	Logger log = LoggerFactory.getLogger(getClass());
	
    /**
     * Calculates the possible buy in range.
     *
     * @param tableMinBuyIn min buy in on table
     * @param tableMaxBuyIn max buy in on table
     * @param anteLevel     ante level on table
     * @param balanceAtTable players balance
     * @return a container for min, max and a buy in possible flag
     */
    public MinAndMaxBuyInResult calculateBuyInLimits(BigDecimal tableMinBuyIn, BigDecimal tableMaxBuyIn, BigDecimal anteLevel, BigDecimal balanceAtTable) {
        if (balanceAtTable.compareTo(tableMaxBuyIn) >= 0) {
            return new MinAndMaxBuyInResult(BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        return new MinAndMaxBuyInResult(
                calculateMinBuyIn(tableMinBuyIn, tableMaxBuyIn, anteLevel, balanceAtTable),
                calculateMaxBuyIn(tableMinBuyIn, tableMaxBuyIn, anteLevel, balanceAtTable),
                true);
    }

    public BigDecimal calculateAmountToReserve(BigDecimal tableMaxBuyIn, BigDecimal playerBalanceIncludingPending, BigDecimal amountRequestedByUser) {
        return amountRequestedByUser.min(tableMaxBuyIn.subtract(playerBalanceIncludingPending));
    }

    private BigDecimal calculateMinBuyIn(BigDecimal tableMinBuyIn, BigDecimal tableMaxBuyIn, BigDecimal anteLevel, BigDecimal playerBalance) {
    	log.info("calculateMinBuyIn tableMinBuyIn["+tableMinBuyIn+"] tableMaxBuyIn["+tableMaxBuyIn+"] anteLevel["+anteLevel+"] playerBalance["+playerBalance+"]");
        if (playerBalance.compareTo(tableMinBuyIn) < 0) {
            return anteLevel.max(tableMinBuyIn.subtract(playerBalance));
        } else {
            return anteLevel.min(tableMaxBuyIn.subtract(playerBalance));
        }
    }

    private BigDecimal calculateMaxBuyIn(BigDecimal tableMinBuyIn, BigDecimal tableMaxBuyIn, BigDecimal anteLevel, BigDecimal balanceAtTable) {
        return tableMaxBuyIn.subtract(balanceAtTable);
    }

    /**
     * Buy in range result. If {@link MinAndMaxBuyInResult#isBuyInPossible()} is false both min and max are
     * set to zero.
     *
     * @author w
     */
    public static class MinAndMaxBuyInResult {
        private final BigDecimal minBuyIn;
        private final BigDecimal maxBuyIn;
        private final boolean buyInPossible;

        public MinAndMaxBuyInResult(BigDecimal minBuyIn, BigDecimal maxBuyIn, boolean buyInPossible) {
            this.minBuyIn = minBuyIn;
            this.maxBuyIn = maxBuyIn;
            this.buyInPossible = buyInPossible;
        }

        public BigDecimal getMinBuyIn() {
            return minBuyIn;
        }

        public BigDecimal getMaxBuyIn() {
            return maxBuyIn;
        }

        public boolean isBuyInPossible() {
            return buyInPossible;
        }
    }
}
