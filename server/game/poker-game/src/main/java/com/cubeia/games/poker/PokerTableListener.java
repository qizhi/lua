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

package com.cubeia.games.poker;

import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TournamentTableListener;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.games.poker.adapter.DisconnectHandler;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.handler.BackendPlayerSessionHandler;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.blinds.MissedBlindsStatus;
import com.cubeia.poker.player.PokerPlayer;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.cubeia.firebase.api.game.player.PlayerStatus.CONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.DISCONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.LEAVING;
import static com.cubeia.firebase.api.game.player.PlayerStatus.WAITING_REJOIN;

/**
 * <p>In this class we are modifying a stored watcher list in the poker state object.
 * The reason for this is that we want to detect if a player that joins the
 * game was previously a watcher. If the player was a watcher then we don't need to
 * send the game state again.</p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerTableListener implements TournamentTableListener {

    private static Logger log = LoggerFactory.getLogger(PokerTableListener.class);

    @Inject
    @VisibleForTesting
    ActionCache actionCache;

    @Inject
    @VisibleForTesting
    GameStateSender gameStateSender;

    @Inject
    @VisibleForTesting
    BackendPlayerSessionHandler backendPlayerSessionHandler;

    @Inject
    @VisibleForTesting
    StateInjector stateInjector;

    @Inject
    @VisibleForTesting
    PokerState state;

    @Inject
    DisconnectHandler disconnectHandler;
    
    @Service
    PublicClientRegistryService clientRegistry;

    /**
     * A Player has joined our table. =)
     */
    public void playerJoined(Table table, GenericPlayer player) {
        stateInjector.injectAdapter(table);
        log.debug("Player[" + player.getPlayerId() + ":" + player.getName() + "] joined Table[" + table.getId() + ":" + table.getMetaData().getName() + "]");
        if (state.isPlayerSeated(player.getPlayerId())) {
            // rejoin
            // TODO Possibly add reconnect event to action cache?
            sitInPlayer(table, player);
        } else {
            addPlayer(table, player, false);
        }
    }

    /**
     * Check if joined from watching state, only send if not a previous watcher.
     */
    private void sendGameStateToSittingInPlayerIfNeeded(Table table, GenericPlayer player) {
        if (!state.removeAsWatcher(player.getPlayerId())) {
            gameStateSender.sendGameState(table, player.getPlayerId());
            state.sendGameStateTo(player.getPlayerId());
        }
    }

    /**
     * A Player has left our table. =(
     */
    @Override
    public void playerLeft(Table table, int playerId) {
        log.debug("Player left: " + playerId);
        stateInjector.injectAdapter(table);
        removePlayer(table, playerId, false);
    }

    @Override
    public void tournamentPlayerJoined(Table table, GenericPlayer player, Serializable balance) {
        stateInjector.injectAdapter(table);
        PokerPlayer pokerPlayer = addPlayer(table, player, true);
        pokerPlayer.setHasPostedEntryBet(true);
        pokerPlayer.addChips((BigDecimal)balance);
        pokerPlayer.saveStartingBalance();
    }

    @Override
    public void tournamentPlayerRejoined(Table table, GenericPlayer player) {
        stateInjector.injectAdapter(table);
        log.debug("Tournament player rejoined: " + player);
        sendGameStateToSittingInPlayerIfNeeded(table, player);
    }

    @Override
    public void tournamentPlayerRemoved(Table table, int playerId, Reason reason) {
        stateInjector.injectAdapter(table);
        removePlayer(table, playerId, true);
    }

    /**
     * Send current game state to the watching player
     */
    @Override
    public void watcherJoined(Table table, int playerId) {
        log.debug("Player[" + playerId + "] watching Table[" + table.getId() + ":" + table.getMetaData().getName() + "]");
        stateInjector.injectAdapter(table);
        state.addWatcher(playerId);
        sendGameStateToWatcherIfNeeded(state, table, playerId);
    }

    @VisibleForTesting
    private void sendGameStateToWatcherIfNeeded(PokerState state, Table table, int playerId) {
        if (state.getPlayerInCurrentHand(playerId) == null) {
            gameStateSender.sendGameState(table, playerId);
            state.sendGameStateTo(playerId);
        }
    }


    public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {
        log.debug("Player status changed: " + playerId+" -> "+status);
        stateInjector.injectAdapter(table);
        if (status.equals(DISCONNECTED) || status.equals(LEAVING) || status.equals(WAITING_REJOIN)) {
            log.debug("Player status changed will be set as sit out, tid[" + table.getId() + "] pid[" + playerId + "] status[" + status + "]");
            state.playerSitsOutNextHand(playerId);
        } else if (status.equals(CONNECTED)) {
            log.debug("Player status changed will be set as sit in, tid[" + table.getId() + "] pid[" + playerId + "] status[" + status + "]");
            state.playerIsSittingIn(playerId);
        } else {
            log.debug("Player status changed but we don't care, tid[" + table.getId() + "] pid[" + playerId + "] status[" + status + "]");
        }

        disconnectHandler.checkDisconnectTime(table, playerId, status);
    }

    public void seatReserved(Table table, GenericPlayer player) {
    }

    public void watcherLeft(Table table, int playerId) {
        state.removeAsWatcher(playerId);
    }

    private void sitInPlayer(Table table, GenericPlayer player) {
        sendGameStateToSittingInPlayerIfNeeded(table, player);
        state.playerIsSittingIn(player.getPlayerId());
    }

    @VisibleForTesting
    protected PokerPlayer addPlayer(Table table, GenericPlayer player, boolean tournamentPlayer) {

        log.debug("adding player {} to state, was seated before (sitting -> watcher): {}",
                player.getPlayerId(),
                state.getPlayerInCurrentHand(player.getPlayerId()) != null);

        sendGameStateToSittingInPlayerIfNeeded(table, player);
        PokerPlayer pokerPlayer = new PokerPlayerImpl(player);
        int operatorId = clientRegistry.getOperatorId(player.getPlayerId());
        pokerPlayer.setOperatorId(operatorId);
        state.addPlayer(pokerPlayer);

        if (!tournamentPlayer) {
            log.debug("Start wallet session for player: " + player);
            backendPlayerSessionHandler.startWalletSession(state, table, player.getPlayerId());
            pokerPlayer.setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        }

        return pokerPlayer;
    }

    private int getCurrentRoundNumber() {
        return ((FirebaseState) state.getAdapterState()).getHandCount();
    }

    private void removePlayer(Table table, int playerId, boolean tournamentPlayer) {
        if (!tournamentPlayer) {
            PokerPlayerImpl pokerPlayer = (PokerPlayerImpl) state.getPokerPlayer(playerId);
            log.debug("Close player session: "+pokerPlayer);
            if (pokerPlayer != null) { // Check if player was removed already
                backendPlayerSessionHandler.endPlayerSessionInBackend(table, pokerPlayer, getCurrentRoundNumber(), state);
            }
        }
        log.debug("Remove player from state : "+playerId);
        state.removePlayer(playerId);
    }
}
