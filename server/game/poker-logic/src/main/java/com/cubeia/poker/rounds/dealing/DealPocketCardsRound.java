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

package com.cubeia.poker.rounds.dealing;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundHelper;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.timing.Periods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Round for dealing pocket cards.
 *
 */
public class DealPocketCardsRound implements Round {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = LoggerFactory.getLogger(DealPocketCardsRound.class);
    private final PokerContext context;
    private final ServerAdapterHolder serverAdapterHolder;

    public DealPocketCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, int numberOfFaceDownCards, int numberOfFaceUpCards) {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        RoundHelper roundHelper = new RoundHelper(context, serverAdapterHolder);
        if (numberOfFaceDownCards > 0) {
            dealFaceDownPocketCards(numberOfFaceDownCards);
        }
        if (numberOfFaceUpCards > 0) {
            dealFaceUpPocketCards(numberOfFaceUpCards);
        }
        roundHelper.scheduleRoundTimeout(context, serverAdapterHolder.get(), Periods.POCKET_CARDS);
    }

    public DealPocketCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, int numberOfFaceDownCards) {
        this(context, serverAdapterHolder, numberOfFaceDownCards, 0);
    }

    private void dealFaceDownPocketCards(int numberOfCardsToDeal) {
        log.debug("Dealing pocket cards.");
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            /*
             * Note, not checking if the player is sitting in. If he was sitting in at hand start (and thus ended up in the current hand seating map),
             * he should still be sitting in. Any player who declined the entry bet should also already have been removed from this map.
             */
            dealPocketCards(p, numberOfCardsToDeal);
        }
    }

    private void dealPocketCards(PokerPlayer p, int n) {
        log.debug("Dealing cards to " + p.getId());
        for (int i = 0; i < n; i++) {
            p.addPocketCard(context.getDeck().deal(), false);
        }
        serverAdapterHolder.get().notifyPrivateCards(p.getId(), p.getPocketCards().getCards());
    }

    public void dealFaceUpPocketCards(int cardsToDeal) {
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            if (!p.hasFolded()) {
                dealExposedPocketCards(p, cardsToDeal);
            }
        }
    }

    private void dealExposedPocketCards(PokerPlayer player, int n) {
        ArrayList<Card> cardsDealt = new ArrayList<Card>();
        for (int i = 0; i < n; i++) {
            Card card = context.getDeck().deal();
            cardsDealt.add(card);
            player.addPocketCard(card, true);
        }
        log.debug("notifying all users of private exposed cards to {}: {}", player.getId(), cardsDealt);
        serverAdapterHolder.get().notifyPrivateExposedCards(player.getId(), cardsDealt);
    }

    @Override
    public boolean act(PokerAction action) {
        log.info("Perform action not allowed during DealInitialPocketCardsRound. Action received: " + action);
        return false;
    }

    @Override
    public String getStateDescription() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void visit(RoundVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void timeout() {
    }

}
