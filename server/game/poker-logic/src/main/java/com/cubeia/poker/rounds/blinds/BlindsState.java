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

package com.cubeia.poker.rounds.blinds;

import com.cubeia.poker.context.PokerContext;

import java.io.Serializable;


public interface BlindsState extends Serializable {

    boolean smallBlind(int playerId, PokerContext context, BlindsRound blindsRound);

    boolean deadSmallBlind(int playerId, PokerContext context, BlindsRound blindsRound);

    boolean bigBlind(int playerId, PokerContext context, BlindsRound blindsRound);

    boolean bigBlindPlusDeadSmallBlind(int playerId, PokerContext context, BlindsRound round);

    boolean entryBet(int playerId, PokerContext context, BlindsRound round);

    boolean waitForBigBlind(int playerId, PokerContext context, BlindsRound round);

    boolean declineEntryBet(int playerId, PokerContext context, BlindsRound blindsRound);

    boolean timeout(PokerContext context, BlindsRound round);

    boolean isFinished();

    boolean isCanceled();

}
