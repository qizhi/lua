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

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.blinds.BlindsCalculator;
import com.cubeia.poker.blinds.BlindsPlayer;
import com.cubeia.poker.blinds.EntryBetType;
import com.cubeia.poker.blinds.EntryBetter;
import com.cubeia.poker.blinds.MissedBlind;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundHelper;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.settings.PokerSettings;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;

public class BlindsRound implements Round, Serializable {

    private static final long serialVersionUID = -6452364533249060511L;

    private static transient Logger log = Logger.getLogger(BlindsRound.class);

    private BlindsState currentState;

    private BlindsInfo blindsInfo = new BlindsInfo();

    private BlindsInfo previousBlindsInfo;

    private boolean isTournamentBlinds;
    
    private PokerContext context;
    
    private ServerAdapterHolder serverAdapterHolder;

    private BlindsCalculator blindsCalculator;

    private RoundHelper roundHelper;

    public static final BlindsState WAITING_FOR_SMALL_BLIND_STATE = new WaitingForSmallBlindState();

    public static final BlindsState WAITING_FOR_BIG_BLIND_STATE = new WaitingForBigBlindState();

    public static final BlindsState WAITING_FOR_ENTRY_BET_STATE = new WaitingForEntryBetState();

    public static final BlindsState FINISHED_STATE = new FinishedState();

    public static final BlindsState CANCELED_STATE = new CanceledState();

    private Queue<EntryBetter> entryBetters;

    private int pendingEntryBetterId;

    private PokerSettings settings;

