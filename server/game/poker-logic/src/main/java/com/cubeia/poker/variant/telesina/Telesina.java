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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.RevealOrderCalculator;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.rounds.ante.AnteRound;
import com.cubeia.poker.rounds.betting.BettingRound;
import com.cubeia.poker.rounds.blinds.BlindsRound;
import com.cubeia.poker.rounds.dealing.DealCommunityCardsRound;
import com.cubeia.poker.rounds.dealing.DealExposedPocketCardsRound;
import com.cubeia.poker.rounds.dealing.DealPocketCardsRound;
import com.cubeia.poker.rounds.dealing.ExposePrivateCardsRound;
import com.cubeia.poker.rounds.discard.DiscardRound;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.util.ThreadLocalProfiler;
import com.cubeia.poker.variant.AbstractGameType;
import com.cubeia.poker.variant.HandResultCreator;
import com.cubeia.poker.variant.telesina.hand.TelesinaHandStrengthEvaluator;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Telesina game.
 * <p/>
 * Game rounds:
 * 1  AnteRound, 1 private + 1 public pocket cards
 * 2  BettingRound
 * 3  DealPocketCardsRound, 1 + 2
 * 4  BettingRound
 * 5  DealPocketCardsRound, 1 + 3
 * 6  BettingRound
 * 7  DealPocketCardsRound, 1 + 4
 * 8  BettingRound
 * 9  DealVelaCard, 1 + 4 & 1 community (vela)
 * 10  Betting round
 */
public class Telesina extends AbstractGameType implements RoundVisitor {

    private static final int VELA_ROUND_ID = 4;

    private static final long serialVersionUID = -1523110440727681601L;

    private static transient Logger log = LoggerFactory.getLogger(Telesina.class);

    private Round currentRound;

    private TelesinaDealerButtonCalculator dealerButtonCalculator;

//    private TelesinaDeck deck;

    /**
     * Betting round sequence id:
     * 0 -> ante (1 private + 1 public pocket cards)
     * 1 -> betting round 1 (1 + 2)
     * 2 -> betting round 2 (1 + 3)
     * 3 -> betting round 3 (1 + 4)
     * 4 -> vela betting round (1 + 4 + vela)
     */
    private int bettingRoundId;

    private final TelesinaDeckFactory deckFactory;

    private final TelesinaRoundFactory roundFactory;

    public Telesina(TelesinaDeckFactory deckFactory, TelesinaRoundFactory roundFactory, TelesinaDealerButtonCalculator dealerButtonCalculator) {
        this.deckFactory = deckFactory;
        this.roundFactory = roundFactory;
        this.dealerButtonCalculator = dealerButtonCalculator;
    }

    @Override
    public String toString() {
        return "Telesina, current round[" + getCurrentRound() + "] roundId[" + getBettingRoundId() + "] ";
    }

    @Override
    public void startHand() {
        log.debug("start hand");
        initHand();
    }

    private void initHand() {
        log.debug("init hand");
        context.setDeck(deckFactory.createNewDeck(getServerAdapter().getSystemRNG(), context.getTableSize()));
        try {
            getServerAdapter().notifyDeckInfo(context.getDeck().getTotalNumberOfCardsInDeck(), context.getDeck().getDeckLowestRank());
        } catch (Throwable th) {
            log.error(th.getMessage(), th);
        }
        setCurrentRound(roundFactory.createAnteRound(context, serverAdapterHolder));
        resetBettingRoundId();
    }

    @Override
    public boolean act(PokerAction action) {
        ThreadLocalProfiler.add("Telesina.act.start");
        boolean handled = getCurrentRound().act(action);
        checkFinishedRound();
        ThreadLocalProfiler.add("Telesina.act.stop");
        return handled;
    }

    private void checkFinishedRound() {
        if (getCurrentRound().isFinished()) {
            ThreadLocalProfiler.add("Telesina.checkFinishedRound");
            handleFinishedRound();
        }
    }

    public void handleFinishedRound() {
        ThreadLocalProfiler.add("Telesina.handleFinishedRound");
        log.debug("handle finished round: {}", getCurrentRound());
        getCurrentRound().visit(this);
    }

    private void reportPotAndRakeUpdates(Collection<PotTransition> potTransitions) {
        notifyPotAndRakeUpdates(potTransitions);
    }

    private void startBettingRound() {
        setCurrentRound(roundFactory.createBettingRound(context, serverAdapterHolder, getDeckLowestRank()));
        incrementBettingRoundId();
        log.debug("started new betting round, betting round id = {}", getBettingRoundId());
        getServerAdapter().notifyNewRound();
    }

    @VisibleForTesting
    protected boolean isHandFinished() {
        return (getBettingRoundId() >= 5 || context.countNonFoldedPlayers() <= 1);
    }

