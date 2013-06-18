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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.AbstractPlayerAction;
import com.cubeia.firebase.api.action.CreateTableResponseAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.MttPickedUpAction;
import com.cubeia.firebase.api.action.MttSeatedAction;
import com.cubeia.firebase.api.action.NotifyInvitedAction;
import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.ReserveSeatResponseAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.action.UnWatchAction;
import com.cubeia.firebase.api.action.UnWatchResponseAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinTableAvailableAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.visitor.DefaultActionVisitor;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.transform.ActionTransformer;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.wlist.FilteredJoinServiceContract;
import com.cubeia.firebase.util.BinaryData;



/**
 * Handler for incoming Actions.
 * 
 * The handler also implements action visitor. 
 * This is to provide a filter chain for server operations
 * needed on outgoing actions.
 * 
 * E.g. for successful table joins we need to add a client -> table
 * mapping, so we add an interceptor here.
 * 
 * 
 * Created on 2006-sep-11
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class ClientGameActionHandler extends DefaultActionVisitor {
    
    private final static transient Logger log = Logger.getLogger(ClientGameActionHandler.class);
    
    /** The client for this handler */
    protected Client client;
    
	private final ClientRegistry registry;

	private final FilteredJoinServiceContract filteredJoinService;

	private Lobby lobby;
    
    public ClientGameActionHandler(Client client, ServiceRegistry serviceRegistry) {
        this.client = client;
		this.registry = serviceRegistry.getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
		this.filteredJoinService = serviceRegistry.getServiceInstance(FilteredJoinServiceContract.class);
		this.lobby = serviceRegistry.getServiceInstance(LobbyServiceContract.class).getLobby();
    }        

    /**
     * First check for intercepting logic, then translate to a packet
     * and send to the client.
     * 
     * @param action
     */
	public void handleAction(GameAction action) {
		// Check if this action is from a known table.
		if (checkValidAction(action)) {
			// Intercept actions first
			action.visit(this);
			
			ActionTransformer transformer = new ActionTransformer(registry);
	        ProtocolObject packet = transformer.transformActionToPacket(action);
	        
	        if (packet != null) { 
	        	client.sendClientPacket(packet);
	        } else {
	            // log.warn("Why are you trying to send null-packets? Action: "+action);
	        }
			
		} else {
			log.warn("Action received for unknown table. Pid: "+client.getId()+" Action: "+action);
			handleUnknownTable(action.getTableId());
		}
    }
	
    public void handleAction(MttAction action) {
        ActionTransformer transformer = new ActionTransformer(registry);
        ProtocolObject packet = transformer.transformActionToPacket(action);
        
        if (packet != null) { 
            client.sendClientPacket(packet);
        } else {
            log.warn("Why are you trying to send null-packets? Action: "+action);
        }        
    }	
	
	/**
	 * See if player has joined a table.
	 * If joined, add table 
	 */
	@Override
	public void visit(JoinResponseAction action) {
		if (action.getStatus() == Status.OK.ordinal()) {
			// CGWMonitor.localSeatedPlayers.incrementAndGet();
			joinedTable(action.getTableId(), action.getSeatId());
		}
	}
	
	@Override
	public void visit(LeaveResponseAction action) {
		if (action.getStatus() == Status.OK.ordinal()) {
			// CGWMonitor.localSeatedPlayers.decrementAndGet();
			leftTable(action.getTableId());
		}
	}
	
	@Override
	public void visit(WatchResponseAction action) {
		if (action.getStatus() == WatchResponseStatus.OK) {
			registry.addWatchingTable(client.getId(), action.getTableId());
			joinedTable(action.getTableId(), -1);
		}
	}

	
	
	@Override
	public void visit(UnWatchAction action) {
		if (action.getStatus() == Status.OK.ordinal()) {
			leftTable(action.getTableId());
		}
	}
	
	@Override
	public void visit(MttSeatedAction action) {
		// CGWMonitor.localSeatedPlayers.incrementAndGet();
		joinedTable(action.getTableId(), action.getSeatId());
	}
	
	@Override
	public void visit(MttPickedUpAction action) {
		// CGWMonitor.localSeatedPlayers.decrementAndGet();
		if (!action.keepWatching()) {
			leftTable(action.getTableId());
		}
	}
	
	/**
	 * The client is receiving a response on a request for
	 * seat reservation. 
	 * 
	 * We will check if the request originated from a waiting list
	 * and if applicable, adjust the list and send a notification 
	 * to the client.
	 * 
	 */
	@Override
	public void visit(ReserveSeatResponseAction action) {
		if (action.getStatus() == Status.OK.ordinal()) {
			joinedTable(action.getTableId(), action.getSeatId());
			FilteredJoinTableAvailableAction notification = new FilteredJoinTableAvailableAction(client.getId(), action.getTableId());
			notification.setRequestId(action.getWaitingListId());
			notification.setSequenceId(action.getWaitingListSequence());
			notification.setSeat(action.getSeatId());
			
			if (action.isWaitingList()) {
				filteredJoinService.consumeFilteredJoinRequest(action.getWaitingListId());
			}
			
			// Let the local action handler handle the notification
			client.getLocalActionHandler().handleAction(notification);
			
		} else {
			if (action.isWaitingList()) {
				log.debug("The service seat failed. We should return the filter request. Action: "+action);
				filteredJoinService.returnFilteredJoinRequest(action.getWaitingListId());
			}
		}
		
	}
	
