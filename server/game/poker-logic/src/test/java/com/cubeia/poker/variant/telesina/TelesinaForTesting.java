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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.variant.telesina.hand.TelesinaHandStrengthEvaluator;

public class TelesinaForTesting extends Telesina {

    private static final long serialVersionUID = 1L;

    private int numberOfSentBestHands = 0;
    public int currentRoundId = 0;

    public TelesinaForTesting(TelesinaDeckFactory deckFactory, TelesinaRoundFactory roundFactory, TelesinaDealerButtonCalculator dealerButtonCalculator) {
        super(deckFactory, roundFactory, dealerButtonCalculator);
    }

    @Override
    protected void calculateAndSendBestHandToPlayer(TelesinaHandStrengthEvaluator handStrengthEvaluator, PokerPlayer player) {
        ++numberOfSentBestHands;
    }

    public int getNumberOfSentBestHands() {
        return numberOfSentBestHands;
    }

    @Override
    public Rank getDeckLowestRank() {
        return Rank.TWO;
    }

    @Override
    protected int getBettingRoundId() {
        return currentRoundId;
    }
}
