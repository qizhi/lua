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

package com.cubeia.poker.handhistory.provider.impl;

import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.games.poker.common.mongo.MongoStorage;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHand;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandIds;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandSummaries;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHands;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHand;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHandIds;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHandSummaries;
import com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHands;
import com.cubeia.games.poker.routing.service.io.protocol.ProtocolObjectFactory;
import com.cubeia.poker.handhistory.api.HandHistoryEvent;
import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.handhistory.api.PlayerCardsDealt;
import com.cubeia.poker.handhistory.provider.api.HandHistoryProviderService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;

public class HandHistoryProviderServiceImpl implements HandHistoryProviderService, Service, RoutableService {

    private static final Logger log = Logger.getLogger(HandHistoryProviderServiceImpl.class);
    public static final int MAX_HANDS = 500;
    public static final int MAX_HAND_IDS = 500;

    private ServiceRouter router;
    private MongoStorage mongoStorage;
    private DatabaseStorageConfiguration configuration;

    @Override
    public String getHandIdsAsJson(int tableId, int playerId, int count, long time) {
        List<Key<HistoricHand>> resultList = getHandIds(tableId, playerId, count, time);
        return convertToJson(resultList);
    }

    public List<Key<HistoricHand>> getHandIds(int tableId, int playerId, int count, long time) {
        log.debug("GetHandIds request data - TableId: " + tableId + " PlayerId: " + playerId + " Count: " + count + " Time: " + time);
        List<Key<HistoricHand>> resultList;
        Query<HistoricHand> query = this.createHistoricHandQuery(playerId);
        query.field("table.tableId").equal(tableId);
        if (count > 0) {
            if (count > MAX_HAND_IDS) {
                count = MAX_HAND_IDS;
            }

            resultList = query.order("-startTime").limit(count).asKeyList();
        } else {
            query.field("startTime").greaterThanOrEq(time);
            resultList = query.order("-startTime").limit(MAX_HAND_IDS).asKeyList();
        }
        return resultList;
    }

    @Override
    public String getHandAsJson(String handId, int playerId) {
        List<HistoricHand> hands = getHand(handId, playerId);
        return convertToJson(hands);
    }

    public List<HistoricHand> getHand(String handId, int playerId) {
        log.debug("GetHand request data - HandId: " + handId + " PlayerId: " + playerId);
        Query<HistoricHand> query = this.createHistoricHandQuery(playerId);

        query.field("id").equal(handId);

        return this.filterHistoricHand(query.asList(), playerId);
    }

