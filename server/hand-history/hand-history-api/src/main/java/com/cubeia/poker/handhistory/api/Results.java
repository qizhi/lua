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

package com.cubeia.poker.handhistory.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Results implements Serializable {

    private static final long serialVersionUID = 5742358497502861176L;

    private BigDecimal totalRake;
    private Map<Integer, HandResult> results = new HashMap<Integer, HandResult>();

    public Results(BigDecimal totalRake) {
        this.totalRake = totalRake;
    }

    public Results() {
    }

    public BigDecimal getTotalRake() {
        return totalRake;
    }

    public void setTotalRake(BigDecimal totalRake) {
        this.totalRake = totalRake;
    }

    public Map<Integer, HandResult> getResults() {
        return results;
    }

    public void setResults(Map<Integer, HandResult> results) {
        this.results = results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Results results1 = (Results) o;

        if (results != null ? !results.equals(results1.results) : results1.results != null) return false;
        if (totalRake != null ? !totalRake.equals(results1.totalRake) : results1.totalRake != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = totalRake != null ? totalRake.hashCode() : 0;
        result = 31 * result + (results != null ? results.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Results{" +
                "totalRake=" + totalRake +
                ", results=" + results +
                '}';
    }
}
