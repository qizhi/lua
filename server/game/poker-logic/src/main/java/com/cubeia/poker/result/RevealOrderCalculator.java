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

package com.cubeia.poker.result;

import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.util.PokerUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class RevealOrderCalculator implements Serializable {

    /**
     * Calculates the order in which the players should reveal their hidden card(s).
     * If there's only one non folded player left, an empty list will be returned.
     *
     * The general rules is: the last player to be called will be the first player to reveal. The remaining
     * players reveal clockwise from that player. If there's no "last player to be called" (everyone checked),
     * then we start revealing from the first player after the dealer.
     *
     * @param currentHandSeatingMap seating map
     * @param lastPlayerToBeCalled  last called player, can be null
     * @param dealerButtonPlayer    player at the dealer button, never null
     * @param nonFoldedPlayers      the number of non folded players in the hand
     * @return list of player id:s, never null
     */
    public List<Integer> calculateRevealOrder(SortedMap<Integer, PokerPlayer> currentHandSeatingMap, PokerPlayer lastPlayerToBeCalled, PokerPlayer dealerButtonPlayer, int nonFoldedPlayers) {
        if (nonFoldedPlayers <= 1) return new ArrayList<Integer>();
        Integer startPlayerSeat;

        if (lastPlayerToBeCalled != null) {
            startPlayerSeat = getSeatByPlayer(currentHandSeatingMap, lastPlayerToBeCalled);
        } else {
            ArrayList<PokerPlayer> playerList = new ArrayList<PokerPlayer>(currentHandSeatingMap.values());
            int dealerButtonPlayerIndex = playerList.indexOf(dealerButtonPlayer);
            if (dealerButtonPlayerIndex == playerList.size() - 1) {
                startPlayerSeat = getSeatByPlayer(currentHandSeatingMap, playerList.get(0));
            } else {
                startPlayerSeat = getSeatByPlayer(currentHandSeatingMap, playerList.get(dealerButtonPlayerIndex + 1));
            }
        }

        List<PokerPlayer> sortedPlayerList = PokerUtils.unwrapList(currentHandSeatingMap, startPlayerSeat);

        List<Integer> playerIdList = new ArrayList<Integer>();
        for (PokerPlayer player : sortedPlayerList) {
            if (!player.hasFolded()) {
                playerIdList.add(player.getId());
            }
        }

        return playerIdList;
    }

    private Integer getSeatByPlayer(SortedMap<Integer, PokerPlayer> currentHandSeatingMap, PokerPlayer lastPlayerToBeCalled) {
        for (Map.Entry<Integer, PokerPlayer> entry : currentHandSeatingMap.entrySet()) {
            if (entry.getValue().equals(lastPlayerToBeCalled)) {
                return entry.getKey();
            }
        }
        return null;
    }


}
