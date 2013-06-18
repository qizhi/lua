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

import static com.cubeia.firebase.api.action.Attribute.fromAttributesToProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cubeia.firebase.api.action.CreateTableResponseAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.KickPlayerAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.MttPickedUpAction;
import com.cubeia.firebase.api.action.MttRegisterResponseAction;
import com.cubeia.firebase.api.action.MttSeatedAction;
import com.cubeia.firebase.api.action.MttUnregisterResponseAction;
import com.cubeia.firebase.api.action.NotifyInvitedAction;
import com.cubeia.firebase.api.action.PlayerInfoAction;
import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.RequestStatusAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.action.TableChatAction;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.action.UnWatchResponseAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.visitor.DefaultActionVisitor;
import com.cubeia.firebase.api.common.Stamp;
import com.cubeia.firebase.io.PacketReceiver;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.BadPacket;
import com.cubeia.firebase.io.protocol.CreateTableResponsePacket;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.GoodPacket;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.KickPlayerPacket;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.MttPickedUpPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttSeatedPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyChannelChatPacket;
import com.cubeia.firebase.io.protocol.NotifyInvitedPacket;
import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.SystemMessagePacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
import com.cubeia.firebase.io.protocol.UnwatchResponsePacket;
import com.cubeia.firebase.io.protocol.WatchResponsePacket;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.util.BinaryData;

/**
 * This class is used to visit actions and transform them into packets.
 * 
 * When an action has been visited, the <code>packetCreated</code>
 * method will be called.
 * 
 * 
 */
public class ActionToPacketTranformerVisitor extends DefaultActionVisitor {
	
	private PacketReceiver receiver;

	private final ClientRegistry registry;

	public ActionToPacketTranformerVisitor(PacketReceiver receiver, ClientRegistry registry) {
		this.receiver = receiver;
		this.registry = registry;
	}
	
