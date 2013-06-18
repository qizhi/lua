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

package com.cubeia.poker.rounds.ante;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.RoundHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Testable helper class for the ante round.
 *
 * @author w
 */
public class AnteRoundHelper extends RoundHelper {

    private static final long serialVersionUID = 1101877161161688793L;

    private PokerContext context;

    public AnteRoundHelper(PokerContext context, ServerAdapterHolder serverAdapter) {
        super(context, serverAdapter);
        this.context = context;
    }

    /**
     * Returns true if all players have acted.
     *
     * @param players players
     * @return true if all players have acted
     */
    boolean hasAllPlayersActed(Collection<PokerPlayer> players) {
        boolean allActed = true;
        for (PokerPlayer player : players) {
            allActed = allActed && (player.hasActed() || player.isSittingOut());
        }

        return allActed;
    }

    /**
     * Tests if a player can act. A player can act if all of the following are true:
     * - has not folded
     * - has not acted
     * - is not sitting out
     * - is not all in
     *
     * @param player player to check
     * @return true if player can act
     */
    boolean canPlayerAct(PokerPlayer player) {
        return !player.hasFolded() && !player.hasActed() && !player.isSittingOut() && !player.isAllIn();
    }

    /**
     * Setup the given players current action requests as ante requests and send them via the game.
     *
     * @param players the players who should be asked to post ante
     */
    public void requestAntes(Collection<PokerPlayer> players) {
        BigDecimal anteLevel = context.getSettings().getAnteAmount();
        ArrayList<ActionRequest> requests = new ArrayList<ActionRequest>();
        for (PokerPlayer player : players) {
            player.enableOption(new PossibleAction(PokerActionType.ANTE, anteLevel));
            player.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
            ActionRequest request = player.getActionRequest();
            requests.add(request);
        }
        requestMultipleActions(requests);
    }

    /**
     * Returns true if it is impossible to start the round.
     * If all players but one has declined ante the round can't be started.
     *
     * @param playersInHand all players who could potentially have paid the ante
     * @return true if round is impossible to start
     */
    boolean isImpossibleToStartRound(Collection<PokerPlayer> playersInHand) {
        boolean allPlayersButOneIsOut = (numberOfPlayersPayedAnte(playersInHand) + numberOfPendingPlayers(playersInHand)) <= 1;
        return allPlayersButOneIsOut;
    }

    int numberOfPendingPlayers(Collection<PokerPlayer> players) {
        Collection<PokerPlayer> hasActed = Collections2.filter(players, new Predicate<PokerPlayer>() {
            @Override
            public boolean apply(PokerPlayer player) {
                return !player.hasActed();
            }
        });
        return hasActed.size();
    }

    int numberOfPlayersPayedAnte(Collection<PokerPlayer> players) {
        Collection<PokerPlayer> hasPostedEntryBet = Collections2.filter(players, new Predicate<PokerPlayer>() {
            @Override
            public boolean apply(PokerPlayer player) {
                return player.hasPostedEntryBet();
            }
        });
        return hasPostedEntryBet.size();
    }

    /**
     * Set all pending players to decline entry bet and return the list of modified players.
     *
     * @param players all players in hand
     * @return players that were modified
     */
    Collection<PokerPlayer> setAllPendingPlayersToDeclineEntryBet(Collection<PokerPlayer> players) {
        ArrayList<PokerPlayer> pendingPlayers = new ArrayList<PokerPlayer>();
        for (PokerPlayer pokerPlayer : players) {
            if (!pokerPlayer.hasActed()) {
                pokerPlayer.setHasPostedEntryBet(false);
                pokerPlayer.setHasActed(true);
                pendingPlayers.add(pokerPlayer);
            }
        }
        return pendingPlayers;
    }

}