    public BlindsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, BlindsCalculator blindsCalculator) {
        this.serverAdapterHolder = serverAdapterHolder;
        this.blindsCalculator = blindsCalculator;
        this.roundHelper = new RoundHelper(context, serverAdapterHolder);
        this.isTournamentBlinds = context.isTournamentBlinds();
        this.context = context;
        this.settings = context.getSettings();
        this.previousBlindsInfo = context.getBlindsInfo();
        clearPlayerActionOptions();
        initBlinds();
        if (blindsInfo.hasDeadSmallBlind()) {
            currentState = WAITING_FOR_BIG_BLIND_STATE;
        } else {
            currentState = WAITING_FOR_SMALL_BLIND_STATE;
        }
    }

    private void clearPlayerActionOptions() {
        SortedMap<Integer, PokerPlayer> seatingMap = context.getCurrentHandSeatingMap();
        for (PokerPlayer p : seatingMap.values()) {
            p.clearActionRequest();
        }
    }

    private void initBlinds() {
        com.cubeia.poker.blinds.BlindsInfo newBlindsInfo = blindsCalculator.initializeBlinds(
                convertBlindsInfo(),
                context.getPlayerMap().values(),
                isTournamentBlinds);
        if (newBlindsInfo != null) {
            setNewBlindsInfo(newBlindsInfo);
            moveDealerButtonToSeatId(newBlindsInfo.getDealerSeatId());
            if (!isTournamentBlinds()) {
                markMissedBlinds(blindsCalculator.getMissedBlinds());
            }

            if (newBlindsInfo.getSmallBlindPlayerId() != -1) {
                requestSmallBlind(getPlayerInSeat(newBlindsInfo.getSmallBlindSeatId()));
            } else {
                requestBigBlind(getPlayerInSeat(newBlindsInfo.getBigBlindSeatId()));
            }
        } else {
            throw new RuntimeException("Could not initialize blinds. Not enough players. Players at table: " + context.getSeatedPlayers());
        }
    }

    private void markMissedBlinds(List<MissedBlind> missedBlinds) {
        for (MissedBlind missed : missedBlinds) {
            log.info("Setting missed blinds status to " + missed.getMissedBlindsStatus() + " for player " + missed.getPlayer().getId());
            context.getPlayer(missed.getPlayer().getId()).setMissedBlindsStatus(missed.getMissedBlindsStatus());
        }
    }

    private void setNewBlindsInfo(com.cubeia.poker.blinds.BlindsInfo newBlindsInfo) {
        blindsInfo.setDealerButtonSeatId(newBlindsInfo.getDealerSeatId());
        // Small blind
        if (newBlindsInfo.getSmallBlindPlayerId() != -1) {
            blindsInfo.setSmallBlind(getPlayerInSeat(newBlindsInfo.getSmallBlindSeatId()));
        } else {
            blindsInfo.setHasDeadSmallBlind(true);
        }
        blindsInfo.setSmallBlindPlayerId(newBlindsInfo.getSmallBlindPlayerId());
        blindsInfo.setSmallBlindSeatId(newBlindsInfo.getSmallBlindSeatId());

        // Big blind
        blindsInfo.setBigBlindSeatId(newBlindsInfo.getBigBlindSeatId());
        blindsInfo.setBigBlindPlayerId(newBlindsInfo.getBigBlindPlayerId());
    }

    private com.cubeia.poker.blinds.BlindsInfo convertBlindsInfo() {
        return new com.cubeia.poker.blinds.BlindsInfo(previousBlindsInfo.getDealerButtonSeatId(),
                previousBlindsInfo.getSmallBlindSeatId(), previousBlindsInfo.getBigBlindSeatId(), previousBlindsInfo.getBigBlindPlayerId());
    }

    private void requestSmallBlind(PokerPlayer smallBlind) {
        getBlindsInfo().setSmallBlind(smallBlind);
        smallBlind.enableOption(new PossibleAction(PokerActionType.SMALL_BLIND, settings.getSmallBlindAmount()));
        smallBlind.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
        if (isTournamentBlinds()) {
            roundHelper.scheduleTimeoutForAutoAction();
        } else {
            roundHelper.requestAction(smallBlind.getActionRequest());
        }
    }

    private ServerAdapter getServerAdapter() {
        return serverAdapterHolder.get();
    }

    private void requestBigBlind(PokerPlayer bigBlind) {
        bigBlind.enableOption(new PossibleAction(PokerActionType.BIG_BLIND, settings.getBigBlindAmount()));
        bigBlind.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
        if (isTournamentBlinds()) {
            roundHelper.scheduleTimeoutForAutoAction();
        } else {
            roundHelper.requestAction(bigBlind.getActionRequest());
        }
    }

    private void requestEntryBet(PokerPlayer player) {
        player.enableOption(new PossibleAction(PokerActionType.ENTRY_BET, settings.getBigBlindAmount()));
        player.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
        player.enableOption(new PossibleAction(PokerActionType.WAIT_FOR_BIG_BLIND));
        roundHelper.requestAction(player.getActionRequest());
    }

    private void moveDealerButtonToSeatId(int newDealerSeatId) {
        blindsInfo.setDealerButtonSeatId(newDealerSeatId);
        getServerAdapter().notifyDealerButton(blindsInfo.getDealerButtonSeatId());
    }

    private PokerPlayer getPlayerInSeat(int seatId) {
        return context.getCurrentHandSeatingMap().get(seatId);
    }

    private int numberPlayersSittingIn() {
        int playersSittingIn = 0;
        for (PokerPlayer player : context.getPlayersInHand()) {
            if (player.isSittingIn()) {
                playersSittingIn++;
            }
        }
        return playersSittingIn;
    }

    public boolean act(PokerAction action) {
        PokerPlayer player = context.getPlayerInCurrentHand(action.getPlayerId());
        if (player == null) {
            log.debug("Ignoring action from playerId " + action.getPlayerId() + " because player was null.");
            return false;
        }
        boolean handled;
        switch (action.getActionType()) {
            case SMALL_BLIND:
                handled = currentState.smallBlind(action.getPlayerId(), context, this);
                break;
            case BIG_BLIND:
                handled = currentState.bigBlind(action.getPlayerId(), context, this);
                break;
            case DECLINE_ENTRY_BET:
                handled = currentState.declineEntryBet(action.getPlayerId(), context, this);
                break;
            case BIG_BLIND_PLUS_DEAD_SMALL_BLIND:
                handled = currentState.bigBlindPlusDeadSmallBlind(action.getPlayerId(), context, this);
                break;
            case DEAD_SMALL_BLIND:
                handled = currentState.deadSmallBlind(action.getPlayerId(), context, this);
                break;
            case ENTRY_BET:
                handled = currentState.entryBet(action.getPlayerId(), context, this);
                break;
            case WAIT_FOR_BIG_BLIND:
                handled = currentState.waitForBigBlind(action.getPlayerId(), context, this);
                break;
            default:
                log.debug(action.getActionType() + " is not legal here");
                return false;
        }
        if (handled) {
            player.clearActionRequest();
            getServerAdapter().notifyActionPerformed(action, player);
            getServerAdapter().notifyPlayerBalance(player);
        }
        return handled;
    }

    public BlindsInfo getBlindsInfo() {
        return blindsInfo;
    }

    public void smallBlindPosted(int playerId) {
        if (isTournamentBlinds) {
            notifySmallBlindPosted(playerId);
        }
        this.currentState = WAITING_FOR_BIG_BLIND_STATE;
        PokerPlayer bigBlind = getPlayerInSeat(blindsInfo.getBigBlindSeatId());
        requestBigBlind(bigBlind);
    }

    public void smallBlindDeclined(PokerPlayer player) {
        markPlayerAsSittingOut(player);

        if (numberPlayersSittingIn() >= 2) {
            PokerPlayer bigBlind = getPlayerInSeat(blindsInfo.getBigBlindSeatId());
            requestBigBlind(bigBlind);
            currentState = WAITING_FOR_BIG_BLIND_STATE;
        } else {
            currentState = CANCELED_STATE;
        }
    }

    public void bigBlindPosted(int playerId) {
        if (isTournamentBlinds) {
            notifyBigBlindPosted(playerId);
        }
        entryBetters = blindsCalculator.getEntryBetters(blindsInfo.getDealerButtonSeatId(), blindsInfo.getSmallBlindSeatId(), blindsInfo.getBigBlindSeatId());
        askForNextEntryBetOrFinishBlindsRound();
    }

    private void blindsFinished() {
        removePlayersNotEligibleToPlayThisHand();
        currentState = FINISHED_STATE;
    }

    private void requestNextEntryBet() {
        if (!entryBetters.isEmpty()) {
            EntryBetter entryBetter = entryBetters.poll();
            PokerPlayer player = context.getPlayer(entryBetter.getPlayer().getId());
            if (entryBetter.getEntryBetType() == EntryBetType.BIG_BLIND) {
                log.debug("Requesting entry big blind from " + player);
                requestEntryBet(player);
            } else if (entryBetter.getEntryBetType() == EntryBetType.DEAD_SMALL_BLIND) {
                requestDeadSmallBlind(player);
            } else if (entryBetter.getEntryBetType() == EntryBetType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND) {
                requestBigBlindPlusDeadSmallBlind(player);
            }
            pendingEntryBetterId = player.getId();
        } else {
            log.debug("No more entry betters.");
        }
    }

    private void removePlayersNotEligibleToPlayThisHand() {
        Set<Integer> eligiblePlayerIds = getSetOfEligiblePlayerIds();
        log.debug("Removing players not eligible to play this hand. Eligible players: " + eligiblePlayerIds);

        List<PokerPlayer> playersToRemove = new ArrayList<PokerPlayer>();
        for (PokerPlayer player : context.getPlayersInHand()) {
            log.debug("Checking if " + player.getId() + " is eligible.");
            if (!eligiblePlayerIds.contains(player.getId())) {
                log.debug("Removing " + player.getId() + " from hand since he's not eligible to play.");
                playersToRemove.add(player);
            }
        }

        for (PokerPlayer player : playersToRemove) {
            // Note that the player is not marked as sitting out. If he's sitting in, he should remain sitting in so he gets a chance to play next hand.
            removePlayerFromCurrentHand(player);
        }
    }

    private Set<Integer> getSetOfEligiblePlayerIds() {
        Set<Integer> eligiblePlayerIds = new HashSet<Integer>();
        for (BlindsPlayer player : blindsCalculator.getEligiblePlayerList()) {
            eligiblePlayerIds.add(player.getId());
        }
        return eligiblePlayerIds;
    }

    private void markPlayerAsSittingOut(PokerPlayer player) {
        roundHelper.setPlayerSitOut(player, context, getServerAdapter());
        removePlayerFromCurrentHand(player);
    }

    private void removePlayerFromCurrentHand(PokerPlayer player) {
        blindsCalculator.removePlayerFromCurrentHand(player);
        roundHelper.removePlayerFromCurrentHand(player, context);
    }

    private void requestBigBlindPlusDeadSmallBlind(PokerPlayer player) {
        log.debug("Requesting big blind plus dead small blind from " + player);

        player.enableOption(new PossibleAction(PokerActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND, settings.getBigBlindAmount().add(settings.getSmallBlindAmount())));
        player.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
        player.enableOption(new PossibleAction(PokerActionType.WAIT_FOR_BIG_BLIND));
        roundHelper.requestAction(player.getActionRequest());
    }

    private void requestDeadSmallBlind(PokerPlayer player) {
        log.debug("Requesting dead small blind from " + player);

        player.enableOption(new PossibleAction(PokerActionType.DEAD_SMALL_BLIND, settings.getSmallBlindAmount()));
        player.enableOption(new PossibleAction(PokerActionType.DECLINE_ENTRY_BET));
        player.enableOption(new PossibleAction(PokerActionType.WAIT_FOR_BIG_BLIND));
        roundHelper.requestAction(player.getActionRequest());
    }

    private boolean thereAreUnEnteredPlayersBetweenBigBlindAndDealerButton() {
        boolean empty = entryBetters.isEmpty();
        log.debug("Checking if there are any players up for posting entry bets: " + entryBetters.size());
        return !empty;
    }

    public void bigBlindDeclined(PokerPlayer player) {
        log.debug(player + " declined big blind.");
        markPlayerAsSittingOut(player);
        BlindsPlayer nextBig = blindsCalculator.getNextBigBlindPlayer(player.getSeatId());

        if (nextBig != null) {
            PokerPlayer next = getPlayerInSeat(nextBig.getSeatId());
            requestBigBlind(next);
            // Set the new player as big blind in the context
            blindsInfo.setBigBlind(next);
        } else {
            currentState = CANCELED_STATE;
        }
    }

    public void entryBetDeclined(PokerPlayer player) {
        markPlayerAsSittingOut(player);
        askForNextEntryBetOrFinishBlindsRound();
    }

    public void entryBetPosted() {
        askForNextEntryBetOrFinishBlindsRound();
    }

    public void askForNextEntryBetOrFinishBlindsRound() {
        if (!isTournamentBlinds() && thereAreUnEnteredPlayersBetweenBigBlindAndDealerButton()) {
            log.debug("There are unentered players, requesting entry bet");
            this.currentState = WAITING_FOR_ENTRY_BET_STATE;
            requestNextEntryBet();
        } else {
            blindsFinished();
        }
    }

    public void timeout() {
        currentState.timeout(context, this);
    }

    public boolean isTournamentBlinds() {
        return isTournamentBlinds;
    }

    public String getStateDescription() {
        return currentState != null ? currentState.getClass().getName() : "currentState=null";
    }

    public boolean isFinished() {
        return currentState.isFinished();
    }

    public boolean isCanceled() {
        return currentState.isCanceled();
    }

    public void visit(RoundVisitor visitor) {
        visitor.visit(this);
    }

    public int getPendingEntryBetterId() {
        return pendingEntryBetterId;
    }

    private void notifySmallBlindPosted(int playerId) {
        PokerAction action = new PokerAction(playerId, PokerActionType.SMALL_BLIND, settings.getSmallBlindAmount());
        notifyActionPerformed(action);
    }

    private void notifyBigBlindPosted(int playerId) {
        PokerAction action = new PokerAction(playerId, PokerActionType.BIG_BLIND, settings.getBigBlindAmount());
        notifyActionPerformed(action);
    }

    private void notifyActionPerformed(PokerAction action) {
        PokerPlayer player = context.getPlayerInCurrentHand(action.getPlayerId());
        serverAdapterHolder.get().notifyActionPerformed(action, player);
        serverAdapterHolder.get().notifyPlayerBalance(player);
    }
}
