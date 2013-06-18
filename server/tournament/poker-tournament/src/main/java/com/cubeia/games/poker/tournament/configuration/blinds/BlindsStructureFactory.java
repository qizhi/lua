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

package com.cubeia.games.poker.tournament.configuration.blinds;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class BlindsStructureFactory {

    public static BlindsStructure createDefaultBlindsStructure() {
        List<Level> blindsLevelList = createBlindsLevels();
        return new BlindsStructure(blindsLevelList);
    }

    private static List<Level> createBlindsLevels() {
        List<Level> levels = newArrayList();
        BigDecimal smallBlind = new BigDecimal(2000);
        BigDecimal bigBlind = new BigDecimal(4000);
        BigDecimal ante = BigDecimal.ZERO;
        for (int i = 0; i < 20; i++) {
            levels.add(new Level(smallBlind, bigBlind, ante, 1, false));
            smallBlind = smallBlind.multiply(new BigDecimal(2));
            bigBlind = bigBlind.multiply(new BigDecimal(2));
        }
        return levels;
    }
}
