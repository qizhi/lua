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
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BettingRoundTest extends TestCase {

    private BigDecimal minBet;

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
        when(settings.getTiming()).thenReturn(new DefaultTimingProfile());
        when(settings.getRakeSettings()).thenReturn(RakeSettings.createDefaultRakeSettings(new BigDecimal(0.01)));
        when(settings.getCurrency()).thenReturn(new Currency("EUR",2));
        when(adapterHolder.get()).thenReturn(adapter);
        minBet = new BigDecimal(10);
    }

    public void testHeadsUpBetting() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);

        preparePlayers(p);
        assertFalse("Round should not be finished.", round.isFinished());

        verifyAndAct(p[1], BET, 100);
        verifyAndAct(p[0], FOLD, 100);

        assertTrue(round.isFinished());
    }

    public void testCallAmount() {
        MockPlayer[] p = TestUtils.createMockPlayers(2, 100);

        preparePlayers(p);
        assertFalse(round.isFinished());
        act(p[1], BET, 70);

        PossibleAction bet = requestedAction.getOption(CALL);
        assertEquals(bd(70), bet.getMaxAmount());
    }

    public void testCallTellsState() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        context = createMockContext();
        round = createRound(new NoLimitBetStrategy(minBet));
        round.call(player);

        verify(context).callOrRaise();
    }

    public void testCall() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        BigDecimal betStack = new BigDecimal(75);
        when(player.getBetStack()).thenReturn(betStack);
        when(player.getBalance()).thenReturn(betStack.multiply(bd(10)));
        when(settings.getBigBlindAmount()).thenReturn(BigDecimal.ZERO);

        preparePlayers(player);

        round.highBet = bd(100);

        BigDecimal amountToCall = round.getAmountToCall(player);
        round.call(player);

        assertThat(amountToCall, is(round.highBet.subtract(betStack)));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    public void testCallNotifiesRakeInfo() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        preparePlayers(player);

        round.call(player);

        verify(adapter).notifyRakeInfo(Mockito.<RakeInfoContainer>any());
    }

    public void testHandleActionOnCallSetsAmountOnResponse() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        preparePlayers(player);

        round.highBet = bd(100);
        BigDecimal betStack = bd(75);
        when(player.getBetStack()).thenReturn(betStack);
        when(player.getBalance()).thenReturn(betStack.multiply(bd(10)));

        PokerAction action = new PokerAction(1337, CALL);

        round.handleAction(action, player);

        assertThat(action.getBetAmount(), is(round.highBet.subtract(betStack)));
        verify(player).setHasActed(true);
    }

    public void testRaise() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);

        preparePlayers(p);

        assertFalse(round.isFinished());
        verifyAndAct(p[1], BET, 100);

        assertTrue(requestedAction.isOptionEnabled(RAISE));
        verifyAndAct(p[0], RAISE, 200);
    }

    public void testRaiseNotifiesRakeInfo() {
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        ActionRequest request = mock(ActionRequest.class);
        when(player.getActionRequest()).thenReturn(request);
        when(request.getOption(RAISE)).thenReturn(new PossibleAction(RAISE, bd(10), bd(20)));
        preparePlayers(player);

        round.raise(player, bd(10));

        verify(adapter).notifyRakeInfo(Mockito.<RakeInfoContainer>any());
    }

    public void testRaiseNotifiesCallOrRaise() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        preparePlayers(player);
        ActionRequest request = mock(ActionRequest.class);
        when(player.getActionRequest()).thenReturn(request);
        when(request.getOption(RAISE)).thenReturn(new PossibleAction(RAISE, bd(10), bd(20)));

        context = createMockContext();
        round = createRound(new NoLimitBetStrategy(minBet));
        round.raise(player, bd(10));
        verify(context).callOrRaise();
    }

    public void testNoRaiseAllowedWhenAllOtherPlayersAreAllIn() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);
        preparePlayers(p);

        assertFalse(round.isFinished());

        actMax(BET);
        assertFalse(requestedAction.isOptionEnabled(RAISE));
    }

    public void testCallSetsLastPlayerToBeCalled() {
        MockPlayer[] p = createAndAddPlayersToBettingRound(2);

        act(p[1], BET, 100);
        act(p[0], CALL, 100);

        PokerPlayer player = p[1];
        assertThat(round.getLastPlayerToBeCalled(), CoreMatchers.is(player));
    }

    public void testRaiseAnAllInBetSetsLastCallerToAllInPlayer() {
        MockPlayer[] p = createAndAddPlayersToBettingRound(2);
        act(p[1], BET, 100);
        act(p[0], RAISE, 200);
        PokerPlayer player = p[1];
        assertThat(round.getLastPlayerToBeCalled(), CoreMatchers.is(player));
    }

    public void testBetNotifiesRakeInfo() {
        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        preparePlayers(player);

        ActionRequest actionRequest = Mockito.mock(ActionRequest.class);
        when(player.getActionRequest()).thenReturn(actionRequest);
        PossibleAction possibleAction = new PossibleAction(BET, bd(5), bd(10));
        when(actionRequest.getOption(BET)).thenReturn(possibleAction);

        round.bet(player, bd(10));

        verify(adapter).notifyRakeInfo(Mockito.<RakeInfoContainer>any());
    }

    public void testTimeoutTwice() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);
        preparePlayers(p);

        assertFalse(round.isFinished());

        round.timeout();
        round.timeout();

        assertTrue(round.isFinished());
    }

    public void testTimeout() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);
        preparePlayers(p);

        assertFalse(round.isFinished());

        verifyAndAct(p[1], BET, 100);
        round.timeout();
        assertTrue(round.isFinished());
    }

    public void testDealerLeft() {
        MockPlayer[] p = TestUtils.createMockPlayers(2);
        preparePlayers(p);

        assertFalse(round.isFinished());

        round.timeout();
        round.timeout();

        assertTrue(round.isFinished());
    }

    @SuppressWarnings("unchecked")
    public void testFutureActionsNotifiedWhenInitializingVanillaBetRound() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(p);

        // starting player gets empty list the others get check and fold
        verify(adapter).notifyFutureAllowedActions(eq(p[1]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[0]), argThat(new IsListOfNElements(2)), eq(bd(0)), eq(bd(10)));
        verify(adapter).notifyFutureAllowedActions(eq(p[2]), argThat(new IsListOfNElements(2)),eq(bd(0)),eq(bd(10)));
    }

    @SuppressWarnings("unchecked")
    public void testFutureActionsNotNotifiedWhenInitializingBetRoundAndAllPlayersSittingOut() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[0].forceAllIn(true);
        p[1].forceAllIn(true);
        p[2].forceAllIn(true);
        preparePlayers(p);

        verify(adapter).notifyFutureAllowedActions(eq(p[0]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[1]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[2]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
    }

    @SuppressWarnings("unchecked")
    public void testFutureActionsNotNotifiedWhenAllPlayersButOneAreAllIn() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[0].forceAllIn(true);
        p[1].forceAllIn(true);
        p[2].forceAllIn(false);
        preparePlayers(p);

        verify(adapter).notifyFutureAllowedActions(eq(p[0]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[1]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[2]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
    }

    @SuppressWarnings("unchecked")
    public void testFutureActionsNotifiedWhenPlayerActed() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(p);

        // starting player gets empty list the others get check and fold and raise
        verify(adapter).notifyFutureAllowedActions(eq(p[0]), argThat(new IsListOfNElements(2)),eq(bd(0)),eq(bd(10)));
        verify(adapter).notifyFutureAllowedActions(eq(p[1]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
        verify(adapter).notifyFutureAllowedActions(eq(p[2]), argThat(new IsListOfNElements(2)),eq(bd(0)),eq(bd(10)));

        PokerAction action = new PokerAction(p[1].getId(), PokerActionType.CHECK);
        round.act(action);

        // next player gets empty list the others get check and fold
        verify(adapter, times(2)).notifyFutureAllowedActions(eq(p[0]), argThat(new IsListOfNElements(2)),eq(bd(0)),eq(bd(10)));
        verify(adapter).notifyFutureAllowedActions(eq(p[1]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(10)));
        verify(adapter).notifyFutureAllowedActions(eq(p[2]), argThat(new IsListOfNElements(0)),eq(bd(0)),eq(bd(0)));
    }

    public void testIncompleteBetShouldNotIncreaseHighestCompleteBet() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(p);

        act(p[1], BET, 9);
        assertThat(round.getHighestCompleteBet(), is(bd(0)));
        assertThat(round.getSizeOfLastCompleteBetOrRaise(), is(bd(0)));
    }

    public void testIncompleteRaiseShouldNotIncreaseHighestCompleteBet() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[2].setBalance(bd(49));
        preparePlayers(p);

        act(p[1], BET, 25);
        act(p[2], RAISE, 49);
        assertThat(round.getHighestCompleteBet(), is(bd(25)));
        assertThat(round.getSizeOfLastCompleteBetOrRaise(), is(bd(25)));
        assertThat(p[0].getActionRequest().getOption(RAISE).getMinAmount(), is(bd(49 + 25)));
    }

    /*
     * In fixed limit, if the big blind is $10 and player A bets $7, it counts as a complete bet
     * which means that the next raise should be to $20.
     */
    public void testCompleteBetShouldIncreaseCompleteBetAllTheWayToTheNextLevel() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[1].setBalance(bd(7));
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 7);
        assertThat(round.getHighestCompleteBet(), is(bd(10)));
        assertThat(round.getSizeOfLastCompleteBetOrRaise(), is(bd(10))); // (This is a bit undefined, the fixed limit strategy doesn't use this value though)
        assertThat(p[2].getActionRequest().getOption(RAISE).getMinAmount(), is(bd(20)));
    }

    /*
     * In fixed limit, the betting should be capped after 4 bets.
     */
    public void testBettingCappedAfterFourBets() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 10);
        act(p[2], RAISE, 20);
        act(p[0], RAISE, 30);
        act(p[1], RAISE, 40);
        assertThat(requestedAction.isOptionEnabled(RAISE), is(false));
    }

    /*
     * There's no cap in fixed limit if betting is two-handed (heads up).
     */
    public void testBettingNoCappedAfterFourBetsWhenHeadsUp() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 10);
        act(p[2], RAISE, 20);
        act(p[0], RAISE, 30);
        act(p[1], FOLD, 0);
        act(p[2], RAISE, 40);
        act(p[0], RAISE, 50);
        assertThat(requestedAction.isOptionEnabled(RAISE), is(true));
    }
    /*
     * Once the betting is capped, it does not up again even if the round becomes heads up.
     */
    public void testCapStillActiveEvenIfBettingBecomesHeadsUp() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 10);
        act(p[2], RAISE, 20);
        act(p[0], RAISE, 30);
        act(p[1], RAISE, 40);
        act(p[2], FOLD, 0);
        assertThat(requestedAction.isOptionEnabled(RAISE), is(false));
    }


    public void testDefaultActionWhenAway() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], CHECK, 0);
        act(p[2], CHECK, 20);

        round.timeout();

        assertTrue(p[0].isSittingOutNextHand());
        assertTrue(p[0].isAway());
        assertFalse(p[0].hasFolded());
    }

    public void testFoldWhenAwayWithUncalledAmount() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[0].setBalance(bd(30));
        p[1].setBalance(bd(30));
        p[2].setBalance(bd(30));
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 10);
        act(p[2], CALL, 10);
        round.timeout();

        assertTrue(p[0].isAway());
        assertFalse(p[0].isSittingOutNextHand());
        assertTrue("Player should have been folded", p[0].hasFolded());
        assertTrue(p[0].isSittingOut());//when folded the player is set to sit out

    }

    public void testCompleteRaiseShouldIncreaseCompleteBetAllTheWayToTheNextLevel() {
        MockPlayer[] p = TestUtils.createMockPlayers(3);
        p[2].setBalance(bd(17));
        preparePlayers(new FixedLimitBetStrategy(bd(10), false), p);

        act(p[1], BET, 10);
        act(p[2], RAISE, 17);
        assertThat(round.getHighestCompleteBet(), is(bd(20)));
        assertThat(round.getSizeOfLastCompleteBetOrRaise(), is(bd(10)));
        assertThat(p[0].getActionRequest().getOption(CALL).getMinAmount(), is(bd(17)));
        assertThat(p[0].getActionRequest().getOption(RAISE).getMinAmount(), is(bd(30)));
    }

    // HELPERS

    private void act(MockPlayer player, PokerActionType action, long amount) {
        PokerAction a = new PokerAction(player.getId(), action);
        a.setBetAmount(new BigDecimal(amount));
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
        round = createRound(new NoLimitBetStrategy(minBet));
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
