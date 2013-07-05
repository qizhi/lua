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

package com.cubeia.poker.variant;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;

import java.io.Serializable;

/**
 * Each game type, such as Texas Hold'em or Omaha should implement this interface.
 * <p/>
 * This interface should define a minimal set of methods needed to implement the differences of all
 * major types of poker. Common functionality, such as player handling etc., goes into
 * the poker state.
 * <p/>
 * TODO: *SERIOUS* cleanup and probably major refactoring.
 * INFO: #AH2 refers to refactorings in point 2 in Andreas Holméns mail with the goal to only have event handling methods here.
 */
public interface GameType extends Serializable {

    public void startHand();

    public boolean act(PokerAction action);

    // TODO: #AH2 remove???
    public void scheduleRoundTimeout();

    public void prepareNewHand();

    // TODO: #AH2 move to state
    public void timeout();

    // TODO: #AH2 move from here? Or?
    public String getStateDescription();

//    public void sendAllNonFoldedPlayersBestHand();

    /**
     * Returns true if the player can buy in according to the poker variant. In Telesina for example
     * the player can buy in if he can afford the ante.
     *
     * @param player         player
     * @param settings       settings
     * @param includePending if true include the pending balance (that will be committed in the future)
     * @return true if player can buy in, false otherwise
     */
    public boolean canPlayerAffordEntryBet(PokerPlayer player, PokerSettings settings, boolean includePending);

    public void addHandFinishedListener(HandFinishedListener handFinishedListener);

    public void removeHandFinishedListener(HandFinishedListener handFinishedListener);

    public void setPokerContextAndServerAdapter(PokerContext context, ServerAdapterHolder serverAdapterHolder);

}
