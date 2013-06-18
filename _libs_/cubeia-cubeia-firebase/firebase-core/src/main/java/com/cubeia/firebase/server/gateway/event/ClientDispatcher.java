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
package com.cubeia.firebase.server.gateway.event;

import static com.cubeia.firebase.server.event.processing.ReceiverEventDaemonBase.MDC_PLAYER_ID;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.messagebus.RouterEvent;

public class ClientDispatcher {

	private final Logger log = Logger.getLogger(getClass());
	
	private final ClientRegistry clientRegistry;
	
	ClientDispatcher(ClientRegistry clientRegistry) {
		this.clientRegistry = clientRegistry;
	}

    public void dispatch(RouterEvent ev) {
    	try {
	    	Event<?> event = ev.getRoutedEvent();
	    	event.unwrapForTarget(null);
	    	/*
	    	 * Since the multiple player events extends the 
	    	 * single player events we need to inspect
	    	 * the instance in correct order. I.e. multiple 
	    	 * player events first.
	    	 */
	    	/*if (event instanceof MulticastGameEvent) {
	    		dispatch((MulticastGameEvent) event);
	    		
			} else*/ if (event instanceof MulticastClientEvent) {
	    		dispatch((MulticastClientEvent) event);
	    		
			} else if (event instanceof ClientEvent<?>) {
				dispatch((ClientEvent<?>)event);
				
			} else if (event instanceof GameEvent) {
				dispatch((GameEvent)event);
			} else if (event instanceof MttEvent) {
			    dispatch((MttEvent)event);
			}
	    	
    	} catch (Throwable e) {
    		log.error("Exception when dispatching client event: "+e, e);
    		throw new RuntimeException(e);
    	} finally {
    		ev.acknowledge();
    	}
    }
    
    private void dispatch(MttEvent event) {
        executeEvent(event.getPlayerId(), event.getAction());        
    }

    /**
     * Handle Client Events.
     * 
     * @param event
     */
    private void dispatch(ClientEvent<?> event) {
    	Action action = event.getAction();
        if(action instanceof GameAction) {
        	executeEvent(event.getPlayerId(), (GameAction)action);
        } else {
        	executeEvent(event.getPlayerId(), (MttAction)action);
        }
    }
    
    private void dispatch(GameEvent event) {
        executeEvent(event.getPlayerId(), event.getAction());
    }
        
    /*@SuppressWarnings("unchecked")
	private void dispatch(MulticastGameEvent event) {
        if(!event.isGlobalEvent()) {
        	dispatchToList(event.getPlayers(), event.getAction());
        } else {
        	GameAction action = event.getAction();
        	Client[] clients = clientRegistry.getLocalClients();
        	for (Client c : clients) {
        		executeEvent(c.getClientData().getId(), action);
        	}
        }
    }*/

	private void dispatchToList(List<Integer> ids, GameAction act) {
		for (int playerid : ids) {
            // TODO: Add check for client [handle(playerid)]
            executeEvent(playerid, act);
        }
	}
    
    private void dispatch(MulticastClientEvent event) {
        if(!event.isGlobalEvent()) {
        	dispatchToList(event.getPlayers(), event.getAction());
        } else {
        	GameAction action = event.getAction();
        	Client[] clients = clientRegistry.getLocalClients();
        	for (Client c : clients) {
        		executeEvent(c.getClientData().getId(), action);
        	}
        }
    }

    /**
     * Fetch the client from the client registry and execute the GameAction.
     * 
     * @param playerid
     * @param action
     */
    private void executeEvent(int playerid, GameAction action) {
        try {            
            // Get Client
            Client client = clientRegistry.getClient(playerid);
            if (client != null) {
            	// Check status before executing
            	ClientState state = client.getState();
            	if (state.equals(ClientState.CONNECTED) || state.equals(ClientState.LOGGED_IN)) {
            		// Handle action
            		client.getActionHandler().handleAction(action);
            	} else {
            		log.debug("Action received for disconnected client: pid: "+playerid+" action: "+action);
            	}
            }
            
        } catch (Exception ex) {
            log.error("Could not dispatch action: "+ex,ex);
        }
    }

    private void executeEvent(int playerId, MttAction action) {
    	MDC.put(MDC_PLAYER_ID, playerId);
        try {            
            // Get Client
            Client client = clientRegistry.getClient(playerId);
            if (client != null) {
                // Check status before executing
                ClientState state = client.getState();
                if (state.equals(ClientState.CONNECTED) || state.equals(ClientState.LOGGED_IN)) {
                    // Handle action
                    client.getActionHandler().handleAction(action);
                } else {
                    log.debug("Action received for disconnected client: pid: " + playerId + " action: "+action);
                }
            }            
        } catch (Exception ex) {
            log.error("Could not dispatch action: " + ex, ex);
        } finally {
        	MDC.remove(MDC_PLAYER_ID);
        }
    }    
}
