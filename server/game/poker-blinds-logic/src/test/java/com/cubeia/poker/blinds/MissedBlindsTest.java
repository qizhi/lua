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
import com.cubeia.poker.blinds.utils.MockPlayer;

import java.util.Arrays;
import java.util.List;

import static com.cubeia.poker.blinds.Fixtures.blindsInfo;
import static com.cubeia.poker.blinds.Fixtures.players;

public class MissedBlindsTest extends TestCase {

    private BlindsCalculator calc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calc = new BlindsCalculator();
    }

    public void testSmallBlindMarkedAsMissedSmallIfSittingOut() {
        // Given
        BlindsInfo blinds = blindsInfo(1, 2, 3);
        List<BlindsPlayer> players = players(1, 2, 3, 4);
        sitOut(players, 3); // Small blind is sitting out.

        // When
        BlindsInfo blindsInfo = calc.initializeBlinds(blinds, players, false);

        // Then
        System.out.println(blindsInfo.getSmallBlindPlayerId());
        List<MissedBlind> missedBlinds = calc.getMissedBlinds();
        assertEquals(MissedBlindsStatus.MISSED_SMALL_BLIND, missedBlinds.get(0).getMissedBlindsStatus());
    }

    private void sitOut(List<BlindsPlayer> players, int... seatIds) {
        for (int seatId : seatIds) {
            MockPlayer player = getPlayerInSeat(players, seatId);
            player.setSittingIn(false);
            player.setMissedBlindsStatus(MissedBlindsStatus.NO_MISSED_BLINDS);
        }
    }

    private MockPlayer getPlayerInSeat(List<BlindsPlayer> players, int seatId) {
        for (BlindsPlayer player : players) {
            if (player.getSeatId() == seatId) {
                return (MockPlayer) player;
            }
        }
        return null;
    }

    public void testMarkPlayersBetweenOldAndNewDealerButtonAsMissesBig() {
        // Given
        BlindsInfo blinds = blindsInfo(1, 4, 5);
        List<BlindsPlayer> players = players(1, 2, 3, 4, 5, 6, 7);
        sitOut(players, 2, 3, 4, 5); // Players 2, 3, 4 and 5 sit out.

        // When
        calc.initializeBlinds(blinds, players, false);

        // Then
        List<MissedBlind> missedBlinds = calc.getMissedBlinds();
        assertEquals(3, missedBlinds.size());
        assertEquals(MissedBlindsStatus.MISSED_SMALL_BLIND, missedBlinds.get(0).getMissedBlindsStatus());
        assertEquals(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND, missedBlinds.get(1).getMissedBlindsStatus());
        assertEquals(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND, missedBlinds.get(2).getMissedBlindsStatus());

        // Note, player in seat 4 is now on the dealer button, but sitting out. He did not miss any blinds.
    }

    public void testMarkPlayersBetweenSmallAndBigAsMissedBig() {
        // Given
        BlindsInfo blinds = blindsInfo(5, 6, 1);
        List<BlindsPlayer> players = players(1, 2, 3, 4, 5, 6);
        sitOut(players, 2, 3, 4); // Players 2, 3, 4 and 5 sit out.

        // When
        calc.initializeBlinds(blinds, players, false);

        // Then
        List<MissedBlind> missedBlinds = calc.getMissedBlinds();
        assertEquals(3, missedBlinds.size());
        assertEquals(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND, missedBlinds.get(0).getMissedBlindsStatus());
        assertEquals(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND, missedBlinds.get(1).getMissedBlindsStatus());
        assertEquals(MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND, missedBlinds.get(2).getMissedBlindsStatus());
    }

    public void testGetEligiblePlayerList() {
        // Given
        BlindsInfo blinds = blindsInfo(5, 6, 1);
        List<BlindsPlayer> players = players(1, 2, 3, 4, 5, 6);
        sitOut(players, 2, 3, 4); // Players 2, 3, 4 and 5 sit out.
        calc.initializeBlinds(blinds, players, false);

        // When
        List<BlindsPlayer> eligiblePlayerList = calc.getEligiblePlayerList();

        // Then
        assertEquals(Arrays.asList(players.get(0), players.get(4), players.get(5)), eligiblePlayerList);
    }

}