//	@Override
//	public void visit(LeaveAction action) {
//        log.debug("action: " + action.toString());
//        registry.removeClientTable(client.getId(), action.getTableId());
//        CGWMonitor.localSeatedPlayers.decrementAndGet();
//        leftTable(action.getTableId());
//	}
	
	/**
	 * Check if this action is 'ok' to process.
	 * The check will:
	 * 
	 *  0. See if it is a table action (AbstractPlayerAction)
	 *  1. See if tableid > 0
	 *  2. See if the table is a known table (to the local client session)
	 *  3. See if the action is of a type that should be let through regardless
	 *  
	 * @param action
	 * @return
	 */
	private boolean checkValidAction(GameAction action) {
		boolean valid = true;
		if (action instanceof AbstractPlayerAction) {
			boolean unknown = action.getTableId() > 0 && !client.getLocalData().isTableKnown(action.getTableId());
			
			valid = !unknown || action instanceof ProbeAction;
			valid |= action instanceof JoinResponseAction;
			valid |= action instanceof WatchResponseAction;
			valid |= action instanceof MttSeatedAction;
			valid |= action instanceof ReserveSeatResponseAction;
			valid |= action instanceof CreateTableResponseAction;
			valid |= action instanceof NotifyInvitedAction;
			valid |= action instanceof UnWatchResponseAction;
			valid |= action instanceof TableQueryResponseAction;
			valid |= action instanceof LeaveResponseAction;
		}
		return valid;
	}
	
	/**
	 * If we have not handled this unknown table yet, then we
	 * will set player status = WAITING_REJOIN and notify the client.
	 * 
	 * @param tableId
	 */
	private void handleUnknownTable(int tableId) {
		if (!client.getLocalData().isTableNotified(tableId)) {
			client.getLocalData().addNotifiedTable(tableId, -1);
			registry.reportTableStatusChanged(tableId, client.getId(), PlayerStatus.WAITING_REJOIN);
			NotifySeatedPacket seated = new NotifySeatedPacket();
			seated.tableid = tableId;
			seated.seat = BinaryData.intToByte(-1);
			seated.mttid = registry.getTableMttId(client.getId(), tableId);
			seated.snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, tableId);
			if (seated.snapshot != null) {
				client.sendClientPacket(seated);
			} else {
				log.warn("Cold not send notify seated packet in handleUnknownTable since no snapshot could be found. pid["+client.getId()+"] tid["+tableId+"]");
			}
		} else {
			log.warn("Action received for unknown AND notified table. pid["+client.getId()+"] tid["+tableId+"]");
		}
	}
	
	/**
	 * Client joined a table (watcher or player) so we need to update the
	 * local cache.
	 * 
	 * @param tableid
	 * @param seat
	 */
	private void joinedTable(int tableid, int seat) {
		client.getLocalData().addKnownTable(tableid,seat);
		client.getLocalData().removeNotifiedTable(tableid);
	}
	
	/**
	 * Client left a table (watcher or player) so we need to update the
	 * local cache.
	 * 
	 * @param tableid
	 * @param seat
	 */
	private void leftTable(int tableid) {
		client.getLocalData().removeKnownTable(tableid);
		client.getLocalData().removeNotifiedTable(tableid);
	}
}

