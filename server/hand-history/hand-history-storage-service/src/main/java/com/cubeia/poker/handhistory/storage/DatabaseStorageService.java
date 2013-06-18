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

package com.cubeia.poker.handhistory.storage;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.games.poker.common.mongo.MongoStorage;
import com.cubeia.poker.handhistory.api.HandHistoryPersistenceService;
import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.handhistory.impl.JsonHandHistoryLogger;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.apache.log4j.Logger;

/**
 * A database based implementation of the hand history persistence service, which stores the hand
 * history in a MongoDB database.
 */
public class DatabaseStorageService implements HandHistoryPersistenceService, Service {

    private static final Logger log = Logger.getLogger(DatabaseStorageService.class);

    private DatabaseStorageConfiguration configuration;
    private MongoStorage mongoStorage;
    private JsonHandHistoryLogger jsonLogger;

    public void persist(HistoricHand hand) {
        log.info("Persisting hand to mongo");

        try {
            mongoStorage.persist(hand);
        } catch (Exception e) {
            log.warn("Failed persisting hand history to mondodb. Please start a mongodb server on host " + configuration.getHost()
                     + " and port " + configuration.getPort(), e);
            jsonLogger.persist(hand);
        }
    }

    @Override
    public void init(ServiceContext context) throws SystemException {
        configuration = getConfiguration(context);
        mongoStorage = getMongoStorage();
        jsonLogger = new JsonHandHistoryLogger();
    }

    protected DatabaseStorageConfiguration getConfiguration(ServiceContext context) {
        return new DatabaseStorageConfiguration().load(context.getServerConfigDirectory().getAbsolutePath());
    }

    protected MongoStorage getMongoStorage() {
        return new MongoStorage(configuration);
    }

    private void initHandsCollection()
    {
        mongoStorage.map(HistoricHand.class);
        //Indexing
        DBCollection coll = mongoStorage.getCollection(HistoricHand.class.getSimpleName());
        if(0 == coll.getCount()) {
            coll.createIndex(new BasicDBObject("startTime", -1));
        }
    }

    @Override
    public void start() {
        mongoStorage.connect();
        initHandsCollection();
    }

    @Override
    public void stop() {
        mongoStorage.disconnect();
    }

    @Override
    public void destroy() {
    }
}