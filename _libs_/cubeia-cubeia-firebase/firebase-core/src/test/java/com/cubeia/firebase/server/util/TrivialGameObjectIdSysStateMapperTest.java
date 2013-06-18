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
package com.cubeia.firebase.server.util;

import java.util.Set;
import java.util.TreeSet;

import com.cubeia.firebase.server.util.GameObjectIdSysStateMapper;
import com.cubeia.firebase.server.util.TrivialIdSysStateMapperMemory;

import junit.framework.TestCase;

public class TrivialGameObjectIdSysStateMapperTest extends TestCase {

	private GameObjectIdSysStateMapper mapper;

	protected void setUp() throws Exception {
		TrivialIdSysStateMapperMemory mem = new TrivialIdSysStateMapperMemory();
		mapper = new GameObjectIdSysStateMapper(mem);
	}

	public void testMapper() {
		int size = 100000;
		Set<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < size; i++) {
			set.add(mapper.generateNewObjectId());
		}
		super.assertEquals(size, set.size());
	}
}
