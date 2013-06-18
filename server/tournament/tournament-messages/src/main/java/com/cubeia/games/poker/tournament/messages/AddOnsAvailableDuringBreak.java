/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.messages;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Sent to a table when add-ons are available during a break.
 */
public class AddOnsAvailableDuringBreak implements Serializable {

    private BigDecimal chipsForAddOn;

    private BigDecimal addOnCost;

    public AddOnsAvailableDuringBreak(BigDecimal chipsForAddOn, BigDecimal addOnCost) {
        this.chipsForAddOn = chipsForAddOn;
        this.addOnCost = addOnCost;
    }

    public BigDecimal getChipsForAddOn() {
        return chipsForAddOn;
    }

    public BigDecimal getAddOnCost() {
        return addOnCost;
    }
}
