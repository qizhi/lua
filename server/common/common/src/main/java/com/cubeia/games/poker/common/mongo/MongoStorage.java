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

package com.cubeia.games.poker.common.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.mongodb.*;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;

/**
 * Used for storing stuff to a mongo database. Configured in admin.properties.
 */
public class MongoStorage {

    private static final Logger log = Logger.getLogger(MongoStorage.class);
    private Morphia morphia = null;
    private Datastore datastore = null;
    private String host;
    private int port;
    private String databaseName;

    public MongoStorage(DatabaseStorageConfiguration configuration) {
        host = configuration.getHost();
        port = configuration.getPort();
        databaseName = configuration.getDatabaseName();
    }

    public void persist(Object object) {
        datastore.save(object);
    }

    public void map(Class classType) {
        if (morphia != null && datastore != null)
        {
            morphia.map(classType);
//            datastore.ensureIndexes();
        }
    }
    public Datastore getDatastore() {
        return this.datastore;
    }

    public DBCollection getCollection(String name)
    {
        if (datastore != null && datastore.getMongo() != null)
        {
            try {
                return datastore.getDB().getCollection(name);
            } catch (Exception e) {
                log.error("Could not get collection from mongo db. Collection name: " + name);
            }
        }
        return null;
    }

    public Query createQuery(Class classType)
    {
        return datastore.createQuery(classType);
    }

    private void connectToMongo() throws UnknownHostException {
        morphia = new Morphia();
        morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);
        datastore = morphia.createDatastore(new Mongo(host.trim(), port), databaseName);
    }

    public void connect() {
        try {
            connectToMongo();
        } catch (UnknownHostException e) {
            log.warn("Could not connect to mongo on host " + host + " and port " + port);
        }
    }

    public void disconnect() {
        if (datastore != null && datastore.getMongo() != null) {
            log.info("Closing mongo.");
            datastore.getMongo().close();
        }
    }
}