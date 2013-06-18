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
/*
 * Created on 2006-sep-07
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
package com.cubeia.firebase.server.gateway.packet;

import static com.cubeia.firebase.api.action.Attribute.fromProtocolAttributes;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttRegisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.MttUnregisterPlayerAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.MttRegisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterRequestPacket;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.util.NackUtil;
import com.cubeia.firebase.server.gateway.util.StyxConversions;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;


/**
 * The MTT Packet Handler's responsibility is to convert 
 * incoming tournament communication into Actions and send them out into the
 * great void where they will be happily processed.
 * 
 */
public class MTTPacketHandler extends AbstractPacketHandler {
    
    private final Logger log = Logger.getLogger(MTTPacketHandler.class);

    private final Sender<MttEvent> mttRouter;

    public MTTPacketHandler(Client client, Sender<MttEvent> gameRouter, ServiceRegistry reg) {
        super(client, getIdGenerator(reg));
		this.mttRouter = gameRouter;
    }
    
    
    /**
     * @param packet
     * @param action
     */
    private void sendEvent(int mttId, MttAction action, ProtocolObject incomingPacket) {
        setActionSequenceNumber(action);
    	MttEvent event = createGameEvent();
        addressAndSend(mttId, action, event, incomingPacket);
    }

    
    /**
     * Creates a basic game event with the player id set.
     * 
     * @param packet
     * @param action
     */
    private MttEvent createGameEvent() {
        // Create the event
        MttEvent event = new MttEvent();
        event.setPlayerId(getPlayerid());
        return event;
    }
    
    /**
     * Sets the address of the event and sends it.
     * 
     * @param packet
     * @param action
     * @param event
     */
    private void addressAndSend(int mttId, MttAction action, MttEvent event, ProtocolObject incomingPacket) {
    	event.setMttId(mttId);
    	event.setAction(action);
    	
        try {
            mttRouter.dispatch(event);
        } catch (ChannelNotFoundException e) {
            log.warn("Event for non-existing tournament was discarded: "+event);
            // FIXME: Not valid commands here
            client.sendClientPacket(NackUtil.createNack(incomingPacket.classId(), -1));
        }
    }
    
    public String toString() {
        return "MTTPacketHandler";
    }
    
    @Override
    public void visit(MttRegisterRequestPacket packet) {
    	MttRegisterPlayerAction action = new MttRegisterPlayerAction(packet.mttid, getPlayerid());
    	List<Attribute> atts = StyxConversions.convertToAttributes(packet.params);
        action.setParameters(atts);
    	action.setScreenname(getPlayerData().getScreenname());
    	sendEvent(packet.mttid, action, packet);
    }
    
    @Override
    public void visit(MttUnregisterRequestPacket packet) {
    	MttUnregisterPlayerAction action = new MttUnregisterPlayerAction(packet.mttid, getPlayerid());
    	sendEvent(packet.mttid, action, packet);
    }
    
    @Override
    public void visit(MttTransportPacket packet) {
        MttDataAction action = new MttDataAction(packet.mttid, getPlayerid());
        action.getAttributes().addAll(fromProtocolAttributes(packet.attributes));
        action.setData(ByteBuffer.wrap(packet.mttdata));
        sendEvent(packet.mttid, action, packet);
    }
    
}
