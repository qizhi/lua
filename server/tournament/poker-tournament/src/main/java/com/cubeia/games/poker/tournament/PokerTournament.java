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

package com.cubeia.games.poker.tournament;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TournamentId;
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.cashgame.dto.*;
import com.cubeia.backend.cashgame.exceptions.CloseSessionFailedException;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttPlayerStatus;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.tables.Move;
import com.cubeia.firebase.api.mtt.support.tables.TableBalancer;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.guice.tournament.TournamentAssist;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.io.protocol.TournamentOut;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.games.poker.tournament.history.HistoryPersister;
import com.cubeia.games.poker.tournament.messages.*;
import com.cubeia.games.poker.tournament.payouts.ConcretePayout;
import com.cubeia.games.poker.tournament.payouts.PayoutHandler;
import com.cubeia.games.poker.tournament.rebuy.RebuySupport;
import com.cubeia.games.poker.tournament.state.PendingBackendRequests;
import com.cubeia.games.poker.tournament.state.PendingBackendRequests.PendingRequestType;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.cubeia.games.poker.tournament.util.TableNotifier;
import com.cubeia.network.users.firebase.api.UserServiceContract;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static com.cubeia.firebase.api.common.AttributeValue.wrap;
import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.OUT;
import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.PLAYING;
import static com.cubeia.games.poker.tournament.state.PendingBackendRequests.PendingRequestType.ADD_ON;
import static com.cubeia.games.poker.tournament.state.PendingBackendRequests.PendingRequestType.REBUY;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.*;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

/**
 * Implementation of poker tournaments.
 * <p/>
 * Please note a few things:
 * 1. This class is serializable, but we'll keep all data in the PokerTournamentState.
 * 2. The reason this class exists is that previously all of this was done in PokerTournamentProcessor, but
 * since that class has to be thread safe it had no fields, so all parameters (such as MttInstance)
 * were being passed on as arguments between all methods, which was not very elegant or convenient.
 * 3. That being said, this is becoming a God class. Break it down.
 */
public class PokerTournament implements TableNotifier, Serializable {

    private static final Logger log = Logger.getLogger(PokerTournament.class);

    private static final long serialVersionUID = 0L;

    private static final int MILLIS_PER_MINUTE = 60 * 1000;

    private static final String REREGISTRATION = "REREGISTRATION";

    private static final long REBUY_TIMEOUT = 10000;

    private PokerTournamentState pokerState;

    // TODO: Move all transient dependencies to a "TransientDependencies" class?
    private transient SystemTime dateFetcher;

    private transient MTTStateSupport state;

    private transient MttInstance instance;

    private transient TournamentAssist mttSupport;

    private transient HistoryPersister historyPersister;

    private transient CashGamesBackendService backend;
    private transient UserServiceContract userService;

    private transient PacketSender sender;

    private transient ShutdownServiceContract shutdownService;

    private transient TournamentPlayerRegistry tournamentPlayerRegistry;

    private transient RebuySupport rebuySupport = RebuySupport.NO_REBUYS;

	private transient DomainEventsService domainEventService;

    public PokerTournament(PokerTournamentState pokerState) {
        this.pokerState = pokerState;
    }

    /**
     * Invoked when a player has logged out or disconnected. If this is a sit&go, we should
     * un-register the player, it the tournament hasn't started already.
     *
     * @param playerLeft holds information about the player who left, not null
     */
    public void handlePlayerLeft(PlayerLeft playerLeft) {
        if (pokerState.isSitAndGo() && pokerState.getStatus() == REGISTERING) {
            int playerId = playerLeft.getPlayerId();
            unregisterPlayer(playerId);
            tournamentPlayerRegistry.unregister(playerId, instance.getId());
            state.getPlayerRegistry().removePlayer(playerId);
        }
    }

    public void handleRebuyTimeout(int tableId) {
        if (rebuySupport.tableIsWaitingForRebuys(tableId)) {
            log.debug("Handling rebuy timeout on table " + tableId);
            Set<Integer> playersOut = newHashSet();
            for (Integer playerId : rebuySupport.getRebuyRequestsForTable(tableId)) {
                BigDecimal playerBalance = pokerState.getPlayerBalance(playerId);
                if (playerBalance.compareTo(BigDecimal.ZERO) == 0) {
                    playersOut.add(playerId);
                }
            }
            rebuySupport.removeRebuyRequestsForTable(tableId);
            if (!playersOut.isEmpty()) {
                payAndRemovePlayers(tableId, playersOut);
            }
            handleEndOfHand(tableId);
        } else {
            log.debug("Table " + tableId + " was not waiting for rebuys (they could have all acted), ignoring.");
        }
    }

    public void handleRebuyResponse(RebuyResponse rebuyResponse) {
        int tableId = rebuyResponse.getTableId();
        int playerId = rebuyResponse.getPlayerId();
        Set<Integer> rebuyRequests = rebuySupport.getRebuyRequestsForTable(tableId);
        if (rebuyRequests.contains(playerId)) {
            if (rebuyResponse.getAnswer()) {
                // Player wanted to do a rebuy.
                performRebuy(playerId, tableId);
                rebuyRequests.remove(playerId);
                // Note, we are not checking if the hand is finished, because we need to complete the rebuy (which is asynchronous) first.
            } else {
                // Player declined rebuy, he's out!
                rebuyRequests.remove(playerId);
                historyPersister.rebuyDeclined(playerId);
                payAndRemovePlayers(tableId, singleton(playerId));
                // If we have no more pending requests, consider this hand as finished and move on.
                if (!tableHasPendingRequests(tableId)) {
                    handleEndOfHand(tableId);
                }
            }
        } else {
            log.debug("We have not requested any rebuy from player " + playerId + " Checking if he is eligible to perform a rebuy.");
            if (rebuySupport.isPlayerAllowedToRebuy(playerId, rebuyResponse.getChipsAtHandFinish())) {
                performRebuy(playerId, tableId);
            }
        }
    }

    public void handleAddOnRequest(AddOnRequest request) {
        int tableId = request.getTableId();
        int playerId = request.getPlayerId();
        log.debug("Handling add-on on table " + tableId + " from player " + playerId);
        if (rebuySupport.isPlayerAllowedToPerformAddOn(playerId)) {
            performAddOn(playerId, tableId);
        } else {
            log.debug("Player " + playerId + " on table " + tableId + " tried to perform an add-on but was not allowed to do that.");
        }
    }

