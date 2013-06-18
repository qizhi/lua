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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

/**
 * Defines the payout percentage for a given range of positions.
 *
 */
@Entity
public class Payout implements Serializable {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = EAGER, cascade = ALL)
    private IntRange positionRange;

    private BigDecimal percentage;

    Payout() {
    }

    public Payout(IntRange positionRange, BigDecimal percentage) {
        this.positionRange = positionRange;
        this.percentage = percentage;
    }

    /**
     * Gets the total payout percentage for this range. Used for validating payout structures.
     * For example, if position 6-10 get 2% of the pot each, this method will return 10.
     *
     * @return the total payout percentage for this range
     */
    public BigDecimal getTotalPayoutPercentage() {
        return percentage.multiply(BigDecimal.valueOf(positionRange.size()));
    }

    public IntRange getPositionRange() {
        return positionRange;
    }

    void setPositionRange(IntRange positionRange) {
        this.positionRange = positionRange;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "Payout{" +
                "positionRange=" + positionRange +
                ", percentage=" + percentage +
                '}';
    }
}