	@Override
	public void visit(GameDataAction action) {
		GameTransportPacket packet = new GameTransportPacket();
		packet.attributes = fromAttributesToProtocol(action.getAttributes());
		packet.pid = action.getPlayerId();
		packet.tableid = action.getTableId();
		packet.gamedata = action.getData().array();		
		receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(SystemMessageAction action) {
		SystemMessagePacket p = new SystemMessagePacket();
		p.level = action.getLevel();
		p.message = action.getMessage();
		p.type = action.getType();
		int[] pids = action.getPlayerIds();
		if(pids != null) {
			p.pids = pids;
		}
		receiver.packetCreated(p);
	}
	
	/**
	 * TODO: Send cmd id instead of enum ordinal
	 */
	@Override
	public void visit(RequestStatusAction action) {
		ProtocolObject packet;
		if (action.isAccepted()) {
			GoodPacket pckt = new GoodPacket();
			pckt.cmd = BinaryData.intToByte(action.getRequest().ordinal());
			packet = pckt;
			pckt.extra = action.getResponseCode();
			
		} else {
			BadPacket pckt = new BadPacket();
			pckt.cmd = BinaryData.intToByte(action.getRequest().ordinal());
			pckt.error = BinaryData.intToByte(action.getResponseCode());
			packet = pckt;
		}
		receiver.packetCreated(packet);
	}
    
    @Override
    public void visit(ProbeAction action) {
    	action.addTimestamp(getClass());
        ProbePacket packet = new ProbePacket();
        packet.tableid = action.getTableId();
        packet.id = action.getId();
        
        // We need to convert Stamps to IO-Stamps and set them
        Collection<Stamp> timestamps = action.getTimestamps();
        List<ProbeStamp> stamps = new ArrayList<ProbeStamp>(timestamps.size());
        for (Stamp stamp : timestamps) {
        	ProbeStamp ioStamp = new ProbeStamp();
        	ioStamp.clazz = stamp.clazz;
        	ioStamp.timestamp = stamp.timestamp;
        	stamps.add(ioStamp);
        }
        
        packet.stamps = stamps;
        receiver.packetCreated(packet);
    }

	@Override
	public void visit(JoinRequestAction action) {
        // Create client side packet
        NotifyJoinPacket packet = new NotifyJoinPacket();
        packet.tableid = action.getTableId();
        packet.pid = action.getPlayerId();
        packet.seat = BinaryData.intToByte(action.getSeatId());
        packet.nick = action.getNick();

		receiver.packetCreated(packet);
	}

	@Override
	public void visit(PlayerInfoAction action) {
        // Create client side packet
        PlayerInfoPacket packet = createPlayerInfoPacket(action);
        receiver.packetCreated(packet);
	}

	@Override
	public void visit(LeaveAction action) {
		NotifyLeavePacket packet = new NotifyLeavePacket();
        packet.tableid = action.getTableId();
        packet.pid = action.getPlayerId();
        
        receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(LeaveResponseAction action) {
		LeaveResponsePacket packet = new LeaveResponsePacket();
        packet.tableid = action.getTableId();
        packet.status =  Enums.ResponseStatus.values()[action.getStatus()];
        packet.code = action.getResponseCode();
        
        receiver.packetCreated(packet);
	}

	@Override
	public void visit(UnWatchResponseAction action) {
		UnwatchResponsePacket packet = new UnwatchResponsePacket();
        packet.tableid = action.getTableId();
        packet.status = Enums.ResponseStatus.values()[action.getStatus()];
        
        receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(ChannelChatAction action) {
		NotifyChannelChatPacket packet = new NotifyChannelChatPacket();
		//ChannelChatPacket packet = new ChannelChatPacket();
		packet.channelid = action.getChannelid();
		packet.pid = action.getPlayerid();
		packet.nick = action.getNick();
		packet.message = action.getMessage();
		packet.targetid = action.getTargetid();
		
        // Send packet.
        receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(TableChatAction action) {
		TableChatPacket packet = new TableChatPacket();
		packet.tableid = action.getTableId();
		packet.pid = action.getPlayerId();
		packet.message = action.getMessage();
		
		receiver.packetCreated(packet);
	}

	@Override
	public void visit(JoinResponseAction action) {
		JoinResponsePacket packet = new JoinResponsePacket();
		packet.tableid = action.getTableId();
		packet.seat = BinaryData.intToByte(action.getSeatId());
		packet.status = Enums.JoinResponseStatus.values()[action.getStatus()];
		packet.code = action.getResponseCode();
		
		receiver.packetCreated(packet);
	}

    @Override
    public void visit(WatchResponseAction action) {
        WatchResponsePacket packet = new WatchResponsePacket();
        packet.tableid = action.getTableId();
        packet.status = action.getStatus();
        receiver.packetCreated(packet);
    }

    @Override
    public void visit(KickPlayerAction action) {
        KickPlayerPacket packet = new KickPlayerPacket();
        packet.tableid = action.getTableId();
        packet.reasonCode = action.getReasonCode();
        receiver.packetCreated(packet);
    }
    
    @Override
    public void visit(ClientServiceAction action) {
    	ServiceTransportPacket packet = new ServiceTransportPacket();
    	packet.attributes = fromAttributesToProtocol(action.getAttributes());
    	packet.pid = action.getPlayerId();
    	packet.servicedata = action.getData();
        packet.service = "";
        packet.seq = action.getSeq();
        receiver.packetCreated(packet);
    }

    @Override
    public void visit(SeatInfoAction action) {
        SeatInfoPacket packet = createSeatInfoPacket(action);
        receiver.packetCreated(packet);
    }

	private SeatInfoPacket createSeatInfoPacket(SeatInfoAction action) {
		SeatInfoPacket packet = new SeatInfoPacket();
        packet.seat = (byte) action.getSeatId();
        packet.tableid = action.getTableId();
        packet.status = Enums.PlayerStatus.values()[action.getStatus().ordinal()];
        packet.player = createPlayerInfoPacket(action.getPlayerInfo());
		return packet;
	}
    
    @Override
    public void visit(TableQueryResponseAction action) {
    	TableQueryResponsePacket packet = new TableQueryResponsePacket();
    	packet.tableid = action.getTableId();
    	packet.seats = createSeatInfoPackets(action.getSeatInfos());
    	packet.status = Enums.ResponseStatus.values()[action.getStatus().ordinal()];
    	receiver.packetCreated(packet);
    }
    
    @Override
    public void visit(MttSeatedAction action) {
    	MttSeatedPacket packet = new MttSeatedPacket();
        packet.mttid = action.getMttid();
        packet.tableid = action.getTableId();
        packet.seat = (byte) action.getSeatId();
        receiver.packetCreated(packet);
    }
    
    @Override
    public void visit(MttPickedUpAction action) {
    	MttPickedUpPacket packet = new MttPickedUpPacket();
        packet.mttid = action.getMttid();
        packet.tableid = action.getTableId();
        packet.keepWatching = action.keepWatching();
        receiver.packetCreated(packet);
    }
    
    private List<SeatInfoPacket> createSeatInfoPackets(List<SeatInfoAction> seatInfos) {
    	List<SeatInfoPacket> packets = new ArrayList<SeatInfoPacket>(seatInfos.size());
    	for (SeatInfoAction s : seatInfos) {
			packets.add(createSeatInfoPacket(s));
		}
    	return packets;
	}

	private PlayerInfoPacket createPlayerInfoPacket(PlayerInfoAction playerInfo) {
        PlayerInfoPacket packet = new PlayerInfoPacket();
        packet.pid = playerInfo.getPlayerId();
        packet.nick = playerInfo.getNick();
        packet.details = playerInfo.getDetails();
        return packet;
    }
    
	@Override
	public void visit(MttRegisterResponseAction action) {
	    MttRegisterResponsePacket packet = new MttRegisterResponsePacket();
	    packet.mttid = action.getMttId();
	    packet.status = Enums.TournamentRegisterResponseStatus.values()[action.getStatus().ordinal()];
	    receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(MttDataAction action) {
        MttTransportPacket packet = new MttTransportPacket();
        packet.attributes = fromAttributesToProtocol(action.getAttributes());
        packet.mttid = action.getMttId();
        packet.pid = action.getPlayerId();
        packet.mttdata = action.getData().array();
        receiver.packetCreated(packet);	    
	}
	
	@Override
	public void visit(CreateTableResponseAction action) {
		CreateTableResponsePacket packet = new CreateTableResponsePacket();
		packet.tableid = action.getTableId();
		packet.seat = (byte) action.getSeat();
		packet.status = Enums.ResponseStatus.values()[action.getStatus().ordinal()];
		packet.code = action.getResponseCode();
		packet.seq = action.getSeq();
		receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(NotifyInvitedAction action) {
		NotifyInvitedPacket packet = new NotifyInvitedPacket();
		packet.inviter = action.getInviterId();
		String screenname = registry.getScreenname(packet.inviter);
		if (screenname == null) screenname = "";
		packet.screenname = screenname;
		packet.tableid = action.getTableId();
		packet.seat = (byte) action.getSeat();
		receiver.packetCreated(packet);
	}
	
	@Override
	public void visit(MttUnregisterResponseAction action) {
	    MttUnregisterResponsePacket packet = new MttUnregisterResponsePacket();
	    packet.mttid = action.getMttId();
	    packet.status = Enums.TournamentRegisterResponseStatus.values()[action.getStatus().ordinal()];
	    receiver.packetCreated(packet);		
	}
	
	
}