    public void handleReservationResponse(ReserveResponse response) {
        // Money has now been reserved from the operator, now we need to transfer it to the tournament.
        PendingBackendRequests pendingRequests = pokerState.getPendingRequests();
        Money amountReserved = response.getAmountReserved();
        PlayerSessionId playerSessionId = response.getPlayerSessionId();
        int playerId = playerSessionId.playerId;
        log.debug("Player " + playerId + " reserved " + amountReserved + ". Moving it to the tournament session.");
        TournamentSessionId toAccount = pokerState.getTournamentSession();
        int tableId = pokerState.getTableFor(playerId, state);
        PendingRequestType pendingRequest = pendingRequests.getAndClearPendingRequest(playerId, tableId);
        if (pendingRequest == REBUY) {
            handleSuccessfulRebuy(amountReserved, playerSessionId, playerId, toAccount, tableId);
        } else if (pendingRequest == ADD_ON) {
            handleSuccessfulAddOn(amountReserved, playerSessionId, playerId, toAccount, tableId);
        } else {
            log.error("Player " + playerId + " reserved money but had no pending request.");
        }
    }

    private void handleSuccessfulRebuy(Money amountReserved, PlayerSessionId playerSessionId, int playerId, TournamentSessionId toAccount, int tableId) {
        TransferMoneyRequest request = new TransferMoneyRequest(amountReserved, playerSessionId, toAccount,
                "Rebuy for tournament " + pokerState.getHistoricId());
        backend.transfer(request);
        addChipsTo(playerId, tableId, rebuySupport.getRebuyChipsAmount(), PlayerAddedChips.Reason.REBUY);
        addMoneyToPrizePool(amountReserved);
        rebuySupport.increaseRebuyCount(playerId);
        startNextHandIfThisWasLastRebuyQuestionAtTable(tableId);
        historyPersister.rebuyPerformed(playerId);
    }

    private void handleSuccessfulAddOn(Money amountReserved, PlayerSessionId playerSessionId, int playerId, TournamentSessionId toAccount, int tableId) {
        TransferMoneyRequest request = new TransferMoneyRequest(amountReserved, playerSessionId, toAccount,
                "Add-on for tournament " + pokerState.getHistoricId());
        backend.transfer(request);
        rebuySupport.addOnPerformed(playerId);
        addChipsTo(playerId, tableId, rebuySupport.getAddOnChipsAmount(), PlayerAddedChips.Reason.ADD_ON);
        addMoneyToPrizePool(amountReserved);
        historyPersister.addOnPerformed(playerId);
    }

    private void addMoneyToPrizePool(Money money) {
        pokerState.addMoneyToPrizePool(money.getAmount());
        updatePayouts();
    }

    public void handleReservationFailed(ReserveFailedResponse response) {
        int playerId = response.getSessionId().playerId;
        int tableId = pokerState.getTableFor(playerId, state);
        PendingBackendRequests pendingRequests = pokerState.getPendingRequests();
        PendingRequestType request = pendingRequests.getAndClearPendingRequest(playerId, tableId);
        if (request == REBUY) {
            historyPersister.rebuyFailed(playerId);
        } else if (request == ADD_ON) {
            historyPersister.addOnFailed(playerId);
        }
        startNextHandIfThisWasLastRebuyQuestionAtTable(tableId);
        // TODO: Tell the player something went wrong.
    }

    private void startNextHandIfThisWasLastRebuyQuestionAtTable(int tableId) {
        /*
         * Here, we check that there are no outstanding backend requests, there are no unanswered
         * rebuy questions and we are in fact waiting for rebuys to finish until starting the next hand.
         *
         * This should cover the case where someone who wasn't asked to perform a rebuy does one spontaneously,
         * (for example) just before the questions go out.
         */
        if (!tableHasPendingRequests(tableId) && rebuySupport.tableIsWaitingForRebuys(tableId)) {
            log.debug("Table " + tableId + " has no pending requests and was waiting for rebuys, starting next hand.");
            handleEndOfHand(tableId);
        }
    }

    /**
     * Adds chips to the given player, buy sending a message to the table where he sits.
     *
     * @param chipsToAdd Chips to add, in number of chips  For 2000 chips, pass 2000 in.
     */
    private void addChipsTo(int playerId, int tableId, BigDecimal chipsToAdd, PlayerAddedChips.Reason reason) {
        log.debug("Adding " + chipsToAdd + " chips (" + chipsToAdd + ") to " + playerId + " who sits at table " + tableId);
        notifyTable(tableId, new PlayerAddedChips(playerId, chipsToAdd, reason));
    }

    private void performRebuy(int playerId, int tableId) {
        performAddChips(playerId, tableId, rebuySupport.getRebuyCost(), REBUY);
    }

    private void performAddOn(int playerId, int tableId) {
        performAddChips(playerId, tableId, rebuySupport.getAddOnCost(), ADD_ON);
    }

    private void performAddChips(int playerId, int tableId, BigDecimal cost, PendingRequestType type) {
        PendingBackendRequests pendingRequests = pokerState.getPendingRequests();
        if (!pendingRequests.playerHasPendingRequests(playerId)) {
            pendingRequests.addPendingRequest(playerId, tableId, type);
            ReserveRequest request = new ReserveRequest(pokerState.getPlayerSession(playerId), pokerState.convertToMoney(cost));
            backend.reserveMoneyForTournament(request, new TournamentId(pokerState.getTournamentSession().integrationSessionId, instance.getId()));
        } else {
            log.warn("Player " + playerId + " tried to perform " + type + " when there was already a pending request. Ignoring.");
        }
    }

    public void injectTransientDependencies(MttInstance instance, TournamentAssist support, MTTStateSupport state,
            TournamentHistoryPersistenceService historyService, CashGamesBackendService backend, SystemTime dateFetcher,
            ShutdownServiceContract shutdownService, TournamentPlayerRegistry tournamentPlayerRegistry, PacketSender sender,
            UserServiceContract userService, DomainEventsService domainEventService) {
        this.instance = instance;
        this.mttSupport = support;
        this.state = state;
        this.historyPersister = new HistoryPersister(pokerState.getHistoricId(), historyService, dateFetcher);
        this.backend = backend;
        this.dateFetcher = dateFetcher;
        this.shutdownService = shutdownService;
        this.tournamentPlayerRegistry = tournamentPlayerRegistry;
        this.sender = sender;
        this.rebuySupport = pokerState.getRebuySupport();
        this.userService = userService;
        this.domainEventService = domainEventService;
        rebuySupport.injectTransientDependencies(this, historyPersister);
    }

