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

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;

import static com.cubeia.poker.action.PokerActionType.BET;
import static com.cubeia.poker.action.PokerActionType.BIG_BLIND;
import static com.cubeia.poker.action.PokerActionType.CALL;
import static com.cubeia.poker.action.PokerActionType.CHECK;
import static com.cubeia.poker.action.PokerActionType.DECLINE_ENTRY_BET;
import static com.cubeia.poker.action.PokerActionType.FOLD;
import static com.cubeia.poker.action.PokerActionType.RAISE;
import static com.cubeia.poker.action.PokerActionType.SMALL_BLIND;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test for poker logic.
 */
public class PokerLogicTest extends GuiceTest {

    private MockPlayer[] ps;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSimpleHoldemHand() {
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        assertEquals(4, p.length);
        addPlayers(state, mp);
        assertEquals(4, state.getSeatedPlayers().size());

        BigDecimal chipsInPlay = countChipsAtTable(p).setScale(2);

        // Force start
        state.timeout();

        // Blinds
        act(p[1], SMALL_BLIND);

        assertTrue(mp[2].isActionPossible(BIG_BLIND));
        assertEquals(102, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[2], BIG_BLIND);

        assertTrue(mp[2].hasOption());
        assertAllPlayersHaveCards(mp, 2);

        assertEquals(0, state.getCommunityCards().size());
        state.timeout();

        // Pre flop round
        assertEquals(103, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[3], CALL);
        assertTrue(mp[3].hasActed());
        assertEquals(100, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[0], CALL);
        act(p[1], CALL);
        act(p[2], CHECK);
        // everyone checked so now we should be in DealCommunityCards round

        assertEquals(3, state.getCommunityCards().size());

        // Trigger deal community cards
        state.timeout(); // timeout deal community cards. Starts a new betting round

        assertEquals(3, state.getCommunityCards().size());

        // Flop round
        act(p[1], BET);
        act(p[2], CALL);
        act(p[3], CALL);
        act(p[0], CALL);

        assertEquals(4, state.getCommunityCards().size());

        // Trigger deal community cards
        state.timeout();// timeout deal community cards. Starts a new betting round

        // Turn round
        act(p[1], CHECK);
        act(p[2], BET);
        act(p[3], FOLD);
        act(p[0], FOLD);
        act(p[1], CALL);

        // Trigger deal community cards
        state.timeout();

        assertEquals(5, state.getCommunityCards().size());

        // River round
        act(p[1], CHECK);
        act(p[2], BET);
        act(p[1], FOLD);

        // Assertions
        assertTrue(state.isFinished());

        // Check that we didn't create or lose any chips.
        assertEquals(chipsInPlay, countChipsAtTable(p));
    }

