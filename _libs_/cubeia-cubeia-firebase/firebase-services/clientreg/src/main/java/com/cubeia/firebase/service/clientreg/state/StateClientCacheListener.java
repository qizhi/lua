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
package com.cubeia.firebase.service.clientreg.state;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent.ModificationType;

import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.service.clientreg.ClientStatusFields;

/**
 * This class handles changes on the distributed player registry.
 * 
 * Primarily it checks for changes to the session id, if the session
 * id is changed for a local client then we might have a new login
 * which should result in a forced logout for the local client.
 *
 * @author Fredrik
 */
@CacheListener
public class StateClientCacheListener {

	private static final int CLIENT_ID_GENERATION = 2;
	private static final int CLIENT_TABLE_GENERATION = 3;
	private static transient Logger log = Logger.getLogger(StateClientCacheListener.class);
	private final StateClientRegistry registry;
	
	public StateClientCacheListener(StateClientRegistry registry) {
		this.registry = registry;
	}
	
	/**
	 * Check if the supplied client id and session matches what we have in the local
	 * registry. If they do not match then forcibly logout the local client. The
	 * distributed client registry is the law here.
	 * 
	 * @param pid
	 * @param session
	 */
	private void checkSessionId(int pid, String session) {
		Client client = registry.getClient(pid);
		if (client != null && !client.getSessionId().equals(session)) {
			log.debug("Session id changed, will disconnect client : "+pid);
			disconnectClient(pid);
		}
	}
	
	/**
	 * Kick out the client. 
	 * 
	 * @param pid
	 */
	private void disconnectClient(int pid) {
		registry.clientLoggedInRemotely(pid);
	}
	
	
	//-----------------------------
	//
	//	CacheListener Methods
	//
	//-----------------------------
	
	
	/**
	 * Inspect changes in session id's. These should be verified against local clients.
	 * The data reported is the delta change data and not all data contained in the node.
	 */
	@NodeModified
	@SuppressWarnings("rawtypes")
	public void modified(NodeModifiedEvent event) {
		try {
			Fqn fqn = event.getFqn();
			Map data = event.getData();
			
			// Check for committed node within the client region
			if (!event.isPre() && fqn.toString().startsWith(SystemStateConstants.CLIENT_ROOT_FQN)) {
				// We use FQN size below to verify that it is a client node.
				// We DO NOT want to inspect the node data since that has sync issues, so there....
				// I.E. DO NOT make system state data lookups with this thread. (JBoss Cache issue)
				if (fqn.size() == CLIENT_ID_GENERATION && event.getModificationType() != ModificationType.REMOVE_DATA) {
					// If the layout of the registry in the cache is changed, we need to change below
					int pid = Integer.parseInt(fqn.getLastElementAsString());
					
					for(Object key : data.keySet()) {
						if (String.valueOf(key).equals(ClientStatusFields.SESSION_ID.toString())) {
							checkSessionId(pid, String.valueOf(data.get(key)));
						}
					}
				} else {
					// Check if we have a table participant or watcher association for the managed nodes
					if (fqn.getAncestor(CLIENT_TABLE_GENERATION).toString().endsWith(SystemStateConstants.CLIENT_TABLES)  ) {
						// check so we have some data
						if (data.size() > 0) {
							// Is the status of the client DISCONNECTED?
							
							// FIXME: JBossCache deadlock when looking up other node
							// This will cause disconnected clients to not be updated on the table to status = WAIT_REJOIN
							
//							int pid = Integer.parseInt(fqn.getAncestor(CLIENT_ID_GENERATION).getLastElementAsString());
//							ClientState clientStatus = registry.getClientStatus(pid);
//							if (clientStatus.equals(ClientState.DISCONNECTED)) {
//								// Is this player within the managed nodes?
//								if (registry.isLocal(pid)) {
//									log.debug("A disconnected client("+pid+") was seated at table("+data.keySet().iterator().next()+"). I will send status update to table.");
//									// Client has disconnected from us. We need to signal this to the table
//									String key = String.valueOf(data.keySet().iterator().next());
//									registry.reportTableStatusChanged(Integer.parseInt(key), pid, PlayerStatus.WAITING_REJOIN);
//								}
//							}
						}
					}
					
				}
			}
		} catch (Exception e) {
			// Do not propagate errors to the cache handler
			log.error("Could not update client session from cache. Event: "+event, e);
		}
	}
	
	/**
	 * Currently we do nothing. This scenario should not occur, however the node removed
	 * will be called upon for every client logging out so it is not optimal to keep cleanup
	 * here either.
	 * 
	 * If clients are not cleaned up in the registry, we can get the pid here and safely remove all
	 * clients with the corresponding pid.
	 */
	@SuppressWarnings("rawtypes")
	public void nodeRemoved(Fqn fqn, boolean pre, boolean isLocal, Map<Object, Object> data) {}

	
	//-----------------------------
	//
	//	END CacheListener Methods
	//
	//-----------------------------
}
