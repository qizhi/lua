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

import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinTableAvailableAction;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.action.local.SystemInfoRequestAction;
import com.cubeia.firebase.api.action.local.SystemInfoResponseAction;
import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.PacketReceiver;
import com.cubeia.firebase.io.protocol.BadPacket;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelResponsePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.PlayerQueryResponsePacket;
import com.cubeia.firebase.io.protocol.SystemInfoResponsePacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.util.BinaryData;

/**
 * Transformer for Local Action
 *
 * @author Fredrik
 */
public class LocalActionToPacketTransformerVisitor implements LocalActionVisitor {
	
	private final PacketReceiver receiver;
	private final Client client;
	
	public LocalActionToPacketTransformerVisitor(PacketReceiver receiver, Client client) {
		this.receiver = receiver;
		this.client = client;
	}

	public void handle(LoginResponseAction action) {
		LoginResponsePacket packet = new LoginResponsePacket();
		packet.pid = action.getPlayerid();
		packet.credentials = action.getData();
		packet.message = action.getErrorMessage();
		packet.code = action.getErrorCode();
		packet.screenname = action.getScreenname();
		
		if (action.isAccepted()) {
			// Login successful
			packet.status = Enums.ResponseStatus.values()[Status.OK.ordinal()];
			
		} else {
			// Login failed 
			packet.status = Enums.ResponseStatus.values()[Status.DENIED.ordinal()];
		}
		receiver.packetCreated(packet);
	}

	
	

	/**
     * Returns a BAD packet.
     * 
     * @param cmd, command to NAK
     * @param code, error code (see protocol)
     * @return
     */
    public BadPacket getNAKPacket(int cmd, int code) {
        BadPacket nak = new BadPacket();
        nak.cmd  = BinaryData.intToByte(cmd);
        nak.error = BinaryData.intToByte(code);
        return nak;
    }

	public void handle(FilteredJoinResponseAction action) {
		FilteredJoinTableResponsePacket packet = new FilteredJoinTableResponsePacket();
		packet.address = action.getAddress();
		packet.gameid = action.getGameId();
		packet.status = Enums.FilteredJoinResponseStatus.values()[action.getStatus()];
		packet.seq = action.getSeq();
		
		receiver.packetCreated(packet);
	}
	
	public void handle(FilteredJoinCancelResponseAction action) {
		FilteredJoinCancelResponsePacket packet = new FilteredJoinCancelResponsePacket();
		packet.status = Enums.ResponseStatus.values()[Status.OK.ordinal()];
		int joinRequestSeq = client.getLocalData().getJoinRequestSeq(action.getRequestId());
		packet.seq = joinRequestSeq;
		
		receiver.packetCreated(packet);
	}
	
	
	public void handle(FilteredJoinTableAvailableAction action) {
		FilteredJoinTableAvailablePacket packet = new FilteredJoinTableAvailablePacket();
		packet.tableid = action.getTableId();
		packet.seat = BinaryData.intToByte(action.getSeat());
		
		// Get sequence number for the request
		int seq = client.getLocalData().getJoinRequestSeq(action.getRequestId());
		if(seq == -1) {
			seq = action.getSequenceId();
		}
		packet.seq = seq;
		
		receiver.packetCreated(packet);
	}
	
	
	public void handle(PlayerQueryResponseAction action) {
		PlayerQueryResponsePacket packet = new PlayerQueryResponsePacket();
		packet.pid = action.getPlayerId();
		packet.nick = action.getScreenname();
		packet.data = action.getData();
		packet.status = Enums.ResponseStatus.values()[action.getStatus().ordinal()];
		receiver.packetCreated(packet);
	}
	
	
	public void handle(LocalServiceAction action) {
		LocalServiceTransportPacket packet = new LocalServiceTransportPacket();
		packet.servicedata = action.getData();
		packet.seq = action.getSequence();
		receiver.packetCreated(packet);
	}
	
	
	public void handle(SystemInfoResponseAction systemInfoResponseAction) {
        SystemInfoResponsePacket packet = new SystemInfoResponsePacket();
        packet.players = systemInfoResponseAction.getPlayers();
        packet.params = new ArrayList<Param>();
        for (Parameter<?> p : systemInfoResponseAction.getParameters()) {
            packet.params.add(ParameterUtil.convert(p));
        }
        receiver.packetCreated(packet);
    }
	
	public void handle(SystemInfoRequestAction systemInfoRequestAction) {}
	public void handle(LoginRequestAction action) {}
	public void handle(FilteredJoinAction action) {}
	public void handle(FilteredJoinCancelAction action) {}
	public void handle(PlayerQueryRequestAction action) {}
	
}
