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
package com.cubeia.firebase.service.wlist.linear;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.service.wlist.WaitingList;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.service.wlist.model.NodeList;

/**
 * Implements a Waiting List that uses sub-linear search algorithms.
 * First, all waiting requests are partitioned by end branches in the tree.
 * Then all waiting requests are mapped by parameter key so that we can 
 * extract all requests that depends on the changed attribute.
 * 
 *
 * @author Fredrik
 */
public class LinearWaitingList implements WaitingList {

	private transient Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Holds the end nodes waiting lists.
	 */
	private final ConcurrentMap<String, NodeList> nodes = new ConcurrentHashMap<String, NodeList>();
	
	public void addJoinRequest(FilteredRequest request) {
		NodeList nodeList = getOrCreateNodeList(request.getFqn().getRootLobbyPath());
		nodeList.addRequest(request);
	}
	
	
	public boolean returnRequest(FilteredRequest request) {
	    log.debug("Returning Filtered Request: "+request);
		NodeList nodeList = getOrCreateNodeList(request.getFqn().getRootLobbyPath());
		return nodeList.returnRequest(request);
	}
	
	public void removeRequest(FilteredRequest request) {
		NodeList nodeList = nodes.get(request.getFqn().getRootLobbyPath());
		if (nodeList != null) {
			nodeList.removeRequest(request);
		}
	}
	
	/**
	 * Get all NodeLists for the given FQN and all FQN's branching
	 * downwards and find the longest waiting match.
	 * 
	 * @return The match or null if not found
	 */
	public FilteredRequest getMatch(LobbyPath path, Map<?, ?> data) {
		return getMatch(path, "", data);
	}
	
	/**
	 * Get all NodeLists for the given FQN and all FQN's branching
	 * downwards and find the longest waiting match.
	 * 
	 * @return The match or null if not found
	 */
	public FilteredRequest getMatch(LobbyPath path, String attribute, Map<?, ?> data) {
		// Check capacity
		if (checkTableIsFull(data)) {
			return null; // EARLY RETURN, table is full 
		}
		
		List<FilteredRequest> requests = new LinkedList<FilteredRequest>();
		Map<FilteredRequest, NodeList> mapping = new HashMap<FilteredRequest, NodeList>();
		
		// Get request matches from all nodes
		Fqn<?> fqn = Fqn.fromString(path.getRootLobbyPath());
		while (!fqn.isRoot()) {
			String key = LobbyPath.formatMiddleFqn(fqn.toString());
			NodeList node = nodes.get(key);
			if (node != null) {
				FilteredRequest match = getNodeMatch(node, attribute, data);
				if (match != null) {
					requests.add(match);
					mapping.put(match, node);
					// log.debug("Node: "+fqn+"\t Match: "+match);
				}
			} 
			fqn = fqn.getParent();
		}
		
		FilteredRequest match = selectRequest(requests, mapping);
			
		return match;
	}

	


	
	
	
	


	public void dumpToLog() {
		for (String fqn : nodes.keySet()) {
			log.debug(" --------- Lobby Waiting List for FQN: ["+fqn+"] --------- ");
			nodes.get(fqn).dumpToLog();
		}
	}
	
	
	/**
	 * Get a queue for a key or create on and insert it if not yet defined.
	 * 
	 * @param key
	 * @return
	 */
	private NodeList getOrCreateNodeList(String fqn) {
		NodeList nodeList = nodes.get(fqn);
		if (nodeList == null) {
			// Get synch lock, update ref and try again and create new if still null
			synchronized (nodes) {
				if ((nodeList = nodes.get(fqn)) == null) {
					nodeList = new NodeWaitingList();
					bindNodeListToJMX(fqn, (NodeWaitingList)nodeList);
					nodes.put(fqn, nodeList);
				}
			}
		}
		return nodeList;
	}
	
	
	/**
	 * Find the longest waiting match in a Node List.
	 * This method will take a synch lock in the Node since the
	 * node list will not be well suited for concurrent operations.
	 * 
	 * The request found will be removed from the list, so after we have
	 * acquired the match we can release the lock.
	 * 
	 * Of course, if we decide not to use this request we must return it
	 * to the queue where it will be placed back in it's proper position.
	 * 
	 * This makes it possible for non-fairness, i.e. while we get a match 
	 * and compare it to other node matches, another thread might get a newer
	 * match in the queue and use that one. This is a calculated risk since
	 * we do not wish to hold a synch lock over the whole execution.
	 * 
	 * @param node
	 * @param attribute
	 * @param data
	 * @return
	 */
	private FilteredRequest getNodeMatch(NodeList node, String attribute, Map<?, ?> data) {
		FilteredRequest match = null;
		synchronized (node) {
			match = node.getMatch(attribute, data);
		}
		return match;
	}
	
	
	/**
	 * Select the longest waiting request
	 * 
	 * @param requests
	 * @param mapping
	 * @return
	 */
	private FilteredRequest selectRequest(List<FilteredRequest> requests, Map<FilteredRequest, NodeList> mapping) {
		Collections.sort(requests);
		FilteredRequest match = null;
		
		// We need to return the requests not prioritized.
		int i = 0;
		for (FilteredRequest req : requests) {
			if (i == 0) {
				match = req; // Use this one
				i++;
			} else {
				// Return these to the waiting list
				mapping.get(req).returnRequest(req);
			}
			
		}
		return match;
	}
	/**
     * Register the node to the JMX server
     *
     */
    private void bindNodeListToJMX(String fqn, NodeWaitingListMBean bean) {
    	try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.waitlist:type=NodeList-"+fqn);
            mbs.registerMBean(bean, monitorName);
            
    	} catch (Exception ex) {
    		log.warn("Could not bind Node Waiting List to JMX: "+ex);
    	}
    }

    /**
     * Check if we have any free seats
     * 
     * @param data
     * @return
     */
    private boolean checkTableIsFull(Map<?, ?> data) {
		boolean full = false;
		if (data.containsKey(DefaultTableAttributes._CAPACITY.toString())) {
			if (data.containsKey(DefaultTableAttributes._SEATED.toString())) {
				try {
					Integer capacity = (Integer)data.get(DefaultTableAttributes._CAPACITY.toString());
					Integer seated = (Integer)data.get(DefaultTableAttributes._SEATED.toString());
					full = seated >= capacity;
					
				} catch (NumberFormatException e) {
					log.warn("Could not parse table capacity and seated from data set: "+data);
				}
			}
		}
		return full;
	}
	
}