    public void testSmallBlindShouldBeAbleToRaiseAfterBigMinRaises() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        state.timeout();
        // Blinds
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);

        state.timeout();

        // Pre flop round
        act(p[0], CALL);
        act(p[1], BET, new BigDecimal("100"));
        assertThat(mp[0].isActionPossible(RAISE), is(true));
    }

    public void testPlayerTimingOut() {
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        assertEquals(4, p.length);
        addPlayers(state, mp);
        assertEquals(4, state.getSeatedPlayers().size());

        BigDecimal chipsInPlay = countChipsAtTable(p);

        // Force start
        state.timeout();

        // Blinds
        act(p[1], SMALL_BLIND);

        assertTrue(mp[2].isActionPossible(BIG_BLIND));
        assertEquals(102, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[2], BIG_BLIND);

        assertTrue(mp[2].hasOption());
        assertAllPlayersHaveCards(mp, 2);

        assertEquals(0, state.getCommunityCards().size());
        state.timeout();

        // Pre flop round
        assertEquals(103, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[3], CALL);
        assertTrue(mp[3].hasActed());
        assertEquals(100, mockServerAdapter.getLastActionRequest().getPlayerId());
        act(p[0], CALL);
        act(p[1], CALL);
        state.timeout(); // p[2] will auto check

        // everyone checked so now we should be in DealCommunityCards round

        assertEquals(3, state.getCommunityCards().size());

        // Trigger deal community cards
        state.timeout(); // timeout deal community cards. Starts a new betting round

        assertEquals(3, state.getCommunityCards().size());

        // Flop round
        act(p[1], BET);
        //p[2] is away = autofolded
        assertTrue(mp[2].isAway());
        assertTrue(mp[2].hasFolded());
        assertFalse(mp[2].isSittingOutNextHand());
        assertTrue(mp[2].isSittingOut());
        act(p[3], CALL);
        act(p[0], CALL);

        assertEquals(4, state.getCommunityCards().size());

        // Trigger deal community cards
        state.timeout();// timeout deal community cards. Starts a new betting round

        // Turn round
        act(p[1], BET);
        act(p[3], CALL);
        act(p[0], FOLD);

        // Trigger deal community cards
        state.timeout();

        assertEquals(5, state.getCommunityCards().size());

        // River round
        act(p[1], BET);
        act(p[3], FOLD);

        // Assertions
        assertTrue(state.isFinished());

        // Check that we didn't create or lose any chips.
        assertEquals(chipsInPlay.setScale(2), countChipsAtTable(p).setScale(2));

        state.timeout();
        assertTrue(mp[2].isSittingOut());

    }

    private BigDecimal countChipsAtTable(int[] p) {
        BigDecimal chipsInPlay = BigDecimal.ZERO;
        for (int pid : p) {
            chipsInPlay = chipsInPlay.add(state.getBalance(pid));
        }
        return chipsInPlay;
    }

    private void act(int playerId, PokerActionType actionType) {
        act(playerId, actionType, mockServerAdapter.getLastActionRequest().getOption(actionType).getMinAmount());
    }

    private void act(int playerId, PokerActionType actionType, BigDecimal amount) {
        PokerAction action = new PokerAction(playerId, actionType);
        action.setBetAmount(amount);
        state.act(action);
    }

    public void testPostBlindsAndFold() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();

        // Small blind folds, hand should finish.
        assertFalse(state.isFinished());
        act(p[0], FOLD);

        // Assertions
        assertTrue(state.isFinished());
    }

    public void testDeclinedPlayerSitsOut() {
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[1], DECLINE_ENTRY_BET);
        act(p[2], BIG_BLIND);
        state.timeout();

        assertEquals(2, mp[0].getPocketCards().getCards().size());
        assertTrue(mp[1].isSittingOut());
        assertEquals(0, mp[1].getPocketCards().getCards().size());

        act(p[0], FOLD);
        assertTrue(state.isFinished());
    }

    public void testPostBlindsAndTimeout() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();

        // Small blind folds, hand should finish.
        assertFalse(state.isFinished());
        state.timeout();

        // Assertions
        assertTrue(state.isFinished());
    }

    public void testSmallBlindTimeout() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        state.timeout();

        assertFalse(mockServerAdapter.getLastActionRequest().isOptionEnabled(BIG_BLIND));
    }

    public void testPostBlindsCallAndFold() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();

        // Small blind folds, hand should finish.
        act(p[0], CALL);
        assertFalse(state.isFinished());
        act(p[1], FOLD);

        // Assertions
        assertTrue(state.isFinished());
    }

    public void testConsecutiveHands() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        BigDecimal chipsInPlay = countChipsAtTable(p);

        // Force start
        state.timeout();

        // Blinds
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();
        act(p[0], FOLD);

        // Assertions
        assertTrue(state.isFinished());
        assertEquals(chipsInPlay, countChipsAtTable(p).setScale(2));

        // Second hand, check that pocket cards have been cleared.
        state.timeout();
        assertFalse(state.isFinished());
        act(p[1], SMALL_BLIND);
        act(p[0], BIG_BLIND);
        state.timeout();

        assertAllPlayersHaveCards(mp, 2);
        act(p[1], CALL);
        act(p[0], CHECK);

        // Trigger deal community cards
        state.timeout();

        assertEquals(3, state.getCommunityCards().size());
        act(p[0], BET);
        act(p[1], FOLD);

        assertTrue(state.isFinished());
        assertEquals(chipsInPlay, countChipsAtTable(p));

        // Third hand, check that community cards have been cleared.
        state.timeout();
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();

        assertAllPlayersHaveCards(mp, 2);
        act(p[0], CALL);
        act(p[1], CHECK);

        // Trigger deal community cards
        state.timeout();

        assertEquals(3, state.getCommunityCards().size());
        act(p[1], BET);
        act(p[0], FOLD);

        assertTrue(state.isFinished());

        assertEquals(chipsInPlay, countChipsAtTable(p));
    }

    public void testEndHandReport() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        state.timeout();
        act(p[0], SMALL_BLIND);
        act(p[1], BIG_BLIND);
        state.timeout();
        act(p[0], CALL);
        act(p[1], CHECK);

        // Trigger deal community cards
        state.timeout();

        act(p[1], CHECK);
        act(p[0], CHECK);

        // Trigger deal community cards
        state.timeout();

        act(p[1], CHECK);
        act(p[0], CHECK);

        // Trigger deal community cards
        state.timeout();

        act(p[1], CHECK);
        act(p[0], CHECK);

        assertEquals(7, findByPlayerId(p[0], mockServerAdapter.hands).getHand().getCards().size());
    }

    private RatedPlayerHand findByPlayerId(int playerId, Collection<RatedPlayerHand> hands) {
        for (RatedPlayerHand ph : hands) {
            if (playerId == ph.getPlayerId()) {
                return ph;
            }
        }
        return null;
    }

    public void testRequestAction() {
        createGame(3);
        // Trigger timeout that should start the game
        state.timeout();

        ActionRequest request = mockServerAdapter.getLastActionRequest();
        assertEquals(101, request.getPlayerId());
    }

    /**
     * This test might look messy to the untrained eye. But if you
     * just look hard, it's pretty clever.. :)
     * <p/>
     * Okay okay, I'll tell you (man, you're slow). I'm creating my own
     * server adapter so I can fail if I get the handFinished message
     * before the fold message. NEAT!
     */
    public void testWrongOrder() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);


        state.setServerAdapter(new MockServerAdapter() {
            boolean foldActionReceived = false;

            @Override
            public Random getSystemRNG() {
                return new Random();
            }

            @Override
            public void notifyActionPerformed(PokerAction action, PokerPlayer pokerPlayer) {
                if (action.getActionType() == FOLD) {
                    foldActionReceived = true;
                }
            }

            @Override
            public void notifyHandEnd(HandResult result, HandEndStatus status, boolean tournamentTable) {
                if (!foldActionReceived) {
                    fail();
                }
            }

        });
        state.timeout();
        act(p[0], SMALL_BLIND, BigDecimal.TEN);
        act(p[1], BIG_BLIND, new BigDecimal("20"));
        act(p[0], FOLD, BigDecimal.ZERO);
    }

    public void testBlindsActionPerformedNotification() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        state.timeout();

        act(p[0], SMALL_BLIND);
        assertNotNull(mockServerAdapter.getLatestActionPerformed());
    }

    public void testDenySmallBlind() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        state.timeout();
        mockServerAdapter.hands = null;
        act(p[0], DECLINE_ENTRY_BET);
        assertEquals(HandEndStatus.CANCELED_TOO_FEW_PLAYERS, mockServerAdapter.handEndStatus);
    }

    public void testPlayerLeavesBeforeStartOfHand() {
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        state.removePlayer(p[1]);

        // Force start
        state.timeout();

        // Blinds
        act(p[2], SMALL_BLIND);
        act(p[3], BIG_BLIND);
        state.timeout();

        // All players fold, hand should finish.
        assertFalse(state.isFinished());
        act(p[0], FOLD);
        act(p[2], FOLD);

        // Assertions
        assertTrue(state.isFinished());
    }

    public void testRejectSmallBlindStallsGameBug() {
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        mockServerAdapter.clearActionRequest();
        act(p[1], DECLINE_ENTRY_BET, BigDecimal.ZERO);
        assertNotNull("The next player should be asked to post big blind.", mockServerAdapter.getLastActionRequest());
    }

    private void createGame(int players) {
        ps = TestUtils.createMockPlayers(players);
        assertEquals(0, mockServerAdapter.getTimeoutRequests());
        addPlayers(state, ps);
        assertEquals(1, mockServerAdapter.getTimeoutRequests());
    }

    private void assertAllPlayersHaveCards(PokerPlayer[] p,
                                           int expectedNumberOfCards) {
        for (PokerPlayer pl : p) {
            assertEquals(expectedNumberOfCards, pl.getPocketCards().getCards().size());
        }
    }

    private void addPlayers(PokerState game, PokerPlayer[] p) {
        for (PokerPlayer pl : p) {
            game.addPlayer(pl);
        }
    }

}
