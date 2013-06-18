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

package com.cubeia.poker.hand;

import java.util.List;

/**
 * Generator of cards with assigned id:s.
 *
 * @author w
 */
public interface CardIdGenerator {

    /**
     * Copy the given list of cards and assigned id:s to the new cards.
     * The order of the list is not altered.
     *
     * @param cards cards to copy
     * @return list of cards with id:s assigned
     */
    List<Card> copyAndAssignIds(List<Card> cards);

}