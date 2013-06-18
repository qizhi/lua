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
package com.cubeia.poker;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.model.GameStateSnapshot;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.states.NotStartedSTM;
import com.cubeia.poker.states.PlayingSTM;
import com.cubeia.poker.states.PokerGameSTM;
import com.cubeia.poker.states.ShutdownSTM;
import com.cubeia.poker.states.StateChanger;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.variant.GameType;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static java.util.Collections.singleton;

/**
 * This is the class that users of the poker api will interface with.
 * <p/>
 * This class is responsible for handling all poker actions.
 * <p/>
 * Also, the current state of the game can be queried from this class, to be able to send a snapshot
 * view of the game to new players.
 * <p/>
 * NOTE: The name of the class should really be PokerGame.
 */
public class PokerState implements Serializable, IPokerState {

    private static final Logger log = LoggerFactory.getLogger(PokerState.class);

    private static final long serialVersionUID = -7208084698542289729L;

    /* -------- Dependency Injection Members, initialization needed -------- */

    /**
     * The server adapter is the layer between the server and the game logic.
     * You must set the adapter before using the game logic. The adapter is
     * declared transient, so if you serialize the game state you will need to
     * reset the server adapter.
     */
    private transient ServerAdapter serverAdapter;

    private ServerAdapterHolder serverAdapterHolder = new ServerAdapterHolder();

    /**
     * Used by the server adapter layer to store state. (Should be removed)
     */
    private Object adapterState;

    /* ------------------------- Internal Members -------------------------- */

    private StateHolder stateHolder = new StateHolder();

    private StateChanger stateChanger = stateHolder;

    /**
     * Map of external table properties. External properties are optional stuff that might be needed
     * when integrating to external systems. Session/table/tournament id's for example.
     */
    private Map<String, Serializable> externalTableProperties = new HashMap<String, Serializable>();

    @VisibleForTesting
    protected PokerContext pokerContext;

    private GameType gameType;

    public PokerState() {
    }

    public String toString() {
        return pokerContext.toString();
    }

    @Override
    public void init(GameType gameType, PokerSettings settings) {
        pokerContext = new PokerContext(settings);
        this.gameType = gameType;
        gameType.setPokerContextAndServerAdapter(pokerContext, serverAdapterHolder);
        stateHolder.changeState(new NotStartedSTM(gameType, pokerContext, serverAdapterHolder, stateChanger));
    }

    /**
     * Adds a player to this game.
     *
     * Might trigger the game to start, if we were waiting for one more player.
     *
     * If we are in the middle of a hand, the added player will have no effect on that game.
     *
     * @param player the player to add
     */
    public void addPlayer(PokerPlayer player) {
        pokerContext.addPlayer(player);
        stateHolder.get().playerJoined(player);
    }

    public boolean act(PokerAction action) {
        // Check sizes of caches and log warnings
        pokerContext.checkWarnings();
        return getCurrentState().act(action);
    }

    public List<Card> getCommunityCards() {
        return pokerContext.getCommunityCards();
    }

    public boolean isFinished() {
        return pokerContext.isFinished();
    }

    public void timeout() {
        getCurrentState().timeout();
    }

    @Override
    public void playerSitsOutNextHand(int playerId) {
        getCurrentState().setPlayerSitOutNextHand(playerId);
    }

    public boolean isPlayerSeated(int playerId) {
        return pokerContext.isPlayerSeated(playerId);
    }

    public Collection<PokerPlayer> getSeatedPlayers() {
        return pokerContext.getSeatedPlayers();
    }

    @Override
    public Map<Integer, PokerPlayer> getCurrentHandPlayerMap() {
        return pokerContext.getCurrentHandPlayerMap();
    }

    public Map<String, Serializable> getExternalTableProperties() {
        return externalTableProperties;
    }

    @Override
    public PokerPlayer getPlayerInCurrentHand(Integer playerId) {
        return pokerContext.getPlayerInCurrentHand(playerId);
    }

    @Override
    public SortedMap<Integer, PokerPlayer> getCurrentHandSeatingMap() {
        return pokerContext.getCurrentHandSeatingMap();
    }

    /**
     * Returns true if the player is in the set of players for the hand and
     * we are in a playing state (i.e. not playing or waiting to start will result
     * in false being returned).
     */
    @Override
    public boolean isPlayerInHand(int playerId) {
        return stateHolder.get().isPlayerInHand(playerId);
    }

