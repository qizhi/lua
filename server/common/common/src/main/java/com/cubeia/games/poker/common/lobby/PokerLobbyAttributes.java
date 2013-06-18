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

package com.cubeia.games.poker.common.lobby;

/**
 * <p>Attributes used in the lobby on poker tables.</p>
 * <p/>
 * <p>Use the enums using .name() to get the String representation used for the
 * lobby attribute.</p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public enum PokerLobbyAttributes {

    /**
     * <p>Int attribute. > 0 means remove from lobby asap</p>
     * <p/>
     * <p><em>NOTE: If you set this flag then the table will be forcibly
     * removed from the system even if there are seated players.</em></p>
     */
    TABLE_READY_FOR_CLOSE,
    VISIBLE_IN_LOBBY,
    SPEED,
    ANTE,
    SMALL_BLIND,
    BIG_BLIND,
    BETTING_GAME_BETTING_MODEL,
    MONETARY_TYPE,
    VARIANT,
    MIN_BUY_IN,
    MAX_BUY_IN,
    DECK_SIZE,
    TABLE_TEMPLATE, // table template ID
    TABLE_EXTERNAL_ID,
    CURRENCY_CODE
}
