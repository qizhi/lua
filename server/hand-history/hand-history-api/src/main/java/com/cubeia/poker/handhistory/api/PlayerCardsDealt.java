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

public class PlayerCardsDealt extends HandHistoryEvent {

    private static final long serialVersionUID = 2419782176272291069L;
	
    private int playerId;
    private List<GameCard> cards = new LinkedList<GameCard>();
    private boolean isExposed;

    public PlayerCardsDealt() {
    }

    public PlayerCardsDealt(int playerId, boolean isExposed) {
        this.playerId = playerId;
        this.isExposed = isExposed;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<GameCard> getCards() {
        return cards;
    }

    public void setCards(List<GameCard> cards) {
        this.cards = cards;
    }

    public boolean isExposed() {
        return isExposed;
    }

    public void setExposed(boolean exposed) {
        isExposed = exposed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerCardsDealt that = (PlayerCardsDealt) o;

        if (isExposed != that.isExposed) return false;
        if (playerId != that.playerId) return false;
        if (cards != null ? !cards.equals(that.cards) : that.cards != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + playerId;
        result = 31 * result + (cards != null ? cards.hashCode() : 0);
        result = 31 * result + (isExposed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayerCardsDealt{" +
                "playerId=" + playerId +
                ", cards=" + cards +
                ", isExposed=" + isExposed +
                '}';
    }
}
