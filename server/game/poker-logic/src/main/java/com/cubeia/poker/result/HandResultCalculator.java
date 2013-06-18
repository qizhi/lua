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
package com.cubeia.poker.result;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.hand.HandTypeEvaluator;
import com.cubeia.poker.model.PlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.util.PlayerHandComparator;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


import static java.util.Arrays.asList;

public class HandResultCalculator implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HandTypeEvaluator handEvaluator;


    public HandResultCalculator(HandTypeEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    /**
     * Gets a map mapping the results of each player.
     * <p/>
     * The results are the winnings or losses in the hand.
     * <p/>
     * Rake is taken before calculating the result.
     * <p/>
     * For example, if player A, B and C bet $10 each and player C won, the results will be:
     * A->-$10, B->-$10, C->$20
     *
     * @param hands     the hands of the non folded players
     * @param potHolder pot holder
     * @return results map
     */
    public Map<PokerPlayer, Result> getPlayerResults(Collection<PlayerHand> hands, PotHolder potHolder,
                                                     RakeInfoContainer rakeInfoContainer, Map<Integer, PokerPlayer> playerMap,
                                                     Currency currency) {

        int fractions = currency.getFractionalDigits();
        BigDecimal minFractionValue = BigDecimal.ONE.divide(BigDecimal.TEN.pow(fractions),2, RoundingMode.DOWN);

        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();

        // Player ID to Net result (including own bets)
        Map<Integer, BigDecimal> netResults = new HashMap<Integer, BigDecimal>();
        Map<Integer, BigDecimal> netStakes = new HashMap<Integer, BigDecimal>();
        Map<PokerPlayer, Map<Pot, BigDecimal>> playerPotWinningsShares = new HashMap<PokerPlayer, Map<Pot, BigDecimal>>();

        /*
         * For each pot we need to figure out:
         *
         * 1. Participating players
         * 2. Winner(s) of the participating players
         * 3. Net winnings and net losses for the participating players
         *
         * Net winnings and losses are aggregated over all pots since
         * a player can participate in more than one pot.
         *
         * Finally we construct a result DTO for each player and return them
         * as a map.
         */
        for (Pot pot : potHolder.getPots()) {
            // Get participating players
            Map<PokerPlayer, BigDecimal> participants = pot.getPotContributors();
            Map<PokerPlayer, BigDecimal> potContributors = pot.getPotContributors();

            List<Integer> participantIds = new ArrayList<Integer>();
            for (PokerPlayer player : participants.keySet()) {
                participantIds.add(player.getId());
            }

            // We need to remove all non-participants first
            Collection<PlayerHand> filteredHands = filter(hands, participantIds);

            // Check if we have participant hands first, if the hand was canceled this may be empty
            if (filteredHands.size() > 0) {

                // --- WINNERS ---
                List<Integer> winners = getWinners(filteredHands);

                BigDecimal potRake = rakeInfoContainer.getPotRakes().get(pot);
                BigDecimal potSizeWithRakeRemoved = pot.getPotSize().subtract(potRake);

                BigDecimal potShare = potSizeWithRakeRemoved.divide(new BigDecimal(winners.size()), fractions, BigDecimal.ROUND_DOWN);
                BigDecimal extraCents = potSizeWithRakeRemoved.subtract(potShare.multiply(new BigDecimal(winners.size()))).setScale(fractions,RoundingMode.DOWN);

                // Report winner shares
                for (Integer winnerId : winners) {
                    PokerPlayer player = playerMap.get(winnerId);
                    BigDecimal stake = potContributors.get(player);
                    BigDecimal playerWinnings = potShare;

                    if (extraCents.compareTo(BigDecimal.ZERO) > 0) {
                        playerWinnings = playerWinnings.add(minFractionValue);
                        extraCents = extraCents.subtract(minFractionValue);
                    }
                    addResultBalance(netResults, netStakes, winnerId, playerWinnings, stake);
                    addPotWinningShare(player, pot, playerWinnings, playerPotWinningsShares);
                }

                // --- LOSERS ---
                // Report loser losses
                participantIds.removeAll(winners);
                for (Integer loserId : participantIds) {
                    PokerPlayer player = playerMap.get(loserId);
                    BigDecimal stake = pot.getPotContributors().get(player);
                    addResultBalance(netResults, netStakes, loserId, BigDecimal.ZERO, stake);
                }
            }
        }

        // Create result DTO's for the net results over all Pots
        for (Integer playerId : playerMap.keySet()) {
            PokerPlayer player = playerMap.get(playerId);

            BigDecimal netResult = netResults.get(playerId);
            BigDecimal playerStake = netStakes.get(playerId);

            BigDecimal netResultSafe = netResult == null ? BigDecimal.ZERO : netResult;
            BigDecimal playerStakeSafe = playerStake == null ? BigDecimal.ZERO : playerStake;

            Map<Pot, BigDecimal> potShares = playerPotWinningsShares.get(player);
            if (potShares == null) {
                potShares = Collections.<Pot, BigDecimal>emptyMap();
            }

            Result result = new Result(netResultSafe, playerStakeSafe, potShares);
            results.put(player, result);
        }
        return results;
    }

    /**
     * Add the given players winning share of the given pot to the holding data structure (nested maps).
     *
     * @param player                  player
     * @param pot                     the pot
     * @param potShare                the player's share of the pot
     * @param playerPotWinningsShares data holder
     */
    private void addPotWinningShare(PokerPlayer player, Pot pot,
                                    BigDecimal potShare, Map<PokerPlayer, Map<Pot, BigDecimal>> playerPotWinningsShares) {
        if (!playerPotWinningsShares.containsKey(player)) {
            playerPotWinningsShares.put(player, new HashMap<Pot, BigDecimal>());
        }

        playerPotWinningsShares.get(player).put(pot, potShare);
    }

    protected Collection<PlayerHand> filter(Collection<PlayerHand> hands, final Collection<Integer> players) {
        return Collections2.filter(hands, new Predicate<PlayerHand>() {
            @Override
            public boolean apply(PlayerHand hand) {
                return players.contains(hand.getPlayerId());
            }
        });
    }

    private void addResultBalance(Map<Integer, BigDecimal> netResults, Map<Integer, BigDecimal> netStakes, Integer playerId, BigDecimal winnings, BigDecimal stake) {
        BigDecimal balance = netResults.get(playerId);
        if (balance == null) {
            netResults.put(playerId, winnings.subtract(stake));
        } else {
            netResults.put(playerId, balance.add(winnings).subtract(stake));
        }

        BigDecimal totalStake = netStakes.get(playerId);
        if (totalStake == null) {
            netStakes.put(playerId, stake);
        } else {
            netStakes.put(playerId, totalStake.add(stake));
        }
    }

    private List<Integer> getWinners(Collection<PlayerHand> hands) {
        List<Integer> winners = new ArrayList<Integer>();
        List<PlayerHand> copy = new LinkedList<PlayerHand>(hands);
        Comparator<PlayerHand> handComparator = Collections.reverseOrder(new PlayerHandComparator(handEvaluator.createHandComparator(hands.size())));
        Collections.sort(copy, handComparator);

        if (copy.size() == 1) {
            // Only one hand, there can be only one winner.
            return asList(copy.get(0).getPlayerId());
        } else {
            PlayerHand strongestHand = copy.get(0);

            for (PlayerHand hand : copy) {
                Integer pid = hand.getPlayerId();

                if (handComparator.compare(strongestHand, hand) == 0) {
                    winners.add(pid);
                }
            }

            return winners;
        }
    }
}
