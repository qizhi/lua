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

package com.cubeia.poker.rounds.ante;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnteRoundTest {

    @Mock
    private PokerPlayer player1;

    @Mock
    private PokerPlayer player2;

    private AnteRoundHelper realAnteRoundHelper;

    @Mock
    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private AnteRoundHelper anteRoundHelper;

    @Mock
    private PokerSettings settings;

    private ActionRequest actionRequest1;
    private ActionRequest actionRequest2;
    private int dealerButtonSeatId = 1;
    private BlindsInfo blindsInfo;
    private SortedMap<Integer, PokerPlayer> playerMap;

    @Before
    public void setUp() {
        initMocks(this);

        when(player1.getId()).thenReturn(111);
        when(player2.getId()).thenReturn(222);

        actionRequest1 = new ActionRequest();
        actionRequest1.setPlayerId(111);
        actionRequest1.enable(new PossibleAction(PokerActionType.ANTE, new BigDecimal(10)));

        actionRequest2 = new ActionRequest();
        actionRequest2.setPlayerId(222);
        actionRequest2.enable(new PossibleAction(PokerActionType.ANTE, new BigDecimal(10)));

        when(player1.getActionRequest()).thenReturn(actionRequest1);
        when(player2.getActionRequest()).thenReturn(actionRequest2);

        playerMap = new TreeMap<Integer, PokerPlayer>();
        playerMap.put(0, player1);
        playerMap.put(1, player2);
        when(context.getCurrentHandSeatingMap()).thenReturn(playerMap);

        blindsInfo = mock(BlindsInfo.class);
        when(blindsInfo.getDealerButtonSeatId()).thenReturn(dealerButtonSeatId);
        when(context.getBlindsInfo()).thenReturn(blindsInfo);

        when(context.getPlayerInCurrentHand(player1.getId())).thenReturn(player1);
        when(context.getPlayerInCurrentHand(player2.getId())).thenReturn(player2);
        when(context.getPlayersInHand()).thenReturn(asList(player1, player2));

        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getSettings()).thenReturn(settings);

        realAnteRoundHelper = new AnteRoundHelper(context, serverAdapterHolder);
        anteRoundHelper = mock(AnteRoundHelper.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCreationAndAnteRequestBroadcast() {
        BigDecimal anteLevel = new BigDecimal(1000);
        when(settings.getAnteAmount()).thenReturn(anteLevel);

        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, realAnteRoundHelper);

        verify(player1).clearActionRequest();
        verify(player2).clearActionRequest();


        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(serverAdapter).requestMultipleActions(captor.capture());
        Collection<ActionRequest> captured = captor.getValue();

        Iterator<ActionRequest> iterator = captured.iterator();
        Iterator<PokerPlayer> original = playerMap.values().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getPlayerId(), is(original.next().getId()));
        }

        assertThat(anteRound.isFinished(), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testActOnAnte() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);
        int player1Id = 1337;
        when(context.getPlayerInCurrentHand(player1Id)).thenReturn(player1);
        BigDecimal anteLevel = new BigDecimal(1000);
        when(settings.getAnteAmount()).thenReturn(anteLevel);

        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(false);
        //        when(realAnteRoundHelper.getNextPlayerToAct(Mockito.eq(0), Mockito.any(SortedMap.class))).thenReturn(player2);
        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        BigDecimal resultingBalance = new BigDecimal(23434);
        when(player1.getBalance()).thenReturn(resultingBalance);

        PokerAction action = new PokerAction(player1Id, PokerActionType.ANTE);
        anteRound.act(action);

        verify(player1).addBet(anteLevel);
        verify(player1).setHasActed(true);
        verify(player1).setHasPostedEntryBet(true);
        verify(serverAdapter).notifyActionPerformed(action, player1);
        verify(serverAdapter).notifyPlayerBalance(player1);

        verify(anteRoundHelper).notifyPotSizeAndRakeInfo();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testActOnAnteImpossibleToStartHandWillAutoDecline() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);
        BigDecimal anteLevel = new BigDecimal(1000);
        int currentDealerButtonSeatId = 0;
        when(settings.getAnteAmount()).thenReturn(anteLevel);
        when(blindsInfo.getDealerButtonSeatId()).thenReturn(currentDealerButtonSeatId);

        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(false);
        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        BigDecimal resultingBalance1 = new BigDecimal(23434);
        when(player1.getBalance()).thenReturn(resultingBalance1);
        BigDecimal resultingBalance2 = new BigDecimal(349834);
        when(player2.getBalance()).thenReturn(resultingBalance2);
        when(anteRoundHelper.isImpossibleToStartRound(Mockito.anyCollection())).thenReturn(true);

        when(anteRoundHelper.setAllPendingPlayersToDeclineEntryBet(Mockito.anyCollection())).thenReturn(asList(player2));

        PokerAction action1 = new PokerAction(player1.getId(), PokerActionType.DECLINE_ENTRY_BET);
        anteRound.act(action1);

        verify(serverAdapter).notifyActionPerformed(action1, player1);
        verify(serverAdapter).notifyPlayerBalance(player1);

        ArgumentCaptor<PokerAction> captor = ArgumentCaptor.forClass(PokerAction.class);
        verify(serverAdapter, Mockito.times(2)).notifyActionPerformed(captor.capture(), Mockito.eq(player1));

        PokerAction declineAction = captor.getAllValues().get(0);
        assertThat(declineAction, is(action1));

        PokerAction declineAction2 = captor.getAllValues().get(1);
        assertThat(declineAction2.getActionType(), is(PokerActionType.DECLINE_ENTRY_BET));
        assertThat(declineAction2.getPlayerId(), is(player2.getId()));

    }


    @SuppressWarnings("unchecked")
    @Test
    public void testActOnDeclineAnte() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);
        int player1Id = 1337;
        when(context.getPlayerInCurrentHand(player1Id)).thenReturn(player1);
        BigDecimal anteLevel = new BigDecimal(1000);
        when(settings.getAnteAmount()).thenReturn(anteLevel);

        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(false);
        //        when(realAnteRoundHelper.getNextPlayerToAct(Mockito.eq(0), Mockito.any(SortedMap.class))).thenReturn(player2);
        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        BigDecimal resultingBalance = new BigDecimal(343);
        when(player1.getBalance()).thenReturn(resultingBalance);

        PokerAction action = new PokerAction(player1Id, PokerActionType.DECLINE_ENTRY_BET);
        anteRound.act(action);

        verify(player1, never()).addBet(anteLevel);
        verify(player1).setHasActed(true);
        verify(player1, times(2)).setHasPostedEntryBet(false);
        verify(serverAdapter).notifyActionPerformed(action, player1);
        verify(serverAdapter).notifyPlayerBalance(player1);
    }

    @Test
    public void testCancelHandWhenAllButOneRejectedAnte() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, realAnteRoundHelper);

        int player1Id = 1;
        DefaultPokerPlayer player1 = createPlayer(player1Id, 1000L);
        int player2Id = 2;
        DefaultPokerPlayer player2 = createPlayer(player2Id, 1000L);

        SortedMap<Integer, PokerPlayer> playerMap = new TreeMap<Integer, PokerPlayer>();
        playerMap.put(player1Id, player1);
        playerMap.put(player2Id, player2);

        addAnteActionRequestToPlayer(player1);

        when(context.getPlayerInCurrentHand(player1Id)).thenReturn(player1);
        when(context.getPlayerInCurrentHand(player2Id)).thenReturn(player2);
        when(context.getCurrentHandSeatingMap()).thenReturn(playerMap);
        PokerAction action = new PokerAction(player1Id, PokerActionType.DECLINE_ENTRY_BET);

        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        anteRound.act(action);

        assertThat(anteRound.isCanceled(), is(true));
    }

    @Test
    public void testCancelHandWhenAllButOneRejectedAnte3Players() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, realAnteRoundHelper);

        int player1Id = 1;
        DefaultPokerPlayer player1 = createPlayer(player1Id, 1000L);
        int player2Id = 2;
        DefaultPokerPlayer player2 = createPlayer(player2Id, 1000L);
        int player3Id = 3;
        DefaultPokerPlayer player3 = createPlayer(player3Id, 1000L);

        SortedMap<Integer, PokerPlayer> playerMap = new TreeMap<Integer, PokerPlayer>();
        playerMap.put(player1Id, player1);
        playerMap.put(player2Id, player2);
        playerMap.put(player3Id, player3);

        addAnteActionRequestToPlayer(player1);
        addAnteActionRequestToPlayer(player2);

        when(context.getPlayerInCurrentHand(player1Id)).thenReturn(player1);
        when(context.getPlayerInCurrentHand(player2Id)).thenReturn(player2);
        when(context.getPlayerInCurrentHand(player3Id)).thenReturn(player3);
        when(context.getCurrentHandSeatingMap()).thenReturn(playerMap);


        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        PokerAction action1 = new PokerAction(player1Id, PokerActionType.DECLINE_ENTRY_BET);
        anteRound.act(action1);
        assertThat(anteRound.isCanceled(), is(false));

        PokerAction action2 = new PokerAction(player2Id, PokerActionType.DECLINE_ENTRY_BET);
        anteRound.act(action2);
        assertThat(anteRound.isCanceled(), is(true));
    }

    private void addAnteActionRequestToPlayer(DefaultPokerPlayer player) {
        ActionRequest playerActionRequest = new ActionRequest();
        playerActionRequest.enable(new PossibleAction(PokerActionType.ANTE));
        player.setActionRequest(playerActionRequest);
    }

    private DefaultPokerPlayer createPlayer(int playerId, long balance) {
        DefaultPokerPlayer player = new DefaultPokerPlayer(playerId);
        player.setBalance(new BigDecimal(1000));
        when(context.getPlayerInCurrentHand(playerId)).thenReturn(player);
        return player;
    }

    @Test
    public void testIsFinished() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, realAnteRoundHelper);

        when(player1.hasActed()).thenReturn(true);
        when(player1.hasPostedEntryBet()).thenReturn(true);
        when(player2.hasActed()).thenReturn(false);
        assertThat(anteRound.isFinished(), is(false));

        when(player1.hasActed()).thenReturn(true);
        when(player1.hasPostedEntryBet()).thenReturn(true);
        when(player2.hasActed()).thenReturn(true);
        when(player2.hasPostedEntryBet()).thenReturn(true);
        assertThat(anteRound.isFinished(), is(true));
    }

    @Test
    public void testIsFinishedFalseOnTooFewAntes() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, realAnteRoundHelper);

        when(player1.hasActed()).thenReturn(true);
        when(player1.hasPostedEntryBet()).thenReturn(false);
        when(player2.hasActed()).thenReturn(true);
        when(player2.hasPostedEntryBet()).thenReturn(false);
        assertThat(anteRound.isFinished(), is(true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsCanceled() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);

        // both declined: canceled
        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(true);
        when(anteRoundHelper.numberOfPlayersPayedAnte(Mockito.anyCollection())).thenReturn(0);
        assertThat(anteRound.isCanceled(), is(true));

        // one declined: canceled
        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(true);
        when(anteRoundHelper.numberOfPlayersPayedAnte(Mockito.anyCollection())).thenReturn(1);
        assertThat(anteRound.isCanceled(), is(true));

        // both accepted: not canceled
        when(anteRoundHelper.hasAllPlayersActed(Mockito.anyCollection())).thenReturn(true);
        when(anteRoundHelper.numberOfPlayersPayedAnte(Mockito.anyCollection())).thenReturn(2);
        assertThat(anteRound.isCanceled(), is(false));
    }

    @Test
    public void testTimeoutDeclinesAllOutstandingAntes() {
        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);

        SortedMap<Integer, PokerPlayer> playerMap = new TreeMap<Integer, PokerPlayer>();
        playerMap.put(1, player1);
        playerMap.put(2, player2);
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(false);

        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        when(context.getCurrentHandSeatingMap()).thenReturn(playerMap);

        anteRound.timeout();

        verify(player1, never()).setHasActed(true);
        verify(player2).setHasActed(true);
        verify(player2).setHasFolded(true);
        verify(player2, times(2)).setHasPostedEntryBet(false);
        verify(serverAdapter).notifyActionPerformed(Mockito.any(PokerAction.class), Mockito.eq(player2));
        verify(serverAdapter).notifyPlayerBalance(player2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testSitOutDeclinesAnte() {

        when(player2.isSittingOut()).thenReturn(true);

        SortedMap<Integer, PokerPlayer> playerMap = new TreeMap<Integer, PokerPlayer>();
        playerMap.put(1, player1);
        playerMap.put(2, player2);
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(false);


        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        when(context.getPlayersInHand()).thenReturn(playerMap.values());

        AnteRound anteRound = new AnteRound(context, serverAdapterHolder, anteRoundHelper);

        // Verify that only player 1 got a request for Ante since player 2 is sitting out
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(anteRoundHelper).requestAntes(captor.capture());
        Collection<PokerPlayer> captured = captor.getValue();

        assertThat(captured.size(), is(1));
        assertThat(captured.iterator().next(), is(player1));

        // Player 1 posts ANTE
        // Player 2 should have been auto act to decline ante

        PokerAction action1 = new PokerAction(player1.getId(), PokerActionType.ANTE);
        anteRound.act(action1);

        verify(player1).setHasActed(true);
        verify(player1).setHasPostedEntryBet(true);
        verify(player2).setHasActed(true);
        verify(player2).setHasFolded(true);
        verify(player2, times(2)).setHasPostedEntryBet(false);
    }

}
