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

package com.cubeia.poker.pot;

import com.cubeia.poker.player.PokerPlayer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * holds all the active pots for a table
 */
public class PotHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PotHolder.class);
    /**
     * Holds all pots.
     */
    private List<Pot> pots = new ArrayList<Pot>();

    private Set<Integer> allInPlayers = new HashSet<Integer>();

    @VisibleForTesting
    protected boolean callOrRaiseHasBeenMadeInHand = false;

    private final RakeCalculator rakeCalculator;

    public PotHolder(RakeCalculator rakeCalculator) {
        this.rakeCalculator = rakeCalculator;
    }

    /**
     * Moves chips to the pot.
     * split the chips into side pots if we have all-ins
     * return unmatched chips to the player.
     *
     * @param players a collection of players
     */
    public Collection<PotTransition> moveChipsToPotAndTakeBackUncalledChips(Collection<PokerPlayer> players) {

        Collection<PotTransition> potTransitions = new ArrayList<PotTransition>();

        // Maps player's to the bet they made.
        Map<PokerPlayer, BigDecimal> playerToBetMap = new HashMap<PokerPlayer, BigDecimal>();

        // Tree set of all-in levels.
        SortedSet<BigDecimal> allInLevels = new TreeSet<BigDecimal>();

        // First, return any uncalled chips.
        Collection<PotTransition> returnedChipsTransitions = returnUnCalledChips(players);
        potTransitions.addAll(returnedChipsTransitions);

        // Add all bets to the map and check if we have all-ins.
        for (PokerPlayer player : players) {

            // Exclude players who are already all-in.
            if (player.isAllIn() && !allInPlayers.contains(player.getId())) {
                allInLevels.add(player.getBetStack());
                allInPlayers.add(player.getId());
            }

            // Exclude players who did not bet.
            if (player.getBetStack().compareTo(BigDecimal.ZERO) > 0) {
                playerToBetMap.put(player, player.getBetStack());
            }

        }

        if (!allInLevels.isEmpty()) {
            // There are all-ins, split them up into side pots.
            handleAllIns(playerToBetMap, allInLevels, potTransitions);
        }

        // The remaining chips are placed in the active pot.
        for (Map.Entry<PokerPlayer, BigDecimal> entry : playerToBetMap.entrySet()) {
            BigDecimal stack = entry.getValue();
            if (stack.compareTo(BigDecimal.ZERO) > 0) {
                potTransitions.add(new PotTransition(entry.getKey(), getActivePot(), stack));
                getActivePot().bet(entry.getKey(), stack);
                entry.getKey().removeFromBetStack(stack);
            }
        }

        printDiagnostics();

        return potTransitions;
    }

    public RakeInfoContainer calculateRake() {
        return rakeCalculator.calculateRakes(getPots(), callOrRaiseHasBeenMadeInHand);
    }

    public BigDecimal calculatePlayersContributionToPotIncludingBetStacks(PokerPlayer player) {
        BigDecimal tot = BigDecimal.ZERO;

        // calculate totat contribution to pot
        for (Pot pot : pots) {
            Map<PokerPlayer, BigDecimal> potContributors = pot.getPotContributors();

            if (potContributors.containsKey(player)) {
                tot = tot.add(potContributors.get(player));
            }
        }

        // add the players betstack
        tot = tot.add(player.getBetStack());

        return tot;
    }

    /**
     * Calculate the rake info including all the bet stacks of the players
     *
     * @param players
     * @return
     */
    public RakeInfoContainer calculateRakeIncludingBetStacks(Collection<PokerPlayer> players) {
        Collection<Pot> allPots = new ArrayList<Pot>(getPots());

        Pot betPot = new Pot(Integer.MAX_VALUE);

        for (PokerPlayer player : players) {
            betPot.bet(player, player.getBetStack());
        }

        allPots.add(betPot);

        return rakeCalculator.calculateRakes(allPots, callOrRaiseHasBeenMadeInHand);


    }

    private void printDiagnostics() {
        log.debug("pots: ");

        RakeInfoContainer rakeInfoContainer = calculateRake();

        if (rakeInfoContainer != null) {
            for (Map.Entry<Pot, BigDecimal> entry : rakeInfoContainer.getPotRakes().entrySet()) {
                Pot pot = entry.getKey();
                BigDecimal rake = entry.getValue();

                Collection<Integer> playerIds = Collections2.transform(pot.getPotContributors().keySet(), new Function<PokerPlayer, Integer>() {
                    public Integer apply(PokerPlayer pp) {
                        return pp.getId();
                    }

                    ;
                });
                log.debug("  pot {}: bets = {}, rake = {}, open = {}, players: {}",
                          new Object[]{pot.getId(), pot.getPotSize(), rake, pot.isOpen(), playerIds});
            }

            log.debug("{}, total pot size = {}, total rake = {}",
                      new Object[]{rakeCalculator, rakeInfoContainer.getTotalPot(), rakeInfoContainer.getTotalRake()});
        }
    }

    /**
     * Returns uncalled chips.
     *
     * @param players
     * @return
     */
    public Collection<PotTransition> returnUnCalledChips(Iterable<PokerPlayer> players) {
        PokerPlayer biggestBetter = getBiggestBetter(players);
        PokerPlayer secondBiggestBetter = getBiggestBetter(players, biggestBetter);

        ArrayList<PotTransition> transitions = new ArrayList<PotTransition>();

        try {
            if (biggestBetter.getBetStack().compareTo(secondBiggestBetter.getBetStack()) > 0 ) {
                BigDecimal returnedChips = biggestBetter.getBetStack().subtract(secondBiggestBetter.getBetStack());
                biggestBetter.returnBetStackAmountToBalance(returnedChips);

                PotTransition potTransition = PotTransition.createTransitionFromBetStackToPlayer(biggestBetter, returnedChips);
                transitions.add(potTransition);

                log.debug("returning " + returnedChips + " uncalled chips to " + biggestBetter);
            }
        } catch (NullPointerException e) {
            // FIXME: Tournaments get this exception
            log.warn("FIXME: Should not be nullpointer here! -> PotHolder.returnUnCalledChips()", e);
        }

        return transitions;
    }

    /**
     * Gets the biggest better in this round, possibly excluding one player.
     *
     * @param players
     * @param excludedPlayer the player to exclude, may be null
     * @return the biggest better
     */
    private PokerPlayer getBiggestBetter(Iterable<PokerPlayer> players, PokerPlayer excludedPlayer) {
        BigDecimal biggestBet = new BigDecimal(-1);
        PokerPlayer biggestBetter = null;

        for (PokerPlayer player : players) {
            if (player.getBetStack().compareTo(biggestBet) > 0) {
                if (!player.equals(excludedPlayer)) {
                    biggestBet = player.getBetStack();
                    biggestBetter = player;
                }
            }
        }
        return biggestBetter;
    }

    /**
     * Gets the biggest better in this round.
     *
     * @param players
     * @return
     */
    private PokerPlayer getBiggestBetter(Iterable<PokerPlayer> players) {
        return getBiggestBetter(players, null);
    }

    /**
     * Handles all-in bets by splitting them up into side pots.
     *
     * @param betMap         a map of all bets, mapping the player to the amount bet
     * @param allInLevels    sorted set of the different levels where players went all-in
     * @param potTransitions
     */
    private void handleAllIns(Map<PokerPlayer, BigDecimal> betMap, SortedSet<BigDecimal> allInLevels, Collection<PotTransition> potTransitions) {
        BigDecimal currentLevel = BigDecimal.ZERO;

        /*
           * Go through each all-in level and add chips from all players who still
           * have chips.
           */
        for (BigDecimal allInLevel : allInLevels) {
            BigDecimal diff = allInLevel.subtract(currentLevel);

            Pot activePot = getActivePot();
            for (Map.Entry<PokerPlayer, BigDecimal> entry : betMap.entrySet()) {
                PokerPlayer player = entry.getKey();
                BigDecimal stack = entry.getValue();
                if (stack.compareTo(diff) >= 0) {
                    potTransitions.add(new PotTransition(player, activePot, diff));
                    activePot.bet(player, diff);
                    player.removeFromBetStack(diff);
                    betMap.put(player, (stack.subtract(diff)));
                } else if (stack.compareTo(BigDecimal.ZERO) > 0) {
                    /*
                     * If a player has folded, he might not have enough chips,
                     * add the remaining chips in this pot.
                     */
                    potTransitions.add(new PotTransition(player, activePot, stack));
                    activePot.bet(player, stack);
                    player.removeFromBetStack(stack);
                    betMap.put(player, BigDecimal.ZERO);
                }
            }
            // Close the pot, so no more bets can be placed in the pot.
            activePot.close();

            // Update the current level.
            currentLevel = allInLevel;
        }
    }

    /**
     * Gets the active pot.
     * <p/>
     * Creates a new pot if there are no pots.
     *
     * @return the active pot, or a newly created pot if there were no pots
     */
    public Pot getActivePot() {
        if (pots.size() == 0 || !pots.get(pots.size() - 1).isOpen()) {
            Pot pot = new Pot(pots.size());
            pots.add(pot);
        }
        return (pots.get(pots.size() - 1));
    }

    /**
     * Gets the number of pots.
     *
     * @return the number of pots
     */
    public int getNumberOfPots() {
        return pots.size();
    }

    /**
     * Gets the total pot size. That is the sum of the pot size
     * in all pots plus the rake.
     *
     * @return the total pot size
     */
    public BigDecimal getTotalPotSize() {
        BigDecimal total = BigDecimal.ZERO;
        for (Pot pot : pots) {
            total = total.add(pot.getPotSize());
        }

        return total;
    }

    /**
     * Gets the pot size of a specific pot.
     *
     * @param i the number of the pot
     * @return the pot size in the i:th pot
     */
    public BigDecimal getPotSize(int i) {
        return pots.get(i).getPotSize();
    }

    /**
     * Gets the i:th pot.
     *
     * @param i the number of the pot
     * @return the i:th pot
     */
    public Pot getPot(int i) {
        if (pots.size() > i) {
            return pots.get(i);
        } else {
            return new Pot(i);
        }
    }

    /**
     * Iterates over the pots.
     *
     * @return
     */
    public Collection<Pot> getPots() {
        return pots;
    }

    /**
     * Indicate that a call or a raise has been made by some player in this hand. This method must be
     * invoked at least when the first call or raise is made in a hand for the rake calculation to be correct.
     */
    public void callOrRaise() {
        callOrRaiseHasBeenMadeInHand = true;
    }

    /**
     * Adds a pot of size potSize.
     *
     * @param pot
     */
    @VisibleForTesting
    protected void addPot(Pot pot) {
        pots.add(pot);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int potIndex = 0;
        for (Pot pot : pots) {
            b.append("Pot: " + ++potIndex + "=" + pot.getPotSize() + " ");
        }
        return b.toString();
    }

    public void clearPots() {
        pots.clear();
    }

}