    private List<HistoricHand> filterHistoricHand(List<HistoricHand> list, int playerId) {
        for(HistoricHand h : list) {
            List<HandHistoryEvent> events = h.getEvents();
            for(HandHistoryEvent e: events) {
                if(e instanceof PlayerCardsDealt) {
                    PlayerCardsDealt playerCardsDealt = (PlayerCardsDealt) e;
                    if(playerId != playerCardsDealt.getPlayerId()) {
                        playerCardsDealt.getCards().clear();
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String getHandsAsJson(int tableId, int playerId, int count, long time) {
        List<HistoricHand> resultList = getHands(tableId, playerId, count, time);
        return convertToJson(resultList);
    }

    public List<HistoricHand> getHands(int tableId, int playerId, int count, long time) {
        log.debug("GetHands request data - TableId: " + tableId + " PlayerId: " + playerId + " Count: " + count + " Time: " + time);

        List<HistoricHand> resultList = null;
        if (count > 0) {
            if (count > MAX_HANDS) {
                count = MAX_HANDS;
            }
            Query<HistoricHand> query = this.createHistoricHandQuery(playerId);
            query.field("table.tableId").equal(tableId);
            resultList = query.order("-startTime").limit(count).asList();

        } else {
            Query query = this.createHistoricHandQuery(playerId);
            query.field("table.tableId").equal(tableId);
            query.field("startTime").greaterThanOrEq(time);
            resultList = query.order("-startTime").limit(MAX_HANDS).asList();
        }
        resultList = this.filterHistoricHand(resultList,playerId);
        return resultList;
    }

    @Override
    public String getHandSummariesAsJson(int tableId, int playerId, int count, long time) {
        List<HistoricHand> hands = getHandSummaries(tableId, playerId, count, time);
        return convertToJson(hands);

    }

    public List<HistoricHand> getHandSummaries(int tableId, int playerId, int count, long time) {
        log.debug("GetHandSummary request data - TableId: " + tableId + " PlayerId: " + playerId + " Count: " + count + " Time: " + time);
        Query<HistoricHand> query = mongoStorage.createQuery(HistoricHand.class);
        query.filter("seats elem", new BasicDBObject("playerId", playerId));
        query.field("table.tableId").equal(tableId);
        query.retrievedFields(false, "events","seats","results");

        if (count > 0) {
            if (count > MAX_HANDS) {
                count = MAX_HANDS;
            }
        } else {
            query.field("startTime").greaterThanOrEq(time);
            count = MAX_HANDS;
        }


        return query.order("-startTime").limit(count).asList();
    }

    private Query<HistoricHand> createHistoricHandQuery(int playerId) {
        Query<HistoricHand> query = mongoStorage.createQuery(HistoricHand.class);
        query.filter("seats elem", new BasicDBObject("playerId", playerId));
        return query;
    }

    @Override
    public void setRouter(ServiceRouter router) {
        this.router = router;
    }

    @Override
    public void onAction(ServiceAction e) {
        log.debug("Hand history requested.");
        StyxSerializer serializer = new StyxSerializer(new ProtocolObjectFactory());
        ProtocolObject protocolRequestObject = serializer.unpack(ByteBuffer.wrap(e.getData()));
        ProtocolObject protocolResponseObject = null;

        log.debug("Class is: " + protocolRequestObject.getClass().getName());

        if (protocolRequestObject instanceof HandHistoryProviderRequestHand) {
            HandHistoryProviderRequestHand request = (HandHistoryProviderRequestHand)protocolRequestObject;
            protocolResponseObject = new HandHistoryProviderResponseHand(getHandAsJson(request.handId, e.getPlayerId()));
        } else if (protocolRequestObject instanceof HandHistoryProviderRequestHands) {
            HandHistoryProviderRequestHands request = (HandHistoryProviderRequestHands)protocolRequestObject;
            protocolResponseObject = new HandHistoryProviderResponseHands(request.tableId, getHandsAsJson(request.tableId, e.getPlayerId(), request.count, getTime(request.time)));
        } else if (protocolRequestObject instanceof HandHistoryProviderRequestHandIds) {
            HandHistoryProviderRequestHandIds request = (HandHistoryProviderRequestHandIds)protocolRequestObject;
            protocolResponseObject = new HandHistoryProviderResponseHandIds(request.tableId, getHandIdsAsJson(request.tableId, e.getPlayerId(), request.count, getTime(request.time)));
        } else if(protocolRequestObject instanceof HandHistoryProviderRequestHandSummaries) {
            HandHistoryProviderRequestHandSummaries request = (HandHistoryProviderRequestHandSummaries)protocolRequestObject;
            protocolResponseObject = new HandHistoryProviderResponseHandSummaries(request.tableId, getHandSummariesAsJson(request.tableId, e.getPlayerId(), request.count, getTime(request.time)));
        }

        if (protocolResponseObject != null) {
            byte[] responseData = serializer.pack(protocolResponseObject).array();
            ServiceAction action = new ClientServiceAction(e.getPlayerId(), -1, responseData);
            router.dispatchToPlayer(e.getPlayerId(), action);
        }
    }

    private long getTime(String value) {
        long time = 0L;
        if (!(value == null || value.isEmpty())) {
            try {
                time = Long.parseLong(value);
            }
            catch (Throwable t) { }
        }
        return time;
    }

    @Override
    public void init(ServiceContext context) throws SystemException {
        log.debug("HandHistoryProviderService STARTED! ");
        configuration = getConfiguration(context);
        mongoStorage = getMongoStorage();
    }

    protected DatabaseStorageConfiguration getConfiguration(ServiceContext context) {
        return new DatabaseStorageConfiguration().load(context.getServerConfigDirectory().getAbsolutePath());
    }

    protected MongoStorage getMongoStorage() {
        return new MongoStorage(configuration);
    }

    private String convertToJson(List hands) {
        Gson gson = createGson();

        return gson.toJson(hands);
    }

    private Gson createGson() {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(HandHistoryEvent.class, new HandHistorySerializer());
        //b.setPrettyPrinting();
        return b.create();
    }

    @Override
    public void destroy() { }

    @Override
    public void start() {
        mongoStorage.connect();
    }

    @Override
    public void stop() {
        mongoStorage.disconnect();
    }

    private static class HandHistorySerializer implements JsonSerializer<HandHistoryEvent> {

        @Override
        public JsonElement serialize(HandHistoryEvent src, Type typeOfSrc, JsonSerializationContext context) {
            Class<? extends HandHistoryEvent> cl = src.getClass();
            return context.serialize(src, cl);
        }
    }
}