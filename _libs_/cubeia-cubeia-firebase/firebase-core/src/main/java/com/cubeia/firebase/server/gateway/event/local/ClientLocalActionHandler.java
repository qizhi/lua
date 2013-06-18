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
package com.cubeia.firebase.server.gateway.event.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.FilteredJoinResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinStatus;
import com.cubeia.firebase.api.action.local.LocalAction;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyRegisteredPacket;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.transform.LocalActionTransformer;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.util.BinaryData;

/**
 * This class handles Local Actions.
 * Local Actions are actions that will only execute locally in the node, 
 * i.e. they will never propagate to another node.
 *
 * @author Fredrik
 */
public class ClientLocalActionHandler implements LocalActionHandler {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** Client logger */
	private transient Logger clientLog = Logger.getLogger("CLIENTS");
	
    /** The client for this handler */
    private Client client;

	private final ClientRegistry clientRegistry;

	private final TournamentPlayerRegistry tournamentRegistry;

	private Lobby lobby;
	
	public ClientLocalActionHandler(Client client, ServiceRegistry serviceRegistry) {
		super();
		this.client = client;
		this.tournamentRegistry = serviceRegistry.getServiceInstance(TournamentPlayerRegistry.class);
		this.clientRegistry = serviceRegistry.getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
		this.lobby = serviceRegistry.getServiceInstance(LobbyServiceContract.class).getLobby();
	}
    
	/**
	 * Will convert the action to the designated client protocol
	 * and send it to the client.
	 * 
	 * @param action
	 */
	public void handleAction(LocalAction action) {
		try {
			LocalActionTransformer transformer = new LocalActionTransformer(client);
			ProtocolObject packet = transformer.transformActionToPacket(action);
	        
	        if (packet != null) {
	        	client.sendClientPacket(packet);
	        	// Call interceptors
	        	interceptAction(action);
	        	interceptPacket(packet); 
	           
	        } else {
	            log.warn("Why are you trying to send null-packets? Local Action: "+action);
	        }
		} catch (Exception e) {
			// Defensive catch here since we don't want the error to 
			// propagate upwards any further.
			log.error("Could not handle action: "+action, e);
		}
	}

	private void interceptAction(LocalAction action) {
		if (action instanceof FilteredJoinResponseAction) {
			FilteredJoinResponseAction filteredJoin = (FilteredJoinResponseAction) action;
			interceptFilteredJoinResponse(filteredJoin);
		}
	}

	/**
	 * Inspect and intercept packets as needed.
	 * 
	 * @param com
	 * @param packet
	 */
	private void interceptPacket(ProtocolObject packet) {
		// Intercept login responses
		if (packet instanceof LoginResponsePacket) {
			LoginResponsePacket p = (LoginResponsePacket) packet;
			List<ProtocolObject> packets = interceptLoginResponse(p);
			if ( packets != null ) {
				client.sendClientPackets(packets);
			}
		}
	}

	/**
	 * If we have an existing client session we get the associated tables and
	 * return them as a list of packets which should be sent to the client.
	 * 
	 * @param packet
	 * @return
	 */
	private List<ProtocolObject> interceptLoginResponse(LoginResponsePacket packet) {
		List<ProtocolObject> response = new ArrayList<ProtocolObject>(5);
		if (packet.status == Enums.ResponseStatus.OK) {
			if ( client.getState() == ClientState.DISCONNECTED ){
				clientLog.info("Client pid["+packet.pid+"] disconnected before login response was intercepted");
				return null;
			}
				
			int pid = packet.pid;
			client.getClientData().setId(pid);
			client.getClientData().setScreenname(packet.screenname);
			client.setState(ClientState.LOGGED_IN);
			
			/*
			 * Check tournament registrations
			 */
			int[] tours = tournamentRegistry.getTournamentsForPlayer(pid);
			if(tours.length > 0) {
				NotifyRegisteredPacket pack = new NotifyRegisteredPacket();
				pack.tournaments = tours;
				response.add(pack);
			}
			
			/*
			 * Check client registry
			 */
			if (!clientRegistry.exists(pid)) {
				
				// New client
				clientRegistry.addClient(pid, client);
				clientLog.info("Client ["+client+"] logged in");
			} else {
				clientLog.info("Player logging in with pid["+pid+"] was found. Will logout old session.");
				forciblyLogoutClient(pid);

				// We still have to add the new client
				clientRegistry.addClient(pid, client);
				clientLog.info("Client ["+client+"] logged in and removed old session");
				
				// We have a rejoined client
				List<Integer> watchingTables = clientRegistry.getWatchingTables(pid);
				Map<Integer, Integer> seatedTables = clientRegistry.getSeatedTables(pid);
				Map<Integer, Integer> tourTables = clientRegistry.getSeatedTournamentTables(pid);
				
				for (Integer tid : seatedTables.keySet()) {
                    /*
                     * Set the status to WAITING_REJOIN so that the new client won't
                     * get messages until he actually opens the table
                     */ 
                    clientRegistry.reportTableStatusChanged(tid, pid, PlayerStatus.WAITING_REJOIN);
                    
					NotifySeatedPacket seated = new NotifySeatedPacket();
					seated.tableid = tid;
					seated.seat = BinaryData.intToByte(seatedTables.get(tid));
					seated.mttid = checkGetMttId(tourTables, tid);
					seated.snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, tid);
					
					// If no snapshot could be found, the packet cannot be sent.
					if (seated.snapshot != null) {
						response.add(seated);
					} else {
						log.warn("Could not find snapshot for table " + tid + " where player " + pid + " was seated.");
					}
				}
				
				for (Integer tid : watchingTables) {
					NotifyWatchingPacket watching = new NotifyWatchingPacket();
					watching.tableid = tid;
					response.add(watching);
				}
                
                // Now remove the player as from waiting lists, chat channels and as watcher
                clientRegistry.removeWatcherFromAllTables(pid, false);
                clientRegistry.removeClientFromWaitingLists(pid);
                clientRegistry.removeClientFromChatChannels(pid);
			}
		}
		return response;
	}

    private int checkGetMttId(Map<Integer, Integer> tourTables, int tid) {
		if(tourTables.containsKey(tid)) {
			return tourTables.get(tid);
		} else {
			return -1;
		}
	}

	private void forciblyLogoutClient(int pid) {
		try {
			// Logs out local clients
			Client client = clientRegistry.getClient(pid);
			if (client != null) {
				ForcedLogoutPacket packet = new ForcedLogoutPacket();
				// TODO: change to non hardcoded values  
				packet.code = 1; 
				packet.message = "You have logged in from another place";
				// send packet
				client.sendClientPacket(packet);
				client.close();
				lobby.unsubscribeAll(client);
			}
		} catch (Exception e) {
			// Defensive catch so that we don't stop the ongoing login
			log.warn("Problem when forcibly logging out a client: "+e, e);
		}
	}
	
	/**
	 * We intercept here so we can store the filtered join requests for clean up and
	 * other house keeping tasks.
	 * 
	 * @param filteredJoin
	 */
	private void interceptFilteredJoinResponse(FilteredJoinResponseAction filteredJoin) {
		if (filteredJoin.getStatus() == FilteredJoinStatus.WAIT_LIST.ordinal()) {
			client.getLocalData().addJoinRequest(filteredJoin.getSeq(), filteredJoin.getRequestId());
		}
	}
    
	public String toString() {
		return "loc_"+client.getId();
	}   
}