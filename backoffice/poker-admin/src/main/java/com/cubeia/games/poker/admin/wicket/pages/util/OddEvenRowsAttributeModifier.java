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

/**
 * 
 */
package com.cubeia.games.poker.admin.wicket.pages.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

/**
 * Attribute modifier for repeaters that sets the CSS class to "even" for even rows and "odd" for odd rows.
 */
public class OddEvenRowsAttributeModifier extends AttributeModifier {
    private static final long serialVersionUID = 1L;

    public OddEvenRowsAttributeModifier(int index) {
        super("class", Model.of(index % 2 == 0 ? "even" : "odd"));
    }
}