    public void processRoundReport(MttRoundReportAction action) {
        int tableId = action.getTableId();
        if (log.isDebugEnabled()) {
            log.debug("Process round report from table[" + tableId + "] Report: " + action);
        }

        PokerTournamentRoundReport report = (PokerTournamentRoundReport) action.getAttachment();

        updateBalances(report);
        increaseBlindsIfNeeded(report.getCurrentBlindsLevel(), tableId);
        Set<Integer> playersOut = getPlayersOut(report);
        log.info("Players out of tournament[" + instance.getId() + "] : " + playersOut);
        boolean rebuysRequested = handlePlayersOut(tableId, playersOut);
        if (rebuysRequested) {
            log.debug("Rebuys requested, won't start next hand yet.");
            scheduleRebuyTimeout(tableId);
        } else {
            log.debug("No rebuys requested, starting next hand.");
            handleEndOfHand(tableId);
        }
    }

    private void handleEndOfHand(int tableId) {
        boolean tableClosed = balanceTables(tableId);

        if (isTournamentFinished()) {
            handleFinishedTournament();
        } else {
            if (!tableClosed) {
                startNextRoundIfPossible(tableId);
            } else {
                // Table was closed, make sure we don't react to any rebuy timeouts on that table.
                rebuySupport.removeTableWaitingForRebuys(tableId);
            }
            startBreakIfReady(tableId);
        }

        if (log.isDebugEnabled()) {
            log.debug("Remaining players: " + state.getRemainingPlayerCount() + " Remaining tables: " + state.getTables());
        }
        pokerState.invalidatePlayerList();
        pokerState.invalidateTournamentStatistics();
        updatePlayersLeft();
    }

    private void scheduleRebuyTimeout(int tableId) {
        instance.getScheduler().scheduleAction(new MttObjectAction(instance.getId(), new RebuyTimeout(tableId)), REBUY_TIMEOUT);
    }

    private void startBreakIfReady(int tableId) {
        log.debug("Checking if we should start break. Current level is break? " + pokerState.isOnBreak() + " On break already? " + pokerState.getStatus());
        if (pokerState.isOnBreak()) {
            if (pokerState.getStatus() == RUNNING) {
                log.debug("Preparing break.");
                setTournamentStatus(PREPARING_BREAK);
                pokerState.prepareBreak(state.getTables());
                pokerState.addTablesReadyForBreak(tablesWithLonelyPlayer());
                pokerState.addTableReadyForBreak(tableId);
                if (!allTablesAreReadyForBreak()) {
                    notifyWaitingForOtherTablesToFinishBeforeBreak(tableId);
                }
            }

            if (pokerState.getStatus() == PREPARING_BREAK) {
                log.debug("Adding table " + tableId + " to tables ready for break.");
                pokerState.addTableReadyForBreak(tableId);
            }

            if (pokerState.getStatus() != ON_BREAK && allTablesAreReadyForBreak()) {
                // start break
                startBreak();
            } else {
                log.debug("Not starting break, still waiting for tables to finish.");
            }
        }
    }

    private void startBreak() {
        log.debug("Starting break.");
        setTournamentStatus(ON_BREAK);
        scheduleNextBlindsLevel();
        notifyAllTablesThatBreakStarted();

        rebuySupport.notifyNewLevelStarted(pokerState.getCurrentBlindsLevelNr(), true, this);
    }

    private void notifyAllTablesThatBreakStarted() {
        notifyAllTablesOfNewBlinds();
    }

    private void notifyAllTablesOfNewBlinds() {
        for (Integer tableId : state.getTables()) {
            notifyTable(tableId, createBlindsWithDeadline());
        }
    }

    private BlindsWithDeadline createBlindsWithDeadline() {
        log.debug("Creating blinds with deadline: " + pokerState.getNextLevelStartTime());
        Level level = pokerState.getCurrentBlindsLevel();
        return new BlindsWithDeadline(level.getSmallBlindAmount(), level.getBigBlindAmount(), level.getAnteAmount(), level.getDurationInMinutes(),
                level.isBreak(), pokerState.getNextLevelStartTime().getMillis());
    }

    private void notifyWaitingForOtherTablesToFinishBeforeBreak(int tableId) {
        notifyTable(tableId, new WaitingForTablesToFinishBeforeBreak());
    }

    private boolean allTablesAreReadyForBreak() {
        return pokerState.allTablesReadyForBreak();
    }

    private Set<Integer> tablesWithLonelyPlayer() {
        Set<Integer> tables = newHashSet();
        for (int tableId : state.getTables()) {
            if (state.getPlayersAtTable(tableId).size() == 1) {
                tables.add(tableId);
            }
        }
        return tables;
    }

    private void increaseBlindsIfNeeded(PokerTournamentRoundReport.Level currentBlindsLevel, int tableId) {
        if (currentBlindsLevel.getBigBlindAmount().compareTo(pokerState.getSmallBlindAmount()) < 0 && !pokerState.getCurrentBlindsLevel().isBreak()) {
            notifyTable(tableId, createBlindsWithDeadline());
        }
    }

    public void handleTablesCreated(MttTablesCreatedAction action) {
        for (int tableId : action.getTables()) {
            historyPersister.addTable(getExternalTableId(tableId));
        }
        if (pokerState.allTablesHaveBeenCreated(state.getTables().size())) {
            mttSupport.seatPlayers(state, createInitialSeating());
            scheduleSendStartToTables();
        }
    }

    private String getExternalTableId(int tableId) {
        return instance.getTableLobbyAccessor(tableId).getStringAttribute(PokerLobbyAttributes.TABLE_EXTERNAL_ID.name());
    }

    private void updateBalances(PokerTournamentRoundReport report) {
        for (Map.Entry<Integer, BigDecimal> balance : report.getBalances()) {
            pokerState.setBalance(balance.getKey(), balance.getValue());
        }
    }

    private Set<Integer> getPlayersOut(PokerTournamentRoundReport report) {
        Set<Integer> playersOut = new HashSet<Integer>();

        for (Map.Entry<Integer, BigDecimal> balance : report.getBalances()) {
            if (balance.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                playersOut.add(balance.getKey());
            }
        }

        log.debug("These players have 0 balance and are out: " + playersOut);
        return playersOut;
    }

