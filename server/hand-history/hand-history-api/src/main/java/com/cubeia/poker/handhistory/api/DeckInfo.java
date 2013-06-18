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

import com.cubeia.poker.handhistory.api.GameCard.Rank;

public class DeckInfo implements Serializable {

    private static final long serialVersionUID = 8729177028359067170L;
	
    private int size;
    private Rank lowRank;

    public DeckInfo() {
    }

    public DeckInfo(int size, Rank lowRank) {
        this.size = size;
        this.lowRank = lowRank;
    }

    public Rank getLowRank() {
        return lowRank;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Deck size: " + size + "; Low rank: " + lowRank;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lowRank == null) ? 0 : lowRank.hashCode());
        result = prime * result + size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeckInfo other = (DeckInfo) obj;
        if (lowRank != other.lowRank)
            return false;
        if (size != other.size)
            return false;
        return true;
    }
}
