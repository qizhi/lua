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

package com.cubeia.games.poker.cache;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.games.poker.common.time.DefaultSystemTime;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ActionCacheTest {

    private ActionCache cache = new ActionCache(new DefaultSystemTime());

    @Test
    public void testPublicActions() throws Exception {
        GameAction action = new GameDataAction(11, 1);
        cache.addPublicAction(1, action);

        List<GameAction> state = cache.getPublicActions(1);
        assertThat(state.size(), is(1));

        GameAction action2 = new GameDataAction(22, 1);
        cache.addPublicAction(1, action2);
        GameAction action3 = new GameDataAction(33, 1);
        cache.addPublicAction(1, action3);

        state = cache.getPublicActions(1);
        assertThat(state.size(), is(3));

        assertThat(((GameDataAction) state.get(0)).getPlayerId(), is(11));
        assertThat(((GameDataAction) state.get(1)).getPlayerId(), is(22));
        assertThat(((GameDataAction) state.get(2)).getPlayerId(), is(33));

        cache.clear(1);

        state = cache.getPublicActions(1);
        assertThat(state.size(), is(0));
    }

    @Test
    public void testPrivateAndPublicActions() {
        int playerId1 = 11;
        int tableId = 1;
        GameAction actionPublic1 = new GameDataAction(playerId1, tableId);
        cache.addPublicAction(tableId, actionPublic1);
        int playerId2 = 1337;
        GameAction actionPrivate1 = new GameDataAction(playerId2, tableId);
        cache.addPrivateAction(tableId, playerId2, actionPrivate1);

        assertThat(cache.getPublicActions(tableId).size(), is(1));
        assertThat(cache.getPublicActions(tableId), is(asList(actionPublic1)));

        assertThat(cache.getPrivateAndPublicActions(tableId, playerId1).size(), is(1));
        assertThat(cache.getPublicActions(tableId), is(asList(actionPublic1)));

        assertThat(cache.getPrivateAndPublicActions(tableId, playerId2).size(), is(2));
    }

    @Test
    public void testClearActionsForTable() {
        int playerId1 = 11;
        int tableId = 1;
        GameAction actionPublic1 = new GameDataAction(playerId1, tableId);
        cache.addPublicAction(tableId, actionPublic1);
        int playerId2 = 1337;
        GameAction actionPrivate1 = new GameDataAction(playerId2, tableId);
        cache.addPrivateAction(tableId, playerId2, actionPrivate1);

        assertThat(cache.getPrivateAndPublicActions(tableId, playerId2).size(), is(2));
        cache.clear(tableId);
        assertThat(cache.getPrivateAndPublicActions(tableId, playerId2).size(), is(0));
    }

}
