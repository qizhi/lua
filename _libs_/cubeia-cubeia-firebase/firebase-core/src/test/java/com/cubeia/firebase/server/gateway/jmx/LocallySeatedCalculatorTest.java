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
package com.cubeia.firebase.server.gateway.jmx;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.cubeia.firebase.mock.systemstate.SystemStateMapMock;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;


public class LocallySeatedCalculatorTest {

	// private static final String CACHE_CONFIG = "com/game/server/systemstate/systemstate-test-service.xml";
	
	SystemStateServiceContract ss = null;
	
	@Before
	public void setUp() throws Exception {
		/*
		 
		  /client  {}
		    /11  {SESSION_ID=18ea6f29-a49c-4d6c-b408-a009e9fa7366, STATUS=2, TIMESTAMP=1197549045630, SCREEN_NAME=A, NODE=cli1}
		      /table  {1=3}
		    /22  {SESSION_ID=76570ad9-7a5f-4b62-9eb8-a50bfb53657b, STATUS=1, TIMESTAMP=1197548993432, SCREEN_NAME=B, NODE=cli1}
		      /table  {1=0}
		       
        */
		ss = new SystemStateMapMock();
		
		Map<String, Object> playerData1 = new HashMap<String, Object>();
		playerData1.put("SESSION_ID", "18ea6f29-a49c-4d6c-b408-a009e9fa7366");
		playerData1.put("STATUS","1");
		playerData1.put("TIMESTAMP","1197549045630");
		playerData1.put("SCREEN_NAME","Apan");
		playerData1.put("NODE","cli1");
		
		Map<String, Object> playerData2 = new HashMap<String, Object>();
		playerData2.put("SESSION_ID", "18ea6f29-a49c-4d6c-b408-a009e9fa7361");
		playerData2.put("STATUS","1");
		playerData2.put("TIMESTAMP","1197549045630");
		playerData2.put("SCREEN_NAME","Bananen");
		playerData2.put("NODE","cli1");
		
		Map<String, Object> playerData3 = new HashMap<String, Object>();
		playerData3.put("SESSION_ID", "18ea6f29-a49c-4d3c-b408-a009e9fa7361");
		playerData3.put("STATUS","1");
		playerData3.put("TIMESTAMP","1197542045630");
		playerData3.put("SCREEN_NAME","Tobias");
		playerData3.put("NODE","cli2");
		
		Map<String, Object> tables1 = createTableMap(2);
		Map<String, Object> tables2 = createTableMap(4);
		Map<String, Object> tables3 = createTableMap(6);
		
		String fqn1 = "/client/11/";
		String fqn2 = "/client/22/";
		String fqn3 = "/client/33/";
		
		ss.setAttributes(fqn1, playerData1);
		ss.setAttributes(fqn2, playerData2);
		ss.setAttributes(fqn3, playerData3);
		
		fqn1 = fqn1+"table/";
		fqn2 = fqn2+"table/";
		fqn3 = fqn3+"table/";
		
		ss.setAttributes(fqn1, tables1);
		ss.setAttributes(fqn2, tables2);
		ss.setAttributes(fqn3, tables3);
	}

	private Map<String, Object> createTableMap(int count) {
		Map<String, Object> tables = new HashMap<String, Object>();
		for (int i = 0; i < count; i++) {
			tables.put(String.valueOf(100+i), i);
			tables.put("_mtt:" + i, i);
		}
		return tables;
	}

	@Test
	public void testCountSeated() {
		long countSeated = LocallySeatedCalculator.countSeated("cli1", ss);
		assertEquals(6, countSeated);
	}

}
