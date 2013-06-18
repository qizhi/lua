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
package com.cubeia.firebase.server.gateway.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.cache.util.concurrent.ConcurrentHashSet;

/**
 * Local state data.
 *
 * @author Fredrik
 */
public class ClientLocal {
	
	/**
	 * Maps client side sequence numbers to node-local filtered join request id's.
	 */
	private ConcurrentMap<Integer, Long> joinRequests = new ConcurrentHashMap<Integer, Long>();
    
	/**
	 * Maps tables known to local client session.
	 * This will be used to filter out traffic from tables that thinks the client is seated but he is not! 
	 */
	private ConcurrentMap<Integer, Integer> knownTables = new ConcurrentHashMap<Integer, Integer>();
	
	/**
	 * Holds a map of all tables we have notified the client about.
	 * This way we will not notify the client for every unknown packet received. 
	 */
	private ConcurrentMap<Integer, Integer> notifiedTables = new ConcurrentHashMap<Integer, Integer>();
	
    /**
     * Contains the ids of all chat channels this player has joined.
     */
    private Set<Integer> chatChannels = new ConcurrentHashSet<Integer>();

    /**
     * Gets the map of join requests. Not a copy.
     * 
     * @return
     */
	public ConcurrentMap<Integer, Long> getJoinRequests() {
		return joinRequests;
	}

    /**
     * Adds a join request.
     * 
     * @param seq
     * @param requestId
     */
	public void addJoinRequest(int seq, long requestId) {
		joinRequests.put(seq, requestId);
	}
	
    /**
     * Removes a join request.
     * 
     * @param seq
     */
	public void removeJoinRequest(int seq) {
		joinRequests.remove(seq);
	}
	
    /**
     * Gets the request id for a join request, given the client's sequence id.
     *   
     * @param seq
     * @return
     */
	public long getJoinRequestId(int seq) {
		if (joinRequests.containsKey(seq)) {
			return joinRequests.get(seq);
		} else {
			return -1;
		}
	}
	
    /**
     * Gets the client side sequence id, given the node local request id. Note, this is not efficient.
     * 
     * @param requestId
     * @return
     */
	public int getJoinRequestSeq(long requestId) {
		for (Integer key : joinRequests.keySet()) {
			if (joinRequests.get(key) == requestId) {
				return key;
			}
		}
		return -1;
	}

    /**
     * Adds a chat channel to the set of active channels.
     * 
     * @param channelId
     */
    public void addChatChannel(int channelId) {
        chatChannels.add(channelId);
    }
    
    /**
     * Removes a chat channel from the set of active channels.
     * 
     * @param channelId
     */
    public void removeChatChannel(int channelId) {
        chatChannels.remove(channelId);
    }

    /**
     * Gets the set of active channels. Not a copy.
     * 
     * @return a {@link Set} of ids of the active chat channels.
     */
    public Set<Integer> getActiveChatChannels() {
        return chatChannels;
    }

    /**
     * Add a known table.
     * 
     * @param tableId
     * @param seatId
     */
	public void addKnownTable(int tableId, int seatId) {
		knownTables.put(tableId, seatId);
	}
	
	public void removeKnownTable(int tableId) {
		knownTables.remove(tableId);
	}
	
	public boolean isTableKnown(int tableId) {
		return knownTables.containsKey(tableId);
	}
	
	public void clearKnownTables() {
		knownTables.clear();
	}
	
	/**
     * Add a notified table.
     * 
     * @param tableId
     * @param seatId
     */
	public void addNotifiedTable(int tableId, int seatId) {
		notifiedTables.put(tableId, seatId);
	}
	
	public void removeNotifiedTable(int tableId) {
		notifiedTables.remove(tableId);
	}
	
	public boolean isTableNotified(int tableId) {
		return notifiedTables.containsKey(tableId);
	}
	
	public void clearNotifiedTables() {
		notifiedTables.clear();
	}
}
