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

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.states.StateChanger;
import com.cubeia.poker.timing.TimingFactory;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.variant.GameType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PokerStateTest {

    PokerState state;

    @Mock
    PokerSettings settings;

    @Mock
    GameType gameType;

    @Mock
    ServerAdapter serverAdapter;

    @Mock
    PokerContext context;

    @Mock
    StateChanger stateChanger;

    BigDecimal anteLevel;

    @Before
    public void setup() {
        initMocks(this);
        state = new PokerState();
        anteLevel = new BigDecimal(100);
        when(settings.getRakeSettings()).thenReturn(TestUtils.createOnePercentRakeSettings());
        when(settings.getAnteAmount()).thenReturn(anteLevel);
        when(settings.getTiming()).thenReturn(TimingFactory.getRegistry().getDefaultTimingProfile());
        when(gameType.canPlayerAffordEntryBet(Mockito.any(PokerPlayer.class), Mockito.any(PokerSettings.class), Mockito.eq(false))).thenReturn(true);

        state.setServerAdapter(serverAdapter);
        state.init(gameType, settings);
        state.pokerContext.settings = settings;
    }

    @Test
    public void testNotifyHandFinishedPendingBalanceTooHigh() {
        TimingProfile timingProfile = mock(TimingProfile.class);
        when(settings.getTiming()).thenReturn(timingProfile);
        when(settings.getMaxBuyIn()).thenReturn(new BigDecimal(100));

        DefaultPokerPlayer player1 = new DefaultPokerPlayer(1);
        player1.setBalance(bd(40));
        player1.addNotInHandAmount(bd(90));

        DefaultPokerPlayer player2 = new DefaultPokerPlayer(2);
        player2.setBalance(bd(220));
        player2.addNotInHandAmount(bd(120));

        state.pokerContext.playerMap.put(player1.getId(), player1);
        state.pokerContext.playerMap.put(player2.getId(), player2);

        state.commitPendingBalances();

        assertThat(player1.getBalance(), is(bd(100)));
        assertThat(player1.getBalanceNotInHand(), is(bd(30)));

        assertThat(player2.getBalance(), is(bd(220)));
        assertThat(player2.getBalanceNotInHand(), is(bd(120)));

    }

    @Test
    public void testCommitPendingBalances() {
        PokerState state = new PokerState();
        PokerSettings settings = mock(PokerSettings.class);
        when(settings.getMaxBuyIn()).thenReturn(bd(10000));

        state.init(gameType, settings);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);
        Map<Integer, PokerPlayer> playerMap = new HashMap<Integer, PokerPlayer>();
        playerMap.put(0, player1);
        playerMap.put(1, player2);
        state.pokerContext.playerMap = playerMap;

        state.commitPendingBalances();

        // Verify interaction and max buyin level
        verify(player1).commitBalanceNotInHand(bd(10000));
        verify(player2).commitBalanceNotInHand(bd(10000));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNotifyPotUpdated() {
        state.pokerContext.currentHandPlayerMap = new HashMap<Integer, PokerPlayer>();
        PokerPlayer player0 = mock(PokerPlayer.class);
        when(player0.getId()).thenReturn(1337);
        PokerPlayer player1 = mock(PokerPlayer.class);
        when(player1.getId()).thenReturn(1338);
        PokerPlayer player2 = mock(PokerPlayer.class);
        when(player2.getId()).thenReturn(1339);

        state.pokerContext.getCurrentHandPlayerMap().put(player0.getId(), player0);
        state.pokerContext.getCurrentHandPlayerMap().put(player1.getId(), player1);
        state.pokerContext.getCurrentHandPlayerMap().put(player2.getId(), player2);

        state.pokerContext.potHolder = mock(PotHolder.class);

        Collection<Pot> pots = new ArrayList<Pot>();
        when(state.pokerContext.getPotHolder().getPots()).thenReturn(pots);
        BigDecimal totalPot = new BigDecimal(3434);
        when(state.pokerContext.getPotHolder().getTotalPotSize()).thenReturn(totalPot);
        BigDecimal totalRake = new BigDecimal("4444");

        when(state.pokerContext.getPotHolder().calculateRake()).thenReturn(new RakeInfoContainer(totalPot, totalRake, null));
        RakeInfoContainer rakeInfoContainer = mock(RakeInfoContainer.class);

        when(state.pokerContext.getPotHolder().calculateRakeIncludingBetStacks(anyCollection())).thenReturn(rakeInfoContainer);

        Collection<PotTransition> potTransitions = new ArrayList<PotTransition>();
        state.notifyPotAndRakeUpdates(potTransitions);

        verify(serverAdapter).notifyPotUpdates(pots, potTransitions);
        verify(serverAdapter).notifyPlayerBalance(player0);
        verify(serverAdapter).notifyPlayerBalance(player1);
        verify(serverAdapter).notifyPlayerBalance(player2);

        ArgumentCaptor<RakeInfoContainer> rakeInfoCaptor = ArgumentCaptor.forClass(RakeInfoContainer.class);
        verify(serverAdapter).notifyRakeInfo(rakeInfoCaptor.capture());
        RakeInfoContainer rakeInfoContainer1 = rakeInfoCaptor.getValue();
        assertThat(rakeInfoContainer1, is(rakeInfoContainer));

    }

    @Test
    public void testGetTotalPotSize() {
        state.pokerContext.potHolder = mock(PotHolder.class);

        PokerPlayer player0 = mock(PokerPlayer.class);
        Integer player0id = 13371;
        when(player0.getId()).thenReturn(player0id);
        when(player0.getBetStack()).thenReturn(bd(10)); // Bet

        PokerPlayer player1 = mock(PokerPlayer.class);
        Integer player1id = 13372;
        when(player1.getId()).thenReturn(player1id);
        when(player1.getBetStack()).thenReturn(bd(10)); // Raise

        PokerPlayer player2 = mock(PokerPlayer.class);
        Integer player2id = 13373;
        when(player2.getId()).thenReturn(player2id);
        when(player2.getBetStack()).thenReturn(bd(0)); // Nothing yet

        Collection<Pot> pots = new ArrayList<Pot>();
        when(state.pokerContext.getPotHolder().getPots()).thenReturn(pots);
        BigDecimal totalPot = new BigDecimal(500); // already bet in earlier betting rounds
        when(state.pokerContext.getPotHolder().getTotalPotSize()).thenReturn(totalPot);

        Map<Integer, PokerPlayer> playerMap = new HashMap<Integer, PokerPlayer>();
        playerMap.put(player0.getId(), player0);
        playerMap.put(player1.getId(), player1);
        playerMap.put(player2.getId(), player2);

        state.pokerContext.currentHandPlayerMap = playerMap;

        assertThat(state.pokerContext.getTotalPotSize(), is(bd(520)));

    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    @Test
    public void testPotsClearedAtStartOfHand() {
        PokerPlayer player1 = mockPlayer(1);
        PokerPlayer player2 = mockPlayer(2);

        assertThat(state.pokerContext.getPotHolder(), nullValue());
        state.addPlayer(player1);
        state.addPlayer(player2);
        state.timeout();
        assertThat(state.pokerContext.getPotHolder(), notNullValue());
    }

    private PokerPlayer mockPlayer(int id) {
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getId()).thenReturn(id);
        when(player.getSeatId()).thenReturn(id);
        when(player.isSittingOut()).thenReturn(false);
        return player;
    }

    @Test
    public void testNotifyStatusesAtStartOfHand() {
        state.pokerContext.potHolder = new PotHolder(null);
        RakeSettings rakeSettings = TestUtils.createOnePercentRakeSettings();
        BlindsLevel level = new BlindsLevel(bd(0),bd(0), bd(0));
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        state.pokerContext.settings = new PokerSettings(level, betStrategy, bd(0), bd(0), null, 4, rakeSettings, new Currency("EUR",2), null);

        state.pokerContext.playerMap = new HashMap<Integer, PokerPlayer>();
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);

        int player1Id = 1337;
        int player2Id = 666;

        when(player1.getBalanceNotInHand()).thenReturn(bd(100));
        when(player2.getBalanceNotInHand()).thenReturn(bd(100));

        when(player1.getBalance()).thenReturn(bd(10));
        when(player2.getBalance()).thenReturn(bd(10));

        when(player1.getId()).thenReturn(player1Id);
        when(player2.getId()).thenReturn(player2Id);

        when(player1.isSittingOut()).thenReturn(false);
        when(player2.isSittingOut()).thenReturn(false);

        state.pokerContext.playerMap.put(player1Id, player1);
        state.pokerContext.playerMap.put(player2Id, player2);

        state.pokerContext.seatingMap.put(0, player1);
        state.pokerContext.seatingMap.put(1, player2);

        state.timeout();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleBuyInRequestWhileGamePlaying() {
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);

        when(player1.getId()).thenReturn(1);
        when(player2.getId()).thenReturn(2);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.timeout();
        assertTrue(state.isPlaying());
        BigDecimal amount = new BigDecimal(1234);

        context.getCurrentHandPlayerMap().put(1, player1);
        state.handleBuyInRequest(player1, amount);

        verify(player1).addRequestedBuyInAmount(amount);
        // This is called once when starting the hand, but should not be called twice
        verify(serverAdapter, times(1)).performPendingBuyIns(anyCollection());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testHandleBuyInRequestWhenWaitingToStart() {
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);

        when(player1.getId()).thenReturn(1);
        when(player2.getId()).thenReturn(2);

        state.addPlayer(player1);
        state.addPlayer(player2);
        assertFalse(state.isPlaying());

        BigDecimal amount = new BigDecimal(1234);
        state.handleBuyInRequest(player1, amount);

        verify(player1).addRequestedBuyInAmount(amount);
        // This is called once when starting the hand, but should not be called twice
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(serverAdapter).performPendingBuyIns(captor.capture());
        Collection<PokerPlayer> players = captor.getValue();
        assertThat(players.size(), is(1));
        assertThat(players, hasItem(player1));
    }
}
