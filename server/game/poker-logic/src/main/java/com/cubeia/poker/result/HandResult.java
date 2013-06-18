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
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * The result of a hand. This class maps the player to the resulting win/lose amount of the hand.
 */
public class HandResult implements Serializable {

    private static transient Logger log = LoggerFactory.getLogger(HandResult.class);

    private static final long serialVersionUID = -7802386310901901021L;

    private final Map<PokerPlayer, Result> results;

    private final List<RatedPlayerHand> playerHands;

    private final List<Integer> playerRevealOrder;

    private final Collection<PotTransition> potTransitions;

    private final Currency currency;

    private Map<PokerPlayer, BigDecimal> rakeContributions;

    private final BigDecimal totalRake;

    public HandResult(Currency currency) {
        this(Collections.<PokerPlayer, Result>emptyMap(), Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), null,
                Collections.<Integer>emptyList(), currency);
    }

    public HandResult(Map<PokerPlayer, Result> results, List<RatedPlayerHand> playerHands, Collection<PotTransition> potTransitions,
            RakeInfoContainer rakeInfoContainer, List<Integer> playerRevealOrder, Currency currency) {
        this.currency = currency;
        this.totalRake = (rakeInfoContainer == null ? new BigDecimal(-1) : rakeInfoContainer.getTotalRake());
        this.results = unmodifiableMap(results);
        this.playerHands = unmodifiableList(playerHands);
        this.playerRevealOrder = unmodifiableList(playerRevealOrder);
        this.potTransitions = Collections.unmodifiableCollection(potTransitions);
        this.rakeContributions = rakeInfoContainer == null ? Collections.<PokerPlayer, BigDecimal>emptyMap() : calculateRakeContributions(rakeInfoContainer,
                results);

    }

    /**
     * Returns the order the players should reveal their hidden cards in.
     *
     * @return list of player id:s, never null
     */
    public List<Integer> getPlayerRevealOrder() {
        return playerRevealOrder;
    }

    public List<RatedPlayerHand> getPlayerHands() {
        return playerHands;
    }

    public Map<PokerPlayer, Result> getResults() {
        return results;
    }

    public BigDecimal getTotalRake() {
        return totalRake;
    }

    /**
     * Calculate the rake contribution by player.
     * The sum of all rake contributions equals the total rake taken for this hand.
     * The rake contribution for a player is calculated as:
     * <code>
     * contrib = total_rake * player_bets / total_bets
     * </code>
     *
     * @return player to rake contribution map
     */
    private Map<PokerPlayer, BigDecimal> calculateRakeContributions(RakeInfoContainer rakeInfoContainer, Map<PokerPlayer, Result> results) {
        Map<PokerPlayer, BigDecimal> rakeContribs = new HashMap<PokerPlayer, BigDecimal>();

        BigDecimal totalBetsBD = rakeInfoContainer.getTotalPot();

        BigDecimal totalContribution = BigDecimal.ZERO;

        for (Map.Entry<PokerPlayer, Result> e : results.entrySet()) {
            PokerPlayer player = e.getKey();
            Result result = e.getValue();
            BigDecimal playerBets = result.getBets();
            BigDecimal rakeContrib = BigDecimal.ZERO;
            if (totalBetsBD.compareTo(BigDecimal.ZERO) != 0) {
                // Calculate rake per player, but avoid dividing by zero.
                rakeContrib = rakeInfoContainer.getTotalRake().multiply(playerBets).divide(totalBetsBD, currency.getFractionalDigits(), RoundingMode.DOWN);
            }
            totalContribution = totalContribution.add(rakeContrib);
            // Note: Here we floor the rake contribution
            rakeContribs.put(player, rakeContrib);
        }

        // Since above rake contributions were floored, we might have extra cents to distribute.
        if (totalContribution.compareTo(rakeInfoContainer.getTotalRake()) != 0) {
            // One "cent" is really one "minFraction": 0.0000001 if fractionalDigits = 7
            BigDecimal minFractionValue = BigDecimal.ONE.movePointLeft(currency.getFractionalDigits());
            if (log.isDebugEnabled()) {
                log.debug("totalContribution is " + totalContribution + " and totalRake is " + rakeInfoContainer.getTotalRake());
            }
            // Iterate over the players, until we have distributed all cents. We couldn't have more than numberOfPlayers cents to distribute, because maths (mod and stuff).
            for (Map.Entry<PokerPlayer, Result> e : results.entrySet()) {
                PokerPlayer player = e.getKey();
                if (log.isDebugEnabled()) {
                    log.debug("\t-> adding rake to player " + player.getId());
                }
                if (totalContribution.compareTo(rakeInfoContainer.getTotalRake()) == 0) {
                    break;
                }
                rakeContribs.put(player, rakeContribs.get(player).add(minFractionValue));
                totalContribution = totalContribution.add(minFractionValue);

            }
        }
        return rakeContribs;
    }

    public BigDecimal getRakeContributionByPlayer(PokerPlayer player) {
        return rakeContributions.get(player);
    }

    public Collection<PotTransition> getPotTransitions() {
        return potTransitions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RatedPlayerHand rph : playerHands) {
            sb.append("Player ");
            sb.append(rph.getPlayerId());
            sb.append(" best hand: ");
            sb.append(cardsToString(rph.getBestHandCards()));
            sb.append(". ");
        }
        return "HandResult results[" + results + "] Hands: " + sb.toString();
    }

    private String cardsToString(List<Card> bestHandCards) {
        StringBuilder sb = new StringBuilder();
        if (bestHandCards != null) {
            for (Card card : bestHandCards) {
                sb.append(card.toString());
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    public Currency getCurrency() {
        return currency;
    }

}
