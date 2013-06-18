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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.player.PokerPlayer;

import java.math.BigDecimal;

public interface BettingRoundContext {

    /**
     * Returns true if betting is capped.
     *
     */
    boolean isBettingCapped();

    /**
     * Gets the currently highest bet in this betting round.
     *
     * @return the currently highest bet in this betting round
     */
    public BigDecimal getHighestBet();

    /**
     * Gets the currently highest (complete) bet in this betting round.
     *
     * With a complete bet we mean that an all-in that does not take us to the next bet level does not count.
     * For example, player A bets $10 and player B goes all-in for $12.
     *
     * @return the currently highest bet in this betting round
     */
    public BigDecimal getHighestCompleteBet();


    /**
     * Gets the size of the last complete bet or raise.
     *
     * @return the size of the last bet or raise
     */
    public BigDecimal getSizeOfLastCompleteBetOrRaise();

    /**
     * Checks whether all other players in this round are all in.
     *
     * @return <code>true</code> if so, <code>false</code> otherwise
     */
    public boolean allOtherNonFoldedPlayersAreAllIn(PokerPlayer thisPlayer);

    /**
     * Gets the pot size including all sides pots
     * @return the size of the pot
     */
    public BigDecimal getPotSize();

}
