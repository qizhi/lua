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

package com.cubeia.poker;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.RakeSettings;
import com.google.common.collect.Maps;
import org.junit.Ignore;

import java.math.BigDecimal;
import java.util.SortedMap;

@Ignore("not a test")
public class TestUtils {

    private TestUtils() {
    }

    public static MockPlayer[] createMockPlayers(int numberOfPlayers) {
        return createMockPlayers(numberOfPlayers, new BigDecimal(5000).setScale(2));
    }

    public static MockPlayer[] createMockPlayers(int numberOfPlayers, String balance){
        return createMockPlayers(numberOfPlayers, new BigDecimal(balance));
    }
    public static MockPlayer[] createMockPlayers(int numberOfPlayers, long balance) {
        return createMockPlayers(numberOfPlayers, new BigDecimal(balance));
    }
    public static MockPlayer[] createMockPlayers(int numberOfPlayers, BigDecimal balance) {
        MockPlayer[] players = new MockPlayer[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++) {
            players[i] = new MockPlayer(i);
            players[i].setSeatId(i);
            players[i].setBalance(balance);
            players[i].setHasActed(false);

        }

        return players;
    }

    public static int[] createPlayerIdArray(MockPlayer[] players) {
        int[] ids = new int[players.length];

        for (int i = 0; i < players.length; i++) {
            ids[i] = players[i].getId();
        }

        return ids;
    }

    public static void addPlayers(PokerState game, PokerPlayer[] p, long startingChips) {
        for (PokerPlayer pl : p) {
            game.addPlayer(pl);
            pl.addChips(new BigDecimal(startingChips));
        }
    }

    public static void addPlayers(PokerState game, PokerPlayer[] p) {
        addPlayers(game, p, 10000);
    }

    public static void act(PokerState game, int playerId, PokerActionType actionType) {
        game.act(new PokerAction(playerId, actionType));
    }

    public static RakeSettings createOnePercentRakeSettings() {
        return new RakeSettings(new BigDecimal("0.1"), new BigDecimal(Long.MAX_VALUE), new BigDecimal(Long.MAX_VALUE));
    }

    public static RakeSettings createZeroRakeSettings() {
        return new RakeSettings(BigDecimal.ZERO, new BigDecimal(Long.MAX_VALUE), new BigDecimal(Long.MAX_VALUE));
    }

    public static SortedMap<Integer, PokerPlayer> asSeatingMap(PokerPlayer ... players) {
        SortedMap<Integer, PokerPlayer> seatingMap = Maps.newTreeMap();
        for (PokerPlayer player : players) {
            seatingMap.put(player.getSeatId(), player);
        }
        return seatingMap;
    }
}
