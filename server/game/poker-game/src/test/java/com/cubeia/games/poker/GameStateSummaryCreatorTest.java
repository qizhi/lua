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

package com.cubeia.games.poker;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.cache.ActionContainer;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.io.protocol.*;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GameStateSummaryCreatorTest {

    @Mock
    private SystemTime dateFetcher;

    private Long timestamp;
    
    private DateTime now;

    @Before
    public void setup() {
        initMocks(this);
        now = new DateTime();
        timestamp = now.getMillis();
        when(dateFetcher.date()).thenReturn(now);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testSendGameState() throws IOException {
        int tableId = 234;
        int playerId = 1337;

        ActionCache actionCache = mock(ActionCache.class);
        GameStateSender gameStateSender = new GameStateSender(actionCache, dateFetcher);
        JoinRequestAction gameAction = new JoinRequestAction(playerId, 1, 0, "snubbe");
        Collection<ActionContainer> containers = asList(createPrivate(1, gameAction));

        when(actionCache.getPrivateAndPublicActions(tableId, playerId)).thenReturn(containers);

        Table table = mock(Table.class);
        GameNotifier gameNotifier = mock(GameNotifier.class);
        when(table.getNotifier()).thenReturn(gameNotifier);
        when(table.getId()).thenReturn(tableId);

        gameStateSender.sendGameState(table, playerId);

        ArgumentCaptor<Collection> gameStateCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(gameNotifier).notifyPlayer(Mockito.eq(playerId), gameStateCaptor.capture());

        Collection<GameAction> gameStateSummary = gameStateCaptor.getValue();

        assertThat(gameStateSummary.size(), is(3));
        Iterator<GameAction> actionIter = gameStateSummary.iterator();
        assertThat(extractProtocolObject((GameDataAction) actionIter.next()), instanceOf(StartHandHistory.class));
        assertThat(actionIter.next(), instanceOf(JoinRequestAction.class));
        assertThat(extractProtocolObject((GameDataAction) actionIter.next()), instanceOf(StopHandHistory.class));
    }

    @Test
    public void testFilterRequestActions() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();
        JoinRequestAction act0 = new JoinRequestAction(111, 1, 0, "snubbe");
        actions.add(createPrivate(111, act0));
        GameDataAction gda0 = new GameDataAction(333, 1);
        gda0.setData(styx.pack(new RequestAction()));
        actions.add(createPrivate(111, gda0));
        GameDataAction gda1 = new GameDataAction(333, 1);
        gda1.setData(styx.pack(new PerformAction(1, 222, new PlayerAction(), "10", "10", "10", false, new int[]{})));
        actions.add(createPrivate(111, gda1));
        GameDataAction gda2 = new GameDataAction(222, 1);
        gda2.setData(styx.pack(new DealPublicCards(new ArrayList<GameCard>())));
        actions.add(createPrivate(111, gda2));

        assertThat(actions.size(), is(4));

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);
        assertThat(filteredActions.size(), is(3));
        assertThat(filteredActions, is(Arrays.<GameAction>asList(act0, gda1, gda2)));
    }

    @Test
    public void testFilterAllButLastRequestActions() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();
        JoinRequestAction act0 = new JoinRequestAction(111, 1, 0, "snubbe");
        actions.add(createPrivate(111, act0));

        // Request perform pair
        GameDataAction gda0 = new GameDataAction(222, 1);
        gda0.setData(styx.pack(new RequestAction()));
        actions.add(createPrivate(222, gda0));

        GameDataAction gda1 = new GameDataAction(222, 1);
        gda1.setData(styx.pack(new PerformAction(1, 222, new PlayerAction(), "10", "10", "10", false, new int[]{})));
        actions.add(createPrivate(222, gda1));

        // Request without perform - this should not be filtered.
        GameDataAction lastRequest = new GameDataAction(333, 1);
        lastRequest.setData(styx.pack(new RequestAction()));
        actions.add(createPrivate(333, lastRequest));

        assertThat(actions.size(), is(4));

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);
        assertThat(filteredActions.size(), is(3));
        assertThat(filteredActions, is(Arrays.<GameAction>asList(act0, gda1, lastRequest)));
    }

    @Test
    public void testFilterAllButLastRequestActionsButIncludeAllAntes() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();
        JoinRequestAction act0 = new JoinRequestAction(111, 1, 0, "snubbe");
        actions.add(createPrivate(111, act0));

        int tableId = 1;

        // Request ante
        int player0Id = 222;
        GameDataAction gda0 = new GameDataAction(player0Id, tableId);
        RequestAction ra0 = new RequestAction();
        ra0.allowedActions = new ArrayList<PlayerAction>();
        ra0.allowedActions.add(new PlayerAction(Enums.ActionType.ANTE, "0", "0"));
        gda0.setData(styx.pack(ra0));
        actions.add(createPrivate(player0Id, gda0));

        //request ante for other guy
        int player1Id = 223;
        GameDataAction gda1 = new GameDataAction(player1Id, tableId);
        RequestAction ra1 = new RequestAction();
        ra1.allowedActions = new ArrayList<PlayerAction>();
        ra1.allowedActions.add(new PlayerAction(Enums.ActionType.ANTE, "0", "0"));
        gda1.setData(styx.pack(ra1));
        actions.add(createPrivate(player1Id, gda1));

        assertThat(actions.size(), is(3));

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, player0Id);
        assertThat(filteredActions.size(), is(3)); // the join request and two ante requests
        assertThat(filteredActions.contains(gda0), is(true));
        assertThat(filteredActions.contains(gda1), is(true));
    }

    @Test
    public void testFilterAllButLastRequestActionsButIncludeAllAntesEvenIfTheyHaveAResponse() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();
        JoinRequestAction act0 = new JoinRequestAction(111, 1, 0, "snubbe");
        actions.add(createPrivate(111, act0));

        int tableId = 1;

        // Request ante
        int player0Id = 222;
        GameDataAction gda0 = new GameDataAction(player0Id, tableId);
        RequestAction ra0 = new RequestAction();
        ra0.allowedActions = new ArrayList<PlayerAction>();
        ra0.allowedActions.add(new PlayerAction(Enums.ActionType.ANTE, "0", "0"));
        gda0.setData(styx.pack(ra0));
        actions.add(createPrivate(player0Id, gda0));

        //request ante for other guy
        int player1Id = 223;
        GameDataAction gda1 = new GameDataAction(player1Id, tableId);
        RequestAction ra1 = new RequestAction();
        ra1.allowedActions = new ArrayList<PlayerAction>();
        ra1.allowedActions.add(new PlayerAction(Enums.ActionType.ANTE, "0", "0"));
        gda1.setData(styx.pack(ra1));
        actions.add(createPrivate(player1Id, gda1));

        // the other guy sends a perform action
        GameDataAction gda2 = new GameDataAction(player1Id, tableId);
        gda2.setData(styx.pack(new PerformAction(tableId, player1Id, new PlayerAction(),"10", "10", "10", false, new int[]{})));
        actions.add(createPrivate(player1Id, gda2));

        assertThat(actions.size(), is(4));


        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, player0Id);
        assertThat(filteredActions.size(), is(4)); // the join request and two ante requests
        assertThat(filteredActions.contains(gda0), is(true));
        assertThat(filteredActions.contains(gda1), is(true));
    }

    @Test
    public void testFilterOutSkippedPlayer() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();

        // Deal pocket cards sent private and public
        GameDataAction privateCards = new GameDataAction(111, 1);
        privateCards.setData(styx.pack(new DealPrivateCards()));
        actions.add(createPrivate(111, privateCards));

        GameDataAction publicCards = new GameDataAction(111, 1);
        publicCards.setData(styx.pack(new DealPrivateCards()));
        actions.add(ActionContainer.createPublic(publicCards, 111, timestamp));

        assertThat(actions.size(), is(2));

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);
        assertThat(filteredActions.size(), is(2));
        assertThat(filteredActions, is(Arrays.<GameAction>asList(privateCards, publicCards)));

        filteredActions = gameStateSummaryCreator.filterRequestActions(actions, 111);
        assertThat(filteredActions.size(), is(1));
        assertThat(filteredActions, is(Arrays.<GameAction>asList(privateCards)));
    }

    /**
     * Deal hidden cards to the player in scope should be removed
     *
     * @throws IOException
     */
    @Test
    public void testFilterDealHiddenCardsToPlayer() throws IOException {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();
        JoinRequestAction act0 = new JoinRequestAction(111, 1, 0, "snubbe");
        actions.add(createPrivate(111, act0));

        // Request perform pair
        GameDataAction gda0 = new GameDataAction(222, 1);
        gda0.setData(styx.pack(new RequestAction()));
        actions.add(createPrivate(222, gda0));

        GameDataAction gda1 = new GameDataAction(222, 1);
        gda1.setData(styx.pack(new PerformAction(1, 222, new PlayerAction(), "10", "10", "10", false, new int[]{})));
        actions.add(createPrivate(222, gda1));

        // Request without perform - this should not be filtered.
        GameDataAction lastRequest = new GameDataAction(333, 1);
        lastRequest.setData(styx.pack(new RequestAction()));
        actions.add(createPrivate(333, lastRequest));

        assertThat(actions.size(), is(4));

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);
        assertThat(filteredActions.size(), is(3));
        assertThat(filteredActions, is(Arrays.<GameAction>asList(act0, gda1, lastRequest)));
    }

    @Test
    public void testAdjustTimeout() throws Exception {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();

        // Request without perform - this should not be filtered.
        GameDataAction lastRequest = new GameDataAction(333, 1);
        lastRequest.setData(styx.pack(new RequestAction("0", 1, 111, new ArrayList<PlayerAction>(), 100)));
        ActionContainer container = createPrivate(333, lastRequest, timestamp);
        actions.add(container);

        DateTime later = now.plusMillis(20);
        when(dateFetcher.date()).thenReturn(later);

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);

        GameDataAction gameAction = (GameDataAction) filteredActions.get(0);
        Assert.assertEquals(gameAction, lastRequest);
        RequestAction request = (RequestAction) styx.unpack(gameAction.getData());

        assertTrue("Should be < 80, was " + request.timeToAct, request.timeToAct <= 80);
    }

    @Test
    public void testAdjustTimeoutNegative() throws Exception {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();

        // Request without perform - this should not be filtered.
        GameDataAction lastRequest = new GameDataAction(333, 1);
        lastRequest.setData(styx.pack(new RequestAction("0", 1, 111, new ArrayList<PlayerAction>(), 10)));
        actions.add(createPrivate(333, lastRequest));

        DateTime later = now.plusMillis(20);
        when(dateFetcher.date()).thenReturn(later);

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);

        GameDataAction gameAction = (GameDataAction) filteredActions.get(0);
        Assert.assertEquals(gameAction, lastRequest);
        RequestAction request = (RequestAction) styx.unpack(gameAction.getData());

        Assert.assertEquals(0, request.timeToAct);
    }

    @Test
    public void testAdjustTimeoutWithDisconnect() throws Exception {
        GameStateSender gameStateSummaryCreator = new GameStateSender(null, dateFetcher);
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

        List<ActionContainer> actions = new ArrayList<ActionContainer>();

        // Request without perform - this should not be filtered.
        GameDataAction lastRequest = new GameDataAction(333, 1);
        lastRequest.setData(styx.pack(new RequestAction("0", 1, 111, new ArrayList<PlayerAction>(), 100)));
        actions.add(createPrivate(333, lastRequest));

        GameDataAction disconnected = new GameDataAction(333, 1);
        disconnected.setData(styx.pack(new PlayerDisconnectedPacket(1, 200)));
        actions.add(createPrivate(333, disconnected));


        Thread.sleep(20); // Wait so we can check adjustment

        List<GameAction> filteredActions = gameStateSummaryCreator.filterRequestActions(actions, -99);

        GameDataAction gameAction = (GameDataAction) filteredActions.get(1);
        Assert.assertEquals(gameAction, lastRequest);
        RequestAction request = (RequestAction) styx.unpack(gameAction.getData());

        assertTrue(request.timeToAct > 100);
    }

    private ProtocolObject extractProtocolObject(GameDataAction gda) throws IOException {
        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());
        return styx.unpack(gda.getData());
    }

    private ActionContainer createPrivate(int playerId, GameAction gameAction) {
        return ActionContainer.createPrivate(playerId, gameAction, timestamp);
    }

    private ActionContainer createPrivate(int playerId, GameAction gameAction, Long timestamp) {
        return ActionContainer.createPrivate(playerId, gameAction, timestamp);
    }
}
