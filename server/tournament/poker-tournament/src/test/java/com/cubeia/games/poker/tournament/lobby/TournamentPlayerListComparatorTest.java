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

package com.cubeia.games.poker.tournament.lobby;

import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttPlayerStatus;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.OUT;
import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.PLAYING;
import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.REGISTERED;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TournamentPlayerListComparatorTest {

    @Mock
    private PokerTournamentState state;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testSorting() {
        MttPlayer p1 = createPlayer(1, "adam", 520, -1, PLAYING);
        MttPlayer p2 = createPlayer(2, "Ben", 500, -1, PLAYING);
        MttPlayer p3 = createPlayer(3, "Someone", 0, 4, OUT);
        MttPlayer p4 = createPlayer(4, "Caesar", 0, 5, OUT);

        List<MttPlayer> expected = newArrayList(p1, p2, p3, p4);
        List<MttPlayer> random = newArrayList(expected);
        Collections.shuffle(random);
        Collections.sort(random, Collections.reverseOrder(new TournamentPlayerListComparator(state)));
        assertThat(random, is(expected));
    }

    @Test
    public void testSortingBeforeTournamentStarted() {
        MttPlayer p1 = createPlayer(1, "adam", 0, -1, REGISTERED);
        MttPlayer p2 = createPlayer(2, "Ben", 0, -1, REGISTERED);
        MttPlayer p3 = createPlayer(3, "Caesar", 0, -1, REGISTERED);
        MttPlayer p4 = createPlayer(4, "David", 0, -1, REGISTERED);

        List<MttPlayer> expected = newArrayList(p1, p2, p3, p4);
        List<MttPlayer> random = newArrayList(expected);
        Collections.shuffle(random);
        Collections.sort(random, Collections.reverseOrder(new TournamentPlayerListComparator(state)));
        assertThat(random, is(expected));
    }

    public MttPlayer createPlayer(int playerId, String name, long balance, int position, MttPlayerStatus status) {
        when(state.getPlayerBalance(playerId)).thenReturn(new BigDecimal(balance));
        MttPlayer player = new MttPlayer(playerId, name);
        player.setStatus(status);
        player.setPosition(position);
        return player;
    }
}
