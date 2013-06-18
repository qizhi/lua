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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.service.wlist.model.NodeList;
import com.cubeia.firebase.service.wlist.model.RequestQueue;
import com.cubeia.firebase.service.wlist.queue.SortedRequestSet;

/**
 * Models a queue for waiting on tables on a tree branch in the Lobby
 *
 *
 * @author Fredrik
 */
public class NodeWaitingList implements NodeList, NodeWaitingListMBean {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	public static AtomicLong matchCounter = new AtomicLong(0);
	
	/** 
	 * <p>
	 * The queue of waiting players.
	 * This queue maps parameters to all depending keys.
	 * </p>
	 * 
	 * <p>
	 *  I.e. if a parameter constraint is:</br>
	 *  <code>x = 10 & y > 3</code>
	 *  </p>
	 *  </br>
	 *  
	 *  <p>Then parameter will be added under both keys 'x' and 'y'.
	 *  This is so we can check out a subset of all requests when we get an
	 *  attribute changed notification from the lobby.</p>
	 *  
	 *  <p>The implementing Queue is not thread safe and is fail-fast during iterations.
	 *  If we need to change this, change the PriorityQueue to a ConcurrentPriorityQueue.
	 *  </p>
	 *  
	 *  <p>ConcurrentPriorityQueue implementation is roughly 25% slower then PriorityQueue
	 *  when running NodeWaitingListPerfTest. </p>
	 *  
	 */
	private ConcurrentMap<String, RequestQueue> queues = 
		new ConcurrentHashMap<String, RequestQueue>();
	
	public NodeWaitingList() { }

	/**
	 * Provide the Request Queue implementation here.
	 * @return
	 */
	private RequestQueue createRequestQueue() {
		return new SortedRequestSet();
		//return new SimpleRequestList();
		//return new SimpleRequestQueue();
	}
	
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.wlist.NodeList#addRequest(com.cubeia.firebase.api.action.local.FilteredJoinAction)
	 */
	public void addRequest(FilteredRequest request) {
		addToQueue(request);
	}
	
	public boolean returnRequest(FilteredRequest request) {
		return returnToQueue(request);
	}

	
	public FilteredRequest getMatch(String attribute, Map<?, ?> data) {
		RequestQueue queue = getOrCreateQueue(attribute);
		
		for (FilteredRequest request : queue.values()) {
			matchCounter.incrementAndGet();
			if (request.match(data)) {
				removeRequest(request);
				return request;
			}
		}
		
		return null;
	}
	
	
	private void addToQueue(FilteredRequest request) {
		// Always add to default key ("")
		getDefaultQueue().addRequest(request);
		// Map all keys
		for (Parameter<?> p : request.getParameters().values()) {
			RequestQueue queue = getOrCreateQueue(p.getKey());
			// Queue is now guaranteed to not be null
			queue.addRequest(request);
		}
	}
	
	private boolean returnToQueue(FilteredRequest request) {
		// Always add to default key ("")
		boolean atHead = getDefaultQueue().returnRequest(request);
		// Map all keys
		for (Parameter<?> p : request.getParameters().values()) {
			RequestQueue queue = getOrCreateQueue(p.getKey());
			// Queue is now guaranteed to not be null
			boolean test = queue.returnRequest(request);
			if(test) {
				atHead = test;
			}
		}
		return atHead;
	}
	
	
	public void removeRequest(FilteredRequest request) {
		for (RequestQueue queue : queues.values()) {
			queue.remove(request);
		}
	}
	
	
	/**
	 * Get a queue for a key or create on and insert it if not yet defined.
	 * 
	 * @param key
	 * @return
	 */
	private RequestQueue getOrCreateQueue(String key) {
		if (key == null) key = "";
		
		RequestQueue queue = queues.get(key);
		if (queue == null) {
			// Get synch lock, update ref and try again and create new if still null
			synchronized (queues) {
				if ((queue = queues.get(key)) == null) {
					queue = createRequestQueue();
					queues.put(key, queue);
				}
			}
		}
		return queue;
	}



	
	
	private RequestQueue getDefaultQueue() {
		return getOrCreateQueue("");
	}
	
	
	
	/**
	 * Dump all and everything in the node waiting list to logs in a 
	 * human readable format.
	 * 
	 * This is very expensive, so please do call on this in production code.
	 * 
	 */
	public void dumpToLog() {
		
		String debug = "Node Waiting List:\n";
		for (String key : queues.keySet()) {
			debug += "\t"+key+":\n";
			Collection<FilteredRequest> copy = queues.get(key).values();
			for (FilteredRequest fr : copy) {
				debug += "\t\t"+fr+"\n";
			}
		}
		
		log.debug(debug);
	}

	
	/**
	 * Get all created keys.
	 * 
	 * @return
	 */
	public String[] getKeys() {
		return queues.keySet().toArray(new String[]{});
	}
	
	/**
	 * This is the size of the default queue
	 * 
	 */
	public long getSize() {
		return getDefaultQueue().size();
	}
	
	/**
	 * This is the size of the default queue
	 * 
	 */
	public long getSize(String key) {
		RequestQueue queue = queues.get(key);
		if (queue != null) {
			return queue.size();
		} else {
			return -1;
		}
	}
	
	/**
	 * This is the average size for a key.
	 * I.e.
	 * the sum of all queue-sizes divided by number
	 * of keys.
	 * 
	 */
	public long getAverageSize() {
		if (queues.size() == 0) return 0;
		
		long size = 0;
		for (RequestQueue queue : queues.values()) {
			size += queue.size();
		}
		return size/queues.size();
	}

	
	/**
	 * Wooo.. harsh method.
	 * Since the queues are not thread-safe, we will remove all queues.
	 * Of course they will get quickly created again if users
	 * are requesting waiting for tables.
	 * 
	 */
	public void clearAllQueues() {
		for (String key: queues.keySet()) {
			queues.remove(key);
		}
	}

	
	
}
