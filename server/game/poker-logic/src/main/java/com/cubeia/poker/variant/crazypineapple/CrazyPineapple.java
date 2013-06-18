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

package com.cubeia.poker.variant.crazypineapple;

import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.PokerGameBuilder;

import static com.cubeia.poker.rounds.betting.BettingRoundName.FLOP;
import static com.cubeia.poker.rounds.betting.BettingRoundName.PRE_FLOP;
import static com.cubeia.poker.rounds.betting.BettingRoundName.RIVER;
import static com.cubeia.poker.rounds.betting.BettingRoundName.TURN;
import static com.cubeia.poker.variant.RoundCreators.bettingRound;
import static com.cubeia.poker.variant.RoundCreators.blinds;
import static com.cubeia.poker.variant.RoundCreators.dealCommunityCards;
import static com.cubeia.poker.variant.RoundCreators.dealFaceDownCards;
import static com.cubeia.poker.variant.RoundCreators.discardRound;
import static com.cubeia.poker.variant.RoundCreators.fromBigBlind;

public class CrazyPineapple {

    public static GameType createGame() {
        return new PokerGameBuilder().withRounds(
                        blinds(),
                        dealFaceDownCards(3),
                        bettingRound(PRE_FLOP, fromBigBlind()),
                        dealCommunityCards(3),
                        bettingRound(FLOP),
                        discardRound(1),
                        dealCommunityCards(1),
                        bettingRound(TURN),
                        dealCommunityCards(1),
                        bettingRound(RIVER)).build();
    }
}