    public void scheduleTournamentHandStart() {
        log.debug("Received start hand signal. Scheduling a timeout so the hand doesn't start too quickly.");
        long timeout = getSettings().getTiming().getTime(Periods.START_NEW_HAND);
        log.debug("Scheduling timeout in " + timeout + " millis.");
        serverAdapterHolder.get().scheduleTimeout(timeout);
    }

    public long getStartTime() {
        return pokerContext.getStartTime();
    }

    @VisibleForTesting
    public void commitPendingBalances() {
        pokerContext.commitPendingBalances(pokerContext.getMaxBuyIn());
    }

    public PokerGameSTM getGameState() {
        return getCurrentState();
    }

    public void removePlayer(int playerId) {
        pokerContext.removePlayer(playerId);
    }

    public PokerPlayer getPokerPlayer(int playerId) {
        return pokerContext.getPokerPlayer(playerId);
    }

    public TimingProfile getTimingProfile() {
        return pokerContext.getTimingProfile();
    }

    public int getTableSize() {
        return pokerContext.getTableSize();
    }

    // TODO: Refactor to inheritance.
    public void setTournamentTable(boolean tournamentTable) {
        pokerContext.setTournamentTable(tournamentTable);
    }

    public void setTournamentId(int tournamentId) {
        pokerContext.setTournamentId(tournamentId);
    }

    /**
     * Called by the adapter layer when a player rejoins/reconnects.
     *
     * @param playerId the id of the player to check
     */
    public void playerIsSittingIn(int playerId) {
        log.debug("Player " + playerId + " is sitting in.");
        getCurrentState().playerSitsIn(playerId);
    }

    /*------------------------------------------------

         SERVER ADAPTER METHODS

         These methods propagate to the server adapter.
         The nature of the methods is that they
         demand communication with the player(s).

         // TODO: None of these methods should be public here. Instead, inject the server adapter into classes
                  that need to call the server adapter.

      ------------------------------------------------*/

    public void notifyPotAndRakeUpdates(Collection<PotTransition> potTransitions) {
        serverAdapter.notifyPotUpdates(pokerContext.getPotHolder().getPots(), potTransitions);

        // notify all the new balances
        for (PokerPlayer player : pokerContext.getCurrentHandPlayerMap().values()) {
            serverAdapter.notifyPlayerBalance(player);
        }
        notifyRakeInfo();
    }

