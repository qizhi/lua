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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

/**
 * Defines the payouts for a given range of entrants.
 */
@Entity
public class Payouts implements Serializable {

    private static final Logger log = Logger.getLogger(Payouts.class);

    @Id
    @GeneratedValue
    private int id;

    /**
     * The range of entrants these payouts are applicable for.
     */
    @ManyToOne(fetch = EAGER, cascade = ALL)
    private IntRange entrantsRange;

    /** The size of the prize pool. */
    private BigDecimal prizePool;

    private Currency currency;

    /** A list of payouts, each payout defines a percentage for a range of positions in the tournament. */
    @OneToMany(fetch = EAGER, cascade = ALL)
    @OrderColumn
    private List<Payout> payoutList = new ArrayList<Payout>();

    Payouts() {
    }

    public Payouts(IntRange entrants, List<Payout> payouts, BigDecimal prizePool, Currency currency) {
        this.entrantsRange = entrants;
        this.payoutList = payouts;
        this.prizePool = prizePool;
        this.currency = currency;
    }

    public Payouts(IntRange entrants, List<Payout> payouts) {
        this(entrants, payouts, BigDecimal.ZERO,null);
    }

    public boolean inRange(int numberOfEntrants) {
        return entrantsRange.contains(numberOfEntrants);
    }

    public Payouts withPrizePool(BigDecimal prizePool, Currency currency) {
        return new Payouts(entrantsRange, payoutList, prizePool, currency);
    }

    public BigDecimal getPayoutsForPosition(int position) {
        for (Payout payout : payoutList) {
            if (payout.getPositionRange().contains(position)) {
                return payout.getPercentage().multiply(prizePool).divide(new BigDecimal(100),currency.getFractionalDigits(), RoundingMode.DOWN);
            }
        }
        return BigDecimal.ZERO;
    }

    public IntRange getEntrantsRange() {
        return entrantsRange;
    }

    void setEntrantsRange(IntRange entrantsRange) {
        this.entrantsRange = entrantsRange;
    }

    BigDecimal getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(BigDecimal prizePool) {
        this.prizePool = prizePool;
    }

    public List<Payout> getPayoutList() {
        return payoutList;
    }

    public int getNumberOfPlacesInTheMoney() {
        int max = 0;
        for (Payout payout : payoutList) {
            int upperBound = payout.getPositionRange().getStop();
            if (upperBound > max) {
                max = upperBound;
            }
        }
        return max;
    }

    public void setPayoutList(List<Payout> payoutList) {
        this.payoutList = payoutList;
    }

    @Override
    public String toString() {
        return "Payouts{" +
                "entrantsRange=" + entrantsRange +
                ", prizePool=" + prizePool +
                ", payouts=" + payoutList +
                '}';
    }

    public void verify() {
        BigDecimal sum = BigDecimal.valueOf(0);
        for (Payout payout : payoutList) {
            sum = sum.add(payout.getTotalPayoutPercentage());
        }
        if (!between(99.9, 100.0, sum)) {
            throw new IllegalStateException("Sum was not 100, but: " + sum + " for range: " + entrantsRange + ".");
        }
        log.debug("Verified payouts for range: " + entrantsRange + ". Sum = "+ sum);
    }

    private boolean between(double lower, double upper, BigDecimal sum) {
        return sum.compareTo(BigDecimal.valueOf(lower)) >= 0 && sum.compareTo(BigDecimal.valueOf(upper)) <= 0;
    }

    public Currency getCurrency() {
        return this.currency;
    }

}