    /**
     * Handles players out by removing them from the tournament or requesting rebuys is they have that option.
     *
     * @return true if rebuys were requested
     */
    private boolean handlePlayersOut(int tableId, Set<Integer> playersOut) {
        if (!playersOut.isEmpty()) {
            Set<Integer> playersWithRebuyOption = pokerState.getRebuySupport().requestRebuys(tableId, playersOut);
            log.debug("Players with rebuy options " + playersWithRebuyOption);
            playersOut.removeAll(playersWithRebuyOption);
            payAndRemovePlayers(tableId, playersOut);
            boolean empty = playersWithRebuyOption.isEmpty();
            log.debug("Players with rebuy options empty? " + empty + " " + playersWithRebuyOption);
            return !empty;
        }
        return false;
    }

    private void payAndRemovePlayers(int tableId, Set<Integer> playersOut) {
        if (playersOut.isEmpty()) {
            return;
        }
        handlePayouts(playersOut);
        unseatPlayers(tableId, playersOut);
        updatePlayersLeft();
        pokerState.invalidatePlayerToTableMap();
    }

    private void updatePlayersLeft() {
        instance.getLobbyAccessor().setIntAttribute(PokerTournamentLobbyAttributes.PLAYERS_LEFT.name(), state.getRemainingPlayerCount());
    }

    private void unseatPlayers(int tableId, Set<Integer> playersOut) {
        mttSupport.unseatPlayers(state, tableId, playersOut, UnseatPlayersMttAction.Reason.OUT);
    }

    private void handlePayouts(Set<Integer> playersOut) {
        PayoutHandler payoutHandler = new PayoutHandler(pokerState.getPayouts());
        List<ConcretePayout> payouts = payoutHandler.calculatePayouts(playersOut, balancesAtStartOfHand(playersOut), state.getRemainingPlayerCount());
        for (ConcretePayout payout : payouts) {
            // Transfer the given amount of money from the tournament account to the player account.
            transferMoneyAndCloseSession(pokerState.getPlayerSession(payout.getPlayerId()), payout.getPayout());
            setPlayerOutInPosition(payout.getPlayerId(), payout.getPosition());
            MttPlayer tournamentPlayer = pokerState.getTournamentPlayer(payout.getPlayerId(), state);
            domainEventService.sendTournamentPayoutEvent(tournamentPlayer, payout.getPayout(), pokerState.getCurrency().getCode(), payout.getPosition(), instance);
        }
        sendTournamentOutToPlayers(payouts);
    }

    private void transferMoneyAndCloseSession(PlayerSessionId playerSession, BigDecimal payout) {
        if (payout.compareTo(BigDecimal.ZERO) > 0) {
            notifyInTheMoney();
            backend.transfer(createPayoutRequest(payout, playerSession));
            pokerState.setPayout(playerSession.playerId, payout);
        }
        backend.closeTournamentSession(new CloseSessionRequest(playerSession), createTournamentId());
    }

    private void notifyInTheMoney() {
        if (!pokerState.inTheMoney()) {
            pokerState.setInTheMoney(true);
            transferGuaranteedMoney();
            rebuySupport.notifyInTheMoney();
        }
    }

    private void transferGuaranteedMoney() {
        Money guaranteedPrizePoolUsed = pokerState.getGuaranteedPrizePoolUsedAsMoney();
        log.debug("Transferring guaranteed money " + guaranteedPrizePoolUsed + " from promo account to tournament.");
        TournamentSessionId toAccount = pokerState.getTournamentSession();
        backend.transferMoneyFromPromotionsAccount(toAccount, guaranteedPrizePoolUsed,
                "Adding money to reach guaranteed prize pool" + pokerState.getHistoricId());
    }

    private TransferMoneyRequest createPayoutRequest(BigDecimal amount, PlayerSessionId playerSession) {
        PlayerSessionId fromSession = pokerState.getTournamentSession();
        String comment = "Tournament payout";
        Money money = pokerState.convertToMoney(amount);

        return new TransferMoneyRequest(money, fromSession, playerSession, comment);
    }

    private Map<Integer, BigDecimal> balancesAtStartOfHand(Set<Integer> playersOut) {
        Map<Integer, BigDecimal> balances = newHashMap();
        for (int playerId : playersOut) {
            balances.put(playerId, pokerState.getPlayerBalance(playerId));
        }
        return balances;
    }

    private BigDecimal getStartingChips() {
        return pokerState.getStartingChips();
    }

    private void sendTournamentOutToPlayers(List<ConcretePayout> playersOut) {
        for (ConcretePayout payout : playersOut) {
            TournamentOut packet = new TournamentOut();
            packet.position = payout.getPosition();
            int playerId = payout.getPlayerId();
            packet.player = playerId;
            log.debug("Telling player " + playerId + " that he finished in position " + packet.position);
            sender.sendPacketToPlayer(packet, playerId);
            historyPersister.playerOut(packet.player, packet.position, payout.getPayout());
        }
    }

    private void handleFinishedTournament() {
        log.info("Tournament [" + instance.getId() + ":" + instance.getState().getName() + "] was finished.");
        rebuySupport.tournamentFinished();
        historyPersister.tournamentFinished();
        setTournamentStatus(PokerTournamentStatus.FINISHED);

        // Find and pay winner
        MTTStateSupport state = (MTTStateSupport) instance.getState();
        Integer table = state.getTables().iterator().next();
        Collection<Integer> winners = state.getPlayersAtTable(table);
        Integer winner = winners.iterator().next();
        payWinner(winner);
        unseatPlayers(table, singleton(winner));

        closeMainTournamentSession();
        scheduleTournamentClosing();
    }

    private void scheduleTournamentClosing() {
        log.trace("Scheduling tournament to be closed in " + pokerState.getMinutesVisibleAfterFinished() + " minutes.");
        MttAction action = new MttObjectAction(instance.getId(), new CloseTournament());
        instance.getScheduler().scheduleAction(action, pokerState.getMinutesVisibleAfterFinished() * MILLIS_PER_MINUTE);
    }

    private void closeMainTournamentSession() {
        log.trace("Closing tournament session " + pokerState.getTournamentSession() + " for tournament " + pokerState.getHistoricId());
        backend.closeTournamentSession(new CloseSessionRequest(pokerState.getTournamentSession()), createTournamentId());
    }

