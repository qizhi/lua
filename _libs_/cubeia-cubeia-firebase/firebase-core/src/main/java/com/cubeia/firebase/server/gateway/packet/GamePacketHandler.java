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
import static com.cubeia.firebase.server.gateway.util.NackUtil.createNack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.NotifyInvitedAction;
import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.TableChatAction;
import com.cubeia.firebase.api.action.TableQueryRequestAction;
import com.cubeia.firebase.api.action.UnWatchAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.common.Stamp;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.chat.ChatFilter;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.game.table.comm.CreationRequestData;
import com.cubeia.firebase.game.table.comm.TableCreationRequest;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.CreateTableRequestPacket;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.InvitePlayersRequestPacket;
import com.cubeia.firebase.io.protocol.JoinRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
import com.cubeia.firebase.io.protocol.WatchRequestPacket;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.jmx.CGWMonitor;
import com.cubeia.firebase.server.gateway.util.StyxConversions;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;


/**
 * The Game Packet Handler's responsibility is to convert 
 * incoming communication into Actions and send them out into the
 * great void where they will be happily processed.
 * 
 * 
 *
 */
public class GamePacketHandler extends AbstractPacketHandler {
    
    private static Logger log = Logger.getLogger(GamePacketHandler.class);
    
    private final Sender<GameEvent> gameRouter;
    private final Sender<ClientEvent<?>> clientRouter;

	private final CommandDispatcher commandDispatcher;

	/** Optionally used to filter chat messages */
	private ChatFilter filter;
	private boolean isFilterLookedUp;

	private final ServiceRegistry reg;

    /** Used for rejection messages 
     * @param commandDispatcher */
    
    
    public GamePacketHandler(Client client, ClientNodeRouter route, CommandDispatcher commandDispatcher, ServiceRegistry reg) {
        super(client, getIdGenerator(reg));
		this.reg = reg;
		this.gameRouter = route.getGameEventSender();
		this.clientRouter = route.getClientEventSender();
		this.commandDispatcher = commandDispatcher;
    }


