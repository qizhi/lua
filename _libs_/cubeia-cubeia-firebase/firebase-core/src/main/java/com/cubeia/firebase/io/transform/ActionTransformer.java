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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.PlayerAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.io.PacketReceiver;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.service.clientreg.ClientRegistry;

/**
 * This class is used for transforming actions into packets.
 * 
 * Note that this class is not thread-safe. It is up to the user of
 * this class to synchronize usage.
 *
 */
public class ActionTransformer implements PacketReceiver {
	
	ProtocolObject createdPacket;
	ActionToPacketTranformerVisitor actionTranformer;
	
	public ActionTransformer(ClientRegistry registry) {
		actionTranformer = new ActionToPacketTranformerVisitor(this, registry);
	}
	
	public ProtocolObject transformActionToPacket(GameAction action) {
		action.visit(actionTranformer);
		return createdPacket;
	}
	
	public ProtocolObject transformActionToPacket(MttAction action) {
	    action.accept(actionTranformer);
	    return createdPacket;
	}
	
	public Collection<ProtocolObject> transformActionsToPackets(Collection<PlayerAction> actions) {
		List<ProtocolObject> packets = new ArrayList<ProtocolObject>(actions.size());
		
		for (PlayerAction action : actions) {
			// We need to find a way to convert an action to a packet here.
			packets.add(transformActionToPacket(action));
		}
		
		return packets;
	}
	
	public void packetCreated(ProtocolObject packet) {
		createdPacket = packet;
	}
}
