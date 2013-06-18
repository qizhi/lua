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
package com.cubeia.firebase.service.chat.local;

import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.chat.ChatFilter;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.event.ChatEventDaemonLoopback;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.chat.ChatServiceContract;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

/**
 * See the {@link ChatServiceContract implemented interface} for a functional 
 * description of this service. This is an local (internal to the JVM) implementation.
 * 
 * @author Fredrik
 * @see ChatServiceContract
 */
public class LocalChat implements Service, ChatServiceContract {
	
	private Logger log = Logger.getLogger(getClass());
	
	@SuppressWarnings("unused")
	private State state = State.STOPPED;
	
	/** @see setLocalLoopback */
	private ChatEventDaemonLoopback loopback;

    /** Used for setting and getting data in the system state. */
	private SystemStateServiceContract systemState;
	
	/** Optionally used to filter chat messages */
	private ChatFilter filter;
	private boolean isFilterLookedUp;

    /** Used for sending events. */
	private Sender<Event<?>> eventSender;

	private ServiceContext context;
	
	/**
	 * Lookup the system state which is needed
	 */
	@Override
	public void init(ServiceContext con) throws SystemException {
		this.context = con;
		ServiceRegistry parentRegistry = con.getParentRegistry();
		systemState = parentRegistry.getServiceInstance(SystemStateServiceContract.class);
	}

	
	
	/**
	 * This implementation is only due to the fact that I cant get hold of the client
	 * router at the moment and I need to test the other chain of logic.
	 * 
	 * This method is despicable since the event will not reach the
	 * other client gateway nodes.
	 * 
	 * TODO: Remove this method and use a client router. 
	 * 
	 * @param loopback
	 */
	@Deprecated
	public void setLocalLoopback(ChatEventDaemonLoopback loopback) {
		this.loopback = loopback;
	}
	
	@Override
	public void start() {
		try {
			MBusContract mbus = context.getParentRegistry().getServiceInstance(MBusContract.class);
			eventSender = new WrappingSender<Event<?>>(mbus.createSender(EventType.CHAT, context.getPublicId()));
		} catch (MBusException e) {
			throw new RuntimeException("Could not create event sender for chat events.", e);
		}
		state = State.STARTED;
	}

	@Override
	public void stop() {
		state = State.STARTED;
	}
	
	/**
	 * Remove references
	 */
	@Override
	public void destroy() {
		systemState = null;
		if(eventSender != null) {
			eventSender.destroy();
			eventSender = null;
		}
		loopback = null;
	}
	
	@Override
	public void addPlayer(Client client, int channelId) {
		checkFilter();
        int playerId = client.getId();
		log.debug("Add player: "+playerId+" to Chat-Channel: "+channelId);
		String fqn = getFqn(channelId);
		boolean hasNode = systemState.hasNode(fqn);
		// I dont really know what else to use so I'm putting
		// player id as key and value here.
		systemState.setAttribute(fqn, ""+playerId, playerId);
		
		if(!hasNode && filter != null) {
			filter.channelCreated(playerId, channelId);
		}
        // Add this channel to client's list of active channels.
        client.getLocalData().addChatChannel(channelId);
	}



	/**
	 * Transmits the message to the appropiate recipients.
	 */
	@Override
	public void handle(ChannelChatAction chat) {
		checkFilter();
		if(filter != null) {
			chat = filter.filter(chat);
		}
		if(chat == null) {
			// EARLY RETURN, DROPPED MESSAGE
			return;
		} else {
			handleFilteredAction(chat);
		}
	}

	private void handleFilteredAction(ChannelChatAction chat) {
		MulticastClientEvent event = null;
		String fqn = getFqn(chat.getChannelid());
		Map<Object, Object> players = systemState.getAttributes(fqn);
		if (chat.getTargetid() > 0) {
			// Private message
			if (players.containsValue(chat.getTargetid())) {
				// Send to target only
				event = createEvent(chat);
				event.addPlayer(chat.getTargetid());
			} 
		} else {
			// public message, send to all regged
			event = createEvent(chat);
			
			// Add recipients
			for (Object pid : players.values()) {
				int player = (Integer)pid;

				// Don't add the sender again.
				if (player != chat.getPlayerid()) {
					event.addPlayer(player);
				}
			}
		}
		
		// Sanity check first
		if (event != null) {
			if (eventSender != null) {
				try {
					eventSender.dispatch(event);
				} catch (ChannelNotFoundException e) {
					log.error("Chat Event for non-existing chat channel was discarded: "+event);
				}
			} else if (loopback != null){
				log.warn("No client router defined for chat messages. Using local loopback");
				loopback.dispatch(event);
				
			} else {
				// All is lost. Log an error and go back into the hole you came from.
				log.error("There is no way to propagate chat messages!");
			}
		}
	}

    /**
     * Creates a multi player client event.
     * 
     * @param chat
     * @return
     */
	private MulticastClientEvent createEvent(ChannelChatAction chat) {
		MulticastClientEvent event = new MulticastClientEvent();
		event.setPlayerId(chat.getPlayerid());
		event.setAction(chat);
		return event;
	}

	/**
	 * Remove player from chat
	 */
	@Override
	public void removePlayer(Client client, int channelId) {
		
        int playerId = client.getId();
		log.debug("Remove player: " + playerId + " from Channel: "+channelId);
		String fqn = getFqn(channelId);
		systemState.removeAttribute(fqn, String.valueOf(playerId));
        
        // Remove channel from player's list of active channels.
        client.getLocalData().removeChatChannel(channelId);
        
        removeChannelIfEmpty(channelId);
	}

    /**
     * Checks if a chat channel is empty, and if so, removes it.
     * 
     * @param channelId
     */
	private void removeChannelIfEmpty(int channelId) {
		checkFilter();
        String fqn = getFqn(channelId);
        int numberOfPlayers = systemState.getAttributes(fqn).size();
        if (numberOfPlayers == 0) {
            systemState.removeNode(fqn);
            if(filter != null) {
            	filter.channelDestroyed(channelId);
            }
        }
    }

    /**
     * Gets the FQN given a channeldId.
     * 
     * @param channelid
     * @return
     */
    private String getFqn(int channelid) {
		String fqn = SystemStateConstants.CHAT_CHANNEL_ROOT_FQN + channelid;
		return fqn;
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
			ServiceRegistry reg = context.getParentRegistry();
			filter = reg.getServiceInstance(ChatFilter.class);
		}
	}
}