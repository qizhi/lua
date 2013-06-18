/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.rounds.discard;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.DiscardAction;
import com.cubeia.poker.action.DiscardRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;
import com.cubeia.poker.rounds.betting.PlayerToActCalculator;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.List;

public class DiscardRound implements Round {

    private static final Logger log = Logger.getLogger(DiscardRound.class);
    private final PokerContext context;
    private final ServerAdapterHolder serverAdapterHolder;
    private final PlayerToActCalculator playerToActCalculator;
    private final int cardsToDiscard;
    private int playerToAct;
    private boolean forceDiscard;

    public DiscardRound(PokerContext context,
                        ServerAdapterHolder serverAdapterHolder,
                        PlayerToActCalculator playerToActCalculator,
                        int cardsToDiscard,
                        boolean forceDiscard)  {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        this.playerToActCalculator = playerToActCalculator;
        this.cardsToDiscard = cardsToDiscard;
        this.forceDiscard = forceDiscard;
        initRound();
    }

    private void initRound() {
        resetHasActed();
        requestFirstAction();
    }

    private void resetHasActed() {
        for (PokerPlayer pokerPlayer : context.getPlayersInHand()) {
            pokerPlayer.setHasActed(false);
        }
    }

    private void requestFirstAction() {
        // Check if we should request actions at all
        PokerPlayer playerToAct = playerToActCalculator.getFirstPlayerToAct(context.getCurrentHandSeatingMap(), context.getCommunityCards());

        if (playerToAct == null) {
            throw new RuntimeException("No player can discard, something is wrong. Players: " + context.getCurrentHandSeatingMap().values());
        } else {
            requestAction(playerToAct);
        }
    }

    private void requestAction(PokerPlayer player) {
        playerToAct = player.getId();
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.enable(new DiscardRequest(cardsToDiscard));
        actionRequest.setPlayerId(player.getId());
        serverAdapterHolder.get().requestAction(actionRequest);
    }

    @Override
    public boolean act(PokerAction action) {
        PokerPlayer player = context.getPlayer(action.getPlayerId());
        if (action instanceof DiscardAction && isValidAction(action, player)) {
            DiscardAction discard = (DiscardAction) action;
            log.debug("Player " + player.getId() + " discards: " + discard.getCardsToDiscard());
            player.setHasActed(true);
            player.discard(discard.getCardsToDiscard());
            if (!isFinished()) {
                requestNextAction(player.getSeatId());
            }
            return true;
        } else {
            return false;
        }
    }

    private void requestNextAction(int lastActedSeatId) {
        PokerPlayer nextPlayerToAct = playerToActCalculator.getNextPlayerToAct(lastActedSeatId, context.getCurrentHandSeatingMap());
        requestAction(nextPlayerToAct);
    }

    private boolean isValidAction(PokerAction action, PokerPlayer player) {
        if (!action.getPlayerId().equals(playerToAct)) {
            log.warn("Expected " + playerToAct + " to act, but got action from:" + player.getId());
            return false;
        }
        return true;
    }

    @Override
    public void timeout() {
        PokerPlayer player = context.getPlayer(playerToAct);
        if (forceDiscard) {
            List<Integer> forcedCardsToDiscard = Lists.newArrayList();
            for (int i = 0; i < this.cardsToDiscard; i++) {
                forcedCardsToDiscard.add(i);
            }
            player.discard(forcedCardsToDiscard);
        }
        if (!isFinished()) {
            requestNextAction(player.getSeatId());
        }
    }

    @Override
    public boolean isFinished() {
        for (PokerPlayer player : context.getPlayersInHand()) {
            if (!player.hasActed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void visit(RoundVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getStateDescription() {
        return null;
    }
}
