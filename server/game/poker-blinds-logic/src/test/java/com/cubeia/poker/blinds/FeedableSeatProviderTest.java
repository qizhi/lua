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

import com.cubeia.poker.blinds.FeedableSeatProvider;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class FeedableSeatProviderTest extends TestCase {

    private FeedableSeatProvider provider;

    private Random randomizer = mock(Random.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new FeedableSeatProvider(randomizer);
    }

    public void testGetRandomSeatId() {
        // Given
        List<Integer> seatIds = Arrays.asList(3, 6, 9);
        when(randomizer.nextInt(3)).thenReturn(1);

        // When
        int randomSeatId = provider.getRandomSeatId(seatIds);

        // Then
        assertEquals(6, randomSeatId);
        verify(randomizer).nextInt(3);
    }

    public void testFeedNextValue() {
        // Given
        List<Integer> seatIds = Arrays.asList(3, 6, 9);
        when(randomizer.nextInt(3)).thenReturn(0);
        provider.feedNextSeatId(9);

        // When
        int randomSeatId = provider.getRandomSeatId(seatIds);

        // Then
        assertEquals(9, randomSeatId);
        verify(randomizer, never()).nextInt(anyInt());
    }

    public void testNextFedValueNotContainedInSeatIdList() {
        // Given
        List<Integer> seatIds = Arrays.asList(3, 6, 9);
        provider.feedNextSeatId(7);

        // When
        int randomSeatId = provider.getRandomSeatId(seatIds);

        // Then
        assertEquals(3, randomSeatId);
        verify(randomizer).nextInt(3);
    }
}
