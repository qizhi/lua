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
package com.cubeia.firebase.server.processor;

import static com.cubeia.firebase.util.Classes.verifyServerClassLoaderInContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.util.executor.JmxExecutor;

public class Notifier implements GameNotifier {
	
	private static transient Logger log = Logger.getLogger(Notifier.class);
	
	private final FirebaseTable table;

    private final Sender<ClientEvent<?>> clientRouter;
    
    private final static JmxExecutor executor;
	private final Queue<ClientEvent<?>> commitQueue;
    private final boolean useCommit;
    
    static {
    	// If you make this multi threaded then ordering of events must be addressed properly
        executor = new JmxExecutor(1, "Client-Notifier");
    }
	
	public Notifier(FirebaseTable table, Sender<ClientEvent<?>> clientRouter, boolean useCommit) {
		this.table = table;
		this.clientRouter = clientRouter;
		commitQueue = new LinkedList<ClientEvent<?>>();
		this.useCommit = useCommit;
	}
	
	public void commit() {
		if(useCommit) {
			if(log.isTraceEnabled()) {
				log.trace("Handing off commit; Queue length: " + commitQueue.size());
			}
			Runnable exec = new CommitSender();
			executor.submit(exec);
		}
	}
	
	public void broadcast(SystemMessageAction msg) {
		Arguments.notNull(msg, "message");
		MulticastClientEvent e = new MulticastClientEvent();
		e.setTableId(msg.getTableId());
		checkSetPlayers(msg, e);
		e.setAction(msg);
		sendEvent(e);
	}
	
	
    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.game.GameNotifier#sendToPlayer(int, com.cubeia.firebase.api.action.GameAction)
     */
    public void sendToClient(int playerid, GameAction action) {
    	//List<GameAction> actionList = new ArrayList<GameAction>();
        //actionList.add(action);
        sendToClient(playerid, Collections.singletonList(action));
    }
    
    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.game.GameNotifier#sendToPlayer(int, java.util.Collection)
     */
    public void sendToClient(int playerid, Collection<? extends GameAction> actions) {
    	for (GameAction action : actions) {
    		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
	        event.setPlayerId(playerid);
	        event.setTableId(table.getId());
	        event.setAction(action);
	        sendEvent(event);
    	}
    }
    
    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.game.GameNotifier#notifyPlayer(int, com.cubeia.firebase.api.action.GameAction)
     */
    public void notifyPlayer(int playerid, GameAction action) {
    	// If a watcher we will send to him 
    	if (table.getWatcherSet().isWatching(playerid)) {
    		sendToClient(playerid, action);
    		
    	} else {
	    	// Only notify if status = connected
	    	GenericPlayer player = table.getPlayerSet().getPlayer(playerid);
	    	if (player != null && player.getStatus().equals(PlayerStatus.CONNECTED)) {
	    		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
		        event.setPlayerId(playerid);
		        event.setTableId(table.getId());
		        event.setAction(action);
		        sendEvent(event);
	    	}
    	}
    }

   
    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.game.GameNotifier#notifyPlayer(int, java.util.Collection)
     */
    public void notifyPlayer(int playerid, Collection<? extends GameAction> actions) {
        for (GameAction action : actions) {
            notifyPlayer(playerid, action);
        }
    }
    
    

    /* (non-Javadoc)
     * 
     */
    public void notifyAllPlayers(GameAction action) {
    	notifyAllPlayers(action, true);
    }
    
    public void notifyAllPlayers(GameAction action, boolean watchers) {
        //List<GameAction> actionList = new ArrayList<GameAction>();
        //actionList.add(action);
        notifyAllPlayers(Collections.singletonList(action), watchers);
    }
    
    /* (non-Javadoc)
     * 
     */
    public void notifyAllPlayers(Collection<? extends GameAction> actions) {
       notifyAllPlayers(actions, true);
    }
    
    public void notifyAllPlayers(Collection<? extends GameAction> actions, boolean watchers) {
        for (GameAction action : actions) {
        	MulticastClientEvent event = addPlayers(-1, action);
        	
        	 if (watchers) {
             	addWatchers(watchers, event);
             }
            
            if (event.getPlayers().size() > 0) {
            	sendEvent(event);
            }
        }
    }
    
    /* (non-Javadoc)
     * 
     */
    public void notifyAllPlayersExceptOne(GameAction action, int skip) {
    	notifyAllPlayersExceptOne(action, skip, true);
    }
    
