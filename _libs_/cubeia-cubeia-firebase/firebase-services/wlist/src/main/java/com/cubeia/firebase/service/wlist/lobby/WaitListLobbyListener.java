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
package com.cubeia.firebase.service.wlist.lobby;

import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.lobby.systemstate.LobbyListener;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.wlist.FilteredJoinService;
import com.cubeia.firebase.service.wlist.WaitingList;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

/**
 * Listens to changes in the Lobby
 *
 * @author Fredrik
 */
public class WaitListLobbyListener implements LobbyListener {

	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** Handles matching for change notifications */
	private JmxExecutor executor = new JmxExecutor(1, "WaitingListMatcher");
	
	/** This is the waiting list that will be notified regarding changed in the lobby */
	private final WaitingList list;

	private final FilteredJoinService service;

    private final ClientRegistry clientRegistry;

	public WaitListLobbyListener(FilteredJoinService service, WaitingList wList, ClientRegistry clientRegistry) {
		this.service = service;
		list = wList;
        this.clientRegistry = clientRegistry;
	}
	
	/**
	 * A table's attributes has changed.
	 * Hand this off to the executor for match finding.
	 */
	public void nodeAttributeChanged(NodeChangeDTO dto) {
		Handler task = new Handler(dto);
		executor.submit(task);
	}

	/**
	 * Do the actual matching, can be very resource intensive
	 * 
	 * @param dto
	 */
	private void executeMatching(NodeChangeDTO dto) {
		FilteredRequest request = null;
		if (dto.getChanged().size() == 1) {
			// log.debug("Table attribute changed: "+dto.getPath()+" changed: "+dto.getChanged());
			String attribute = String.valueOf(dto.getChanged().keySet().iterator().next());
			request = list.getMatch(dto.getPath(), attribute, dto.getAllData());
			
		} else if (dto.getChanged().size() > 1) {
			// log.debug("Table changed: "+dto.getPath()+" changed: "+dto.getChanged());
			request = list.getMatch(dto.getPath(), dto.getAllData());
			
		} else {
			log.warn("Why are you notifying me of table change when the data set is empty? Fqn: "+dto.getPath());
		}
		
		int tableId = dto.getPath().getObjectId();
		
		if (request == null) {
			return; // EARLY RETURN, no match
		}
		
        if (!playerSeated(request.getPlayerId(), tableId)) {
			log.debug("Changes to FQN: "+dto.getPath()+" found a request: "+request+" for table: "+tableId);
			service.sendJoin(request, tableId, -1);
		} else {
			// We have found a match for a table at which the client is already seated
			// Rerun the matching in the same thread and then return this request.
			// This is not the most efficient way of handling it, but the amount of
			// matches to already seated tables should not be so high.
			log.debug("Re-execute matching of data: "+dto+" (skipping request: "+request);
			executeMatching(dto);
			service.returnFilteredJoinRequest(request.getId());
		}
	}
	
    private boolean playerSeated(int playerId, int tableId) {
        Map<Integer, Integer> seatedTables = clientRegistry.getSeatedTables(playerId);
        return (seatedTables.containsKey(tableId));
    }	

	public void nodeCreated(LobbyPath path) { }

	public void tableRemoved(LobbyPath path) { }
	
	public void nodeRemoved(String path) { }
	
	/**
	 * Task for executing the filter matching asynchronously
	 *
	 * @author Fredrik
	 */
	private class Handler extends SafeRunnable {
		private final NodeChangeDTO dto;
		public Handler(NodeChangeDTO dto) {
			this.dto = dto;
		}
		
		public void innerRun() {
			executeMatching(dto);
		}
	}
	
}