    private void payWinner(Integer playerId) {
        log.debug("Paying winner: " + playerId);
        BigDecimal payout = pokerState.getPayouts().getPayoutsForPosition(1);
        setPlayerOutInPosition(playerId, 1);
        sendTournamentOutToPlayers(singletonList(new ConcretePayout(playerId, 1, payout)));

        PlayerSessionId playerSession = pokerState.getPlayerSession(playerId);
        transferMoneyAndCloseSession(playerSession, payout);
        
        MttPlayer tournamentPlayer = pokerState.getTournamentPlayer(playerId, state);
        
        domainEventService.sendTournamentPayoutEvent(tournamentPlayer, payout, pokerState.getCurrency().getCode(), 1, instance);
    }

    private void setPlayerOutInPosition(int playerId, int position) {
        MttPlayer player = pokerState.getTournamentPlayer(playerId, state);
        player.setStatus(OUT);
        player.setPosition(position);
    }

    private boolean isTournamentFinished() {
        return state.getRemainingPlayerCount() == 1;
    }

    private void startNextRoundIfPossible(int tableId) {
        if (!pokerState.isOnBreak()) {
            if (tableHasPendingRequests(tableId)) {
              log.debug("Won't start new hand at table since it has pending requests.");
            } else {
                if (state.getPlayersAtTable(tableId).size() > 1) {
                    rebuySupport.removeTableWaitingForRebuys(tableId);
                    mttSupport.sendRoundStartActionToTables(state, singleton(tableId));
                } else {
                    // Notify table that we are waiting for more players before we can start the next hand.
                    notifyTable(tableId, new WaitingForPlayers());
                }
            }
        }
    }

    private boolean tableHasPendingRequests(int tableId) {
        if (rebuySupport.tableHasPendingRequests(tableId)) {
            log.debug("There are outstanding rebuy requests at table: " + tableId);
            return true;
        }
        if (pokerState.getPendingRequests().tableHasPendingRequests(tableId)) {
            log.debug("There are outstanding backend requests at table: " + tableId);
            return true;
        }
        return false;
    }

    /**
     * Tries to balance the tables by moving one or more players from this table to other
     * tables.
     *
     * @return <code>true</code> if the table was closed
     */
    private boolean balanceTables(int tableId) {
        TableBalancer balancer = new TableBalancer();
        List<Move> moves = balancer.calculateBalancing(createTableToPlayerMap(), state.getSeats(), tableId);
        return applyBalancing(moves, tableId);
    }

    /**
     * Applies balancing by moving players to the destination table.
     *
     * @param sourceTableId the table we are moving player from
     * @return true if table is closed
     */
    private boolean applyBalancing(List<Move> moves, int sourceTableId) {
        if (moves.isEmpty()) {
            return false; // Nothing to do
        }
        Set<Integer> tablesToStart = new HashSet<Integer>();

        for (Move move : moves) {
            int tableId = move.getDestinationTableId();
            int playerId = move.getPlayerId();

            Collection<Integer> playersAtDestinationTableBeforeMoving = state.getPlayersAtTable(tableId);
            // Move the player, we don't care which seat he gets put at, so set it to -1.
            log.debug("Moving player " + playerId + " from table " + sourceTableId + " to table " + tableId);
            mttSupport.movePlayer(state, playerId, tableId, -1, UnseatPlayersMttAction.Reason.BALANCING, pokerState.getPlayerBalance(playerId));
            if (playersAtDestinationTableBeforeMoving.size() == 1) {
                // There was only one player at the table before we moved this player there, start a new round.
                tablesToStart.add(tableId);
            }
            historyPersister.playerMoved(playerId, tableId);
        }

        if (!tablesToStart.isEmpty() && !pokerState.isOnBreak()) {
            log.debug("Sending explicit start to tables[" + Arrays.toString(tablesToStart.toArray()) + "] due to low number of players.");
            mttSupport.sendRoundStartActionToTables(state, tablesToStart);
        }

        pokerState.invalidatePlayerToTableMap(); // The table to player map is not valid anymore.
        return closeTableIfEmpty(sourceTableId);
    }

    private boolean closeTableIfEmpty(int tableId) {
        if (state.getPlayersAtTable(tableId).isEmpty()) {
            mttSupport.closeTables(state, singleton(tableId));
            return true;
        }

        return false;
    }

    /**
     * Creates a map mapping tableId to a collection of playerIds of the players
     * sitting at the table.
     *
     * @return the map
     */
    private Map<Integer, Collection<Integer>> createTableToPlayerMap() {
        Map<Integer, Collection<Integer>> map = new HashMap<Integer, Collection<Integer>>();

        for (Integer tableId : state.getTables()) {
            List<Integer> players = new ArrayList<Integer>();
            players.addAll(state.getPlayersAtTable(tableId));

            if (players.size() > 0) {
                map.put(tableId, players);
            }
        }
        return map;
    }

    private void scheduleTournamentStart() {
        if (pokerState.shouldScheduleTournamentStart(dateFetcher.date())) {
            MttObjectAction action = new MttObjectAction(instance.getId(), TournamentTrigger.START_TOURNAMENT);
            long timeToTournamentStart = pokerState.getTimeUntilTournamentStart(dateFetcher.date());
            log.debug("Scheduling tournament start in " + Duration.millis(timeToTournamentStart).getStandardMinutes() + " minutes, for tournament " + instance);
            instance.getScheduler().scheduleAction(action, timeToTournamentStart);
        } else {
            log.debug("Won't schedule tournament start because the life cycle says no.");
        }
    }

    private void scheduleRegistrationOpening() {
        MttObjectAction action = new MttObjectAction(instance.getId(), TournamentTrigger.OPEN_REGISTRATION);
        long timeToRegistrationStart = pokerState.getTimeUntilRegistrationStart(dateFetcher.date());
        log.debug("Scheduling registration opening in " + timeToRegistrationStart + " millis, for tournament " + instance);
        instance.getScheduler().scheduleAction(action, timeToRegistrationStart);
    }

    private void scheduleSendStartToTables() {
        MttObjectAction action = new MttObjectAction(instance.getId(), TournamentTrigger.SEND_START_TO_TABLES);
        log.debug("Scheduling round start in " + 1000 + " millis, for tournament " + instance);
        instance.getScheduler().scheduleAction(action, 1000);
    }

