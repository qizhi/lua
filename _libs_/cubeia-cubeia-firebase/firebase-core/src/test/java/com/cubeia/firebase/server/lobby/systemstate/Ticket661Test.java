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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.mock.MockClient;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

public class Ticket661Test extends TestCase {

	private SystemStateCacheHandler cache = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	private StateLobby lobby;
	
	
	/**
	 * Lobby setup:
	 *     
	 * mtt (99)
	 *   /a
	 *    /x
	 *      /1 - mtt
	 * 
	 */
	protected void setUp() throws Exception {
		cache.start();
		
		cache.updateAttributes(getTournamentFQN("99/a/x", 1), SystemStateTestGenerator.createTournamentAttributes(1));
		
		lobby = new StateLobbyAlternative(cache);
		lobby.setBroadcastPeriod(10);
		lobby.start();
		
		Thread.sleep(20);
	}
	
	public void testSubscribe() throws Exception {
		MockClient client = new MockClient(11);
		
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a", -1));
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a/x", -1));
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a/z", -1));
		
		// subscribe to "a/x" & "a/z"
		subTo(client, "a/x");
		subTo(client, "a/z");
		
		// check subscribers
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a", -1));
		assertSubscribers(1, new LobbyPath(LobbyPathType.MTT, 99, "a/x", -1));
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a/z", -1));
		
		// create tournament "a/z/1"
		cache.updateAttributes(getTournamentFQN("99/a/z", 1), SystemStateTestGenerator.createTournamentAttributes(1));
		
		// subscribe to "a/x" & "a/z"
		subTo(client, "a/x");
		subTo(client, "a/z");
		
		// check subscribers
		assertSubscribers(0, new LobbyPath(LobbyPathType.MTT, 99, "a", -1));
		assertSubscribers(1, new LobbyPath(LobbyPathType.MTT, 99, "a/x", -1));
		assertSubscribers(1, new LobbyPath(LobbyPathType.MTT, 99, "a/z", -1));
	}
	
	
	// --- PRIVATE METHODS --- //

	private void subTo(MockClient client, String path) {
		LobbyPath path1 = new LobbyPath(LobbyPathType.MTT, 99, path, -1);
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		request.setType(LobbyPathType.MTT);
		lobby.subscribe(request, client);
	}

	private void assertSubscribers(int i, LobbyPath path) {
		Set<Client> subs = lobby.getSubscribers(path);
		Assert.assertEquals(i, subs.size());
	}

	protected void tearDown() throws Exception {
		cache.stop();
		lobby.stop();
	}
	
	private String getTournamentFQN(String address, int objectId) {
		return SystemStateConstants.TOURNAMENT_ROOT_FQN+address+"/"+objectId;
	}
}
