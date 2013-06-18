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
package com.cubeia.firebase.server.gateway.packet;

import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.io.AbstractServerPacketHandler;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.ChannelChatPacket;
import com.cubeia.firebase.io.protocol.CreateTableRequestPacket;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.GameVersionPacket;
import com.cubeia.firebase.io.protocol.InvitePlayersRequestPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelRequestPacket;
import com.cubeia.firebase.io.protocol.JoinRequestPacket;
import com.cubeia.firebase.io.protocol.KickPlayerPacket;
import com.cubeia.firebase.io.protocol.LeaveChatChannelPacket;
import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.LobbyObjectSubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyObjectUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyQueryPacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.LogoutPacket;
import com.cubeia.firebase.io.protocol.MttRegisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterRequestPacket;
import com.cubeia.firebase.io.protocol.PlayerQueryRequestPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.SystemInfoRequestPacket;
import com.cubeia.firebase.io.protocol.SystemInfoResponsePacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
import com.cubeia.firebase.io.protocol.UnwatchResponsePacket;
import com.cubeia.firebase.io.protocol.VersionPacket;
import com.cubeia.firebase.io.protocol.WatchRequestPacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.IPlayerData;
import com.cubeia.firebase.service.action.IdGenerator;


/**
 * To implement your own logic, extend this class and override the 
 * handlers for the packets you are interested in.
 * 
 * Created on 2006-sep-07
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class AbstractPacketHandler extends AbstractServerPacketHandler {
	
	protected static IdGenerator getIdGenerator(ServiceRegistry reg) {
		return reg.getServiceInstance(IdGenerator.class);
	}
	
    /*
     * This is a shared (for all clients on this node) sequence counter.
     * This counter will wrap so that must be handled in the application logic.
     */
    // protected static AtomicInteger sequenceCounter = new AtomicInteger(new Random().nextInt(100024));
    
    protected Logger log = Logger.getLogger(getClass());
    protected final Client client;
    protected final IdGenerator idGenerator;

    /**
     * Constructor with the client injected
     * @param client
     */
    public AbstractPacketHandler(Client client, IdGenerator idGenerator) {
        //Arguments.notNull(gameRouter, "game router");
    	this.client = client;
		this.idGenerator = idGenerator;
	}
    
    
    
    /**
     * Get the client associated with this handler
     * @return
     */
    public Client getClient() {
        return client;
    }

    /**
     * Get the player id for the current client.
     * I.e. the client maps to the session and the
     * player maps to the logged in entity.
     *
     * @return
     */
    public IPlayerData getPlayerData() {
        IPlayerData data = client.getClientData();
        return data;
    }
    
    /**
     * Get the player id for the current client.
     * I.e. the client maps to the session and the
     * player maps to the logged in entity.
     *
     * @return
     */
    public int getPlayerid() {
    	IPlayerData data = client.getClientData();
        return data.getId();
    }        
    
    /**
     * Send a single packet.
     * @param packet
     */
    public void sendPacket(ProtocolObject packet) {
        client.sendClientPacket(packet);
    }
    
    /**
     * Send a collection of packets.
     * @param packets
     */
    public void sendPackets(List<ProtocolObject> packets) {
    	client.sendClientPackets(packets);
    }
        
    public String toString() {
        return "AbstractPacketHandler";
    }

    protected void setActionSequenceNumber(Action action) {
        action.setActionId(idGenerator.generate());
    	/*int seq = sequenceCounter.incrementAndGet();
        action.setSeq(seq);
        if (seq < 0) {
            synchronized (sequenceCounter) {
                if (sequenceCounter.get() < 0) {
                    log.info("Action sequence counter reset to 0");
                    sequenceCounter.set(0);
                    action.setSeq(sequenceCounter.incrementAndGet());
                }
            }
        }*/
    }
    
    // === EMPTY DEFAULT IMPLEMENTATIONS ===
    public void visit(LoginRequestPacket packet) {}
	public void visit(LogoutPacket packet) {}
	public void visit(WatchRequestPacket packet) {}
	public void visit(UnwatchRequestPacket packet) {}
	public void visit(JoinChatChannelRequestPacket packet) {}
	public void visit(LeaveChatChannelPacket packet) {}
	public void visit(ChannelChatPacket packet) {}
	public void visit(JoinRequestPacket packet) {}
	public void visit(LeaveRequestPacket packet) {}
	public void visit(LobbyQueryPacket packet) {}
	public void visit(GameTransportPacket packet) {}
	public void visit(ServiceTransportPacket packet) {}
	public void visit(ProbePacket packet) {}
	public void visit(ProbeStamp packet) {}
	public void visit(TableChatPacket packet) {}
	public void visit(FilteredJoinTableRequestPacket packet) {}
	public void visit(UnwatchResponsePacket packet) {}
	public void visit(VersionPacket packet) {}
	public void visit(GameVersionPacket packet) {}
	public void visit(LeaveResponsePacket packet) {}
	public void visit(LobbySubscribePacket packet) {}
	public void visit(LobbyUnsubscribePacket packet) {}
	public void visit(FilteredJoinCancelRequestPacket packet) {}
    public void visit(KickPlayerPacket packet) {}
	public void visit(PlayerQueryRequestPacket packet) {}
	public void visit(TableQueryRequestPacket packet) {}
	public void visit(MttRegisterRequestPacket packet) {}
	public void visit(MttUnregisterRequestPacket packet) {}
	public void visit(LocalServiceTransportPacket packet) {}
	public void visit(MttTransportPacket packet) {}
	public void visit(EncryptedTransportPacket packet) {}
	public void visit(CreateTableRequestPacket packet) {}
	public void visit(InvitePlayersRequestPacket packet) {}
	public void visit(LobbyObjectSubscribePacket packet) {}
	public void visit(LobbyObjectUnsubscribePacket packet) {}
    public void visit(SystemInfoRequestPacket packet) {}
    public void visit(SystemInfoResponsePacket packet) {}
	
}