    private void scheduleNextBlindsLevel() {
        Duration levelDuration = Duration.standardMinutes(pokerState.getCurrentBlindsLevel().getDurationInMinutes());
        long millisecondsToNextLevel = levelDuration.getMillis();
        pokerState.setNextLevelStartTime(dateFetcher.date().plus(levelDuration));
        log.debug("Scheduling next blinds level in " + millisecondsToNextLevel + " millis, for tournament " + instance);
        instance.getScheduler().scheduleAction(new MttObjectAction(instance.getId(), TournamentTrigger.INCREASE_LEVEL), millisecondsToNextLevel);
    }

    private Collection<SeatingContainer> createInitialSeating() {
        Collection<MttPlayer> players = state.getPlayerRegistry().getPlayers();
        Set<Integer> tableIds = state.getTables();
        List<SeatingContainer> initialSeating = new ArrayList<SeatingContainer>();
        Integer[] tableIdArray = new Integer[tableIds.size()];
        tableIds.toArray(tableIdArray);

        int i = 0;
        for (MttPlayer player : players) {
            pokerState.setBalance(player.getPlayerId(), getStartingChips());
            initialSeating.add(createSeating(player.getPlayerId(), tableIdArray[i++ % tableIdArray.length]));
        }

        return initialSeating;
    }

    private SeatingContainer createSeating(int playerId, int tableId) {
        return new SeatingContainer(playerId, tableId, getStartingChips());
    }

    public PokerTournamentState getPokerTournamentState() {
        return pokerState;
    }

    private void setTournamentStatus(PokerTournamentStatus status) {
        if (status == pokerState.getStatus()) {
            log.debug("Status is already " + status + ". Ignoring.");
            return;
        }
        historyPersister.statusChanged(status.name());
        instance.getLobbyAccessor().setStringAttribute(PokerTournamentLobbyAttributes.STATUS.name(), status.name());
        pokerState.setStatus(status);
    }

    private void addJoinedTimestamps() {
        if (state.getRegisteredPlayersCount() == 1) {
            pokerState.setFirstRegisteredTime(System.currentTimeMillis());

        } else if (state.getRegisteredPlayersCount() == state.getMinPlayers()) {
            pokerState.setLastRegisteredTime(System.currentTimeMillis());
        }
    }

    private void startTournament() {
        if (shutdownService.isSystemShuttingDown()) {
            log.info("Won't start tournament, since system is shutting down.");
            cancelTournament();
            return;
        }

        long registrationElapsedTime = pokerState.getLastRegisteredTime() - pokerState.getFirstRegisteredTime();
        log.debug("Starting tournament [" + instance.getId() + " : " + instance.getState()
                .getName() + "]. Registration time was " + registrationElapsedTime + " ms");

        setTournamentStatus(RUNNING);
        updatePlayerStatuses(PLAYING);
        pokerState.setStartTime(dateFetcher.date());
        updatePayouts();
        createTables();
        historyPersister.tournamentStarted(state.getName());
        pokerState.invalidatePlayerToTableMap();
    }

    private void updatePlayerStatuses(MttPlayerStatus status) {
        for (MttPlayer player : state.getPlayerRegistry().getPlayers()) {
            player.setStatus(status);
        }
    }

    private void updatePayouts() {
        // The tournament will not start unless we have min player registered, so payouts can assume minPlayers participants.
        pokerState.setPayouts(state.getMinPlayers(), state.getRegisteredPlayersCount());
    }

    private void createTables() {
        int tablesToCreate = numberOfTablesToCreate();
        pokerState.setTablesToCreate(tablesToCreate);
        TournamentTableSettings settings = getTableSettings();
        log.debug("Creating tables for tournament [" + instance + "] . State.getID: " + state.getId());
        mttSupport.createTables(state, tablesToCreate, "mtt", settings);
    }

    int numberOfTablesToCreate() {
        return (int) Math.ceil(state.getRegisteredPlayersCount() / (double) state.getSeats());
    }

    private boolean tournamentShouldStart() {
        return pokerState.shouldTournamentStart(dateFetcher.date(), state.getRegisteredPlayersCount(), state.getMinPlayers());
    }

    private boolean tournamentShouldBeCancelled() {
        boolean resurrectingSitAndGo = pokerState.isResurrectingTournament() && pokerState.isSitAndGo();
        return resurrectingSitAndGo || pokerState.shouldCancelTournament(dateFetcher.date(), state.getRegisteredPlayersCount(), state.getMinPlayers());
    }

    private TournamentTableSettings getTableSettings() {
        return new TournamentTableSettings(pokerState.getTiming(), pokerState.getBetStrategy());
    }

    public void playerRegistered(MttRegistrationRequest request) {
        addJoinedTimestamps();
        if (isReRegistration(request)) {
            historyPersister.playerReRegistered(request.getPlayer().getPlayerId());
        }
        pokerState.invalidatePlayerMap();
    }

    private HistoricPlayer historicPlayer(MttPlayer player, PlayerSessionId sessionId) {
        return new HistoricPlayer(player.getPlayerId(), player.getScreenname(), sessionId.integrationSessionId);
    }

    public void playerUnregistered(int playerId) {
        unregisterPlayer(playerId);
        updatePayouts();
    }

    private void unregisterPlayer(int playerId) {
        try {
            log.debug("Player " + playerId + " unregistered. Closing session.");
            // Transfer money back from tournament session to player session.
            PlayerSessionId playerSession = pokerState.getPlayerSession(playerId);
            resetPlayerSession(playerSession);
            pokerState.removePlayerSession(playerId);
            historyPersister.playerUnregistered(playerId);
            pokerState.invalidatePlayerMap();
            pokerState.removeBuyInFromPrizePool();
        } catch (CloseSessionFailedException e) {
            log.error("Failed closing session for player " + playerId, e);
            historyPersister.playerFailedUnregistering(playerId, e.getMessage());
        }
    }

    /*
     * Transfer buy-in and fee and close session.
     */
	private void resetPlayerSession(PlayerSessionId playerSession) throws CloseSessionFailedException {
		transferMoneyFromTournamentSessionToPlayerSession(playerSession);
		transferFeeFromRakeAccount(playerSession);
		backend.closeSession(new CloseSessionRequest(playerSession));
	}

