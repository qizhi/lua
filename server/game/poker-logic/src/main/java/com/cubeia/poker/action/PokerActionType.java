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

package com.cubeia.poker.action;

import com.cubeia.poker.handhistory.api.PlayerAction;

/**
 * NOTE!!!!
 * If you add a definition to this class you must also add to the protocol
 * implementation and the translation adapter code.
 * <p/>
 * (For firebase this would be protocol.xml and ActionTransformer in the
 * poker-game project).
 * <p/>
 * Failure to do so will result in missing options sent to the client!
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public enum PokerActionType {
    SMALL_BLIND,
    BIG_BLIND,
    CALL,
    CHECK,
    BET,
    RAISE,
    FOLD,
    DECLINE_ENTRY_BET,
    ANTE,
    BIG_BLIND_PLUS_DEAD_SMALL_BLIND,
    DEAD_SMALL_BLIND,
    ENTRY_BET,
    WAIT_FOR_BIG_BLIND,
    DISCARD;

    public PlayerAction.Type translate() {
        return PlayerAction.Type.values()[ordinal()];
    }
}