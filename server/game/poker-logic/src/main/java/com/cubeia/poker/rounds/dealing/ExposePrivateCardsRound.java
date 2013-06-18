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
import com.cubeia.poker.hand.ExposeCardsHolder;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.RevealOrderCalculator;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExposePrivateCardsRound implements Round {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = LoggerFactory.getLogger(ExposePrivateCardsRound.class);
    private final PokerContext context;
    private final ServerAdapterHolder serverAdapterHolder;

    public ExposePrivateCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, RevealOrderCalculator revealOrderCalculator) {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        exposeShowdownCards(revealOrderCalculator.calculateRevealOrder(
                context.getCurrentHandSeatingMap(),
                context.getLastPlayerToBeCalled(),
                context.getPlayerInDealerSeat(),
                context.countNonFoldedPlayers()));
    }

    /**
     * Exposes all pocket cards for players still in the hand
     * i.e. not folded. Will set a flag so that sequential calls
     * will not generate any outgoing packets.
     *
     * @param playerRevealOrder the order in which cards should be revealed.
     */
    private void exposeShowdownCards(List<Integer> playerRevealOrder) {
        ExposeCardsHolder holder = new ExposeCardsHolder();
        for (int playerId : playerRevealOrder) {
            PokerPlayer player = context.getPlayer(playerId);
            if (player == null) {
                log.error("Player is null in expose showdown! playerId: " + playerId + " playerMap: " + context.getPlayerMap().values() + " seatingMap: " +
                        context.getCurrentHandSeatingMap() + " reveal order: " + playerRevealOrder);
                continue;
            }
            if (!player.hasFolded() && !player.isExposingPocketCards()) {
                holder.setExposedCards(playerId, player.getPrivatePocketCards());
                player.setExposingPocketCards(true);
            }
        }
        if (holder.hasCards()) {
            exposePrivateCards(holder);
        }
    }

    private void exposePrivateCards(ExposeCardsHolder holder) {
        serverAdapterHolder.get().exposePrivateCards(holder);
    }

    @Override
    public boolean act(PokerAction action) {
        log.debug("Perform action not allowed during DealPocketCardsRound. Action received: " + action);
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
