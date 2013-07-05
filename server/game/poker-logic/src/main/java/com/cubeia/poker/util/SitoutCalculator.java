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

package com.cubeia.poker.util;

import com.cubeia.poker.player.PokerPlayer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

public class SitoutCalculator {

    Logger log = Logger.getLogger(this.getClass());

    public Collection<PokerPlayer> checkTimeoutPlayers(Collection<PokerPlayer> seatedPlayers, long sitoutTimeLimitMillis) {
        Collection<PokerPlayer> result = new ArrayList<PokerPlayer>();
        for (PokerPlayer player : seatedPlayers) {
            if (player.isSittingOut()) {
                Long timestamp = player.getSitOutTimestamp();
                if (timestamp.longValue() + sitoutTimeLimitMillis < System.currentTimeMillis()) {
                    log.debug("Sitout timeout (will be forcibly removed) for player: " + player);
                    result.add(player);
                }
            }
        }
        return result;
    }

}
