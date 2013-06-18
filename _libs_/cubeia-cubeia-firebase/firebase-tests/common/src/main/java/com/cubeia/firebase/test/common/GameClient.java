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
package com.cubeia.firebase.test.common;

import static com.cubeia.firebase.api.action.Attribute.fromAttributesToProtocol;

import java.io.IOException;
import java.util.List;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.protocol.Enums.LobbyType;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.Enums.ServiceIdentifier;
import com.cubeia.firebase.test.common.rules.Builder;
import com.cubeia.firebase.test.common.util.Serializer;

/**
 * The game client is a {@link Client} extension which contains
 * methods and objects to send and expect game specific messages.
 * 
 * @author larsan
 */
public class GameClient extends Client {

	private final Serializer gameSerializer;
	
	/**
	 * @param serializer Serializer to use for game specific objects, must not be null
	 */
	public GameClient(Serializer serializer, ConnectorType type, String host, int port) {
		super(type, host, port);
		Arguments.notNull(serializer, "serializer");
		gameSerializer = serializer;
	}
	
	
	// --- GAME PACKET METHODS --- //
	
	@SuppressWarnings("unchecked")
	public <T> T expectGamePacket(Class<T> cl) {
		return (T) super.expect(Builder.expect(cl, gameSerializer));
	}
	
	public void sendGamePacket(Object packet, int tableId) throws IOException {
		super.sendFirebasePacket(new GameTransportPacket(tableId, playerId, gameSerializer.serialize(packet), null));
	}
	
	public void sendGamePacket(Object packet, int tableId, List<Attribute> atts) throws IOException {
		GameTransportPacket p = new GameTransportPacket(tableId, playerId, gameSerializer.serialize(packet), null);
		if(atts != null) {
			p.attributes = fromAttributesToProtocol(atts);
		}
		super.sendFirebasePacket(p);
	}

	public void sendServicePacket(Class<? extends Contract> cl, int seq, Object packet) throws IOException {
		sendServicePacket(cl, seq, packet, null);
	}
	
	public void sendServicePacket(Class<? extends Contract> cl, int seq, Object packet, List<Attribute> atts) throws IOException {
		ServiceTransportPacket pack = new ServiceTransportPacket(playerId, seq, cl.getName(), (byte)ServiceIdentifier.CONTRACT.ordinal(), gameSerializer.serialize(packet), null);
		if(atts != null) {
			pack.attributes = fromAttributesToProtocol(atts);
		}
		super.sendFirebasePacket(pack);
	}
	
	public void sendLobbySubscriptionPacket(LobbyType type, int gameid, String domain) {
		super.sendFirebasePacket(new LobbySubscribePacket(type, gameid, domain));
	}

	/*public JoinResponsePacket joinTable(String path) throws IOException {
		send(new LobbySubscribePacket(Enums.LobbyType.REGULAR, 99, path));
		TableSnapshotPacket table = expect(TableSnapshotPacket.class);
		
		send(new JoinRequestPacket(table.tableid, (byte) -1, emptyParams()));
		JoinResponsePacket response = expect(JoinResponsePacket.class);
		assertEquals(Enums.JoinResponseStatus.OK, response.status);
		
		return response;
	}
	
	public JoinResponsePacket joinTable() throws IOException {
		return joinTable("/");
	}	*/

}