    public void notifyAllPlayersExceptOne(GameAction action, int skip, boolean watchers) {
        //List<GameAction> actionList = new ArrayList<GameAction>();
        //actionList.add(action);
        notifyAllPlayersExceptOne(Collections.singletonList(action), skip, watchers);
    }
    
    
    /* (non-Javadoc)
     * 
     */
    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> actions, int skip) {
    	notifyAllPlayersExceptOne(actions, skip, true);
    }
    
    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> actions, int skip, boolean watchers) {
        for (GameAction action : actions) {
        	MulticastClientEvent event = addPlayers(skip, action);
            
            if (watchers) {
            	addWatchers(watchers, event);
            }
            
            if (event.getPlayers().size() > 0) {
            	sendEvent(event);
            }
        }
    }

    /**
     * Creates a {@link MulticastClientEvent} and adds players from this table as recipients.
     * 
     * @param skip the playerId of the player to skip. Set to -1 to not skip any players.
     * @param action the action to create an event for.
     * @return a {@link MulticastClientEvent} with the given players as recipients.
     */
	private MulticastClientEvent addPlayers(int skip, GameAction action) {
		MulticastClientEvent event = createMultiPlayerGameEvent(action);
		for (GenericPlayer player : table.getPlayerSet().getPlayers()) {
		    if (player.getPlayerId() != skip) {
		    	// Only notify connected players
		    	if (player.getStatus().equals(PlayerStatus.CONNECTED)) {
		    		event.addPlayer(player.getPlayerId());
		    	}
		    }
		}
		return event;
	}

	/**
	 * Adds all watchers as recipient.
	 * 
	 * @param watchers
	 * @param event
	 */
	private void addWatchers(boolean watchers, MulticastClientEvent event) {
		// get watchers
		for (int playerid : table.getWatcherSet().getWatchers()) {
	        event.addPlayer(playerid);
	    }
	}

    private MulticastClientEvent createMultiPlayerGameEvent(GameAction action) {
    	MulticastClientEvent event = new MulticastClientEvent();
        event.setTableId(table.getId());
        event.setAction(action);
        return event;
    }
    
    /**
     * Sends an event to the client router.
     */
    private void sendEvent(ClientEvent<?> event) {
    	verifyServerClassLoaderInContext();
        if(!useCommit) {
        	if(log.isTraceEnabled()) {
        		log.trace("Handing off direct send for client event, with action: " + event.getAction());
        	}
        	Runnable send = new SingleSender(event);
            executor.submit(send);
        } else {
        	if(log.isTraceEnabled()) {
        		log.trace("Queueing event for commit later, with action: " + event.getAction());
        	}
        	commitQueue.add(event);
        }
    }
    
	private void checkSetPlayers(SystemMessageAction msg, MulticastClientEvent e) {
		int[] ids = msg.getPlayerIds();
		if(ids != null) {
			e.setIsGlobalEvent(false);
			for (int id : ids) {
				e.addPlayer(id);
			}
		} else {
			e.setIsGlobalEvent(true);
		}
	}
    
    /*
     * Send event while blocking the calling thread.
     * 
     * @param event
     */
    /*private void sendEventBlocking(ClientEvent event) {
        try {
			clientRouter.dispatch(event);
		} catch (ChannelNotFoundException e) {
			log.error("GameEvent to client with no channel discarded: "+event);
		}
    }*/
    
    
    
    // --- INNER CLASSES --- //
    
    /**
     * Hands off sending off a message through the router.
     */
    private class SingleSender implements Runnable {
       
    	private ClientEvent<?> event;
        
        public SingleSender(ClientEvent<?> event) {
            this.event = event;
        }

        public void run() {
            try {
            	if(log.isTraceEnabled()) {
            		log.trace("Direct dispatch of event, with action: " + event.getAction());
            	}
				clientRouter.dispatch(event);
			} catch (ChannelNotFoundException e) {
				log.error("Event for non-existing table was discarded: " + event);
			}
        }
    }
    
    /**
     * Hands off sending off a message through the router.
     */
    private class CommitSender implements Runnable {

        public void run() {
        	for (ClientEvent<?> e : commitQueue) {
	            try {
	            	if(log.isTraceEnabled()) {
	            		log.trace("Commit dispatch of event, with action: " + e.getAction());
	            	}
					clientRouter.dispatch(e);
				} catch (ChannelNotFoundException ex) {
					log.error("Event for non-existing table was discarded: " + e);
				} catch(Throwable th) {
					log.error("Failed to send client event: " + e, th);
				}
        	}
        }
    }
}
