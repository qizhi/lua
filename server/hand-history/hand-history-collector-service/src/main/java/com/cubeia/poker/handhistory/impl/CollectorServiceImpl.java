/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.handhistory.impl;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.poker.handhistory.api.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the collector implementation. It caches hands in a map
 * and will use an optional hand history persister service to
 * persist the result when the hand is ended. If no persister service
 * is deployed it will write the hand to the logs in JSON format on
 * DEBUG level.
 *
 * @author Lars J. Nilsson
 */

// TODO Reap dead hands?
// TODO Persist on each event to support fail-over?
// TODO Read hand from database (if not found) to support fail-over
@Singleton
public class CollectorServiceImpl implements HandHistoryCollectorService, Service {

    @Log4j
    private Logger log;

    private Map<Integer, HistoricHand> cache = new ConcurrentHashMap<Integer, HistoricHand>();

    @Inject
    private ServiceContext context;

    @Inject
    private JsonHandHistoryLogger jsonPersist;

    @Override
    public void startHand(String id, Table table, List<Player> seats) {
        log.debug("Start hand on table: " + id);
        if (cache.containsKey(table.getTableId())) {
            log.warn("Starting new hand, but cache is not empty, for table: " + table.getTableId());
        }
        HistoricHand hand = new HistoricHand(id);
        hand.setTable(table);
        hand.setStartTime(new DateTime().getMillis());
        hand.getSeats().addAll(seats);
        cache.put(table.getTableId(), hand);
    }
    
    @Override
    public void reportEvent(int tableId, HandHistoryEvent event) {
        HistoricHand hand = getCurrent(tableId);
        hand.getEvents().add(event);
    }

    @Override
    public void reportDeckInfo(int tableId, DeckInfo deckInfo) {
        HistoricHand hand = getCurrent(tableId);
        hand.setDeckInfo(deckInfo);
    }

    @Override
    public void reportResults(int tableId, Results res) {
        HistoricHand hand = getCurrent(tableId);
        hand.setResults(res);
    }

    @Override
    public void stopHand(int tableId) {
        HistoricHand hand = getCurrent(tableId);
        hand.setEndTime(new DateTime().getMillis());
        log.debug("Storing hand via persister.");
        getPersister().persist(hand);
        log.debug("Done storing hand via persister.");
        cache.remove(tableId);
    }

    @Override
    public void cancelHand(int tableId) {
        // TODO Report this?
        cache.remove(tableId);
    }

    // --- PRIVATE METHODS --- //

    private HistoricHand getCurrent(int tableId) {
        if (!cache.containsKey(tableId)) {
            // TODO Read from database...
            throw new RuntimeException("Current hand for table " + tableId + " not found!");
        } else {
            return cache.get(tableId);
        }
    }

    private HandHistoryPersister getPersister() {
        HandHistoryPersistenceService service = context.getParentRegistry().getServiceInstance(HandHistoryPersistenceService.class);
        if (service != null) {
            return service;
        } else {
            return jsonPersist;
        }
    }

    @Override
    public void init(ServiceContext con) throws SystemException { }

    @Override
    public void destroy() { }

    @Override
    public void start() { }

    @Override
    public void stop() { }

}
