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

public class DealExposedPocketCardsRound implements Round {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = LoggerFactory.getLogger(DealExposedPocketCardsRound.class);
    private final PokerContext context;
    private final ServerAdapterHolder serverAdapterHolder;

    public DealExposedPocketCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        dealExposedPocketCards();
        RoundHelper roundHelper = new RoundHelper(context, serverAdapterHolder);
        roundHelper.scheduleRoundTimeout(context, serverAdapterHolder.get(), Periods.POCKET_CARDS);
    }

    private void dealExposedPocketCards() {
        for (PokerPlayer p : context.getCurrentHandSeatingMap().values()) {
            if (!p.hasFolded()) {
                dealExposedPocketCards(p, 1);
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
        log.info("Perform action not allowed during DealExposedPocketCardsRound. Action received: " + action);
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
