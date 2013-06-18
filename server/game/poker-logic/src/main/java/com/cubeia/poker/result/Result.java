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

package com.cubeia.poker.result;

import com.cubeia.poker.handhistory.api.*;
import com.cubeia.poker.pot.Pot;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal netResult;

    private final BigDecimal winningsIncludingOwnBets;

    private final BigDecimal bets;

    /**
     * winning share by pot for this player
     */
    private final Map<Pot, BigDecimal> winningsByPot;

    public Result(BigDecimal netResult, BigDecimal ownBets, Map<Pot, BigDecimal> winningsByPot) {
        this.netResult = netResult;
        this.bets = ownBets;
        this.winningsByPot = winningsByPot;
        this.winningsIncludingOwnBets = netResult.add(ownBets);
    }

    public com.cubeia.poker.handhistory.api.HandResult translate(int playerId) {
        BigDecimal value = getWinningsIncludingOwnBets().subtract(getNetResult());
        return new com.cubeia.poker.handhistory.api.HandResult(playerId, getNetResult(), getWinningsIncludingOwnBets(), new BigDecimal(-1), value);
    }

    /**
     * The net result for this player. This value might be negative.
     *
     * @return net result
     */
    public BigDecimal getNetResult() {
        return netResult;
    }

    /**
     * Returns the winnings including own bets.
     *
     * @return
     */
    public BigDecimal getWinningsIncludingOwnBets() {
        return winningsIncludingOwnBets;
    }

    public BigDecimal getBets() {
        return bets;
    }

    public Map<Pot, BigDecimal> getWinningsByPot() {
        return winningsByPot;
    }

    @Override
    public String toString() {
        return "Result [netResult=" + netResult + ", winningsIncludingOwnBets=" + winningsIncludingOwnBets
                + ", winningsByPot=" + winningsByPot + "]";
    }


}
