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
import com.cubeia.poker.hand.HandInfo;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.handhistory.api.BestHandType;
import com.cubeia.poker.handhistory.api.GameCard;
import com.cubeia.poker.handhistory.api.PlayerBestHand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RatedPlayerHand implements Serializable {

    private static final long serialVersionUID = 1L;

    private PlayerHand playerHand;
    private HandInfo handInfo;
    private List<Card> bestHandCards;

    public RatedPlayerHand(PlayerHand playerHand, HandInfo handInfo, List<Card> bestHandCards) {
        this.playerHand = playerHand;
        this.handInfo = handInfo;
        this.bestHandCards = bestHandCards;
    }

    public static List<PlayerBestHand> translateBestHands(Collection<RatedPlayerHand> hands) {
        List<PlayerBestHand> list = new ArrayList<PlayerBestHand>(hands.size());
        for (RatedPlayerHand c : hands) {
            list.add(c.translate());
        }
        return list;
    }

    public PlayerBestHand translate() {
        List<Card> cards = getBestHandCards();
        List<GameCard> bestHandCards = new ArrayList<GameCard>(cards.size());
        for (Card c : cards) {
            bestHandCards.add(c.translate());
        }
        return new PlayerBestHand(getPlayerHand().translate(), getHandInfo().translate(), bestHandCards);
    }

    public HandInfo getHandInfo() {
        return handInfo;
    }

    public PlayerHand getPlayerHand() {
        return playerHand;
    }

    public Integer getPlayerId() {
        return playerHand.getPlayerId();
    }

    public Hand getHand() {
        return playerHand.getHand();
    }

    public List<Card> getBestHandCards() {
        return bestHandCards;
    }

    @Override
    public String toString() {
        return "RatedPlayerHand{" +
                "playerHand=" + playerHand +
                ", handInfo=" + handInfo +
                ", bestHandCards=" + bestHandCards +
                '}';
    }
}