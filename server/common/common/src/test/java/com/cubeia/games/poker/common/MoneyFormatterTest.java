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

package com.cubeia.games.poker.common;

import org.junit.Test;

import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MoneyFormatterTest {

    @Test
    public void testFormatMoney() {
        assertThat(format("$", 0), is("$0"));
        assertThat(format("$", 2000), is("$20"));
        assertThat(format("$", 2010), is("$20.10"));
        assertThat(format("$", 2012), is("$20.12"));
        assertThat(format("$", 200012), is("$2000.12"));
        assertThat(format("$", 200000012), is("$2000000.12"));
        assertThat(format("$", 200000000), is("$2000000"));

    }
}
