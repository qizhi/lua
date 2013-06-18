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
package com.cubeia.firebase;

import com.cubeia.firebase.api.game.activator.DefaultActivatorConfig;

import junit.framework.TestCase;

public class DefaultActivatorConfigTest extends TestCase {

	public void testDefConstructor() throws Exception {
		/*
		 * Note, the com/cubeia/firebase/api/game/activator/defaultActivatorConfig.xml
		 * is placed under test/resources and will be used if test/resources is in your 
		 * classpath. Maven will automatically add it to the classpath when running tests.
		 */  
		DefaultActivatorConfig c = new DefaultActivatorConfig();
		super.assertEquals(10, c.getSeats());
		super.assertEquals(10, c.getMinTables());
		super.assertEquals(5000, c.getScanFrequency());
		super.assertEquals(10, c.getMinAvailTables());
		super.assertEquals(10, c.getIncrementSize());
		super.assertEquals(120000, c.getTableTimeout());
	}
}
