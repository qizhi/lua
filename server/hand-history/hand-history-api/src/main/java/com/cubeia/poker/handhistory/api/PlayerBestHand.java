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
import java.util.List;

public class PlayerBestHand  extends HandHistoryEvent implements Serializable {

    private static final long serialVersionUID = -5191267888404550951L;

    private FullHand playerHand;
    private HandInfoCommon handInfoCommon;
    private List<GameCard> bestHandCards;

    public PlayerBestHand() {
    }

    public PlayerBestHand(FullHand fullHand, HandInfoCommon handInfoCommon, List<GameCard> bestHandCards) {
        this.playerHand = fullHand;
        this.handInfoCommon = handInfoCommon;
        this.bestHandCards = bestHandCards;
    }

    public FullHand getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(FullHand playerHand) {
        this.playerHand = playerHand;
    }

    public HandInfoCommon getHandInfoCommon() {
        return handInfoCommon;
    }

    public void setHandInfoCommon(HandInfoCommon handInfoCommon) {
        this.handInfoCommon = handInfoCommon;
    }

    public List<GameCard> getBestHandCards() {
        return bestHandCards;
    }

    public void setBestHandCards(List<GameCard> bestHandCards) {
        this.bestHandCards = bestHandCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerBestHand that = (PlayerBestHand) o;

        if (bestHandCards != null ? !bestHandCards.equals(that.bestHandCards) : that.bestHandCards != null)
            return false;
        if (handInfoCommon != null ? !handInfoCommon.equals(that.handInfoCommon) : that.handInfoCommon != null)
            return false;
        if (playerHand != null ? !playerHand.equals(that.playerHand) : that.playerHand != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (playerHand != null ? playerHand.hashCode() : 0);
        result = 31 * result + (handInfoCommon != null ? handInfoCommon.hashCode() : 0);
        result = 31 * result + (bestHandCards != null ? bestHandCards.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayerBestHand{" +
                "playerHand=" + playerHand +
                ", handInfoCommon=" + handInfoCommon +
                ", bestHandCards=" + bestHandCards +
                '}';
    }
}
