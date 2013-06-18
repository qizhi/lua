/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.service.mttplayerreg;

import java.util.Arrays;

import junit.framework.TestCase;

public class TournamentPlayerRegistryServiceTest extends TestCase {

	private TournamentPlayerRegistryService reg;

	@Override
	protected void setUp() throws Exception {
		this.reg = new TournamentPlayerRegistryService(new SystemStateMock());
	}
	
	public void testRegister() throws Exception {
		/*
		 * Mtt 1 : Players 1 & 2
		 * Player 1 : Mtt 1 & 2
		 * Player 2 : Mtt 1
		 */
		reg.register(1, 1);
		reg.register(1, 2);
		reg.register(2, 1);
		
		int[] test1 = reg.getPlayersForTournament(1);
		assertTrue(Arrays.equals(sort(test1), new int[] { 1, 2 }));
		int[] test2 = reg.getTournamentsForPlayer(1);
		assertTrue(Arrays.equals(sort(test2), new int[] { 1, 2 }));
		int[] test3 = reg.getTournamentsForPlayer(2);
		assertTrue(Arrays.equals(sort(test3), new int[] { 1 }));
	}
	
	public void testUnregister() throws Exception {
		/*
		 * Mtt 1 : Players 1 & 2
		 * Player 1 : Mtt 1 & 2
		 * Player 2 : Mtt 1
		 */
		reg.register(1, 1);
		reg.register(1, 2);
		reg.register(2, 1);
		
		/*
		 * Mtt 1 : Players 1
		 * Player 1 : Mtt 1
		 */
		reg.unregister(2, 1);
		
		int[] test1 = reg.getPlayersForTournament(1);
		assertTrue(Arrays.equals(sort(test1), new int[] { 1 }));
		int[] test2 = reg.getTournamentsForPlayer(1);
		assertTrue(Arrays.equals(sort(test2), new int[] { 1, 2 }));
		int[] test3 = reg.getTournamentsForPlayer(2);
		assertTrue(Arrays.equals(sort(test3), new int[0]));
	}
	
	public void testDestroy() throws Exception {
		/*
		 * Mtt 1 : Players 1 & 2
		 * Player 1 : Mtt 1 & 2
		 * Player 2 : Mtt 1
		 */
		reg.register(1, 1);
		reg.register(1, 2);
		reg.register(2, 1);
		
		/*
		 * Mtt 2 : Players 1
		 */
		reg.unregisterAll(1);
		
		int[] test1 = reg.getPlayersForTournament(1);
		assertTrue(Arrays.equals(sort(test1), new int[0]));
		int[] test2 = reg.getTournamentsForPlayer(1);
		assertTrue(Arrays.equals(sort(test2), new int[] { 2 }));
		int[] test3 = reg.getTournamentsForPlayer(2);
		assertTrue(Arrays.equals(sort(test3), new int[0]));
		int[] test4 = reg.getPlayersForTournament(2);
		assertTrue(Arrays.equals(sort(test4), new int[] { 1 }));
	}

	
	// --- PRIVATE METHODS --- //
	
	private int[] sort(int[] test) {
		Arrays.sort(test);
		return test;
	}
}
