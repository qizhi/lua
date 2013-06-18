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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.player.PokerPlayer;
import org.junit.Test;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DefaultPlayerToActCalculatorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testFirstPlayerToAct() {
        DefaultPlayerToActCalculator pac = new DefaultPlayerToActCalculator(1);
        SortedMap<Integer, PokerPlayer> seatingMap = new TreeMap<Integer, PokerPlayer>();
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        PokerPlayer player3 = mock(PokerPlayer.class);

        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        seatingMap.put(2, player3);


        PokerPlayer playerToAct = pac.getFirstPlayerToAct(seatingMap, Collections.EMPTY_LIST);
        assertThat(playerToAct, is(player3));

        pac = new DefaultPlayerToActCalculator(2);
        playerToAct = pac.getFirstPlayerToAct(seatingMap, Collections.EMPTY_LIST);
        assertThat(playerToAct, is(player1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNextPlayerToAct() {
        DefaultPlayerToActCalculator pac = new DefaultPlayerToActCalculator(1);
        SortedMap<Integer, PokerPlayer> seatingMap = new TreeMap<Integer, PokerPlayer>();
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        PokerPlayer player3 = mock(PokerPlayer.class);
        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        seatingMap.put(2, player3);

        PokerPlayer playerToAct = pac.getFirstPlayerToAct(seatingMap, Collections.EMPTY_LIST);
        assertThat(playerToAct, is(player3));

        pac = new DefaultPlayerToActCalculator(2);
        playerToAct = pac.getFirstPlayerToAct(seatingMap, Collections.EMPTY_LIST);
        assertThat(playerToAct, is(player1));
    }

}
