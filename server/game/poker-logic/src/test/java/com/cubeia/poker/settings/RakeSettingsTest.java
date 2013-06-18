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

package com.cubeia.poker.settings;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RakeSettingsTest {

    @Test
    public void testGetRakeFraction() {
        RakeSettings settings = new RakeSettings();
        BigDecimal twoPercent = BigDecimal.valueOf(0.02);
        settings.setRakeFraction2Plus(twoPercent);
        assertThat(settings.getRakeFraction(2), is(twoPercent));
        assertThat(settings.getRakeFraction(3), is(RakeSettings.DEFAULT_RAKE_FRACTION));
    }
}
