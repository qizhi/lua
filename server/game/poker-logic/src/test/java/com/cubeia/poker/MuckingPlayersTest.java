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

import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MuckingPlayersTest {


    private PokerContext pokerContext = new PokerContext(null);
    @Mock
    private PokerPlayer player1;
    @Mock
    private PokerPlayer player2;
    @Mock
    private PokerPlayer player3;


    @Before
    public void setup() {
        initMocks(this);
        // TODO: This test is a bit intrusive
        pokerContext.getCurrentHandPlayerMap().put(1001, player1);
        pokerContext.getCurrentHandPlayerMap().put(1002, player2);
        pokerContext.getCurrentHandPlayerMap().put(1003, player3);
    }

    @Test
    public void testGetMuckingPlayersNormal() {
        when(player1.hasFolded()).thenReturn(false);
        when(player2.hasFolded()).thenReturn(false);
        when(player3.hasFolded()).thenReturn(false);

        Set<PokerPlayer> muckingPlayers = pokerContext.getMuckingPlayers();
        assertThat(muckingPlayers.isEmpty(), is(true));
    }

    @Test
    public void testGetMuckingPlayersWhenAllButOneFolded() {
        when(player1.hasFolded()).thenReturn(false);
        when(player2.hasFolded()).thenReturn(true);
        when(player3.hasFolded()).thenReturn(true);

        Set<PokerPlayer> muckingPlayers = pokerContext.getMuckingPlayers();
        assertThat(muckingPlayers.size(), is(3));
        assertThat(muckingPlayers, hasItems(player1, player2, player3));
    }

    @Test
    public void testFoldedPlayerMucks() {
        when(player1.hasFolded()).thenReturn(false);
        when(player2.hasFolded()).thenReturn(true);
        when(player3.hasFolded()).thenReturn(false);

        Set<PokerPlayer> muckingPlayers = pokerContext.getMuckingPlayers();
        assertThat(muckingPlayers.size(), is(1));
        assertThat(muckingPlayers, hasItems(player2));
    }

}
