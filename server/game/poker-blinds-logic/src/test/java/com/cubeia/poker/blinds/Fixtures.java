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

package com.cubeia.poker.blinds;

import com.cubeia.poker.blinds.utils.MockPlayer;

import java.util.ArrayList;
import java.util.List;

public class Fixtures {

    public static List<BlindsPlayer> players(int... seatIds) {
        return players(true, seatIds);
    }

    public static List<BlindsPlayer> players(boolean hasPosted, int... seatIds) {
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        for (int seatId : seatIds) {
            players.add(player(seatId, hasPosted));
        }
        return players;
    }

    public static MockPlayer player(int seatId, boolean hasPostedEntry, boolean sittingIn) {
        MockPlayer player = new MockPlayer(seatId);
        player.setHasPostedEntryBet(hasPostedEntry);
        player.setSittingIn(sittingIn);
        return player;
    }

    public static MockPlayer player(int seatId, boolean hasPostedEntry) {
        return player(seatId, hasPostedEntry, true);
    }

    public static BlindsInfo blindsInfo(int dealer, int small, int big) {
        BlindsInfo blinds = new BlindsInfo();
        blinds.setDealerSeatId(dealer);
        blinds.setSmallBlindSeatId(small);
        blinds.setSmallBlindPlayerId(small);
        blinds.setBigBlindSeatId(big);
        blinds.setBigBlindPlayerId(big);
        return blinds;
    }

}