    public MttRegisterResponse checkRegistration(MttRegistrationRequest request) {
        log.info("Checking if " + request + " is allowed to register.");
        
        if (instance.getState().getCapacity() <= instance.getState().getRegisteredPlayersCount()) {
        	return MttRegisterResponse.DENIED; 
        }
        
        if (isReRegistration(request)) {
            return MttRegisterResponse.ALLOWED;
        }
        
        if (isPrivateAndDisallowed(request)) {
        	return MttRegisterResponse.DENIED;
        }

        if (pokerState.getStatus() != REGISTERING) {
            return MttRegisterResponse.DENIED;
        } else {
    		backend.openTournamentPlayerSession(createOpenTournamentPlayerSessionRequest(request), pokerState.getTournamentSession());
    		pokerState.addPendingRegistration(request.getPlayer().getPlayerId());
    		return MttRegisterResponse.ALLOWED;
        }
    }

    /**
     * Check if this is a private tournament for one or more operators, and 
     * return true if we should block this player.
     */
    private boolean isPrivateAndDisallowed(MttRegistrationRequest request) {
        if(pokerState.isPrivate()) {
            MttPlayer player = request.getPlayer();
            // TODO Remove this remote call?
            User user = userService.getUserById(player.getPlayerId());
            return !pokerState.isOperatorAllowed(user.getOperatorId());
        } else {
            return false;
        }
    }

    private boolean isReRegistration(MttRegistrationRequest request) {
        if (request.getParameters() == null) {
            return false;
        }
        for (Attribute parameter : request.getParameters()) {
            if (REREGISTRATION.equals(parameter.name)) {
                return true;
            }
        }
        return false;
    }

    private OpenTournamentSessionRequest createOpenTournamentPlayerSessionRequest(MttRegistrationRequest request) {
        TournamentId tournamentId = createTournamentId();
        Money money = pokerState.getBuyInPlusFeeAsMoney();
        log.debug("Created money for buy-in: " + money);
        return new OpenTournamentSessionRequest(request.getPlayer().getPlayerId(), tournamentId, money);
    }

    private TournamentId createTournamentId() {
        return new TournamentId(pokerState.getHistoricId(), instance.getId());
    }

    public MttRegisterResponse checkUnregistration(int pid) {
        if (pokerState.getStatus() == REGISTERING && state.getPlayerRegistry().isRegistered(pid)) {
            return MttRegisterResponse.ALLOWED;
        } else {
            return MttRegisterResponse.DENIED;
        }
    }

    public void tournamentCreated() {
        log.trace("Tournament created. Historic id: " + pokerState.getHistoricId());
        log.trace("Resurrecting players: " + pokerState.getResurrectingPlayers());
        if (pokerState.isResurrectingTournament()) {
            reRegisterPlayers(pokerState.getResurrectingPlayers());
            pokerState.invalidatePlayerMap();
        } else {
            backend.openTournamentSession(createOpenTournamentSessionRequest());
        }
        if (tournamentShouldBeCancelled()) {
            cancelTournament();
        } else if (pokerState.shouldOpenRegistration(dateFetcher.date())) {
            openRegistration();
            scheduleTournamentStart();
        } else if (pokerState.shouldScheduleRegistrationOpening(dateFetcher.date())) {
            scheduleRegistrationOpening();
        }
    }

    private void reRegisterPlayers(Set<HistoricPlayer> resurrectingPlayers) {
        for (HistoricPlayer resurrectingPlayer : resurrectingPlayers) {
            reRegisterPlayer(resurrectingPlayer);
        }
        updatePayouts();
        pokerState.getResurrectingPlayers().clear();
        pokerState.addBuyInToPrizePool();
    }

    private void reRegisterPlayer(HistoricPlayer resurrectingPlayer) {
        MttPlayer player = new MttPlayer(resurrectingPlayer.getId(), resurrectingPlayer.getName());
        List<Attribute> parameters = singletonList(new Attribute(REREGISTRATION, wrap("true")));
        MttRegistrationRequest request = new MttRegistrationRequest(player, parameters);
        pokerState.addPlayerSession(new PlayerSessionId(resurrectingPlayer.getId(), resurrectingPlayer.getSessionId()));
        state.getPlayerRegistry().register(instance, request);
    }

    private OpenTournamentSessionRequest createOpenTournamentSessionRequest() {
        TournamentId tournamentId = createTournamentId();
        return new OpenTournamentSessionRequest(-1, tournamentId, pokerState.createZeroMoney());
    }

    public void handleTrigger(TournamentTrigger trigger) {
        switch (trigger) {
            case START_TOURNAMENT:
                startOrCancelTournament();
                break;
            case OPEN_REGISTRATION:
                openRegistration();
                scheduleTournamentStart();
                break;
            case SEND_START_TO_TABLES:
                scheduleNextBlindsLevel();
                sendRoundStartToAllTables();
                break;
            case INCREASE_LEVEL:
                increaseBlindsLevel();
                break;
        }
    }

    private void startOrCancelTournament() {
        if (enoughPlayers() && !hasPendingRegistrations()) {
            startTournament();
        } else if (enoughPlayers() && hasPendingRegistrations()) {
            // Schedule a new start in X seconds.
            log.debug("We have enough players to start the tournament, but there are pending registrations, waiting for them to go through.");
        } else {
            cancelTournament();
        }
    }

    private boolean hasPendingRegistrations() {
        return pokerState.hasPendingRegistrations();
    }

    private boolean enoughPlayers() {
        return state.getRegisteredPlayersCount() >= state.getMinPlayers();
    }

    void increaseBlindsLevel() {
        Level levelBeforeIncreasing = pokerState.getCurrentBlindsLevel();
        Level levelAfterIncreasing = pokerState.increaseBlindsLevel();
        log.debug("Level increased. Before: " + levelBeforeIncreasing + " after: " + levelAfterIncreasing);
        historyPersister.blindsIncreased(pokerState.getCurrentBlindsLevel());

        if (!levelAfterIncreasing.isBreak()) {
            /*
             * Schedule next blinds level unless the new level is a break (because then we have to wait for all tables to finish before
             * we start the break and schedule next level).
             */
            scheduleNextBlindsLevel();
            pokerState.getRebuySupport().notifyNewLevelStarted(pokerState.getCurrentBlindsLevelNr(), false, this);
        }

        if (finishedBreak(levelBeforeIncreasing, levelAfterIncreasing)) {
            // The break has finished. Tell all tables about the new blinds and tell them to start.
            log.debug("The break has finished, notifying all tables about new blinds and telling them to start.");
            notifyAllTablesOfNewBlinds();
            sendRoundStartToAllTables();
            pokerState.breakFinished();
            rebuySupport.breakFinished();
            setTournamentStatus(RUNNING);
        }
    }

