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

package com.cubeia.poker.rounds.blinds;

import com.cubeia.poker.*;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.blinds.BlindsCalculator;
import com.cubeia.poker.blinds.MissedBlindsStatus;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.RakeCalculator;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.texasholdem.NonRandomSeatProvider;
import com.google.common.base.Predicate;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.SortedMap;

import static com.cubeia.poker.TestUtils.createMockPlayers;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BlindsRoundTest extends TestCase {

    private BlindsRound round;

    private ActionRequest requestedAction;

    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private PokerSettings settings;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private RakeCalculator rakeCalculator;

    private Predicate<PokerPlayer> allGood = new Predicate<PokerPlayer>() {
        @Override
        public boolean apply(@Nullable PokerPlayer pokerPlayer) {
            return true;
        }
    };

    private BlindsCalculator blindsCalculator = new BlindsCalculator(new NonRandomSeatProvider());

    @Override
    protected void setUp() throws Exception {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(settings.getTiming()).thenReturn(new DefaultTimingProfile());
        context = new PokerContext(settings);
        context.setPotHolder(new PotHolder(rakeCalculator));
        when(settings.getSmallBlindAmount()).thenReturn(BigDecimal.ZERO);
        when(settings.getBigBlindAmount()).thenReturn(BigDecimal.ZERO);
    }

    public void testBasicBlinds() throws Exception {
        // Seat three players.
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        

        // Check that the next player gets the small blind.
        assertOptionEnabled(PokerActionType.SMALL_BLIND, p[1]);

        // Post small blind.
        verifyAndAct(p[1], PokerActionType.SMALL_BLIND);

        // Check that the next player gets the big blind.
        assertOptionEnabled(PokerActionType.BIG_BLIND, p[2]);

        // Post big blind.
        verifyAndAct(p[2], PokerActionType.BIG_BLIND);

        // Check that the blinds round is finished.
        assertTrue(round.isFinished());
    }

    private void addPlayers(MockPlayer[] p) {
        SortedMap<Integer,PokerPlayer> sortedMap = TestUtils.asSeatingMap(p);
        for (PokerPlayer player : p) {
            context.addPlayer(player);
        }
        context.prepareReadyPlayers(allGood);
    }

    public void testHeadsUpBlinds() throws Exception {
        // Seat two players.
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        

        // Check that the dealer gets the small blind.
        assertOptionEnabled(PokerActionType.SMALL_BLIND, p[0]);

        // Post small blind.
        verifyAndAct(p[0], PokerActionType.SMALL_BLIND);

        // Check that the next player gets the big blind.
        assertOptionEnabled(PokerActionType.BIG_BLIND, p[1]);

        // Post big blind.
        verifyAndAct(p[1], PokerActionType.BIG_BLIND);

        // Check that the blinds round is finished.
        assertTrue(round.isFinished());
    }

    public void testHeadsUpBlindsSecondHand() throws Exception {
        // Seat two players.
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[0], PokerActionType.SMALL_BLIND);
        act(p[1], PokerActionType.BIG_BLIND);
        context.setBlindsInfo(round.getBlindsInfo());
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[1], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[0], PokerActionType.BIG_BLIND);
        assertEquals(1, round.getBlindsInfo().getDealerButtonSeatId());
    }

    public void testMoveFromHeadsUpToNonHeadsUp() throws Exception {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        setPreviousBlindsInfo(0, 0, 1);
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[1], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[2], PokerActionType.BIG_BLIND);

    }

    public void testMoveFromNonHeadsUpToHeadsUp() throws Exception {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[1], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[0], PokerActionType.BIG_BLIND);
    }

    public void testMoveFromNonHeadsUpToHeadsUpSbLeaves() throws Exception {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        setPreviousBlindsInfo(1, 2, 0);
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[0], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[1], PokerActionType.BIG_BLIND);
        assertEquals(0, round.getBlindsInfo().getDealerButtonSeatId());
    }

    public void testKeepHeadsUpToHeadsUpBbLeaves() throws Exception {
        MockPlayer[] p = createMockPlayers(2);

        MockPlayer p2 = new MockPlayer(2);
        p2.setBalance(new BigDecimal(10000));
        p[1] = p2;

        addPlayers(p);
        setPreviousBlindsInfo(0, 0, 1);
        p[0].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[0], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[1], PokerActionType.BIG_BLIND);
        assertEquals(p[0].getSeatId(), round.getBlindsInfo().getDealerButtonSeatId());
    }

    private void assertOptionEnabled(PokerActionType option, MockPlayer player) {
        assertTrue(player.getActionRequest().isOptionEnabled(option));
    }

    public void testOutOfOrderActing() throws Exception {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        // Wrong player posts small blind.
        ActionRequest before = getRequestedAction();
        act(p[0], PokerActionType.SMALL_BLIND);
        assertEquals(before, requestedAction);

        // Right player posts wrong thing
        act(p[1], PokerActionType.BIG_BLIND);
        assertEquals(before, requestedAction);

        // Right player posts right thing at wrong time
        act(p[2], PokerActionType.BIG_BLIND);
        assertEquals(before, requestedAction);

        // Check that only the small blind has been asked so far.
        verify(serverAdapter, times(1)).requestAction(Matchers.<ActionRequest>any());

        // Ok, no more fooling around.
        act(p[1], PokerActionType.SMALL_BLIND);

        // And let's be a pain again.
        // Wrong player posts big blind.
        before = requestedAction;
        act(p[1], PokerActionType.BIG_BLIND);
        assertEquals(before, requestedAction);

        // Right player posts wrong thing
        act(p[2], PokerActionType.SMALL_BLIND);
        assertEquals(before, requestedAction);

        // And be nice.
        act(p[2], PokerActionType.BIG_BLIND);

        // Check that the blinds round is finished.
        assertTrue(round.isFinished());
    }

    public void testDealerButtonPosition() throws Exception {
        // Seat three players.
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        
        assertEquals(0, round.getBlindsInfo().getDealerButtonSeatId());
    }

    public void testSecondHandsDealerButtonPosition() throws Exception {
        MockPlayer[] p = createMockPlayers(3);
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        
        assertEquals(1, round.getBlindsInfo().getDealerButtonSeatId());
    }

    public void testDeclineSmallBlindWithTwoPlayersEndsHand() {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        assertOptionEnabled(PokerActionType.DECLINE_ENTRY_BET, p[0]);
        verifyAndAct(p[0], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(round.isCanceled());
    }

    public void testDeclineSmallBlind() {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        assertOptionEnabled(PokerActionType.DECLINE_ENTRY_BET, p[1]);
        verifyAndAct(p[1], PokerActionType.DECLINE_ENTRY_BET);
        verifyAndAct(p[2], PokerActionType.BIG_BLIND);
        assertTrue(round.isFinished());
    }


    public void testTimeout() {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        round.timeout();
        assertTrue(round.isCanceled());
    }

    public void testTimeoutBigBlind() {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[0], PokerActionType.SMALL_BLIND);
        round.timeout();
        assertTrue(round.isFinished());
    }

    public void testDenyBigBlind() {
        MockPlayer[] p = createMockPlayers(2);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        verifyAndAct(p[0], PokerActionType.SMALL_BLIND);
        verifyAndAct(p[1], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(round.isFinished());
    }

    public void testPlayerAfterDeniedBBGetsAskedForBB() {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.DECLINE_ENTRY_BET);
        verifyAndAct(p[0], PokerActionType.BIG_BLIND);
        assertTrue(round.isFinished());
    }

    public void testSamePlayerDeclinesBBThenPostsBB() {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.DECLINE_ENTRY_BET);
        ActionRequest before = getRequestedAction();
        act(p[2], PokerActionType.BIG_BLIND);
        assertEquals(before, getRequestedAction());
    }

    public void testNoMoreBigBlinds() {
        MockPlayer[] p = createMockPlayers(3);
        addPlayers(p);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.DECLINE_ENTRY_BET);
        act(p[0], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(round.isFinished());
    }

    public void testSittingOutPlayerIsNotAskedToPostSmallBlind() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        p[1].setSitOutStatus(SitOutStatus.SITTING_OUT);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        getRequestedAction();
        assertEquals(p[2].getId(), requestedAction.getPlayerId());
    }

    private ActionRequest getRequestedAction() {
        ArgumentCaptor<ActionRequest> captor = ArgumentCaptor.forClass(ActionRequest.class);
        verify(serverAdapter, atLeastOnce()).requestAction(captor.capture());
        requestedAction = captor.getValue();
        return requestedAction;
    }

    public void testSittingOutPlayerIsNotAskedToPostBigBlind() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);

        p[2].setSitOutStatus(SitOutStatus.SITTING_OUT);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        
        act(p[1], PokerActionType.SMALL_BLIND);

        assertEquals(p[3].getId(), requestedAction.getPlayerId());
    }

    public void testSittingOutPlayerIsNotAskedToPostBigBlindAfterInit() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);

        p[3].setSitOutStatus(SitOutStatus.SITTING_OUT);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        
        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.DECLINE_ENTRY_BET);

        assertEquals(p[0].getId(), requestedAction.getPlayerId());
    }

    public void testConsiderHandFirstOnTableWhenNoPlayersHavePosted() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        
        assertEquals(0, round.getBlindsInfo().getDealerButtonSeatId());
    }

    public void testNonEnteredPlayerCannotPostSmallBlind() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 7);
        context.getBlindsInfo().setBigBlindSeatId(2);
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        // Small blind is on seat 1, but seat 2 has not posted the entry bet, so he should not be asked to post small blind.
        getRequestedAction();
        assertTrue(requestedAction.isOptionEnabled(PokerActionType.BIG_BLIND));
        assertEquals(103, requestedAction.getPlayerId());
    }

    public void testEntryBet() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 0.
        p[0].setHasPostedEntryBet(false);
        p[0].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        

        act(p[2], PokerActionType.SMALL_BLIND);
        act(p[3], PokerActionType.BIG_BLIND);

        assertEquals(100, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
    }

    public void testWaitForBigBlind() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 0.
        p[0].setHasPostedEntryBet(false);
        p[0].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[2], PokerActionType.SMALL_BLIND);
        act(p[3], PokerActionType.BIG_BLIND);

        assertEquals(100, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
        act(p[0], PokerActionType.WAIT_FOR_BIG_BLIND);

        assertTrue(round.isFinished());

        // Player 0 should now get to post the big blind.
        BlindsInfo blindsInfo = round.getBlindsInfo();
        setPreviousBlindsInfo(blindsInfo);
        context.prepareReadyPlayers(allGood);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        act(p[3], PokerActionType.SMALL_BLIND);
        act(p[0], PokerActionType.BIG_BLIND);

        assertTrue(round.isFinished());
    }

    public void testDeclineEntryBet() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 0.
        p[0].setHasPostedEntryBet(false);
        p[0].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        

        act(p[2], PokerActionType.SMALL_BLIND);

        act(p[3], PokerActionType.BIG_BLIND);
        act(p[0], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(round.isFinished());
    }

    public void testEntryBetTimeout() {
        MockPlayer[] p = createMockPlayers(4);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 0.
        p[0].setHasPostedEntryBet(false);
        p[0].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);
        

        act(p[2], PokerActionType.SMALL_BLIND);
        act(p[3], PokerActionType.BIG_BLIND);
        round.timeout();
        assertTrue(round.isFinished());
    }

    public void testNoEntryBetBetweenDealerButtonAndSmallBlind() {
        MockPlayer[] p = createMockPlayers(6);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 3);

        // Everyone has posted the entry bet, except player 4 and 5.
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(false);
        p[2].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[3].setHasPostedEntryBet(true);
        p[4].setHasPostedEntryBet(true);
        p[5].setHasPostedEntryBet(true);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[3], PokerActionType.SMALL_BLIND);

        // Everyone declines bb until the dealer.
        act(p[4], PokerActionType.DECLINE_ENTRY_BET);
        act(p[5], PokerActionType.DECLINE_ENTRY_BET);
        act(p[0], PokerActionType.DECLINE_ENTRY_BET);
        act(p[1], PokerActionType.BIG_BLIND);

        // Round should be finished (p2 can't post entry bet because he's between dealer and sb.
        assertTrue(round.isFinished());
    }

    public void testTwoEntryBets() {
        MockPlayer[] p = createMockPlayers(6);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 4 and 5.
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        p[4].setHasPostedEntryBet(false);
        p[4].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[5].setHasPostedEntryBet(false);
        p[5].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[2], PokerActionType.SMALL_BLIND);

        act(p[3], PokerActionType.BIG_BLIND);
        assertEquals(104, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
        act(p[4], PokerActionType.ENTRY_BET);

        assertEquals(105, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
        act(p[5], PokerActionType.ENTRY_BET);
    }

    public void testTwoEntryBetDeclines() {
        MockPlayer[] p = createMockPlayers(6);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 4 and 5.
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        p[4].setHasPostedEntryBet(false);
        p[4].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[5].setHasPostedEntryBet(false);
        p[5].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[2], PokerActionType.SMALL_BLIND);
        act(p[3], PokerActionType.BIG_BLIND);
        act(p[4], PokerActionType.DECLINE_ENTRY_BET);
        act(p[5], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(round.isFinished());
    }

    public void testEntryBetOutOfTurn() {
        MockPlayer[] p = createMockPlayers(6);
        addPlayers(p);
        setPreviousBlindsInfo(0, 1, 2);

        // Everyone has posted the entry bet, except player 4 and 5.
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(true);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        p[4].setHasPostedEntryBet(false);
        p[4].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[5].setHasPostedEntryBet(false);
        p[5].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[2], PokerActionType.SMALL_BLIND);
        act(p[3], PokerActionType.BIG_BLIND);

        assertEquals(p[4].getId(), getRequestedAction().getPlayerId());
        act(p[5], PokerActionType.BIG_BLIND);
        assertEquals(p[4].getId(), getRequestedAction().getPlayerId());
    }

    public void testTwoEntryBetsWithWrap() {
        MockPlayer[] p = createMockPlayers(6);
        addPlayers(p);
        setPreviousBlindsInfo(1, 2, 3);

        // Everyone has posted the entry bet, except player 5 and 1.
        p[0].setHasPostedEntryBet(true);
        p[1].setHasPostedEntryBet(false);
        p[1].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        p[2].setHasPostedEntryBet(true);
        p[3].setHasPostedEntryBet(true);
        p[4].setHasPostedEntryBet(true);
        p[5].setHasPostedEntryBet(false);
        p[5].setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        round = new BlindsRound(context, serverAdapterHolder, blindsCalculator);

        act(p[3], PokerActionType.SMALL_BLIND);
        act(p[4], PokerActionType.BIG_BLIND);
        assertEquals(105, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
        act(p[5], PokerActionType.ENTRY_BET);

        assertEquals(101, requestedAction.getPlayerId());
        assertThat(requestedAction.isOptionEnabled(PokerActionType.ENTRY_BET), is(true));
        assertFalse(round.isFinished());
        act(p[1], PokerActionType.ENTRY_BET);
        assertTrue(round.isFinished());
    }

    public void testMissedSmall() {
        
    }

    private void setPreviousBlindsInfo(int dealerSeatId, int smallSeatId, int bigSeatId) {
        BlindsInfo bi = new BlindsInfo();
        bi.setDealerButtonSeatId(dealerSeatId);
        bi.setSmallBlindSeatId(smallSeatId);
        bi.setBigBlindSeatId(bigSeatId);
        bi.setBigBlindPlayerId(bigSeatId + 100);

        context.setBlindsInfo(bi);
    }

    private void setPreviousBlindsInfo(BlindsInfo blindsInfo) {
        context.setBlindsInfo(blindsInfo);
    }

    private void verifyAndAct(MockPlayer player, PokerActionType action) {
        getRequestedAction();
        assertTrue("Player " + player + " should have option: " + action,
                player.getActionRequest().isOptionEnabled(action));
        assertTrue(requestedAction.isOptionEnabled(action));
        assertEquals(player.getId(), requestedAction.getPlayerId());
        act(player, action);
    }

    private void act(MockPlayer player, PokerActionType action) {
        PokerAction a = new PokerAction(player.getId(), action);
        round.act(a);
        getRequestedAction();
    }
}