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

package com.cubeia.poker.variant.texasholdem;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.blinds.BlindsCalculator;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandInfo;
import com.cubeia.poker.hand.IndexCardIdGenerator;
import com.cubeia.poker.hand.Shuffler;
import com.cubeia.poker.hand.StandardDeck;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.RevealOrderCalculator;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.rounds.ante.AnteRound;
import com.cubeia.poker.rounds.betting.ActionRequestFactory;
import com.cubeia.poker.rounds.betting.BetStrategy;
import com.cubeia.poker.rounds.betting.BetStrategyFactory;
import com.cubeia.poker.rounds.betting.BettingRound;
import com.cubeia.poker.rounds.betting.BettingRoundName;
import com.cubeia.poker.rounds.betting.DefaultPlayerToActCalculator;
import com.cubeia.poker.rounds.blinds.BlindsRound;
import com.cubeia.poker.rounds.dealing.DealCommunityCardsRound;
import com.cubeia.poker.rounds.dealing.DealExposedPocketCardsRound;
import com.cubeia.poker.rounds.dealing.DealPocketCardsRound;
import com.cubeia.poker.rounds.dealing.Dealer;
import com.cubeia.poker.rounds.dealing.ExposePrivateCardsRound;
import com.cubeia.poker.rounds.discard.DiscardRound;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.variant.AbstractGameType;
import com.cubeia.poker.variant.HandResultCreator;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.cubeia.poker.rounds.betting.BettingRoundName.NOTHING;
import static com.cubeia.poker.rounds.betting.BettingRoundName.PRE_FLOP;
import static com.cubeia.poker.rounds.betting.BettingRoundName.RIVER;

public class TexasHoldem extends AbstractGameType implements RoundVisitor, Dealer {

    private static final long serialVersionUID = -1523110440727681601L;

    private static transient Logger log = Logger.getLogger(TexasHoldem.class);

//    private Deck deck;

    private BettingRoundName roundName = NOTHING;

    private Round currentRound;

    private final TexasHoldemHandCalculator handEvaluator = new TexasHoldemHandCalculator();

    private HandResultCalculator handResultCalculator = new HandResultCalculator(handEvaluator);

    private RevealOrderCalculator revealOrderCalculator;

    public TexasHoldem() {
        revealOrderCalculator = new RevealOrderCalculator();
    }

    @Override
    public String toString() {
        return "TexasHoldem, current round[" + currentRound + "] round[" + roundName + "] ";
    }

    @Override
    public void startHand() {
        initHand();
    }

    private void initHand() {
        context.setDeck(new StandardDeck(new Shuffler<Card>(getServerAdapter().getSystemRNG()), new IndexCardIdGenerator()));
        currentRound = new BlindsRound(context, serverAdapterHolder, new BlindsCalculator(new NonRandomSeatProvider()));
        roundName = NOTHING;
    }

    @Override
    public boolean act(PokerAction action) {
        boolean handled = currentRound.act(action);
        checkFinishedRound();
        return handled;
    }

    private void checkFinishedRound() {
        if (currentRound.isFinished()) {
            handleFinishedRound();
        }
    }

    private void dealPocketCards(PokerPlayer p, int n) {
        log.debug("Dealing cards to " + p.getId());
        for (int i = 0; i < n; i++) {
            p.addPocketCard(context.getDeck().deal(), false);
        }
        getServerAdapter().notifyPrivateCards(p.getId(), p.getPocketCards().getCards());
    }

    public void handleFinishedRound() {
        currentRound.visit(this);
    }

    private void reportPotUpdate() {
        notifyPotAndRakeUpdates(Collections.<PotTransition>emptyList());
    }

    private void startBettingRound(int seatIdToStartBettingFrom) {
        roundName = roundName.next();
        log.debug("Starting new betting round. Round ID: " + roundName);
        currentRound = createBettingRound(seatIdToStartBettingFrom);
    }

