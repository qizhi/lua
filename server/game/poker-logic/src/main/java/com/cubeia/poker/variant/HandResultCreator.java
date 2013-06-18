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

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandInfo;
import com.cubeia.poker.hand.HandTypeEvaluator;
import com.cubeia.poker.model.PlayerHand;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.util.ThreadLocalProfiler;
import com.google.common.annotations.VisibleForTesting;

import java.math.BigDecimal;

import java.util.*;
import java.util.Map.Entry;

public class HandResultCreator {

    private HandTypeEvaluator hte;

    public HandResultCreator(HandTypeEvaluator hte) {
        this.hte = hte;
    }

    public HandResult createHandResult(List<Card> communityCards, HandResultCalculator handResultCalculator, PotHolder potHolder,
                                       Map<Integer, PokerPlayer> currentHandPlayerMap, List<Integer> playerRevealOrder,
                                       Set<PokerPlayer> muckingPlayers, Currency currency) {

        ThreadLocalProfiler.add("HandTypeEvaluator.createHandResult.start");

        RakeInfoContainer calculatedRake = potHolder.calculateRake();
        List<PlayerHand> playerHands = createHandsList(communityCards, currentHandPlayerMap.values());
        Map<PokerPlayer, Result> playerResults = handResultCalculator.getPlayerResults(playerHands, potHolder, calculatedRake, currentHandPlayerMap, currency);
        Collection<PotTransition> potTransitions = createPotTransitionsByResults(playerResults);

        playerHands = filterOutMuckedPlayerHands(playerHands, muckingPlayers);
        List<RatedPlayerHand> ratedHands = rateHands(playerHands);
        HandResult handResult = new HandResult(playerResults, ratedHands, potTransitions, calculatedRake, playerRevealOrder,currency);

        ThreadLocalProfiler.add("HandTypeEvaluator.createHandResult.stop");

        return handResult;
    }

    protected List<PlayerHand> filterOutMuckedPlayerHands(List<PlayerHand> playerHands, Set<PokerPlayer> muckingPlayers) {
        List<PlayerHand> filteredList = new ArrayList<PlayerHand>();
        for (PlayerHand playerHand : playerHands) {
            boolean shouldShowHand = true;
            for (PokerPlayer player : muckingPlayers) {
                if (playerHand.getPlayerId() == player.getId()) {
                    shouldShowHand = false;
                    break;
                }
            }
            if (shouldShowHand) {
                filteredList.add(playerHand);
            }
        }

        return filteredList;
    }

    @VisibleForTesting
    protected Collection<PotTransition> createPotTransitionsByResults(Map<PokerPlayer, Result> playerResults) {
        Collection<PotTransition> potTransitions = new ArrayList<PotTransition>();
        for (Entry<PokerPlayer, Result> entry : playerResults.entrySet()) {
            PokerPlayer player = entry.getKey();
            for (Entry<Pot, BigDecimal> potShare : entry.getValue().getWinningsByPot().entrySet()) {
                potTransitions.add(new PotTransition(player, potShare.getKey(), potShare.getValue()));
            }
        }
        return potTransitions;
    }

    /**
     * Creates a list of participating hands for the non folded players.
     *
     * @param communityCards community cards
     * @param players        players
     * @return hands for all non folded players
     */
    private List<PlayerHand> createHandsList(List<Card> communityCards, Collection<PokerPlayer> players) {
        ArrayList<PlayerHand> playerHands = new ArrayList<PlayerHand>();

        for (PokerPlayer player : players) {
            if (!player.hasFolded()) {
                Hand h = new Hand();
                h.addCards(player.getPocketCards().getCards());
                h.addCards(communityCards);
                playerHands.add(new PlayerHand(player.getId(), h));
            }
        }

        return playerHands;
    }

    private List<RatedPlayerHand> rateHands(List<PlayerHand> hands) {
        List<RatedPlayerHand> result = new LinkedList<RatedPlayerHand>();

        for (PlayerHand hand : hands) {
            HandInfo bestHandInfo = hte.getBestHandInfo(hand.getHand());
            result.add(new RatedPlayerHand(hand, bestHandInfo, bestHandInfo.getCards()));
        }

        return result;
    }
}