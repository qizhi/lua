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

package com.cubeia.poker.blinds;

/**
 * Interface for describing a blinds player.
 *
 * @author viktor
 */
public interface BlindsPlayer {

    /**
     * Gets the seat id of this player.
     *
     * @return the seat id of this player
     */
    public int getSeatId();

    /**
     * Gets the id of this player.
     *
     * @return the id of this player
     */
    public int getId();

    /**
     * Checks whether the player is sitting in.
     * <p/>
     * Any player who is not sitting out is considered as sitting in, whether the entry bet has been posted or not.
     *
     * @return <code>true</code> if the player is sitting in, <code>false</code> otherwise
     */
    public boolean isSittingIn();

    /**
     * Checks whether the player has posted the entry bet.
     * <p/>
     * A player pays the entry bet and then later declines or misses the big blind (due to sitting out) is
     * considered as _not_ having paid the entry bet.
     *
     * @return <code>true</code> if the player has posted the entry bet, <code>false</code> otherwise
     */
    public boolean hasPostedEntryBet();

    /**
     * Gets the {@link MissedBlindsStatus} of this player.
     *
     * @return the {@link MissedBlindsStatus} of this player
     */
    public MissedBlindsStatus getMissedBlindsStatus();

    void setHasPostedEntryBet(boolean hasPostedEntryBet);
}
