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

package com.cubeia.poker.model;

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.handhistory.api.FullHand;
import com.cubeia.poker.handhistory.api.GameCard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerHand implements Serializable {
    private static final long serialVersionUID = 8327782333044163208L;

    private final Integer playerId;
    private final Hand hand;

    public PlayerHand(Integer playerId, Hand hand) {
        this.playerId = playerId;
        this.hand = hand;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public Hand getHand() {
        return hand;
    }

    public FullHand translate() {
        List<Card> cards = getHand().getCards();
        List<GameCard> gameCards = new ArrayList<GameCard>(cards.size());
        for (Card c : cards) {
            gameCards.add(c.translate());
        }
        return new FullHand(getPlayerId(), gameCards);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hand == null) ? 0 : hand.hashCode());
        result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
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
        PlayerHand other = (PlayerHand) obj;
        if (hand == null) {
            if (other.hand != null)
                return false;
        } else if (!hand.equals(other.hand))
            return false;
        if (playerId == null) {
            if (other.playerId != null)
                return false;
        } else if (!playerId.equals(other.playerId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PlayerHand - PlayerId[" + playerId + "] Hand[" + hand + "]";
    }
}
