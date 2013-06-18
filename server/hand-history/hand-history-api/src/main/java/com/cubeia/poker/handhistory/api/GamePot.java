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
import java.util.HashSet;
import java.util.Set;

public class GamePot implements Serializable {

    private static final long serialVersionUID = -5210827391459111003L;

    private int potId;
    private Set<Integer> players = new HashSet<Integer>();

    private BigDecimal potSize;

    public GamePot() {
    }

    public GamePot(int potId) {
        this.potId = potId;
    }

    public GamePot(int potId, BigDecimal potSize, Integer... plyrs) {
        this.potId = potId;
        this.potSize = potSize;
        for (int id : plyrs) {
            players.add(id);
        }
    }

    public int getPotId() {
        return potId;
    }

    public Set<Integer> getPlayers() {
        return players;
    }

    public BigDecimal getPotSize() {
        return potSize;
    }

    public void setPotSize(BigDecimal potSize) {
        this.potSize = potSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GamePot gamePot = (GamePot) o;

        if (potId != gamePot.potId) return false;
        if (players != null ? !players.equals(gamePot.players) : gamePot.players != null) return false;
        if (potSize != null ? !potSize.equals(gamePot.potSize) : gamePot.potSize != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = potId;
        result = 31 * result + (players != null ? players.hashCode() : 0);
        result = 31 * result + (potSize != null ? potSize.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GamePot [potId=" + potId + ", players=" + players + ", potSize=" + potSize + "]";
    }
}
