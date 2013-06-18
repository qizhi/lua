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
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
public class IntRange implements Serializable, Comparable<IntRange> {

    @Id
    @GeneratedValue
    private int id;

    private int start;

    private int stop;

    IntRange() {
    }

    public IntRange(int start, int stop) {
        this.start = start;
        this.stop = stop;
    }

    public boolean contains(int value) {
        return value >= start && value <= stop;
    }

    public int getStart() {
        return start;
    }

    void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    void setStop(int stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return "IntRange{" +
                "start=" + start +
                ", stop=" + stop +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntRange intRange = (IntRange) o;

        if (start != intRange.start) {
            return false;
        }
        if (stop != intRange.stop) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + stop;
        return result;
    }

    public int size() {
        return stop - start + 1;
    }

    @Override
    public int compareTo(IntRange intRange) {
        return start - intRange.start;
    }
}
