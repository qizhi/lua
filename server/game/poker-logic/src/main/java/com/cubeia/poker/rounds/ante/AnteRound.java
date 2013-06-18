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

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.util.ThreadLocalProfiler;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnteRound implements Round {

    private static final long serialVersionUID = -6452364533249060511L;

    private static transient Logger log = Logger.getLogger(AnteRound.class);

    private AnteRoundHelper anteRoundHelper;
    
    private PokerContext context;
    
    private ServerAdapterHolder serverAdapterHolder;

    public AnteRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, AnteRoundHelper anteRoundHelper) {
        this.context = context;
        this.anteRoundHelper = anteRoundHelper;
        this.serverAdapterHolder = serverAdapterHolder;

        clearPlayerActionOptions();
        handleAnteRequests(context.getPlayersInHand());
    }

    /**
     * Check if any players are sitting out but are in the hand and make them
     * auto-decline ante.
     * <p/>
     * Request ante from all other players.
     *
     * @param players the players who should be asked to post ante
     */
    private void handleAnteRequests(Collection<PokerPlayer> players) {
        Collection<PokerPlayer> activePlayers = new ArrayList<PokerPlayer>(players);
        for (PokerPlayer player : players) {
            if (player.isSittingOut()) {
                activePlayers.remove(player);
                declineAnteForPlayer(player);
            }
        }
        requestAnteFromAllPlayersInHand(activePlayers);
    }

    private void declineAnteForPlayer(PokerPlayer player) {
        PokerAction action = new PokerAction(player.getId(), PokerActionType.DECLINE_ENTRY_BET);
        act(action);
    }

    private void requestAnteFromAllPlayersInHand(Collection<PokerPlayer> players) {
        anteRoundHelper.requestAntes(players);
    }

    private void clearPlayerActionOptions() {
        for (PokerPlayer player : context.getPlayersInHand()) {
            player.setHasPostedEntryBet(false);
            player.clearActionRequest();
        }
    }

    public boolean act(PokerAction action) {
        log.debug("Act: " + action);
        PokerPlayer player = context.getPlayerInCurrentHand(action.getPlayerId());
        if (!isValidAnte(player)) {
            return false;
        }
        boolean handled = true;

        switch (action.getActionType()) {
            case ANTE:
                ThreadLocalProfiler.add("AnteRound.act.ante");
                player.addBet(context.getSettings().getAnteAmount());
                player.setHasActed(true);
                player.setHasPostedEntryBet(true);
                action.setBetAmount(context.getSettings().getAnteAmount());
                getServerAdapter().notifyActionPerformed(action, player);
                getServerAdapter().notifyPlayerBalance(player);
                anteRoundHelper.notifyPotSizeAndRakeInfo();

                break;
            case DECLINE_ENTRY_BET:
                ThreadLocalProfiler.add("AnteRound.act.decline");
                player.setHasActed(true);
                player.setHasFolded(true);
                player.setHasPostedEntryBet(false);
                getServerAdapter().notifyActionPerformed(action, player);
                getServerAdapter().notifyPlayerBalance(player);
                removeNonParticipatingPlayer(player);
                break;
            default:
                log.info(action.getActionType() + " is not legal here");
                handled = false;
                break;
        }

        if (handled) {
            player.clearActionRequest();

            Collection<PokerPlayer> playersInHand = context.getCurrentHandSeatingMap().values();

            if (anteRoundHelper.isImpossibleToStartRound(playersInHand)) {
                log.debug("impossible to start hand, too few players payed ante, will cancel");
                Collection<PokerPlayer> declinedPlayers = anteRoundHelper.setAllPendingPlayersToDeclineEntryBet(playersInHand);
                for (PokerPlayer declinedPlayer : declinedPlayers) {
                    PokerAction declineAction = new PokerAction(declinedPlayer.getId(), PokerActionType.DECLINE_ENTRY_BET);
                    getServerAdapter().notifyActionPerformed(declineAction, player);
                }
            }
        }
        return handled;
    }

    private Collection<PokerPlayer> getAllSeatedPlayers() {
        return context.getCurrentHandSeatingMap().values();
    }

    /**
     * Checks whether this player is allowed to place ante.
     *
     * @param player the player who tries to place the ante
     * @return false if the player was not allowed to place ANTE
     */
    private boolean isValidAnte(PokerPlayer player) {
        PossibleAction option = player.getActionRequest().getOption(PokerActionType.ANTE);
        if (option == null) {
            log.info("Illegal ante request from player [" + player + "]");
            return false;
        }
        return true;
    }

    public void timeout() {
        List<PokerPlayer> playersToSitOut = new ArrayList<PokerPlayer>();

        for (PokerPlayer player : getAllSeatedPlayers()) {
            if (!player.hasActed()) {
                log.debug("Player[" + player + "] ante timed out. Will decline entry bet.");
                PokerAction action = new PokerAction(player.getId(), PokerActionType.DECLINE_ENTRY_BET, true);

                player.setHasActed(true);
                player.setHasFolded(true);
                player.setHasPostedEntryBet(false);
                player.clearActionRequest();
                getServerAdapter().notifyActionPerformed(action, player);
                getServerAdapter().notifyPlayerBalance(player);
                playersToSitOut.add(player);
            }
        }

        // sit out all the players
        for (PokerPlayer player : playersToSitOut) {
            removeNonParticipatingPlayer(player);
        }
    }

    private void removeNonParticipatingPlayer(PokerPlayer player) {
        anteRoundHelper.setPlayerSitOut(player, context, getServerAdapter());
        anteRoundHelper.removePlayerFromCurrentHand(player, context);
    }

    public String getStateDescription() {
        return "currentState=null";
    }

    public boolean isFinished() {
        for (PokerPlayer player : getAllSeatedPlayers()) {
            if (!player.hasActed() && !player.isSittingOut()) {
                return false;
            }
        }
        return true;
    }

    public void visit(RoundVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isCanceled() {
        Collection<PokerPlayer> players = context.getCurrentHandSeatingMap().values();
        return anteRoundHelper.hasAllPlayersActed(players) && anteRoundHelper.numberOfPlayersPayedAnte(players) < 2;
    }

    private ServerAdapter getServerAdapter() {
        return serverAdapterHolder.get();
    }
}
