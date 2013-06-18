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
package com.cubeia.firebase.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.LobbyQueryRequest;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.lobby.systemstate.LobbyListener;

@SuppressWarnings("unchecked")
public class MockLobby implements Lobby {

	public void addLobbyListener(LobbyListener listener) {}

	
	public List<LobbyPath> getAllLobbyLeaves(LobbyPathType type) {
		return Collections.EMPTY_LIST;
	}

	public Collection<LobbyPath> getLeaves(LobbyPath path) {
		return Collections.EMPTY_LIST;
	}

	public List<ProtocolObject> getLobbyQuery(LobbyQueryRequest request) {
		return Collections.EMPTY_LIST;
	}

	public Collection<TournamentInfo> getMttInfos(LobbyPath path) {
		return Collections.EMPTY_LIST;
	}

	public Collection<TableInfo> getTableInfos(LobbyPath path) {
		return Collections.EMPTY_LIST;
	}
	
	public void addPath(String path) { }

	public void removeLobbyListener(LobbyListener listener) {}

	public void subscribe(LobbySubscriptionRequest request, Client client) {}

	public void unsubscribe(LobbyUnsubscriptionRequest request, Client client) {}

	public void unsubscribeAll(Client client) {}

	public String getStateDescription() {
		return "";
	}

	public void start() {}
	public void stop() {}

	public void subscribeToLobbyObject(LobbySubscriptionRequest request, Client client) {
		// TODO Auto-generated method stub
		
	}

	public void unsubscribeToLobbyObject(LobbyUnsubscriptionRequest request, Client client) {
		// TODO Auto-generated method stub
		
	}

	public ProtocolObject getSnapshot(LobbyPathType type, int objectId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<Client> getSubscribers(LobbyPath key) {
		return null;
	}
}
