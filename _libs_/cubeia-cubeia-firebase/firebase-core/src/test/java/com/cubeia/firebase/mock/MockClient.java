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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.TableSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TableUpdateListPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdateListPacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.ClientLocal;
import com.cubeia.firebase.server.gateway.client.ClientNodeInfo;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.server.gateway.client.IPlayerData;
import com.cubeia.firebase.server.gateway.event.ClientGameActionHandler;
import com.cubeia.firebase.server.gateway.event.local.ClientLocalActionHandler;
import com.cubeia.firebase.server.gateway.util.CompilationCache;

public class MockClient implements Client {

	private int id;

	private List<ProtocolObject> packets = new ArrayList<ProtocolObject>();
	
	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void pingDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sendPing(int id) {
		// TODO Auto-generated method stub
		
	}
	
	public ClientNodeInfo getNodeInfo() {
		return null;
	}
	
	public int getLocalClientId() {
		return 0;
	}
	
	public MockClient(int id) {
		this.id = id;
	}
	
	public String toString() {
		return ""+id;
	}
	
	public int getId() {
		return id;
	}
	
	public List<ProtocolObject> getPacketsSentToClient() {
		return packets;
	}
	
	public void clearSentPackets() {
		packets.clear();
	}
	
	public void sendClientPacket(ProtocolObject packet) {
		if(packet instanceof TableSnapshotListPacket) {
			for (ProtocolObject o : ((TableSnapshotListPacket)packet).snapshots) {
				sendClientPacket(o);
			}
		} else if(packet instanceof TableUpdateListPacket) {
			for (ProtocolObject o : ((TableUpdateListPacket)packet).updates) {
				sendClientPacket(o);
			}
		} else if(packet instanceof TournamentSnapshotListPacket) {
			for (ProtocolObject o : ((TournamentSnapshotListPacket)packet).snapshots) {
				sendClientPacket(o);
			}
		} else if(packet instanceof TournamentUpdateListPacket) {
			for (ProtocolObject o : ((TournamentUpdateListPacket)packet).updates) {
				sendClientPacket(o);
			}
		} else {
			packets.add(packet);
		}
	}
	
	@Override
	public void sendClientPackets(CompilationCache packets) {
		for (ProtocolObject o : packets.getObjects()) {
			sendClientPacket(o);
		}
	}
	
	@Override
	public void sendClientPackets(List<ProtocolObject> packets) {
		for (ProtocolObject o : packets) {
			sendClientPacket(o);
		}
	}
	
	public void close() {}

	public boolean closeForced() {
		return false;
	}

	public ClientGameActionHandler getActionHandler() {
		return null;
	}

	public IPlayerData getClientData() {
		return null;
	}


	public ClientLocalActionHandler getLocalActionHandler() {
		return null;
	}

	public ClientLocal getLocalData() {
		return null;
	}

	public String getNode() {
		return null;
	}

	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	public String getSessionId() {
		return null;
	}

	public ClientState getState() {
		return null;
	}

	public void setSessionId(String sessionId) {}

	public void setState(ClientState logged_in) {}

    public int getWriteQueueBytes() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getWriteQueueSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}
