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

import static com.cubeia.firebase.api.action.Attribute.fromProtocolAttributes;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelAction;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.api.service.ServiceDiscriminator;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.ChannelChatPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveChatChannelPacket;
import com.cubeia.firebase.io.protocol.LogoutPacket;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;
import com.cubeia.firebase.io.protocol.PlayerQueryRequestPacket;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.LocalServiceEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.util.NackUtil;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.service.chat.ChatServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.playerinfo.PlayerInfoServiceContract;
import com.cubeia.firebase.service.wlist.FilteredJoinService;
import com.cubeia.firebase.service.wlist.FilteredJoinServiceContract;




/**
 * Handles packets concerning the Lobby and general client actions 
 * that will not propagate to any game.
 * 
 * 
 * Created on 2006-sep-07
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class LocalPacketHandler extends AbstractPacketHandler{
    
	/**
	 * Waiting list / Filtered Join services
	 */
	private FilteredJoinService waitingList;
    
	/**
	 * TODO: This is an ad-hoc implementation of the Chat handler.
	 * This should be defined as a server service (probably as a node
	 * service will require a router).
	 */
	private static ChatServiceContract chat;
	
	/**
	 * We need the service registry to be able to lookup
	 * services in the system as needed.
	 */
	private ServiceRegistry serviceRegistry;

	private final ClientNodeRouter route;

    /**
     * Constructor with injected client
     * 
     * @param client
     * @param route  
     */
    public LocalPacketHandler(Client client, ClientNodeRouter route, ServiceRegistry reg) {
        super(client, getIdGenerator(reg));
    	setServiceRegistry(reg, route.getGameEventSender());
    	this.route = route;
	}
    
    /*
     * Inject a service registry.
     * Will lookup lobby services and chat services.
     * 
     * @param serviceRegistry
     */
    private void setServiceRegistry(ServiceRegistry serviceRegistry, Sender<GameEvent> sender) {
		this.serviceRegistry = serviceRegistry;
		chat = serviceRegistry.getServiceInstance(ChatServiceContract.class);
		waitingList = serviceRegistry.getServiceInstance(FilteredJoinServiceContract.class);
		
        // FIXME
		if (waitingList != null) {
			waitingList.setGameRouter(sender);
		}
	}

    /* (non-Javadoc)
     * @see com.cubeia.firebase.server.gateway.packet.AbstractPacketHandler#toString()
     */
    @Override
    public String toString() {
        return "LocalPacketHandler";
    }
    
    
    @Override
    public void visit(LogoutPacket packet) {
    	ClientRegistryServiceContract registry = serviceRegistry.getServiceInstance(ClientRegistryServiceContract.class);
    	registry.getClientRegistry().logoutClient(client, packet.leaveTables);
    	log.debug("Client ["+client.getId()+":"+client.getClientData().getScreenname()+"] logged out (Leave tables: "+packet.leaveTables+")");
    	PostLoginProcessor proc = serviceRegistry.getServiceInstance(PostLoginProcessor.class);
    	if(proc != null) {
    		proc.clientLoggedOut(client.getId());
    	}
    }
    
	
    @Override
	public void visit(JoinChatChannelRequestPacket packet) {
		log.debug("Handle Packet: "+packet);
		Arguments.notNull(chat, "ChatService");
    	chat.addPlayer(client, packet.channelid);
	}

    @Override
	public void visit(LeaveChatChannelPacket packet) {
		log.debug("Handle Packet: "+packet);
		Arguments.notNull(chat, "ChatService");
    	chat.removePlayer(client, packet.channelid);
	}

    @Override
	public void visit(ChannelChatPacket packet) {
		log.debug("Handle Packet: "+packet);
		Arguments.notNull(chat, "ChatService");
    	ChannelChatAction action = new ChannelChatAction(-1);
    	action.setChannelid(packet.channelid);
    	action.setPlayerid(client.getClientData().getId());
    	action.setTargetid(packet.targetid);
    	action.setMessage(packet.message);
    	action.setNick(client.getClientData().getScreenname());
    	
    	chat.handle(action);
	}
  
    
    /**
     * Place player in waiting list for the table filter as described in the packet.
     */
    public void visit(FilteredJoinTableRequestPacket packet) {
		FilteredJoinAction action = new FilteredJoinAction(getPlayerid(), packet.gameid);
		action.setAddress(packet.address);
		action.setSeq(packet.seq);
		
		List<Parameter<?>> parameters = new ArrayList<Parameter<?>>(packet.params.size());
		
		for (ParamFilter filter : packet.params) {
			Parameter.Operator op = Parameter.Operator.values()[filter.op];
			
			Param param = filter.param;
			
			// ByteBuffer buffer = ByteBuffer.wrap(param.value);
			
			Parameter<?> convert = ParameterUtil.convert(param);
			if(convert != null) {
				convert.setOperator(op);
				parameters.add(convert);
			}
			
			/*if (param.type == Parameter.Type.INT.ordinal()) {
				Integer v = new Integer(buffer.getInt());
				Parameter<Integer> p = new Parameter<Integer>(param.key, v, Parameter.Type.INT, op);
				parameters.add(p);
			} else if (param.type == Parameter.Type.STRING.ordinal()) {
				String s = new String(param.value);
				Parameter<String> p = new Parameter<String>(param.key, s, Parameter.Type.STRING, op);
				parameters.add(p);
			} else if(param.type == Parameter.Type.DATE.ordinal()) {
				int v = buffer.getInt();
				Date d = new Date(v * 1000L);
				Parameter<Date> p = new Parameter<Date>(param.key, d, Parameter.Type.DATE, op);
				parameters.add(p);
			} else {
				log.error("Unknown type in parameter for Filtered Join. Type: "+param.type);
			}*/
		}
		
		action.setParameters(parameters);
		waitingList.addFilteredJoinAction(action, client.getLocalActionHandler());
	}
	
	@Override
	public void visit(FilteredJoinCancelRequestPacket packet) {
		// Get the server side request id
		long joinRequestId = client.getLocalData().getJoinRequestId(packet.seq);
		FilteredJoinCancelAction action = new FilteredJoinCancelAction(joinRequestId);
		waitingList.cancelFilteredJoinAction(action, client.getLocalActionHandler());
	}
 
	/**
	 * Service transport packet.
	 * Sends data to an addressable service. 
	 * 
	 * Id type:
	 *  0 : Service ID
	 *  1 : Contract
	 *  
	 */
    @Override
    public void visit(ServiceTransportPacket packet) {
		String identifier = packet.service; // service id or contract interface
		boolean isIdContract = packet.idtype != 0;//see above, false if public id, true for contract
		ServiceDiscriminator disc = new ServiceDiscriminator(identifier, isIdContract);
		Sender<LocalServiceEvent> sender = route.getServiceStackSender(disc);
		if (sender != null) {
			ServiceAction action = new ClientServiceAction(getPlayerid(), packet.seq, packet.servicedata);
			action.getAttributes().addAll(fromProtocolAttributes(packet.attributes));
			LocalServiceEvent event = new LocalServiceEvent(disc, action);
			try {
				sender.dispatch(event);
			} catch (ChannelNotFoundException e) {
				log.warn("Event for non-existing service was discarded: "+event);
				client.sendClientPacket(NackUtil.createNack(packet.classId(), -1));
			} // asynch dispatch
		} else {
			log.warn("Could not find addressable service ["+identifier+"]. Using contract ["+isIdContract+"]");
		}
    }
    
    
    /**
     * Player Query will be routed to the Client Registry.
     * 
     */
    @Override
    public void visit(PlayerQueryRequestPacket packet) {
    	if (serviceRegistry != null) {
    		PlayerInfoServiceContract infoService = serviceRegistry.getServiceInstance(PlayerInfoServiceContract.class);
    		if (infoService == null) {
    			log.warn("No Player Info Service deployed");
    		} else {
    			PlayerQueryRequestAction request = new PlayerQueryRequestAction(packet.pid);
    			infoService.handlePlayerQueryRequest(request, client.getLocalActionHandler());
    		}
    		
    	} else {
    		log.fatal("No service registry defined for player query request.");
    	}
    }

}   