    private BettingRound createBettingRound(int seatIdToStartBettingFrom) {
        DefaultPlayerToActCalculator playerToActCalculator = new DefaultPlayerToActCalculator(seatIdToStartBettingFrom);
        PokerSettings settings = context.getSettings();
        BetStrategy betStrategy = BetStrategyFactory.createBetStrategy(settings.getBetStrategyType(), settings.getBigBlindAmount(),
                                                                       roundName.isDoubleBetRound());
        ActionRequestFactory requestFactory = new ActionRequestFactory(betStrategy);
        TexasHoldemFutureActionsCalculator futureActionsCalculator = new TexasHoldemFutureActionsCalculator(betStrategy.getType());
        return new BettingRound(context, serverAdapterHolder, playerToActCalculator, requestFactory, futureActionsCalculator, betStrategy);
    }

    private boolean isHandFinished() {
        return (roundName == RIVER || context.countNonFoldedPlayers() <= 1);
    }

    private int getCardsToDeal() {
        if (roundName == PRE_FLOP) {
            return 3;
        } else {
            return 1;
        }
    }

    @Override
    public void dealExposedPocketCards() {
        // TODO: Delete.
    }

    @Override
    public void dealInitialPocketCards() {
        // This is used in Telesina but not Hold'em, please unify.
    }

    @Override
    public void exposeShowdownCards(List<Integer> playerRevealOrder) {
        // TODO: Delete.
    }

    private void handleCanceledHand() {
        notifyHandFinished(new HandResult(context.getSettings().getCurrency()), HandEndStatus.CANCELED_TOO_FEW_PLAYERS);
    }

