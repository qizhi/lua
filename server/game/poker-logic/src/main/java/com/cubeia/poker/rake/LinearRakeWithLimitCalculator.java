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

package com.cubeia.poker.rake;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.RakeCalculator;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.settings.RakeSettings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * Rake calculator where rake is linear (defined by a fraction) up to a limit after which
 * no more rake is taken.
 *
 * @author w
 */
@SuppressWarnings("serial")
public class LinearRakeWithLimitCalculator implements RakeCalculator {

    private RakeSettings settings;
    private Currency currency;

    public LinearRakeWithLimitCalculator(RakeSettings rakeSettings, Currency currency) {
        this.settings = rakeSettings;
        this.currency = currency;
    }

    @Override
    public RakeInfoContainer calculateRakes(Collection<Pot> pots, boolean tableHasSeenAction) {
        Map<Pot, BigDecimal> potRake = new HashMap<Pot, BigDecimal>();

        int playersCount = countPlayers(pots);
        BigDecimal limit = settings.getRakeLimit(playersCount);

        List<Pot> potsSortedById = sortPotsInIdOrder(pots);

        BigDecimal totalRake = BigDecimal.ZERO.setScale(currency.getFractionalDigits());
        BigDecimal totalPot = BigDecimal.ZERO.setScale(currency.getFractionalDigits());

        for (Pot pot : potsSortedById) {
            BigDecimal potSize = pot.getPotSize();

            BigDecimal rake = BigDecimal.ZERO.setScale(currency.getFractionalDigits());
            if (tableHasSeenAction) {
                rake = settings.getRakeFraction(playersCount).multiply(potSize);
                if (willRakeAdditionBreakLimit(totalRake, rake, limit)) {
                    rake = limit.subtract(totalRake);
                }
                rake = rake.setScale(currency.getFractionalDigits(),RoundingMode.DOWN);
                totalRake = totalRake.add(rake);
            }

            totalPot = totalPot.add(potSize);
            potRake.put(pot, rake);
        }

        return new RakeInfoContainer(totalPot, totalRake, potRake);
    }

    private int countPlayers(Collection<Pot> pots) {
        HashSet<PokerPlayer> players = new HashSet<PokerPlayer>();
        for (Pot pot : pots) {
            players.addAll(pot.getPotContributors().keySet());
        }
        return players.size();
    }

    /**
     * Returns a new list where the pots are ordered by ascending pot id.
     *
     * @param pots pots to sort
     * @return new sorted list
     */
    private List<Pot> sortPotsInIdOrder(Collection<Pot> pots) {
        List<Pot> potsSortedById = new ArrayList<Pot>(pots);
        Collections.sort(potsSortedById, new Comparator<Pot>() {
            @Override
            public int compare(Pot p1, Pot p2) {
                return p1.getId() - p2.getId();
            }
        });
        return potsSortedById;
    }

    private boolean willRakeAdditionBreakLimit(BigDecimal totalRake, BigDecimal rakeAddition, BigDecimal limit) {
        return totalRake.add(rakeAddition).compareTo(limit)> 0;
    }

    @Override
    public String toString() {
        return "LinearRakeWithLimitCalculator{" +
                "settings=" + settings +
                '}';
    }
}
