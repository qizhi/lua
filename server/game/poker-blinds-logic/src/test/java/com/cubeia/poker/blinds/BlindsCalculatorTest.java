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

package com.cubeia.poker.blinds;

import junit.framework.TestCase;
import org.mockito.Mockito;
import com.cubeia.poker.blinds.utils.MockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static org.mockito.Mockito.*;

public class BlindsCalculatorTest extends TestCase implements LogCallback {

    private BlindsCalculator calc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calc = new BlindsCalculator();
    }

    public void testFirstHeadsUpHand() {
        // Given
        List<Integer> seatIdList = Arrays.asList(0, 1);
        BlindsInfo lastHandsBlinds = new BlindsInfo();
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        players.add(Fixtures.player(1, false));
        players.add(Fixtures.player(0, false));
        RandomSeatProvider randomizer = mock(RandomSeatProvider.class);
        when(randomizer.getRandomSeatId(seatIdList)).thenReturn(1);
        calc = new BlindsCalculator(randomizer);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        verify(randomizer).getRandomSeatId(seatIdList);
        assertBlindsInfo(result, 1, 1, 0);
    }

    public void testNonFirstHeadsUpHand() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 1, 2);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, Fixtures.players(1, 2), false);

        // Then
        assertBlindsInfo(result, 2, 2, 1);
    }

    /**
     * In this test case, a new player is entering the game in seat 5.
     */
    public void testNormalEntryBet() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4);
        players.add(Fixtures.player(5, false));

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        Queue<EntryBetter> entryBetters = calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId());
        EntryBetter entryBetter = entryBetters.peek();
        assertEquals(5, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.BIG_BLIND, entryBetter.getEntryBetType());
    }

    public void testPlayerBetweenDealerButtonAndSmallBlindDoesNotPostEntryBet() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 3, 4);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3);
        players.add(Fixtures.player(4, false));

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertEquals(0, calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getSmallBlindSeatId(), blindsInfo.getBigBlindSeatId()).size());
    }

    public void testPlayerOnDealerButtonDoesNotPostEntryBet() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 3, 4);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 4);
        players.add(Fixtures.player(3, false));

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertEquals(0, calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId()).size());
    }

    public void testCalculatingEntryBetsWhenDealerIsOnEmptySeatDoesNotHang() {
        BlindsInfo blinds = Fixtures.blindsInfo(1, 4, 5);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 5, 6, 7);

        BlindsInfo blindsInfo = calc.initializeBlinds(blinds, players, false);
        assertEquals(0, calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId()).size());
    }

    public void testPlayerShouldPayDeadSmallBlind() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4);
        MockPlayer playerWhoMissedSmallBlind = Fixtures.player(5, false);
        playerWhoMissedSmallBlind.setMissedBlindsStatus(MissedBlindsStatus.MISSED_SMALL_BLIND);
        players.add(playerWhoMissedSmallBlind);

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        Queue<EntryBetter> entryBetters = calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId());
        EntryBetter entryBetter = entryBetters.peek();
        assertEquals(5, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.DEAD_SMALL_BLIND, entryBetter.getEntryBetType());
    }

    public void testPlayerShouldPayBigBlindPlusDeadSmallBlind() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4);
        MockPlayer playerWhoMissedSmallBlind = Fixtures.player(5, false);
        playerWhoMissedSmallBlind.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
        players.add(playerWhoMissedSmallBlind);

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        Queue<EntryBetter> entryBetters = calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId());
        EntryBetter entryBetter = entryBetters.peek();
        assertEquals(5, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND, entryBetter.getEntryBetType());
    }

    public void testPlayerWhoHasNotMissedAnyBlindsDoesNotHaveToPayEntryBet() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4);
        MockPlayer playerWhoMissedNoBlinds = Fixtures.player(5, true);
        playerWhoMissedNoBlinds.setMissedBlindsStatus(MissedBlindsStatus.NO_MISSED_BLINDS);
        players.add(playerWhoMissedNoBlinds);

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        Queue<EntryBetter> entryBetters = calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId());
        assertEquals(0, entryBetters.size());
    }

    public void testOrderOfEntryBetters() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4);

        MockPlayer playerWhoMissedSmallBlind = Fixtures.player(5, false);
        playerWhoMissedSmallBlind.setMissedBlindsStatus(MissedBlindsStatus.MISSED_SMALL_BLIND);
        players.add(playerWhoMissedSmallBlind);

        MockPlayer newPlayer = Fixtures.player(6, false);
        newPlayer.setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        players.add(newPlayer);

        MockPlayer playerWhoMissedBigBlind = Fixtures.player(7, false);
        playerWhoMissedBigBlind.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
        players.add(playerWhoMissedBigBlind);

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        Queue<EntryBetter> entryBetters = calc.getEntryBetters(blindsInfo.getDealerSeatId(), blindsInfo.getBigBlindSeatId(), blindsInfo.getBigBlindSeatId());

        EntryBetter entryBetter = entryBetters.poll();
        assertEquals(5, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.DEAD_SMALL_BLIND, entryBetter.getEntryBetType());

        entryBetter = entryBetters.poll();
        assertEquals(6, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.BIG_BLIND, entryBetter.getEntryBetType());

        entryBetter = entryBetters.poll();
        assertEquals(7, entryBetter.getPlayer().getSeatId());
        assertEquals(EntryBetType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND, entryBetter.getEntryBetType());
    }

    public void testNonHeadsUpToHeadsUp() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = Fixtures.players(4, 5);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 5, 5, 4);
    }

    /**
     * Tests moving from heads up to non heads up.
     * <p/>
     * Note, the player in seat 3 cannot enter the game until the button has passed.
     */
    public void testHeadsUpToNonHeadsUp() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(5, 5, 4);
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        players.add(Fixtures.player(3, false));
        players.add(Fixtures.player(4, true));
        players.add(Fixtures.player(5, true));

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 5, 4, 5);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testUndefinedLastHandCountsAsFirstHandOnTable() {
        // Given
        List<Integer> seatIdList = Arrays.asList(1, 2, 3, 4, 5);
        BlindsInfo lastHandsBlinds = new BlindsInfo();
        assertEquals(false, lastHandsBlinds.isDefined());

        RandomSeatProvider randomizer = mock(RandomSeatProvider.class);
        Mockito.when(randomizer.getRandomSeatId(seatIdList)).thenReturn(5);
        calc = new BlindsCalculator(randomizer);

        // When
        BlindsInfo blinds = calc.initializeBlinds(lastHandsBlinds, Fixtures.players(false, 1, 2, 3, 4, 5), false);

        // Then
        assertEquals(5, blinds.getDealerSeatId());
        verify(randomizer).getRandomSeatId(seatIdList);
    }

    public void testOnlyOneEnteredPlayerDoesNotGetTheBigBlind() {
        // Given that only one player has paid the entry bet.
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 5, 6);
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        players.add(Fixtures.player(2, true));
        MockPlayer player = Fixtures.player(3, false);
        player.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
        players.add(player);

        // When we initialize the blinds.
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then the bb should not be on the already entered player.
        assertBlindsInfo(result, 2, 2, 3);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testOnlyOneEnteredPlayerDoesNotGetTheBigBlindNonHeadsUp() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 2, 0); // This hand was cancelled
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        players.add(Fixtures.player(2, true));
        MockPlayer player3 = Fixtures.player(3, false);
        player3.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
        MockPlayer player4 = Fixtures.player(4, false);
        player4.setMissedBlindsStatus(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
        players.addAll(Arrays.asList(player3, player4));

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 2, 3, 4);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testInitWhenLastHandWasCancelled() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 0, 0);
        lastHandsBlinds.setHandCanceled(); // This hand was canceled
        List<BlindsPlayer> players = Fixtures.players(2, 3);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 2, 2, 3);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testInitWhenLastHandWasCancelledAndDealerNotSeated() {
        // Given
        RandomSeatProvider randomizer = mock(RandomSeatProvider.class);
        when(randomizer.getRandomSeatId(Mockito.anyListOf(Integer.class))).thenReturn(3);
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 0, 0);
        lastHandsBlinds.setHandCanceled();
        List<BlindsPlayer> players = Fixtures.players(3, 4, 5);
        players.add(Fixtures.player(2, false, false));
        calc = new BlindsCalculator(randomizer);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 3, 4, 5);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testReturnNullIfNotEnoughPlayers() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(2, 0, 0); // This hand was canceled
        List<BlindsPlayer> players = Fixtures.players(2);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertNull(result);
    }

    public void testOnlyOneEnteredAndLastHandCancelled() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(5, 2, 0); // This hand was cancelled
        List<BlindsPlayer> players = new ArrayList<BlindsPlayer>();
        players.add(Fixtures.player(3, true));
        MockPlayer player = Fixtures.player(2, false);
        player.setMissedBlindsStatus(MissedBlindsStatus.NOT_ENTERED_YET);
        players.add(player);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 3, 3, 2);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testNonFirstHeadsUpButDealerIsGone() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(5, 5, 4); // This hand was cancelled
        List<BlindsPlayer> players = Fixtures.players(5, 6);

        // When
        BlindsInfo result = calc.initializeBlinds(lastHandsBlinds, players, false);

        // Then
        assertBlindsInfo(result, 5, 5, 6);
        assertEquals(0, calc.getEntryBetters(result.getDealerSeatId(), result.getBigBlindSeatId(), result.getBigBlindSeatId()).size());
    }

    public void testGetPlayersBetweenDealerAndBig() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 2, 5);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4, 5, 6);
        calc.initializeBlinds(lastHandsBlinds, players, false);

        // When
        List<BlindsPlayer> playersBetweenDealerAndBig = calc.getPlayersBetweenDealerAndBig();

        // Then
        assertEquals(Arrays.asList(players.get(2), players.get(3)), playersBetweenDealerAndBig);
    }

    public void testGetPlayersBetweenDealerAndBigHeadsUp() {
        // Given
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 1, 5);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4, 5, 6);
        calc.initializeBlinds(lastHandsBlinds, players, false);

        // When
        List<BlindsPlayer> playersBetweenDealerAndBig = calc.getPlayersBetweenDealerAndBig();

        // Then
        assertEquals(Arrays.asList(players.get(1), players.get(2), players.get(3)), playersBetweenDealerAndBig);
    }

    public void testGetNextBigBlindPlayer() {
        BlindsInfo lastHandsBlinds = Fixtures.blindsInfo(1, 1, 5);
        List<BlindsPlayer> players = Fixtures.players(1, 2, 3, 4, 5, 6);
        calc.initializeBlinds(lastHandsBlinds, players, false);

        assertEquals(6, calc.getNextBigBlindPlayer(-1).getSeatId());
        assertEquals(6, calc.getNextBigBlindPlayer(-1).getSeatId());
        assertEquals(1, calc.getNextBigBlindPlayer(6).getSeatId());
        assertNull(calc.getNextBigBlindPlayer(1));

    }

    private void assertBlindsInfo(BlindsInfo result, int dealer, int small, int big) {
        assertEquals(dealer, result.getDealerSeatId());
        assertEquals(small, result.getSmallBlindSeatId());
        assertEquals(big, result.getBigBlindSeatId());
    }

    public void log(String message) {
        System.out.println(message);
    }

}
