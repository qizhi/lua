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

package com.cubeia.poker.result;

import com.cubeia.poker.player.PokerPlayer;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RevealOrderCalculatorTest {

    PokerPlayer player1;
    PokerPlayer player2;
    PokerPlayer player3;
    RevealOrderCalculator roc;
    SortedMap<Integer, PokerPlayer> seatingMap;
    List<Integer> revealOrder;
    int player1Id;
    int player2Id;
    int player3Id;

    private void setup() {
        roc = new RevealOrderCalculator();
        player1 = mock(PokerPlayer.class);
        player2 = mock(PokerPlayer.class);
        player3 = mock(PokerPlayer.class);

        player1Id = 1001;
        player2Id = 1002;
        player3Id = 1003;

        when(player1.getId()).thenReturn(player1Id);
        when(player2.getId()).thenReturn(player2Id);
        when(player3.getId()).thenReturn(player3Id);

        seatingMap = new TreeMap<Integer, PokerPlayer>();
        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        seatingMap.put(2, player3);
    }

    private void assertPlayersId(int id1, int id2, int id3) {
        Iterator<Integer> revealIter = revealOrder.iterator();
        assertThat(revealIter.next(), is(id1));
        assertThat(revealIter.next(), is(id2));
        assertThat(revealIter.next(), is(id3));
    }

    @Test
    public void testRevealOrderWithLastCaller() {
        setup();
        revealOrder = roc.calculateRevealOrder(seatingMap, player2, player1, 3);
        assertPlayersId(player2Id, player3Id, player1Id);
    }

    @Test
    public void testRevealOrderWithNoLastCaller() {
        setup();
        revealOrder = roc.calculateRevealOrder(seatingMap, null, player2, 3);
        assertPlayersId(player3Id, player1Id, player2Id);
    }

    @Test
    public void testRevealOrderWithNoLastCallerDealerButtonAtLastSeat() {
        setup();
        revealOrder = roc.calculateRevealOrder(seatingMap, null, player3, 3);
        assertPlayersId(player1Id, player2Id, player3Id);
    }

    @Test
    public void testRevealOrderOnePlayerFolded() {
        setup();
        when(player2.hasFolded()).thenReturn(true);
        revealOrder = roc.calculateRevealOrder(seatingMap, null, player1, 2);
        assertThat(revealOrder.size(), is(2));
    }

    @Test
    public void testRevealOrderOnlyOneNonFoldedPlayer() {
        setup();
        when(player2.hasFolded()).thenReturn(true);
        when(player3.hasFolded()).thenReturn(true);
        revealOrder = roc.calculateRevealOrder(seatingMap, null, player1, 1);
        assertThat(revealOrder.isEmpty(), is(true));
    }

}
