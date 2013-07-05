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

import com.cubeia.poker.blinds.MissedBlindsStatus;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import org.apache.log4j.Logger;

public class WaitingForSmallBlindState extends AbstractBlindsState {

    private static final long serialVersionUID = 4983163822097132780L;

    private static transient Logger log = Logger.getLogger(WaitingForSmallBlindState.class);


    @Override
    public boolean smallBlind(int playerId, PokerContext pokerContext, BlindsRound round) {
        int smallBlind = round.getBlindsInfo().getSmallBlindPlayerId();
        if (smallBlind == playerId) {
            PokerPlayer player = pokerContext.getPlayerInCurrentHand(playerId);
            player.addBetOrGoAllIn(pokerContext.getSettings().getSmallBlindAmount());
            round.smallBlindPosted(playerId);
            return true;
        } else {
            log.info("Expected player " + smallBlind + " to act, but got action from " + playerId);
            return false;
        }
    }

    @Override
    public boolean declineEntryBet(int playerId, PokerContext pokerContext, BlindsRound round) {
        int smallBlind = round.getBlindsInfo().getSmallBlindPlayerId();
        if (smallBlind == playerId) {
            PokerPlayer player = pokerContext.getPlayerInCurrentHand(playerId);
            player.setMissedBlindsStatus(MissedBlindsStatus.MISSED_SMALL_BLIND);
            round.getBlindsInfo().setHasDeadSmallBlind(true);
            round.smallBlindDeclined(player);
            return true;
        } else {
            log.info("Expected player " + smallBlind + " to act, but got action from " + playerId);
            return false;
        }
    }

    @Override
    public boolean timeout(PokerContext pokerContext, BlindsRound round) {
        if (round.isTournamentBlinds()) {
            log.debug("Small blind timeout on tournament table - auto post small blind for player: " + round.getBlindsInfo().getSmallBlindPlayerId());
            smallBlind(round.getBlindsInfo().getSmallBlindPlayerId(), pokerContext, round);
        } else {
            int smallBlind = round.getBlindsInfo().getSmallBlindPlayerId();
            PokerPlayer player = pokerContext.getPlayerInCurrentHand(smallBlind);
            player.setMissedBlindsStatus(MissedBlindsStatus.MISSED_SMALL_BLIND);
            round.getBlindsInfo().setHasDeadSmallBlind(true);
            round.smallBlindDeclined(player);
        }
        return true;
    }
}
