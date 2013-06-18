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
package com.cubeia.firebase.mtt;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;

public class MttNotifierImpl implements MttNotifier {

    private final Sender<ClientEvent<?>> clientEventSender;
    private final Sender<GameEvent> gameEventSender;
	private final boolean useCommit;
	
	private final Queue<ClientEvent<?>> clientCommitQueue;
	private final Queue<GameEvent> gameCommitQueue;

	private final Logger log = Logger.getLogger(getClass());
	
    public MttNotifierImpl(Sender<GameEvent> gameEventSender, Sender<ClientEvent<?>> clientEventSender, boolean useCommit) {
        this.gameEventSender = gameEventSender;
        this.clientEventSender = clientEventSender;
		this.useCommit = useCommit;
		clientCommitQueue = new LinkedList<ClientEvent<?>>();
		gameCommitQueue = new LinkedList<GameEvent>(); 
    }
    
    public void commit() {
    	if(useCommit) {
    		commitClientQueue();
    		commitGameQueue();
    	}
    }

	public void notifyTable(int tableId, GameAction action) {
        GameEvent e = new GameEvent();
        e.setTableId(tableId);
        e.setPlayerId(-1);
        e.setAction(action);
        if(useCommit) {
        	gameCommitQueue.add(e);
        } else {
	        try {
	            gameEventSender.dispatch(e);
	        } catch (ChannelNotFoundException ex) {
	            // TODO: the caller must remove tables if this exception is thrown!
	            throw new RuntimeException("error notifying table: tId = " + tableId + ", action = " + action.toString(), ex);
	        }
        }
    }
    
    public void notifyPlayer(int playerId, MttAction action) {
        ClientEvent<MttAction> e = new ClientEvent<MttAction>();
        e.setPlayerId(playerId);
        e.setAction(action);
        if(useCommit) {
        	clientCommitQueue.add(e);
        } else {
	        try {
	            clientEventSender.dispatch(e);
	        } catch (ChannelNotFoundException ex) {
	            throw new RuntimeException("error notifying table: pId = " + playerId + ", action = " + action.toString(), ex);
	        }
        }
    }

    
    // --- PRIVATE METHODS --- //
    
    private void commitGameQueue() {
		for (GameEvent e : gameCommitQueue) {
			try {
				gameEventSender.dispatch(e);
			} catch (ChannelNotFoundException ex) {
				log.error("Failed to dispatch (commit) game event", ex);
			}
		}
	}

	private void commitClientQueue() {
		for (ClientEvent<?> e : clientCommitQueue) {
			try {
				clientEventSender.dispatch(e);
			} catch (ChannelNotFoundException ex) {
				log.error("Failed to dispatch (commit) client event", ex);
			}
		}
	}
}
