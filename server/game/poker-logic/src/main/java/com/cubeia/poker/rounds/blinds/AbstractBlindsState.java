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
import org.apache.log4j.Logger;

public abstract class AbstractBlindsState implements BlindsState {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = Logger.getLogger(AbstractBlindsState.class);

    @Override
    public boolean bigBlind(int playerId, PokerContext context, BlindsRound round) {
        log.debug("Ignoring bigBlind from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean smallBlind(int playerId, PokerContext context, BlindsRound round) {
        log.debug("Ignoring smallBlind from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean entryBet(int playerId, PokerContext context, BlindsRound blindsRound) {
        log.debug("Ignoring entryBet from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean waitForBigBlind(int playerId, PokerContext context, BlindsRound round) {
        log.debug("Ignoring waitForBigBlind from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean declineEntryBet(int playerId, PokerContext context, BlindsRound blindsRound) {
        log.debug("Ignoring declineEntryBet from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean deadSmallBlind(int playerId, PokerContext context, BlindsRound blindsRound) {
        log.debug("Ignoring deadSmallBlind from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean bigBlindPlusDeadSmallBlind(int playerId, PokerContext context, BlindsRound round) {
        log.debug("Ignoring bbPlusDeadSmallBlind from player " + playerId + " in state " + this);
        return false;
    }

    @Override
    public boolean timeout(PokerContext context, BlindsRound round) {
        log.debug("Ignoring timeout in state " + this);
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

}