    @VisibleForTesting
    protected void handleCanceledHand() {
        log.debug("hand canceled in round {}: {}", getCurrentRound(), HandEndStatus.CANCELED_TOO_FEW_PLAYERS);

        // return antes
        returnAllBetStacksToBalance();
        notifyRakeInfo();

        notifyHandFinished(new HandResult(context.getSettings().getCurrency()), HandEndStatus.CANCELED_TOO_FEW_PLAYERS);

        // Make sure status is reported if we get other players joining the table
        // while waiting to start a new hand.
        notifyAllHandStartPlayerStatus();
        cleanupPlayers();
    }

    private Collection<PotTransition> moveChipsToPotAndTakeBackUncalledChips() {
        Collection<PotTransition> potTransitions = context.getPotHolder().moveChipsToPotAndTakeBackUncalledChips(context.getCurrentHandSeatingMap().values());

        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            p.setHasActed(false);
            p.clearActionRequest();
        }

        return potTransitions;
    }

    @Override
    public void scheduleRoundTimeout() {
        ThreadLocalProfiler.add("Telesina.scheduleRoundTimeout");
        log.debug("scheduleRoundTimeout in: " + context.getTimingProfile().getTime(Periods.RIVER));
        getServerAdapter().scheduleTimeout(context.getTimingProfile().getTime(Periods.RIVER));
    }

    public BlindsInfo getBlindsInfo() {
        return context.getBlindsInfo();
    }

    @Override
    public void prepareNewHand() {
        context.getCommunityCards().clear();
    }

    @Override
    public void timeout() {
        log.debug("Timeout");
        getCurrentRound().timeout();
        checkFinishedRound();
    }

    @Override
    public String getStateDescription() {
        return getCurrentRound() == null ? "th-round=null" : getCurrentRound().getClass() + "_" + getCurrentRound().getStateDescription();
    }

    @Override
    public void visit(AnteRound anteRound) {
        ThreadLocalProfiler.add("Telesina.visit.AnteRound");
        updateDealerButtonPosition(anteRound);

        if (anteRound.isCanceled()) {
            handleCanceledHand();
        } else {
            log.debug("ante round finished");

            Collection<PotTransition> potTransitions = moveChipsToPotAndTakeBackUncalledChips();
            reportPotAndRakeUpdates(potTransitions);

            startDealInitialCardsRound();
        }
    }


    private void updateDealerButtonPosition(AnteRound anteRound) {

        if (!anteRound.isFinished()) {
            throw new IllegalStateException("Can not move the dealer button when ante round is not finished");
        }

        boolean wasCancelled = anteRound.isCanceled();

        int currentDealerButtonSeatId = getBlindsInfo().getDealerButtonSeatId();
        int newDealerSeat = dealerButtonCalculator.getNextDealerSeat(context.getCurrentHandSeatingMap(), currentDealerButtonSeatId, wasCancelled);

        getBlindsInfo().setDealerButtonSeatId(newDealerSeat);
        getServerAdapter().notifyDealerButton(newDealerSeat);
    }

    private void startDealInitialCardsRound() {
        setCurrentRound(roundFactory.createDealInitialCardsRound(context, serverAdapterHolder));
    }

    @Override
    public void visit(BettingRound bettingRound) {
        ThreadLocalProfiler.add("Telesina.visit.BettingRound");
        context.setLastPlayerToBeCalled(bettingRound.getLastPlayerToBeCalled());

        Collection<PotTransition> potTransitions = moveChipsToPotAndTakeBackUncalledChips();
        reportPotAndRakeUpdates(potTransitions);

        if (isHandFinished()) {
            handleFinishedHand();
        } else {
            if (context.isAtLeastAllButOneAllIn() && !context.haveAllPlayersExposedCards()) {
                // All-in showdown.
                setCurrentRound(roundFactory.createExposePrivateCardsRound(context, serverAdapterHolder, new RevealOrderCalculator()));
                scheduleRoundTimeout();
            } else {
                startDealPocketOrVelaCardRound();
            }
        }
    }

    private void handleFinishedHand() {
        List<Integer> playerRevealOrder = calculateRevealOrder();
        TelesinaHandStrengthEvaluator evaluator = new TelesinaHandStrengthEvaluator(getDeckLowestRank());
        HandResultCreator resultCreator = new HandResultCreator(evaluator);
        HandResultCalculator resultCalculator = new HandResultCalculator(evaluator);
        Map<Integer, PokerPlayer> players = context.getCurrentHandPlayerMap();
        Set<PokerPlayer> muckingPlayers = context.getMuckingPlayers();

        HandResult handResult = resultCreator.createHandResult(context.getCommunityCards(), resultCalculator,
                context.getPotHolder(), players, playerRevealOrder, muckingPlayers, context.getSettings().getCurrency());

        ThreadLocalProfiler.add("Telesina.handleFinishedHand");
        log.debug("Hand over. Result: " + handResult.getPlayerHands());
        notifyHandFinished(handResult, HandEndStatus.NORMAL);

        context.getPotHolder().clearPots();
    }

    private List<Integer> calculateRevealOrder() {
        PokerPlayer playerAtDealerButton = context.getPlayerInDealerSeat();
        return new RevealOrderCalculator().calculateRevealOrder(context.getCurrentHandSeatingMap(), context.getLastPlayerToBeCalled(), playerAtDealerButton, context.countNonFoldedPlayers());
    }

    @Override
    public void visit(ExposePrivateCardsRound exposePrivateCardsRound) {
        startDealPocketOrVelaCardRound();
    }

    @Override
    public void visit(DiscardRound discardRound) {
    }

    private void startDealPocketOrVelaCardRound() {
        ThreadLocalProfiler.add("Telesina.startDealPocketOrVelaCardRound");
        if (getBettingRoundId() == VELA_ROUND_ID) {
            setCurrentRound(roundFactory.createDealCommunityCardsRound(context, serverAdapterHolder));
        } else {
            setCurrentRound(roundFactory.createDealExposedPocketCardsRound(context, serverAdapterHolder));
        }
    }

    private void returnAllBetStacksToBalance() {
        for (PokerPlayer player : context.getCurrentHandSeatingMap().values()) {

            BigDecimal betStack = player.getBetStack();
            if (betStack.compareTo(BigDecimal.ZERO) > 0) {
                player.returnBetStackToBalance();
                getServerAdapter().notifyTakeBackUncalledBet(player.getId(), betStack);
            }
        }
    }

    @Override
    public void visit(BlindsRound blindsRound) {
        throw new UnsupportedOperationException("blinds round not supported in telesina");
    }

    @Override
    public void visit(DealCommunityCardsRound round) {
        startBettingRound();
    }

    @Override
    public void visit(DealExposedPocketCardsRound round) {
        log.debug("deal pocked cards round finished (betting round {})", getBettingRoundId());
        sendAllNonFoldedPlayersBestHand();
        startBettingRound();
    }

    @Override
    public void visit(DealPocketCardsRound round) {
        startBettingRound();
    }

    public void sendAllNonFoldedPlayersBestHand() {
        TelesinaHandStrengthEvaluator handStrengthEvaluator = new TelesinaHandStrengthEvaluator(getDeckLowestRank());
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            if (!p.hasFolded()) {
                calculateAndSendBestHandToPlayer(handStrengthEvaluator, p);
            }
        }
    }

    /**
     * Calculate the best hand for the player and send it.
     *
     * @param handStrengthEvaluator hand calculator
     * @param player                player
     */
    protected void calculateAndSendBestHandToPlayer(TelesinaHandStrengthEvaluator handStrengthEvaluator, PokerPlayer player) {
        List<Card> playerCards = new ArrayList<Card>(player.getPocketCards().getCards());
        playerCards.addAll(context.getCommunityCards());
        Hand playerHand = new Hand(playerCards);
        HandStrength bestHandStrength = handStrengthEvaluator.getBestHandStrength(playerHand);
        getServerAdapter().notifyBestHand(player.getId(), bestHandStrength.getHandType(), bestHandStrength.getCards(),
                player.isExposingPocketCards() && !player.hasFolded());
    }

    @VisibleForTesting
    protected Round getCurrentRound() {
        return currentRound;
    }

    private void setCurrentRound(Round newRound) {
        log.debug("moved to new round: {} -> {}", currentRound, newRound);
        this.currentRound = newRound;
        // context.notifyNewRound();
    }

    @VisibleForTesting
    protected int getBettingRoundId() {
        return bettingRoundId;
    }

    private void incrementBettingRoundId() {
        this.bettingRoundId++;
    }

    private void resetBettingRoundId() {
        this.bettingRoundId = 0;
    }

    public Rank getDeckLowestRank() {
        return context.getDeck().getDeckLowestRank();
    }

    @Override
    public boolean canPlayerAffordEntryBet(PokerPlayer player, PokerSettings pokerSettings, boolean includePending) {
        BigDecimal balance = player.getBalance().add((includePending ? player.getPendingBalanceSum() : BigDecimal.ZERO));
        System.out.println("Checking if player can afford entry bet. Balance = " + balance + ", ante = " + pokerSettings.getAnteAmount());
        return balance.compareTo(pokerSettings.getAnteAmount()) >= 0 ;
    }
}