	/**
     * @param packet
     * @param action
     */
    private void sendEvent(int tableid, GameAction action, ProtocolObject incomingPacket) {
        setActionSequenceNumber(action);
    	HitCounter.getInstance().inc("EventClientToGame");
    	CGWMonitor.registerGamePacket();
        GameEvent event = createGameEvent();
        addressAndSend(tableid, action, event, incomingPacket);
    }



    
    /**
     * @param packet
     * @param action
     */
    private void sendClientEvent(int[] players, GameAction action, ProtocolObject incomingPacket) {
    	MulticastClientEvent event = new MulticastClientEvent();
        event.setTableId(action.getTableId());
        event.setAction(action);
        for (int pid : players) {
        	event.addPlayer(pid);
        }
        publishClientEvent(event, incomingPacket);
    }

    
    /**
     * Creates a basic game event with the player id set.
     * 
     * @param packet
     * @param action
     */
    private GameEvent createGameEvent() {
        // Create the event
        GameEvent event = new GameEvent();
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
    private void addressAndSend(int tableid, GameAction action, GameEvent event, ProtocolObject incomingPacket) {
        event.setTableId(tableid);
        event.setAction(action);
        
        publishGameEvent(event, incomingPacket);
    }
    
    /**
     * @param event
     */
    protected void publishGameEvent(GameEvent event, ProtocolObject incomingPacket) {
        try {
			gameRouter.dispatch(event);
		} catch (ChannelNotFoundException e) {
			log.warn("Event for non-existing table was discarded: "+event);
			boolean handled = sendContextualNack(event, incomingPacket);
			if (!handled) { // Send generic nack message
			    client.sendClientPacket(createNack(incomingPacket.classId(), -1));
			}
		}
    }
    
    /**
     * Check packet type and create a matching error response.
     * 
     * 
     * @param event
     * @param incomingPacket
     * @return false if no match was found. True if we have sent a response.
     */
    private boolean sendContextualNack(GameEvent event, ProtocolObject incomingPacket) {
        if (incomingPacket instanceof TableQueryRequestPacket) {
            TableQueryRequestPacket request = (TableQueryRequestPacket) incomingPacket;
            TableQueryResponsePacket response = new TableQueryResponsePacket(request.tableid, ResponseStatus.DENIED, new ArrayList<SeatInfoPacket>());
            client.sendClientPacket(response);
            return true;
        }
        
        return false;
    }


    /**
     * @param event
     */
    protected void publishClientEvent(ClientEvent<GameAction> event, ProtocolObject incomingPacket) {
		try {
			clientRouter.dispatch(event);
		} catch (ChannelNotFoundException e) {
			log.error("Event for non-existing client channel was discarded: "+event);
			// FIXME: Not valid commands here
			client.sendClientPacket(createNack(incomingPacket.classId(), -1));
		}		
    }
    
    public String toString() {
        return "GamePacketHandler";
    }


    /**
     * Join a table.
     * 
     * Joinpacket -> SitAction
     * 
     * @param packet
     */
    @Override
    public void visit(JoinRequestPacket packet) {
        JoinRequestAction action = new JoinRequestAction(getPlayerid(), packet.tableid, packet.seat, getPlayerData().getScreenname());
        List<Attribute> atts = StyxConversions.convertToAttributes(packet.params);
        action.setParameters(atts);
        sendEvent(packet.tableid, action, packet);
    }

    /**
     * Handle watch table
     * 
     */
    @Override
    public void visit(WatchRequestPacket packet) {
        WatchAction action = new WatchAction(getPlayerid(), packet.tableid);
        sendEvent(packet.tableid, action, packet);
    }
    
    @Override
    public void visit(UnwatchRequestPacket packet) {
    	UnWatchAction action = new UnWatchAction(getPlayerid(), packet.tableid);
    	sendEvent(packet.tableid, action, packet);
    }
    
    @Override
	public void visit(GameTransportPacket packet) {
		GameDataAction action = new GameDataAction(getPlayerid(), packet.tableid);
		action.setData(ByteBuffer.wrap(packet.gamedata));
		action.getAttributes().addAll(fromProtocolAttributes(packet.attributes));
		sendEvent(packet.tableid, action, packet);
	}
    
    @Override
    public void visit(LeaveRequestPacket packet) {
        LeaveAction action = new LeaveAction(getPlayerid(), packet.tableid);
        sendEvent(packet.tableid, action, packet);
    }

    public void visit(ProbePacket packet) {
        ProbeAction action = new ProbeAction(getPlayerid(), packet.tableid, packet.id);
        
        List<Stamp> stamps = new ArrayList<Stamp>(packet.stamps.size());
        for (ProbeStamp ioStamp : packet.stamps) {
        	Stamp stamp = new Stamp(ioStamp.clazz, ioStamp.timestamp);
        	stamps.add(stamp);
        }
        
        action.getTimestamps().addAll(stamps);
        
        action.addTimestamp(getClass());
        sendEvent(packet.tableid, action, packet);
    }

    public void visit(TableChatPacket packet) {
    	checkFilter();
    	TableChatAction action = new TableChatAction(getPlayerid(), packet.tableid, packet.message);
    	if(filter != null) {
    		action = filter.filter(action);
    		if(action == null && log.isDebugEnabled()) {
    			log.debug("Table chat action dropped by filter: " + action);
    		}
    	}
    	if(action != null) {
    		sendEvent(packet.tableid, action, packet);
    	}
    }
    
    @Override
    public void visit(TableQueryRequestPacket packet) {
    	log.debug("Handle TableQueryRequestPacket: "+packet);
    	TableQueryRequestAction action = new TableQueryRequestAction(getPlayerid(), packet.tableid);
    	sendEvent(packet.tableid, action, packet);    	
    }
    
    
    
    @Override
    public void visit(CreateTableRequestPacket packet) {
    	List<Attribute> atts = StyxConversions.convertToAttributes(packet.params);
    	Attribute[] attributes = atts.toArray(new Attribute[0]);
    	CreationRequestData data = new CreationRequestData(getPlayerid(), packet.seats, attributes, packet.invitees);
    	TableCreationRequest request = new TableCreationRequest(packet.gameid, data);
    	request.setSeq(packet.seq);
    	try {
			commandDispatcher.dispatch(request);
		} catch (ClusterException e) {
			log.error("Could not dispatch Table Creation Request ["+request+"]", e);
		}
    }
    
    
    /**
     * Send out a non-reserved invitation to all players.
     */
    @Override
    public void visit(InvitePlayersRequestPacket packet) {
    	NotifyInvitedAction action = new NotifyInvitedAction(getPlayerid(), getPlayerid(), packet.tableid, -1);
    	sendClientEvent(packet.invitees, action, packet);
    }
    
    
	private void checkFilter() {
		if(!isFilterLookedUp) {
			doCheckFilter();
		}
	}

	private synchronized void doCheckFilter() {
		if(isFilterLookedUp) return; // SANITY CHECK
		else {
			isFilterLookedUp = true;
			filter = reg.getServiceInstance(ChatFilter.class);
		}
	}
}