    private void moveChipsToPot() {

        context.getPotHolder().moveChipsToPotAndTakeBackUncalledChips(context.getCurrentHandSeatingMap().values());

        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            p.setHasActed(false);
            p.clearActionRequest();
        }
    }

    @Override
    public void requestMultipleActions(Collection<ActionRequest> requests) {
        throw new UnsupportedOperationException("sending multiple action requests not implemented");
    }

    @Override
    public void scheduleRoundTimeout() {
        log.debug("scheduleRoundTimeout in: " + context.getTimingProfile().getTime(Periods.RIVER));
        getServerAdapter().scheduleTimeout(context.getTimingProfile().getTime(Periods.RIVER));
    }

    @Override
    public void prepareNewHand() {
        context.getCommunityCards().clear();
        for (PokerPlayer player : context.getCurrentHandPlayerMap().values()) {
            player.clearHand();
            player.setHasFolded(false);
        }
    }

    @Override
    public void timeout() {
        log.debug("Timeout");
        currentRound.timeout();
        checkFinishedRound();
    }

    @Override
    public String getStateDescription() {
        return currentRound == null ? "th-round=null" : currentRound.getClass() + "_" + currentRound.getStateDescription();
    }

    @Override
    public void visit(BettingRound bettingRound) {
        moveChipsToPot();
        reportPotUpdate();

        if (isHandFinished()) {
            handleFinishedHand();
        } else if (allInShowdown()) {
            log.debug("All-in showdown, exposing pocket cards.");
            currentRound = new ExposePrivateCardsRound(context, serverAdapterHolder, revealOrderCalculator);
            scheduleRoundTimeout();
        } else {
            // Start deal community cards round
            currentRound = new DealCommunityCardsRound(context, serverAdapterHolder, getCardsToDeal());
        }
    }

    private boolean allInShowdown() {
        return (context.isAtLeastAllButOneAllIn() && !context.haveAllPlayersExposedCards());
    }

    @VisibleForTesting
    void handleFinishedHand() {
        List<Integer> playerRevealOrder = calculateRevealOrder();

        currentRound = new ExposePrivateCardsRound(context, serverAdapterHolder, revealOrderCalculator);
        Set<PokerPlayer> muckingPlayers = context.getMuckingPlayers();
        HandResult handResult = new HandResultCreator(new TexasHoldemHandCalculator()).createHandResult(
                context.getCommunityCards(), handResultCalculator, context.getPotHolder(), context.getCurrentHandPlayerMap(),
                playerRevealOrder, muckingPlayers, context.getSettings().getCurrency());

        notifyHandFinished(handResult, HandEndStatus.NORMAL);
        context.getPotHolder().clearPots();
    }

    private List<Integer> calculateRevealOrder() {
        PokerPlayer playerAtDealerButton = context.getPlayerInDealerSeat();
        return revealOrderCalculator.calculateRevealOrder(context.getCurrentHandSeatingMap(), context.getLastPlayerToBeCalled(), playerAtDealerButton, context.countNonFoldedPlayers());
    }

    @Override
    public void visit(ExposePrivateCardsRound exposePrivateCardsRound) {
        // Cards were flipped, deal next card.
        currentRound = new DealCommunityCardsRound(context, serverAdapterHolder, getCardsToDeal());
    }

    @Override
    public void visit(DiscardRound discardRound) {
    }

    @Override
    public void visit(AnteRound anteRound) {
    }

    @Override
    public void visit(BlindsRound blindsRound) {
        if (blindsRound.isCanceled()) {
            handleCanceledHand();
        } else {
            updateBlindsInfo(blindsRound);
            dealPocketCards();
            startBettingRound(context.getBlindsInfo().getBigBlindSeatId());
        }
    }

    @Override
    public void visit(DealCommunityCardsRound round) {
        updateHandStrengths();
        startBettingRound(context.getBlindsInfo().getDealerButtonSeatId());
    }

    private void updateHandStrengths() {
        for (PokerPlayer player : context.getCurrentHandSeatingMap().values()) {
            if (player.hasFolded()) {
                continue;
            }
            Hand hand = new Hand();
            hand.addCards(player.getPocketCards().getCards());
            hand.addCards(context.getCommunityCards());
            HandInfo handInfo = handEvaluator.getBestHandInfo(hand);
            if (handInfo.getCards() == null) {
                log.warn("Cards in best hand is null for player " + player + " pocket cards: "
                         + player.getPocketCards().getCards() + " community: " + context.getCommunityCards());
            }
            serverAdapterHolder.get().notifyBestHand(player.getId(), handInfo.getHandType(), handInfo.getCards(), false);
        }
    }

    @Override
    public void visit(DealExposedPocketCardsRound round) {
        throw new UnsupportedOperationException(round.getClass().getSimpleName() + " round not allowed in Texas Holdem");
    }

    @Override
    public void visit(DealPocketCardsRound round) {
        throw new UnsupportedOperationException(round.getClass().getSimpleName() + " round not allowed in Texas Holdem");
    }

    private void updateBlindsInfo(BlindsRound blindsRound) {
        context.setBlindsInfo(blindsRound.getBlindsInfo());
    }

    private void dealPocketCards() {
        log.debug("Dealing pocket cards.");
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            /*
             * Note, not checking if the player is sitting in. If he was sitting in at hand start (and thus ended up in the current hand seating map),
             * he should still be sitting in. Any player who declined the entry bet should also already have been removed from this map.
             */
            dealPocketCards(p, 2);
        }
        updateHandStrengths();
    }

    @Override
    // TODO: Implement for Texas Hold'em.
    public void sendAllNonFoldedPlayersBestHand() {
        log.warn("Implement sendAllNonFoldedPlayersBestHand for Texas Hold'em.");
    }

    @Override
    public boolean canPlayerAffordEntryBet(PokerPlayer player, PokerSettings settings, boolean includePending) {
        BigDecimal pendingBalance = includePending ? player.getPendingBalanceSum() : BigDecimal.ZERO;
        return player.getBalance().add(pendingBalance ).compareTo(settings.getBigBlindAmount())>= 0;
    }
}
