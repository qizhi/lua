/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.state;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cubeia.games.poker.tournament.state.PendingBackendRequests.PendingRequestType.REGISTRATION;
import static com.google.common.collect.Maps.newHashMap;

public class PendingBackendRequests implements Serializable {

    public enum PendingRequestType { REGISTRATION, REBUY, ADD_ON }

    private static final Logger log = Logger.getLogger(PendingBackendRequests.class);

    /**
     * Maps a playerId to the type of request that is pending. Used for knowing what type of request was performed
     * when an asynchronous call has finished.
     */

    private Map<Integer, PendingRequestType> pendingRequests = newHashMap();

    /**
     * Maps tableId to a set of playerIds of players who have pending requests at that table.
     */
    private Map<Integer, Set<Integer>> pendingRequestsByTable = newHashMap();

    public void addPendingRegistration(int playerId) {
        addPendingRequest(playerId, -1, REGISTRATION);
    }

    public void addPendingRequest(int playerId, int tableId, PendingRequestType type) {
        if (pendingRequests.containsKey(playerId)) {
            throw new IllegalArgumentException("Player " + playerId + " already has a pending request: " + pendingRequests.get(playerId));
        }
        pendingRequests.put(playerId, type);
        if (tableId > 0) {
            getPendingRequestsForTable(tableId).add(playerId);
        }
    }

    public PendingRequestType getAndClearPendingRequest(int playerId, int tableId) {
        PendingRequestType pendingRequestType = pendingRequests.get(playerId);
        removePendingRequest(playerId, tableId, pendingRequestType);
        return pendingRequestType;
    }

    public boolean isEmpty() {
        return pendingRequests.isEmpty();
    }

    public boolean playerHasPendingRequests(int playerId) {
        return pendingRequests.containsKey(playerId);
    }

    public void removePendingRegistration(int playerId) {
        removePendingRequest(playerId, -1, REGISTRATION);
    }

    public boolean tableHasPendingRequests(int tableId) {
        return pendingRequestsByTable.containsKey(tableId);
    }

    private Set<Integer> getPendingRequestsForTable(int tableId) {
        if (!pendingRequestsByTable.containsKey(tableId)) {
            pendingRequestsByTable.put(tableId, new HashSet<Integer>());
        }
        return pendingRequestsByTable.get(tableId);
    }

    private void removePendingRequest(int playerId, int tableId, PendingRequestType type) {
        if (type == pendingRequests.get(playerId)) {
            pendingRequests.remove(playerId);
            if (tableId > 0) {
                Set<Integer> playerIds = pendingRequestsByTable.get(tableId);
                playerIds.remove(playerId);
                if (playerIds.isEmpty()) {
                    pendingRequestsByTable.remove(tableId);
                }
            }
        } else {
            log.warn(playerId + " tried to remove pending request of type " + type + " but no such request existed. Request for pid: " + pendingRequests
                    .get(playerId));
        }
    }
}
