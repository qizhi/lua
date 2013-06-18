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

package com.cubeia.poker.variant;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.hand.DeckProvider;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandInfo;
import com.cubeia.poker.hand.HandTypeEvaluator;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.RevealOrderCalculator;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundCreator;
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
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GenericPokerGame extends AbstractGameType implements RoundVisitor {

    private static final long serialVersionUID = -1523110440727681601L;

    private static transient Logger log = Logger.getLogger(GenericPokerGame.class);

    private Round currentRound;

    private final HandTypeEvaluator handEvaluator;

    private HandResultCalculator handResultCalculator;

    private RevealOrderCalculator revealOrderCalculator;

    /**
     * A list of <code>RoundCreator</code>s capable of creating the rounds in this game.
     * Ordered in the order in which the rounds should be played.
     */
    private List<RoundCreator> roundCreators;

    /**
     * An iterator pointing to the current round.
     */
    private Iterator<RoundCreator> rounds;

    private DeckProvider deckProvider;

    public GenericPokerGame(List<RoundCreator> roundCreators, DeckProvider deckProvider, HandTypeEvaluator handEvaluator) {
        this.roundCreators = roundCreators;
        this.deckProvider = deckProvider;
        this.handEvaluator = handEvaluator;
        handResultCalculator = new HandResultCalculator(handEvaluator);
        revealOrderCalculator = new RevealOrderCalculator();
    }

    @Override
    public String toString() {
        return "Current round[" + currentRound + "] ";
    }

    @Override
    public void startHand() {
        initHand();
    }

    private void initHand() {
        rounds = roundCreators.iterator();
        context.setDeck(deckProvider.createNewDeck(serverAdapterHolder.get().getSystemRNG(), context.getTableSize()));
        currentRound = rounds.next().create(context, serverAdapterHolder);
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

    public void handleFinishedRound() {
        currentRound.visit(this);
        if (isHandFinished()) {
            handleFinishedHand();
        } else if (allInShowdown()) {
            currentRound = new ExposePrivateCardsRound(context, serverAdapterHolder, revealOrderCalculator);
            scheduleRoundTimeout();
        } else {
            currentRound = rounds.next().create(context, serverAdapterHolder);
        }
    }

    private void reportPotUpdate() {
        notifyPotAndRakeUpdates(Collections.<PotTransition>emptyList());
    }

    private boolean isHandFinished() {
        return (!rounds.hasNext() || context.countNonFoldedPlayers() <= 1);
    }

    private void handleCanceledHand() {
        notifyHandFinished(new HandResult(context.getSettings().getCurrency()), HandEndStatus.CANCELED_TOO_FEW_PLAYERS);
    }

    private void handleFinishedBetting() {
        moveChipsToPot();
        resetHasActed();
    }

    private void moveChipsToPot() {
        context.getPotHolder().moveChipsToPotAndTakeBackUncalledChips(context.getCurrentHandSeatingMap().values());
    }

    private void resetHasActed() {
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
        handleFinishedBetting();
        reportPotUpdate();
    }

    private boolean allInShowdown() {
        return (context.isAtLeastAllButOneAllIn() && !context.haveAllPlayersExposedCards());
    }

    @VisibleForTesting
    public void handleFinishedHand() {
        List<Integer> playerRevealOrder = calculateRevealOrder();

        currentRound = new ExposePrivateCardsRound(context, serverAdapterHolder, revealOrderCalculator);
        Set<PokerPlayer> muckingPlayers = context.getMuckingPlayers();
        HandResult handResult = new HandResultCreator(new TexasHoldemHandCalculator()).createHandResult(context.getCommunityCards(),
                handResultCalculator, context.getPotHolder(), context.getCurrentHandPlayerMap(), playerRevealOrder, muckingPlayers,context.getSettings().getCurrency());

        notifyHandFinished(handResult, HandEndStatus.NORMAL);
        context.getPotHolder().clearPots();
    }

    private List<Integer> calculateRevealOrder() {
        PokerPlayer playerAtDealerButton = context.getPlayerInDealerSeat();
        return revealOrderCalculator.calculateRevealOrder(context.getCurrentHandSeatingMap(), context.getLastPlayerToBeCalled(), playerAtDealerButton, context.countNonFoldedPlayers());
    }

    @Override
    public void visit(ExposePrivateCardsRound exposePrivateCardsRound) {
        log.debug("Finished exposing private cards.");
        updateHandStrengths();
    }

    @Override
    public void visit(DiscardRound discardRound) {
        log.debug("Discard round finished.");
        resetHasActed();
    }

    @Override
    public void visit(AnteRound anteRound) {
        log.debug("Ante round finished.");
        if (anteRound.isCanceled()) {
            handleCanceledHand();
        } else {
            handleFinishedBetting();
        }
    }

    @Override
    public void visit(BlindsRound blindsRound) {
        if (blindsRound.isCanceled()) {
            handleCanceledHand();
        } else {
            updateBlindsInfo(blindsRound);
        }
    }

    @Override
    public void visit(DealCommunityCardsRound round) {
        updateHandStrengths();
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
        updateHandStrengths();
    }

    @Override
    public void visit(DealPocketCardsRound round) {
        log.debug("Finished dealing pocket cards");
        updateHandStrengths();
    }

    private void updateBlindsInfo(BlindsRound blindsRound) {
        context.setBlindsInfo(blindsRound.getBlindsInfo());
    }

    @Override
    public boolean canPlayerAffordEntryBet(PokerPlayer player, PokerSettings settings, boolean includePending) {
        BigDecimal pendingBalance = includePending ? player.getPendingBalanceSum() : BigDecimal.ZERO;
        return player.getBalance().add(pendingBalance).compareTo(settings.getBigBlindAmount()) >= 0;
    }

    public void setRevealOrderCalculator(RevealOrderCalculator revealOrderCalculator) {
        this.revealOrderCalculator = revealOrderCalculator;
    }
}
