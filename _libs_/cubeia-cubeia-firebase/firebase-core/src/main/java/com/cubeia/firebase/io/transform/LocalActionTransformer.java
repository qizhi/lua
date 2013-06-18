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
package com.cubeia.firebase.io.transform;

import com.cubeia.firebase.api.action.local.LocalAction;
import com.cubeia.firebase.io.PacketReceiver;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.client.Client;

/**
 * This class is used for transforming actions into packets.
 * 
 * Note that this class is not thread-safe. It is up to the user of
 * this class to synchronize usage.
 *
 * @author Fredrik
 *
 */
public class LocalActionTransformer implements PacketReceiver {

	/** The actual class used for the transformation */
	private LocalActionToPacketTransformerVisitor handler;
	
	/** The created Packet */
	ProtocolObject createdPacket;
	
	public LocalActionTransformer(Client client) {
		handler = new LocalActionToPacketTransformerVisitor(this, client);
	}
	
	public ProtocolObject transformActionToPacket(LocalAction action) {
		action.visit(handler);
		return createdPacket;
	}

	public void packetCreated(ProtocolObject packet) {
		createdPacket = packet;
	}
	
}
