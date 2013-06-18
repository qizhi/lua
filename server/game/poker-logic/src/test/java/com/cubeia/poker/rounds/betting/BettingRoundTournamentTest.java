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

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.texasholdem.TexasHoldemFutureActionsCalculator;
import com.google.common.base.Predicate;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

import static com.cubeia.poker.action.PokerActionType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BettingRoundTournamentTest extends TestCase {

    private int minBet;

    private PokerContext context;

    @Mock
    private ServerAdapterHolder adapterHolder;

    @Mock
    private ServerAdapter adapter;

    @Mock
    private PokerSettings settings;

    private ActionRequest requestedAction;

    private BettingRound round;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initMocks(this);
        context = new PokerContext(settings);
        context.setTournamentTable(true);
        context.setTournamentId(1);
        when(settings.getTiming()).thenReturn(new DefaultTimingProfile());
        when(settings.getRakeSettings()).thenReturn(RakeSettings.createDefaultRakeSettings(new BigDecimal(0.01)));
        when(settings.getCurrency()).thenReturn(new Currency("EUR",2));
        when(adapterHolder.get()).thenReturn(adapter);
        minBet = 10;
    }

    public void testHeadsUpBettingAllTimedOut() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);

        preparePlayers(p);

        assertFalse("Round should not be finished.", round.isFinished());

        round.timeout();
        round.timeout();

        assertTrue(p[0].isAway());
        assertFalse(p[0].isSittingOut());
        assertTrue(p[1].isAway());
        assertFalse(p[1].isSittingOut());

        assertTrue(round.isFinished());
    }
    public void testHeadsUpBettingTimedOut() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);

        preparePlayers(p);

        assertFalse("Round should not be finished.", round.isFinished());

        act(p[1],BET,10);
        round.timeout();

        assertFalse(p[1].isAway());
        assertFalse(p[1].isSittingOut());
        assertTrue(p[0].isAway());
        assertFalse(p[0].isSittingOut());
        assertTrue(p[0].hasFolded());

        assertTrue(round.isFinished());
    }

    // HELPERS
    private void act(MockPlayer player, PokerActionType action, long amount) {
        act(player,action,new BigDecimal(amount));
    }
    private void act(MockPlayer player, PokerActionType action, BigDecimal amount) {
        PokerAction a = new PokerAction(player.getId(), action);
        a.setBetAmount(amount);
        round.act(a);
        requestedAction = getRequestedAction();
    }

    private void verifyAndAct(MockPlayer player, PokerActionType action, long amount) {
        requestedAction = getRequestedAction();
        assertTrue("Tried to " + action + " but available actions were: " + player.getActionRequest().getOptions(),
                   player.getActionRequest().isOptionEnabled(action));
        assertTrue(requestedAction.isOptionEnabled(action));
        assertEquals(player.getId(), requestedAction.getPlayerId());
        act(player, action, amount);
    }

    private ActionRequest getRequestedAction() {
        ArgumentCaptor<ActionRequest> captor = ArgumentCaptor.forClass(ActionRequest.class);
        verify(adapter, atLeastOnce()).requestAction(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("rawtypes")
    class IsListOfNElements extends ArgumentMatcher<List> {
        private final int n;

        public IsListOfNElements(int n) {
            this.n = n;
        }

        public boolean matches(Object list) {
            return ((List) list).size() == n;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Should be a list of " + n + " elements");
        }
    }

    private void preparePlayers(PokerPlayer... p) {
        for (PokerPlayer player : p) {
            context.addPlayer(player);
        }
        context.prepareHand(readyPlayerFilter());
        round = createRound(new NoLimitBetStrategy(new BigDecimal(minBet)));
    }

    private void preparePlayers(BetStrategy betStrategy, PokerPlayer... p) {
        for (PokerPlayer player : p) {
            context.addPlayer(player);
        }
        context.prepareHand(readyPlayerFilter());
        round = createRound(betStrategy);
    }

    private Predicate<PokerPlayer> readyPlayerFilter() {
        return new Predicate<PokerPlayer>() {
            @Override
            public boolean apply(@Nullable PokerPlayer pokerPlayer) {
                return true;
            }
        };
    }

    private PokerContext createMockContext() {
        PokerContext mock = mock(PokerContext.class);
        when(mock.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        return mock;
    }

    private MockPlayer[] createAndAddPlayersToBettingRound(int numberOfPlayers) {
        MockPlayer[] p = TestUtils.createMockPlayers(numberOfPlayers);
        preparePlayers(p);
        return p;
    }

    private void actMax(PokerActionType action) {
        requestedAction = getRequestedAction();
        PossibleAction option = requestedAction.getOption(action);
        PokerAction a = new PokerAction(requestedAction.getPlayerId(), action, option.getMaxAmount());
        round.act(a);
    }

    private BettingRound createRound(BetStrategy betStrategy) {
        ActionRequestFactory actionRequestFactory = new ActionRequestFactory(betStrategy);
        return new BettingRound(context, adapterHolder, new DefaultPlayerToActCalculator(0), actionRequestFactory,
                                new TexasHoldemFutureActionsCalculator(betStrategy.getType()), betStrategy);
    }
}
