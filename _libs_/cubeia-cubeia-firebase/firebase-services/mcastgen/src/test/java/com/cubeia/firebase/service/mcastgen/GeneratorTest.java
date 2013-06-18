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
package com.cubeia.firebase.service.mcastgen;

import java.util.Map;
import java.util.TreeMap;

import com.cubeia.firebase.api.util.SocketAddress;

import junit.framework.TestCase;

public class GeneratorTest extends TestCase {

	public void test1() throws Exception {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("_basePort", "9087");
		map.put("", "");
		map.put(" # test comment", "");
		map.put("*.*.*.2$2", "testserviceid");
		Generator g = new Generator(new SocketAddress("127.0.0.1:8900"), map);
		assertEquals(new SocketAddress("127.0.0.3:8902"), g.generate("testserviceid"));
	}
}
