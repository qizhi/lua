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

package com.cubeia.games.poker.tournament.configuration.payouts;

import com.cubeia.games.poker.common.money.Currency;
import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

/**
 * A PayoutStructure holds information about how much of the pot each player should get, given how may players were in the tournament.
 * Note that the structure holds the entire table of payouts, so when a tournament starts, we get they payouts to use in that specific tournament
 * depending on the number of entrants.
 */
@Entity
public class PayoutStructure implements Serializable {

    private static final Logger log = Logger.getLogger(PayoutStructure.class);

    @Id
    @GeneratedValue
    private int id;

    @OneToMany(fetch = LAZY, cascade = ALL)
    @OrderColumn
    private List<Payouts> payoutsPerEntryRange;

    private String name;

    PayoutStructure() {
    }

    public PayoutStructure(List<Payouts> payouts) {
        this.payoutsPerEntryRange = payouts;
    }

    public Payouts getPayoutsForEntrantsAndPrizePool(int numberOfEntrants, BigDecimal prizePool, Currency currency) {
        for (Payouts payout : payoutsPerEntryRange) {
            if (payout.inRange(numberOfEntrants)) {
                return payout.withPrizePool(prizePool, currency);
            }
        }
        Payouts payouts = payoutsPerEntryRange.get(payoutsPerEntryRange.size() - 1).withPrizePool(prizePool,currency);
        log.warn("No payouts defined for " + numberOfEntrants + " entrants. Using the last payouts: " + payouts);
        return payouts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Payouts> getPayoutsPerEntryRange() {
        return payoutsPerEntryRange;
    }

    void setPayoutsPerEntryRange(List<Payouts> payoutsPerEntryRange) {
        this.payoutsPerEntryRange = payoutsPerEntryRange;
    }

    @Override
    public String toString() {
        return "PayoutStructure{" +
                "payouts=" + payoutsPerEntryRange +
                '}';
    }

    public void verify() {
        for (Payouts payoutsRange : payoutsPerEntryRange) {
            payoutsRange.verify();
        }
    }
}
