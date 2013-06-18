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

import java.util.LinkedList;
import java.util.List;

public class PotUpdate extends HandHistoryEvent {

    private static final long serialVersionUID = -7029713511615986370L;

    private List<GamePot> pots = new LinkedList<GamePot>();

    public PotUpdate() {
    }

    public PotUpdate(GamePot... pots) {
        for (GamePot p : pots) {
            this.pots.add(p);
        }
    }

    public List<GamePot> getPots() {
        return pots;
    }

    public void setPots(List<GamePot> pots) {
        this.pots = pots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PotUpdate potUpdate = (PotUpdate) o;

        if (pots != null ? !pots.equals(potUpdate.pots) : potUpdate.pots != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pots != null ? pots.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PotUpdate{" +
                "pots=" + pots +
                '}';
    }
}
