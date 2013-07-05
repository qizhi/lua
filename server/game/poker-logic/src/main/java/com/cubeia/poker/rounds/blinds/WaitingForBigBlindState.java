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
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import org.apache.log4j.Logger;

public class WaitingForBigBlindState extends AbstractBlindsState {

    private static final long serialVersionUID = 5213021240304587621L;

    private static transient Logger log = Logger.getLogger(WaitingForBigBlindState.class);

    @Override
    public boolean bigBlind(int playerId, PokerContext context, BlindsRound blindsRound) {
        BlindsInfo blindsInfo = context.getBlindsInfo();
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player != null && player.getActionRequest().isOptionEnabled(PokerActionType.BIG_BLIND)) {
            blindsInfo.setBigBlind(player);
            player.addBetOrGoAllIn(context.getSettings().getBigBlindAmount());
            player.setHasOption(true);
            player.setHasPostedEntryBet(true);
            blindsRound.bigBlindPosted(playerId);
            return true;
        } else {
            log.info("Player " + player + " is not allowed to post big blind.");
            return false;
        }
    }

    @Override
    public boolean declineEntryBet(int playerId, PokerContext context, BlindsRound blindsRound) {
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player != null && player.getActionRequest().isOptionEnabled(PokerActionType.DECLINE_ENTRY_BET)) {
            player.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
            blindsRound.bigBlindDeclined(player);
            return true;
        } else {
            log.info("Player " + player + " is not allowed to decline big blind.");
            return false;
        }
    }

    @Override
    public boolean timeout(PokerContext context, BlindsRound round) {
        if (context.isTournamentBlinds()) {
            log.debug("Big blind timeout on tournament table - auto post big blind for player: " + round.getBlindsInfo().getBigBlindPlayerId());
            bigBlind(round.getBlindsInfo().getBigBlindPlayerId(), context, round);
        } else {
            int bigBlind = round.getBlindsInfo().getBigBlindPlayerId();
            PokerPlayer player = context.getPlayerInCurrentHand(bigBlind);
            player.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
            round.bigBlindDeclined(player);
        }
        return true;
    }
}
