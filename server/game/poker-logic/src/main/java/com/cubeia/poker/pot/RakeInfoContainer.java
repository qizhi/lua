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

import com.cubeia.poker.pot.Pot;

import java.math.BigDecimal;
import java.util.Map;

public class RakeInfoContainer {

    private final BigDecimal totalPot;
    private final BigDecimal totalRake;
    private final Map<Pot, BigDecimal> potRakes;

    public RakeInfoContainer(BigDecimal totalPot, BigDecimal totalRake, Map<Pot, BigDecimal> potRakes) {
        super();
        this.totalPot = totalPot;
        this.totalRake = totalRake;
        this.potRakes = potRakes;
    }

    public BigDecimal getTotalPot() {
        return totalPot;
    }

    public BigDecimal getTotalRake() {
        return totalRake;
    }

    public Map<Pot, BigDecimal> getPotRakes() {
        return potRakes;
    }

    @Override
    public String toString() {
        return "RakeInfoContainer [totalPot=" + totalPot + ", totalRake=" + totalRake + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RakeInfoContainer that = (RakeInfoContainer) o;

        if (potRakes != null ? !potRakes.equals(that.potRakes) : that.potRakes != null) return false;
        if (totalPot != null ? !totalPot.equals(that.totalPot) : that.totalPot != null) return false;
        if (totalRake != null ? !totalRake.equals(that.totalRake) : that.totalRake != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = totalPot != null ? totalPot.hashCode() : 0;
        result = 31 * result + (totalRake != null ? totalRake.hashCode() : 0);
        result = 31 * result + (potRakes != null ? potRakes.hashCode() : 0);
        return result;
    }
}
