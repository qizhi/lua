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

package com.cubeia.games.poker.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class WalletAmountConverterTest {

    @Test
    public void testConvertToWalletAmount() {
        WalletAmountConverter wac = new WalletAmountConverter(2);

        BigDecimal a = wac.convertToWalletAmount(-12345);
        assertEquals(new BigDecimal("-123.45"), a);
        assertEquals(2, a.scale());
    }

    @Test
    public void testConvertToInternalScaledAmount() {
        WalletAmountConverter wac = new WalletAmountConverter(2);
        int a = wac.convertToInternalScaledAmount(new BigDecimal("-123"));
        assertEquals(-12300, a);
    }

}
