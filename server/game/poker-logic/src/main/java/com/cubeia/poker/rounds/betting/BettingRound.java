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
package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundHelper;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.util.ThreadLocalProfiler;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class BettingRound implements Round, BettingRoundContext {

    private static final long serialVersionUID = -8666356150075950974L;

    private static transient Logger log = LoggerFactory.getLogger(BettingRound.class);

    private PokerContext context;

    private ServerAdapterHolder serverAdapterHolder;
    
    private RoundHelper roundHelper;

    @VisibleForTesting
    protected BigDecimal highBet = BigDecimal.ZERO;

    /*
     * The currently highest (complete) bet in this betting round. Note that the highest complete bet can be greater than the current high bet
     * in the case of an all-in bet that counts as a complete bet.
     */
    protected BigDecimal highestCompleteBet = BigDecimal.ZERO;

    @VisibleForTesting
    protected Integer playerToAct = null;

    private final ActionRequestFactory actionRequestFactory;

    private boolean isFinished = false;

    // The amount that the last player to bet or raise for a complete bet raised with.
    private BigDecimal sizeOfLastCompleteBetOrRaise = BigDecimal.ZERO;

    private final PlayerToActCalculator playerToActCalculator;

    private PokerPlayer lastPlayerToPlaceBet;

    private PokerPlayer lastPlayerToBeCalled;

    private FutureActionsCalculator futureActionsCalculator;

    /**
     * Players still in play (not folded or all in) that entered this round.
     */
    @VisibleForTesting
    protected Set<PokerPlayer> playersInPlayAtRoundStart;

    // Keeps track of the number of bets or raises in this betting round.
    private int numberOfBetsAndRaises = 0;

    private final BetStrategy betStrategy;

    private boolean bettingCapped = false;

    // TODO: Would probably be nice if the playerToActCalculator knew all it needs to know, so we don't need to pass "seatIdToStart.." as well.
    public BettingRound(PokerContext context,
                        ServerAdapterHolder serverAdapterHolder,
                        PlayerToActCalculator playerToActCalculator,
                        ActionRequestFactory actionRequestFactory,
                        FutureActionsCalculator futureActionsCalculator,
                        BetStrategy betStrategy) {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        this.futureActionsCalculator = futureActionsCalculator;
        this.playerToActCalculator = playerToActCalculator;
        this.actionRequestFactory = actionRequestFactory;
        this.roundHelper = new RoundHelper(context, serverAdapterHolder);
        this.betStrategy = betStrategy;
        initBettingRound();
    }

    private void initBettingRound() {
        log.debug("Initializing new betting round.");
        initializeHighBet();
        initializeHighestCompleteBetAndSizeOfLastCompleteBet();
        initializePlayersInPlayAtRoundStart();
        resetCanRaiseFlag();
        requestFirstActionOrFinishRound();
    }

    private void initializeHighestCompleteBetAndSizeOfLastCompleteBet() {
        /*
         * If there are blinds in this round, the highest bet should be the big blind and the
         * size of the last bet should be the big blind as well. This is regardless of whether
         * the player on the big blind could actually afford the big blind.
         *
         * For example, if the big blind is $10 but the player on the big blind only has $3, then
         * the highest complete bet will be $10 and the size of the last complete bet will be $10.
         * This means that the next raise needs to be to $20.
         *
         * For rounds without blinds, the highest complete bet and the size of the last complete bet
         * will be $0.
         */
        highestCompleteBet = highBet;
        sizeOfLastCompleteBetOrRaise = highestCompleteBet;
    }

    private void requestFirstActionOrFinishRound() {
        // Check if we should request actions at all
        PokerPlayer p = playerToActCalculator.getFirstPlayerToAct(context.getCurrentHandSeatingMap(), context.getCommunityCards());

        log.debug("first player to act = {}", p == null ? null : p.getId());

        if (p == null || allOtherNonFoldedPlayersAreAllIn(p)) {
            // No or only one player can act. We are currently in an all-in show down scenario
            log.debug("No players left to act. We are in an all-in show down scenario");
            notifyAllPlayersOfNoPossibleFutureActions();
            isFinished = true;
        } else {
            requestAction(p);
        }
        /*
         * This can be triggered by the if clause above, but also
         * by traversing into requestAction and calling default act on
         * each and every player in sit out scenarios.
         */
        if (isFinished()) {
            roundHelper.scheduleRoundTimeout(context, getServerAdapter());
        }
    }

    private void initializePlayersInPlayAtRoundStart() {
        playersInPlayAtRoundStart = new HashSet<PokerPlayer>();
        for (PokerPlayer player : context.getPlayersInHand()) {
            log.debug("player {}: folded {}, allIn: {}, hasActed: {}", new Object[]{player.getId(), player.hasFolded(), player.isAllIn(), player.hasActed()});

            if (!player.isAllIn() && !player.hasFolded()) {
                playersInPlayAtRoundStart.add(player);
            }
        }
        log.debug("players in play entering round: {}", playersInPlayAtRoundStart);
    }

    private void initializeHighBet() {
        for (PokerPlayer p : context.getPlayersInHand()) {
            /*
             * Initialize the highBet to the highest bet stack of the incoming players.
             * This will be zero on all rounds except when blinds have been posted.
             */
            if (p.getBetStack().compareTo(highBet) > 0) {
                highBet = p.getBetStack();
            }
            p.clearActionRequest();
        }
        if (highBet.compareTo(BigDecimal.ZERO) > 0) {
            numberOfBetsAndRaises = 1;
        }
        makeSureHighBetIsNotSmallerThanBigBlind();
    }

    private void makeSureHighBetIsNotSmallerThanBigBlind() {
        if (highBet.compareTo(BigDecimal.ZERO) > 0 && highBet.compareTo(context.getSettings().getBigBlindAmount()) < 0) {
            highBet = context.getSettings().getBigBlindAmount();
        }
    }

    @Override
    public String toString() {
        return "BettingRound, isFinished[" + isFinished + "]";
    }

    public boolean act(PokerAction action) {
        log.debug("Act : " + action);
        ThreadLocalProfiler.add("BettingRound.act");
        PokerPlayer player = context.getPlayerInCurrentHand(action.getPlayerId());

        if (!isValidAction(action, player)) {
            return false;
        }

        boolean handled = handleAction(action, player);
        if (handled) {
            getServerAdapter().notifyActionPerformed(action, player);
            getServerAdapter().notifyPlayerBalance(player);

            if (calculateIfRoundFinished()) {
                log.debug("BettingRound is finished");
                isFinished = true;
            } else {
                requestNextAction(player.getSeatId());
            }
        }
        return handled;
    }

    private void requestNextAction(int lastSeatId) {
        PokerPlayer player = playerToActCalculator.getNextPlayerToAct(lastSeatId, context.getCurrentHandSeatingMap());
        if (player == null) {
            log.debug("Setting betting round is finished because there is no player left to act.");
            isFinished = true;
            notifyAllPlayersOfNoPossibleFutureActions();
        } else {
            log.debug("Next player to act is: " + player.getId());
            requestAction(player);
        }
    }

    /**
     * Get the player's available actions and send a request to the client
     * or perform default action if the player is sitting out.
     *
     * @param p the player to request an action for
     */
    private void requestAction(PokerPlayer p) {
        playerToAct = p.getId();
        if (p.getBetStack().compareTo(highBet) < 0) {
            p.setActionRequest(actionRequestFactory.createFoldCallRaiseActionRequest(this, p));
        } else {
            ActionRequest ar = actionRequestFactory.createFoldCheckBetActionRequest(this, p);
            p.setActionRequest(ar);
        }

        if (p.isSittingOut() || p.isAway()) {
            performDefaultActionForPlayer(p);
        } else {
            roundHelper.requestAction(p.getActionRequest());
            notifyAllPlayersOfPossibleFutureActions(p);
        }
    }

    /**
     * Notify all the other players about their future action options
     * i.e. check next and fold next checkboxes
     *
     * @param excludePlayer player that should get no actions
     */
    private void notifyAllPlayersOfPossibleFutureActions(PokerPlayer excludePlayer) {
        for (PokerPlayer player : context.getCurrentHandPlayerMap().values()) {

            if (player.getId() != excludePlayer.getId()) {
                BigDecimal callAmount = betStrategy.getCallAmount(this,player);
                BigDecimal minRaiseToAmount = betStrategy.getMinRaiseToAmount(this, player);
                getServerAdapter().notifyFutureAllowedActions(player, futureActionsCalculator.calculateFutureActionOptionList(player, highBet, bettingCapped),callAmount,minRaiseToAmount);
            } else {
                getServerAdapter().notifyFutureAllowedActions(player, Lists.<PokerActionType>newArrayList(),BigDecimal.ZERO,BigDecimal.ZERO);
            }
        }
    }

    /**
     * Notify all players that they will not have any future actions in the current round
     * so they can turn of the check, check/fold and fold checkboxes
     */
    private void notifyAllPlayersOfNoPossibleFutureActions() {
        for (PokerPlayer player : context.getCurrentHandPlayerMap().values()) {
            getServerAdapter().notifyFutureAllowedActions(player, Lists.<PokerActionType>newArrayList(),BigDecimal.ZERO,BigDecimal.ZERO);
        }
    }

    @VisibleForTesting
    protected boolean calculateIfRoundFinished() {
        if (context.countNonFoldedPlayers(playersInPlayAtRoundStart) < 2) {
            return true;
        }
        for (PokerPlayer p : context.getPlayersInHand()) {
            if (!p.hasFolded() && !p.hasActed()) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    protected boolean handleAction(PokerAction action, PokerPlayer player) {
        boolean handled;
        switch (action.getActionType()) {
            case CALL:
                BigDecimal amountToCall = getAmountToCall(player);
                handled = call(player);
                action.setBetAmount(amountToCall);
                break;
            case CHECK:
                handled = check();
                break;
            case FOLD:
                handled = fold(player);
                break;
            case RAISE:
                setRaiseByAmount(action);
                handled = raise(player, action.getBetAmount());
                break;
            case BET:
                handled = bet(player, action.getBetAmount());
                break;
            default:
                log.warn("Can't handle " + action.getActionType());
                handled = false;
                break;
        }
        if (handled) {
            player.setHasActed(true);
            player.setCanRaise(false);
        }
        return handled;
    }

    private boolean isValidAction(PokerAction action, PokerPlayer player) {
        if (!action.getPlayerId().equals(playerToAct)) {
            log.warn("Expected " + playerToAct + " to act, but got action from:" + player.getId());
            return false;
        }

        if (!player.getActionRequest().matches(action)) {
            log.warn("Player " + player.getId() + " tried to act " + action.getActionType() + " but his options were "
                    + player.getActionRequest().getOptions());
            return false;
        }

        if (player.hasActed()) {
            log.warn("Player has already acted in this BettingRound. Player[" + player + "], action[" + action + "]");
            return false;
        }
        return true;
    }

    @VisibleForTesting
    boolean raise(PokerPlayer player, BigDecimal amountRaisedTo) {
        PossibleAction raiseOption = player.getActionRequest().getOption(PokerActionType.RAISE);
        if (amountRaisedTo.compareTo(raiseOption.getMinAmount()) < 0 || amountRaisedTo.compareTo(raiseOption.getMaxAmount()) > 0) {
            log.warn("PokerPlayer[" + player.getId() + "] incorrect raise amount. Options[" + raiseOption + "] amount[" + amountRaisedTo + "].");
            return false;
        }

        if (betStrategy.isCompleteBetOrRaise(this, amountRaisedTo)) {
            // We only increase the number of raises and the size of the last raise if the raise is complete.
            numberOfBetsAndRaises++;
            bettingCapped = betStrategy.shouldBettingBeCapped(numberOfBetsAndRaises, isHeadsUpBetting());
            BigDecimal validLevel = betStrategy.getNextValidRaiseToLevel(this);
            BigDecimal previousCompleteBet = highestCompleteBet;
            highestCompleteBet = determineHighestCompleteBet(amountRaisedTo, validLevel);
            sizeOfLastCompleteBetOrRaise = highestCompleteBet.subtract(previousCompleteBet);
            resetCanRaiseFlag();
        }

        highBet = amountRaisedTo;
        lastPlayerToBeCalled = lastPlayerToPlaceBet;
        context.callOrRaise();
        lastPlayerToPlaceBet = player;
        BigDecimal costToRaise = amountRaisedTo.subtract(player.getBetStack());
        player.addBet(costToRaise);
        resetHasActed();

        notifyPotSizeAndRakeInfo();
        return true;
    }

    private void notifyPotSizeAndRakeInfo() {
        roundHelper.notifyPotSizeAndRakeInfo();
    }

    private void setRaiseByAmount(PokerAction action) {
        action.setRaiseAmount(action.getBetAmount().subtract(highBet));
    }

    @VisibleForTesting
    boolean bet(PokerPlayer player, BigDecimal amount) {
        PossibleAction betOption = player.getActionRequest().getOption(PokerActionType.BET);
        if (amount.compareTo(betOption.getMinAmount()) < 0 || amount.compareTo(betOption.getMaxAmount()) > 0) {
            log.warn("Bet " + amount + " from player " + player + " is not in bounds. Bet option: " + betOption);
            return false;
        }
        BigDecimal totalBet = player.getBetStack().add(amount); // The current stack can be > 0 in case of blinds.
        if (isCompleteBet(totalBet)) {
            // TODO: Test coverage needed here.
            numberOfBetsAndRaises++;
            highestCompleteBet = determineHighestCompleteBet(amount, betStrategy.getNextValidRaiseToLevel(this));
            sizeOfLastCompleteBetOrRaise = highestCompleteBet;
            bettingCapped = betStrategy.shouldBettingBeCapped(numberOfBetsAndRaises, isHeadsUpBetting());
            resetCanRaiseFlag();
        }
        highBet = highBet.add(amount);
        lastPlayerToPlaceBet = player;
        player.addBet(highBet.subtract(player.getBetStack()));
        resetHasActed();

        notifyPotSizeAndRakeInfo();
        return true;
    }

    /**
     * Determines the highest complete bet or raise, given the amount bet or raised and the next valid raise-to-level.
     *
     * Note that if the bet is lower than the next level, we will consider the full next level as the highestCompleteBet.
     * This is because in fixed limit, if A bets 10, B goes all-in for 18 (which counts as a complete raise), then
     * C can call 18 or raise to 30.
     */
    private BigDecimal determineHighestCompleteBet(BigDecimal amount, BigDecimal nextValidRaiseToLevel) {
        return amount.max(nextValidRaiseToLevel);
    }

    private boolean isCompleteBet(BigDecimal amount) {
        return betStrategy.isCompleteBetOrRaise(this, amount);
    }

    private void resetHasActed() {
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            if (!p.hasFolded()) {
                p.setHasActed(false);
            }
        }
    }

    private void resetCanRaiseFlag() {
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            if (!p.hasFolded()) {
                p.setCanRaise(true);
            }
        }
    }

    private boolean fold(PokerPlayer player) {
        player.setHasFolded(true);
        if (player.isSittingOutNextHand()) {
            setPlayerSitOut(player);
        }
        return true;
    }

    private void setPlayerSitOut(PokerPlayer player) {
        if (!context.isTournamentBlinds()) {
            player.setSitOutStatus(SitOutStatus.SITTING_OUT);
        } else {
            player.setAway(true);
            player.setSittingOutNextHand(false);
        }
        notifyPlayerStatusChanged(player);
    }

    private boolean check() {
        // Nothing to do.
        return true;
    }

    @VisibleForTesting
    protected boolean call(PokerPlayer player) {
        BigDecimal amountToCall = getAmountToCall(player);
        player.addBet(amountToCall);
        lastPlayerToBeCalled = lastPlayerToPlaceBet;
        context.callOrRaise();
        notifyPotSizeAndRakeInfo();
        return true;
    }

    /**
     * Returns the amount with which the player has to increase his current bet when doing a call.
     */
    @VisibleForTesting
    BigDecimal getAmountToCall(PokerPlayer player) {
        return  highBet.subtract(player.getBetStack()).min(player.getBalance());
    }

    public void timeout() {
        PokerPlayer player = playerToAct == null ? null : context.getPlayerInCurrentHand(playerToAct);

        if (player == null || player.hasActed()) {
            log.debug("Expected " + playerToAct + " to act, but that player can not be found at the table! I will assume everyone is all in");
            return;
        }
        markPlayerAsAwayAndSitOutNextHand(player);
        performDefaultActionForPlayer(player);
    }

    private void markPlayerAsAwayAndSitOutNextHand(PokerPlayer player) {
        if (!context.isTournamentTable()) {
            player.setSittingOutNextHand(true);
        }
        player.setAway(true);

        notifyPlayerStatusChanged(player);

    }

    private void notifyPlayerStatusChanged(PokerPlayer player) {
        PokerPlayerStatus status = PokerPlayerStatus.SITIN;
        if(player.getSitOutStatus() == SitOutStatus.SITTING_OUT) {
            status = PokerPlayerStatus.SITOUT;
        }
        boolean inHand = context.isPlayerInHand(player.getId());
        getServerAdapter().notifyPlayerStatusChanged(player.getId(), status, inHand,
                player.isAway(), player.isSittingOutNextHand());
    }

    private void performDefaultActionForPlayer(PokerPlayer player) {
        log.debug("Perform default action for player sitting out: " + player);
        if (player.getActionRequest().isOptionEnabled(PokerActionType.CHECK)) {
            act(new PokerAction(player.getId(), PokerActionType.CHECK, true));
        } else {
            act(new PokerAction(player.getId(), PokerActionType.FOLD, true));
        }
    }

    public String getStateDescription() {
        return "playerToAct=" + playerToAct + " roundFinished=" + calculateIfRoundFinished();
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void visit(RoundVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * True when all other players still in play (except the given one) are all in.
     * Sit out and folded players are not counted.
     */
    public boolean allOtherNonFoldedPlayersAreAllIn(PokerPlayer thisPlayer) {
        for (PokerPlayer player : context.getCurrentHandSeatingMap().values()) {
            boolean self = player.equals(thisPlayer);

            if (!self) {
                boolean notFolded = !player.hasFolded();
                boolean notAllIn = !player.isAllIn();

                if (notFolded && notAllIn) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BigDecimal getPotSize() {
        return context.calculateRakeInfo().getTotalPot();
    }

    @Override
    public BigDecimal getHighestBet() {
        return highBet;
    }

    @Override
    public BigDecimal getHighestCompleteBet() {
        return highestCompleteBet;
    }

    @Override
    public boolean isBettingCapped() {
        return bettingCapped;
    }

    private boolean isHeadsUpBetting() {
        int nonFolded = 0;
        for (PokerPlayer player : context.getCurrentHandSeatingMap().values()) {
            if (!player.hasFolded()) {
                nonFolded++;
            }
        }
        return nonFolded < 3;
    }

    public BigDecimal getSizeOfLastCompleteBetOrRaise() {
        return sizeOfLastCompleteBetOrRaise;
    }

    public PokerPlayer getLastPlayerToBeCalled() {
        return lastPlayerToBeCalled;
    }

    private ServerAdapter getServerAdapter() {
        return serverAdapterHolder.get();
    }
}