    public void notifyRakeInfo() {
        serverAdapter.notifyRakeInfo(pokerContext.getPotHolder().calculateRakeIncludingBetStacks(pokerContext.getCurrentHandSeatingMap().values()));
    }

    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        serverAdapterHolder.set(serverAdapter);
    }

    // TODO: Refactor. The holder of this instance can create a new class which holds this instance together with other data.
    public Object getAdapterState() {
        return adapterState;
    }

    // TODO: Refactor. The holder of this instance can create a new class which holds this instance together with other data.
    public void setAdapterState(Object adapterState) {
        this.adapterState = adapterState;
    }

    public void unseatPlayer(int playerId, boolean setAsWatcher) {
        serverAdapter.unseatPlayer(playerId, setAsWatcher);
    }

    /*------------------------------------------------

         END OF SERVER ADAPTER METHODS

      ------------------------------------------------*/


    public int getTableId() {
        return pokerContext.getTableId();
    }

    public void setTableId(int tableId) {
        pokerContext.setTableId(tableId);
    }

    public String getStateDescription() {
        return getCurrentState().getClass().getName() + "_" + gameType.getStateDescription();
    }

    public BigDecimal getBalance(int playerId) {
        return pokerContext.getBalance(playerId);
    }

    public PotHolder getPotHolder() {
        return pokerContext.getPotHolder();
    }

    public BigDecimal getAnteLevel() {
        return pokerContext.getAnteAmount();
    }

    public BigDecimal getMinBuyIn() {
        return pokerContext.getMinBuyIn();
    }

    public BigDecimal getMaxBuyIn() {
        return pokerContext.getMaxBuyIn();
    }

    public boolean removeAsWatcher(int playerId) {
        return pokerContext.removeAsWatcher(playerId);
    }

    public void addWatcher(int playerId) {
        pokerContext.addWatcher(playerId);
    }

    public PokerSettings getSettings() {
        return pokerContext.getSettings();
    }

    @Override
    public void shutdown() {
        log.debug("Shutting down table {}", getTableId());
        stateChanger.changeState(new ShutdownSTM());
    }

    protected PokerGameSTM getCurrentState() {
        return stateHolder.get();
    }

    public BigDecimal getPlayersTotalContributionToPot(PokerPlayer player) {
        return pokerContext.getPlayersTotalContributionToPot(player);
    }

    @Override
    public void handleBuyInRequest(PokerPlayer pokerPlayer, BigDecimal amount) {
        pokerPlayer.addRequestedBuyInAmount(amount);
        getCurrentState().performPendingBuyIns(singleton(pokerPlayer));
    }

    // TODO: Preferably remove this method, or at least replace with code in state class.
    public boolean isPlaying() {
        return getCurrentState() instanceof PlayingSTM;
    }

    public boolean isShutDown() {
        return getCurrentState() instanceof ShutdownSTM;
    }

    public void notifyBuyinInfo(int playerId, boolean mandatoryBuyin) {
        serverAdapter.notifyBuyInInfo(playerId, mandatoryBuyin);
    }

    // A lot of tournament related stuff below. Investigate how we can best hide this from the "normal" poker code.
    public void playerOpenedSession(int playerId) {
        stateHolder.get().playerOpenedSession(playerId);
    }

    public void setBlindsLevels(BlindsLevel level) {
        log.debug("Setting blinds level: sb = " + level.getSmallBlindAmount() + " bb = " + level.getBigBlindAmount());
        pokerContext.setBlindsLevels(level);
        if (level.isBreak()) {
            log.debug("We are now on a break for " + level.getDurationInMinutes() + " minutes.");
        }
        serverAdapter.notifyBlindsLevelUpdated(level);
    }

    public void notifyWaitingToStartBreak() {
        log.debug("We are waiting for all other tables to finish and the we'll start the break.");
        serverAdapter.notifyWaitingToStartBreak();
    }

    public void notifyWaitingForPlayers() {
        log.debug("We are waiting for all other tables to finish and the we'll start the break.");
        serverAdapter.notifyWaitingForPlayers();
    }

    public void notifyTournamentDestroyed() {
        log.debug("Notifying everyone that tournament is destroyed.");
        serverAdapter.notifyTournamentDestroyed();
    }

    public void sendGameStateTo(int playerId) {
        GameStateSnapshot snapshot = pokerContext.createGameStateSnapshot();
        serverAdapter.sendGameStateTo(snapshot, playerId);
    }

    public void handleAddedChips(int playerId, BigDecimal chipsAdded) {
        log.debug("Player " + playerId + " added " + chipsAdded + " chips.");
        PokerPlayer player = pokerContext.getPlayer(playerId);
        if (player != null) {
            player.addNotInHandAmount(chipsAdded);
            log.debug("Checking if player is in hand.");
            if (!isPlayerInHand(playerId)) {
                log.debug("Not in hand, committing balance.");
                player.commitBalanceNotInHand(new BigDecimal(Long.MAX_VALUE));
            }
            serverAdapter.notifyPlayerBalance(player);
        } else {
            log.error("No player with id " + playerId + " found at this table. Players: " + pokerContext.getPlayerMap().values());
        }
    }

    public void offerRebuys(Collection<Integer> players, String rebuyCost, String rebuyChips) {
        serverAdapter.notifyRebuyOffer(players, rebuyCost, rebuyChips);
    }

    public void notifyAddOnsAvailable(String cost, String chips) {
        serverAdapter.notifyAddOnsAvailable(cost, chips);
    }

    public void handleRebuyResponse(int playerId, boolean answer) {
        serverAdapter.sendRebuyResponseToTournament(playerId, answer, pokerContext.getPlayer(playerId).getStartingBalance());
    }

    public void handleAddOnRequest(int playerId) {
        serverAdapter.sendAddOnRequestToTournament(playerId);
    }

    public void notifyPlayerPerformedRebuy(int playerId) {
        serverAdapter.notifyRebuyPerformed(playerId);
    }

    public void notifyPlayerPerformedAddOn(int playerId) {
        serverAdapter.notifyAddOnPerformed(playerId);
    }

    public void notifyAddOnPeriodClosed() {
        serverAdapter.notifyAddOnPeriodClosed();
    }
}
