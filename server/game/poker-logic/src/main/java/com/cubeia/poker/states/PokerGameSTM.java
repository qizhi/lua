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

package com.cubeia.poker.states;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.player.PokerPlayer;

import java.io.Serializable;
import java.util.Set;

public interface PokerGameSTM extends Serializable {

    /**
     * Invoked when a scheduled timeout has occurred.
     *
     */
    public void timeout();

    /**
     * Invoked when an action has been received.
     *
     * @param action the action performed
     * @return true if action was handled, false if the action was ignored
     */
    public boolean act(PokerAction action);

    /**
     * Invoked when a new player has joined the table.
     *
     * @param player the player who joined
     */
    void playerJoined(PokerPlayer player);

    /**
     * Gets a description of the current state.
     *
     * @return a string describing the state
     */
    public String getStateDescription();

    /**
     * Invoked when we enter this state.
     */
    public void enterState();

    /**
     * Invoked when we leave this state.
     */
    public void exitState();

    /**
     * Checks if the player is currently in a hand.
     *
     * @param playerId the id of the player to check
     * @return
     */
    boolean isPlayerInHand(int playerId);

    void setPlayerSitOutNextHand(int playerId);

    void playerSitsIn(int playerId);

    void performPendingBuyIns(Set<PokerPlayer> singleton);

    void playerOpenedSession(int playerId);
}
