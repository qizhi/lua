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
package com.cubeia.firebase.server.statistics;

import junit.framework.TestCase;

public class StatisticsLevelTest extends TestCase {

	private Level oldLevel;
	private Level newLevel;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testIsEnabled() {
		StatisticsLevel.getInstance().setLevel(Level.DEPLOYMENT);
		assertTrue(StatisticsLevel.getInstance().isEnabled(Level.DEPLOYMENT));
		assertTrue(StatisticsLevel.getInstance().isEnabled(Level.MINIMAL));
		assertFalse(StatisticsLevel.getInstance().isEnabled(Level.PROFILING));
	}
	
	public void testListener() {
		StatisticsLevel.getInstance().setLevel(Level.DEPLOYMENT);
		StatisticsLevel.getInstance().addListener(new Listen());
		StatisticsLevel.getInstance().setLevel(Level.PROFILING);
		
		assertEquals(Level.PROFILING, newLevel);
		assertEquals(Level.DEPLOYMENT, oldLevel);
	}
	
	
	private class Listen implements StatisticsLevelListener {

		public void statisticsLevelChanged(Level oldLevel, Level newLevel) {
			StatisticsLevelTest.this.oldLevel = oldLevel;
			StatisticsLevelTest.this.newLevel = newLevel;
			
		}
		
	}
	
}
