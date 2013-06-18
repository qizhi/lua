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

import java.io.Serializable;

/**
 * Describes a player who should pay the entry bet.
 *
 * @author viktor
 */
public class EntryBetter implements Serializable {

    private final BlindsPlayer player;

    private final EntryBetType entryBetType;

    public EntryBetter(BlindsPlayer player, EntryBetType entryBetType) {
        super();
        this.player = player;
        this.entryBetType = entryBetType;
    }

    public BlindsPlayer getPlayer() {
        return player;
    }

    public EntryBetType getEntryBetType() {
        return entryBetType;
    }

    @Override
    public String toString() {
        return "[playerId = " + player.getId() + ", entryBetType = " + entryBetType + "]";
    }
}
