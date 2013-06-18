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

package com.cubeia.poker.states;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class NotStartedSTM extends AbstractPokerGameSTM {

    private static final long serialVersionUID = -1675095508189680830L;

    private static final Logger log = LoggerFactory.getLogger(NotStartedSTM.class);

    public NotStartedSTM() {
    }

    public NotStartedSTM(GameType gameType, PokerContext context, ServerAdapterHolder serverAdapterHolder, StateChanger stateChanger) {
        super(gameType, context, serverAdapterHolder, stateChanger);
    }

    public String toString() {
        return "NotStartedState";
    }

    @Override
    public void playerJoined(PokerPlayer player) {
        // TODO: Probably prettier to have a separate state for tournaments.
        log.debug("Player " + player.getId() + " joined. Number of players: " + context.getNumberOfPlayersSittingIn());
        if (context.getNumberOfPlayersSittingIn() > 1 && !context.isTournamentTable()) {
            changeState(new WaitingToStartSTM());
        }
    }

    @Override
    public void timeout() {
        if (context.isTournamentTable()) {
            // This should only really happen from tournaments.
            log.debug("Starting hand.");
            startHand();
        }
    }

    @Override
    public void performPendingBuyIns(Set<PokerPlayer> players) {
        doPerformPendingBuyIns(players);
    }

    /*
     * If we are not playing, a player who wants to sit out can do it straight sittingOutNextHand.
     */
    @Override
    public void setPlayerSitOutNextHand(int playerId) {
        markPlayerAsSittingOutOrAway(context.getPlayer(playerId));
    }
}