    private boolean finishedBreak(Level levelBeforeIncreasing, Level currentBlindsLevel) {
        return levelBeforeIncreasing.isBreak() && !currentBlindsLevel.isBreak();
    }

    private void openRegistration() {
        setTournamentStatus(REGISTERING);
    }

    void cancelTournament() {
        setTournamentStatus(PokerTournamentStatus.CANCELLED);
        refundPlayers();
        closeMainTournamentSession();
        scheduleTournamentClosing();
    }

    private void refundPlayers() {
        for (MttPlayer player : state.getPlayerRegistry().getPlayers()) {
            unregisterPlayer(player.getPlayerId());
        }
    }

    private void sendRoundStartToAllTables() {
        log.debug("Sending round start to all tables.");
        notifyAllTablesOfNewBlinds();
        mttSupport.sendRoundStartActionToTables(state, state.getTables());
    }

    public void handleOpenSessionResponse(OpenSessionResponse response) {
        log.debug("Open session succeeded: " + response);
        PlayerSessionId sessionId = response.getSessionId();
        MttPlayer tournamentPlayer = pokerState.getTournamentPlayer(sessionId.playerId, state);
        if (sessionId.playerId == -1) {
            setTournamentSessionId(sessionId);
        } else if(tournamentPlayer == null) {
            // we can't find the player, so this session needs to be closed
            log.warn("Cannot find player object for opened session; player ID: " + sessionId.playerId);
            try {
                resetPlayerSession(sessionId);
            } catch (CloseSessionFailedException e) {
                log.error("Failed to close tournament session", e);
            }
        } else {
            // The player has now reserved money, transfer it to the tournament session.
            transferMoneyFromPlayerSessionToTournamentSession(sessionId);
            transferFeeToRakeAccount(response.getSessionId());
            pokerState.addBuyInToPrizePool();
            updatePayouts();
            pokerState.addPlayerSession(sessionId);
            pokerState.removePendingRequest(sessionId.playerId);
            historyPersister.playerOpenedSession(sessionId.playerId, sessionId.integrationSessionId);
            historyPersister.playerRegistered(historicPlayer(tournamentPlayer, sessionId));
            checkIfTournamentShouldBeStartedOrCancelled();
        }
    }

    private void transferFeeToRakeAccount(PlayerSessionId playerSessionId) {
        backend.transferMoneyToRakeAccount(playerSessionId, pokerState.getFeeAsMoney(), "Fee for tournament " + pokerState.getHistoricId());
    }

    private void transferFeeFromRakeAccount(PlayerSessionId playerSessionId) {
        backend.transferMoneyFromRakeAccount(playerSessionId, pokerState.getFeeAsMoney(), "Returning fee for tournament " + pokerState.getHistoricId());
    }

    private void transferMoneyFromPlayerSessionToTournamentSession(PlayerSessionId sessionId) {
        Money buyIn = pokerState.getBuyInAsMoney();
        TournamentSessionId toAccount = pokerState.getTournamentSession();
        TransferMoneyRequest request = new TransferMoneyRequest(buyIn, sessionId, toAccount, "Buy-in for tournament " + pokerState.getHistoricId());
        backend.transfer(request);
    }

    private void transferMoneyFromTournamentSessionToPlayerSession(PlayerSessionId sessionId) {
        Money buyIn = pokerState.getBuyInAsMoney();
        TournamentSessionId fromAccount = pokerState.getTournamentSession();
        TransferMoneyRequest request = new TransferMoneyRequest(buyIn, fromAccount, sessionId, "Returning buy-in for tournament " + pokerState.getHistoricId());
        backend.transfer(request);
    }

    private void setTournamentSessionId(PlayerSessionId sessionId) {
        historyPersister.setTournamentSessionId(sessionId.integrationSessionId);
        pokerState.setTournamentSessionId(sessionId);
    }

    public void handleOpenSessionResponseFailed(OpenSessionFailedResponse response) {
        if (response.getPlayerId() == -1) {
            log.fatal("Failed opening tournament session account. Cancelling tournament.");
            cancelTournament();
        } else {
            log.debug("Open session failed: " + response);
            pokerState.removePendingRequest(response.getPlayerId());
            state.getPlayerRegistry().removePlayer(response.getPlayerId());
            historyPersister.playerFailedOpeningSession(response.getPlayerId(), response.getMessage());

            checkIfTournamentShouldBeStartedOrCancelled();
        }
    }

    private void checkIfTournamentShouldBeStartedOrCancelled() {
        if (tournamentShouldStart()) {
            startTournament();
        } else if (tournamentShouldBeCancelled()) {
            cancelTournament();
        }
    }

    public void closeTournament() {
        log.debug("Closing tournament.");

        /*
         * To future debuggers: there's a (very small) chance that this will cause the tournament to be removed
         * before the "tournament destroyed" messages have been sent to the tables (in which case the tables won't exist).
         * For that to happen the TournamentScanner must run exactly at the same time as this code. To avoid this, we
         * could add some grace time between those events, but for now I've decided it's not that critical if it happens.
         */
        setTournamentStatus(CLOSED);
        notifyTournamentClosed();
    }

    private void notifyTournamentClosed() {
        for (Integer tableId : state.getTables()) {
            notifyTable(tableId, new TournamentDestroyed());
        }
    }

    @Override
    public void notifyAllTables(Object attachment) {
        if (attachment instanceof GameObjectAction) {
            throw new IllegalArgumentException("You should send the attachment and not a GameObjectAction.");
        }
        for (Integer tableId : state.getTables()) {
            notifyTable(tableId, attachment);
        }
    }

    @Override
    public void notifyTable(int tableId, Object attachment) {
        if (attachment instanceof GameObjectAction) {
            throw new IllegalArgumentException("You should send the attachment and not a GameObjectAction.");
        }
        GameObjectAction action = new GameObjectAction(tableId);
        action.setAttachment(attachment);
        instance.getMttNotifier().notifyTable(tableId, action);
    }
}
