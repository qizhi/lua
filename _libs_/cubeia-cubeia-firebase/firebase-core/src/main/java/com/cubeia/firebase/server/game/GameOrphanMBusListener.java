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
package com.cubeia.firebase.server.game;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.ReserveSeatResponseAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.TableQueryRequestAction;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.action.visitor.DefaultActionVisitor;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.routing.GameNodeRouter;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Sender;

/**
 * Listens to actions that are missing a target in the message bus (i.e. NoSuchQueueException).
 * Such action is considered orphaned since we are supposed to execute it, but there is no
 * table targeted for execution available.
 * 
 * In most cases (where applicable) we will visit the action and send a failure response to the client.
 * For some actions we will simply discard the action and not notify anyone.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class GameOrphanMBusListener extends DefaultActionVisitor implements OrphanEventListener<ChannelEvent> {

	private final static Logger log = Logger.getLogger(GameOrphanMBusListener.class);
	
	private Sender<ClientEvent<?>> clientEventSender;
	private ClassLoader deploymentClassLoader;

	protected GameOrphanMBusListener(GameNodeRouter nodeRouter, ClassLoader deploymentClassLoader) {
		this.deploymentClassLoader = deploymentClassLoader;
		clientEventSender = nodeRouter.getClientEventSender();
	}

	/**
	 * Extract the action and respond accordingly.
	 * 
	 */
	public void orphanedEvent(ChannelEvent event) {
		Event<?> routedEvent = event.getRoutedEvent();
		if (routedEvent instanceof GameEvent) {
			orphanedEvent((GameEvent)routedEvent);
		}
	}
	
	public void orphanedEvent(GameEvent event) {
		GameAction action = testUnwrap(event);
		if(action != null) {
			action.visit(this);
		} else {
			log.warn("Received event for unreachable table " + event.getTableId() + " (but failed to unwrap action)");
		}
	}
	
	@Override
	public void visit(JoinRequestAction action) {
		log.warn("Received join request for unreachable table: "+action);
		JoinResponseAction response = new JoinResponseAction(action.getPlayerId(), action.getTableId(), action.getSeatId(), Status.FAILED.ordinal());
		createEvent(action.getPlayerId(), action.getTableId(), response);
	}
	
	@Override
    public void visit(WatchAction action) {
		log.warn("Received watch request for unreachable table: "+action);
		WatchResponseAction response = new WatchResponseAction(action.getTableId(), WatchResponseStatus.FAILED);
		createEvent(action.getPlayerId(), action.getTableId(), response);
	}
	
	@Override
	public void visit(LeaveAction action) {
		log.warn("Received leave request for unreachable table: "+action);
		LeaveResponseAction response = new LeaveResponseAction(action.getPlayerId(), action.getTableId(), Status.FAILED.ordinal());
		createEvent(action.getPlayerId(), action.getTableId(), response);
	}
	
	@Override
	public void visit(ReserveSeatRequestAction action) {
		log.warn("Received seat reservation request for unreachable table: "+action);
		ReserveSeatResponseAction response = new ReserveSeatResponseAction(action.getPlayerId(), action.getTableId(), action.getSeatId(), Status.FAILED.ordinal());
		createEvent(action.getPlayerId(), action.getTableId(), response);
	}
	
	@Override
	public void visit(TableQueryRequestAction action) {
		log.warn("Received table query request for unreachable table: "+action);
		TableQueryResponseAction response = new TableQueryResponseAction(action.getPlayerId(), action.getTableId(), new LinkedList<SeatInfoAction>());
		response.setStatus(Status.DENIED); // Flag that the request was denied since no table exists
		createEvent(action.getPlayerId(), action.getTableId(), response);
	}
	
	@Override
	public void visit(GameDataAction action) {
		log.warn("Received game data action for unreachable table (will be discarded): "+action);
	}
	
	/** We will ignore this */
	@Override
	public void visit(RemovePlayerAction action) {}
	
	private void createEvent(int playerId, int tableId, GameAction action) {
		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
        event.setPlayerId(playerId);
        event.setTableId(tableId);
        event.setAction(action);
        sendEvent(event);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private GameAction testUnwrap(GameEvent gameEvent) {
		try {
			gameEvent.unwrapForTarget(deploymentClassLoader);
			return gameEvent.getAction();
		} catch(Throwable th) {
			log.debug("Failed to unwrap orphaned event, missing game classloader.", th);
			return null;
		}
	}
	
	private void sendEvent(ClientEvent<?> event) {
        try {
			clientEventSender.dispatch(event);
		} catch (ChannelNotFoundException e) {
			log.error("Failure response to client with no channel discarded: "+event);
		}
    }
}
