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

package com.cubeia.poker;

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.states.ShutdownSTM;
import com.cubeia.poker.variant.GameType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public interface IPokerState {

    void init(GameType gameType, PokerSettings settings);

    void playerSitsOutNextHand(int playerId);

    BigDecimal getAnteLevel();

    Map<Integer, PokerPlayer> getCurrentHandPlayerMap();

    SortedMap<Integer, PokerPlayer> getCurrentHandSeatingMap();

    /**
     * Returns a player participating in the current hand (or the last played hand if waiting to start) by its id.
     * NOTE: that this method might return a player even after the hand is finished.
     *
     * @param playerId player id
     * @return player or null if not in hand
     */
    PokerPlayer getPlayerInCurrentHand(Integer playerId);

    boolean isPlayerInHand(int playerId);

    List<Card> getCommunityCards();

    /**
     * Shutdown this table. After calling this method the table cannot be started again.
     * The game will move to the {@link ShutdownSTM} state.
     */
    void shutdown();

    /**
     * Handles a buy in request for a player.
     *
     * @param pokerPlayer player
     * @param amount      amount requested
     */
    void handleBuyInRequest(PokerPlayer pokerPlayer, BigDecimal amount);

}