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
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnteRoundHelperTest {

    @Mock
    private PokerPlayer player1;
    @Mock
    private PokerPlayer player2;
    @Mock
    private PokerPlayer player3;
    @Mock
    PokerContext context;
    @Mock
    PokerSettings settings;
    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    @Mock
    private ServerAdapter serverAdapter;
    @Mock
    private BlindsInfo blindsInfo;

    private AnteRoundHelper helper;


    @Before
    public void setup() {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getBlindsInfo()).thenReturn(blindsInfo);
        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        when(context.getSettings()).thenReturn(settings);
        helper = new AnteRoundHelper(context, serverAdapterHolder);
    }

    @Test
    public void testHasAllPlayersActed() {
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(true);
        when(player3.hasActed()).thenReturn(false);

        assertThat(helper.hasAllPlayersActed(asList(player1, player2, player3)), is(false));
        assertThat(helper.hasAllPlayersActed(asList(player1, player2)), is(true));
        assertThat(helper.hasAllPlayersActed(asList(player1, player3)), is(false));
        assertThat(helper.hasAllPlayersActed(Collections.<PokerPlayer>emptyList()), is(true));
    }

    @Test
    public void testNumberOfPlayersPayedAnte() {
        when(player1.hasPostedEntryBet()).thenReturn(true);
        when(player2.hasPostedEntryBet()).thenReturn(false);
        when(player3.hasPostedEntryBet()).thenReturn(true);

        assertThat(helper.numberOfPlayersPayedAnte(asList(player1, player2, player3)), is(2));
        assertThat(helper.numberOfPlayersPayedAnte(asList(player1, player2)), is(1));
        assertThat(helper.numberOfPlayersPayedAnte(Collections.<PokerPlayer>emptyList()), is(0));
    }

    @Test
    public void testNumberOfPendingPlayers() {
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(false);
        when(player3.hasActed()).thenReturn(true);

        assertThat(helper.numberOfPendingPlayers(asList(player1, player2, player3)), is(1));
        assertThat(helper.numberOfPendingPlayers(asList(player1, player2)), is(1));
        assertThat(helper.numberOfPendingPlayers(Collections.<PokerPlayer>emptyList()), is(0));
    }

    @Test
    public void testCanPlayerAct() {
        when(player1.hasActed()).thenReturn(false);
        when(player1.isAllIn()).thenReturn(false);
        when(player1.isSittingOut()).thenReturn(false);
        when(player1.hasFolded()).thenReturn(false);
        assertThat(helper.canPlayerAct(player1), is(true));

        when(player1.hasActed()).thenReturn(true);
        when(player1.isAllIn()).thenReturn(false);
        when(player1.isSittingOut()).thenReturn(false);
        when(player1.hasFolded()).thenReturn(false);
        assertThat(helper.canPlayerAct(player1), is(false));

        when(player1.hasActed()).thenReturn(true);
        when(player1.isAllIn()).thenReturn(true);
        when(player1.isSittingOut()).thenReturn(true);
        when(player1.hasFolded()).thenReturn(true);
        assertThat(helper.canPlayerAct(player1), is(false));
    }

    @Test
    public void testRequestAntes() {
        BigDecimal anteLevel = new BigDecimal(100);
        when(settings.getAnteAmount()).thenReturn(anteLevel);
        ActionRequest actionRequest1 = new ActionRequest();
        ActionRequest actionRequest2 = new ActionRequest();
        when(player1.getActionRequest()).thenReturn(actionRequest1);
        when(player2.getActionRequest()).thenReturn(actionRequest2);

        helper.requestAntes(Arrays.asList(player1, player2));

        verify(serverAdapter).requestMultipleActions(Arrays.asList(actionRequest1, actionRequest2));

        ArgumentCaptor<PossibleAction> possibleActionCaptor = ArgumentCaptor.forClass(PossibleAction.class);
        verify(player1, times(2)).enableOption(possibleActionCaptor.capture());

        assertThat(possibleActionCaptor.getAllValues().get(0).getActionType(), is(PokerActionType.ANTE));
        assertThat(possibleActionCaptor.getAllValues().get(0).getMinAmount(), is(anteLevel));
        assertThat(possibleActionCaptor.getAllValues().get(1).getActionType(), is(PokerActionType.DECLINE_ENTRY_BET));

        ArgumentCaptor<PossibleAction> possibleActionCaptor2 = ArgumentCaptor.forClass(PossibleAction.class);
        verify(player2, times(2)).enableOption(possibleActionCaptor2.capture());

        assertThat(possibleActionCaptor2.getAllValues().get(0).getActionType(), is(PokerActionType.ANTE));
        assertThat(possibleActionCaptor2.getAllValues().get(0).getMinAmount(), is(anteLevel));
        assertThat(possibleActionCaptor2.getAllValues().get(1).getActionType(), is(PokerActionType.DECLINE_ENTRY_BET));
    }

    @Test
    public void testIsImpossibleToStartRound() {
        when(player1.hasActed()).thenReturn(true);
        when(player1.hasPostedEntryBet()).thenReturn(true);
        when(player2.hasActed()).thenReturn(true);
        when(player2.hasPostedEntryBet()).thenReturn(true);
        when(player3.hasActed()).thenReturn(false);
        when(player3.hasPostedEntryBet()).thenReturn(false);
        List<PokerPlayer> players = Arrays.asList(player1, player2, player3);

        assertThat(helper.isImpossibleToStartRound(players), is(false));

        when(player1.hasActed()).thenReturn(true);
        when(player1.hasPostedEntryBet()).thenReturn(false);
        when(player2.hasActed()).thenReturn(true);
        when(player2.hasPostedEntryBet()).thenReturn(false);
        when(player3.hasActed()).thenReturn(false);
        when(player3.hasPostedEntryBet()).thenReturn(false);

        assertThat(helper.isImpossibleToStartRound(players), is(true));
    }

    @Test
    public void testSetAllPendingPlayersToDeclineEntryBet() {
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(false);
        when(player3.hasActed()).thenReturn(true);


        List<PokerPlayer> players = Arrays.asList(player1, player2, player3);
        helper.setAllPendingPlayersToDeclineEntryBet(players);
        verify(player1, never()).setHasActed(true);
        verify(player2).setHasActed(true);
        verify(player3, never()).setHasActed(true);

    }

}
