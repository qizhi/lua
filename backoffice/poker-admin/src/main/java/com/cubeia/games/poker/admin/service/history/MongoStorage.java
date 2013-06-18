package com.cubeia.games.poker.admin.service.history;

import com.cubeia.games.poker.common.mongo.BigDecimalConverter;
import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.mongodb.Mongo;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;

public class MongoStorage {

    private static final Logger log = Logger.getLogger(MongoStorage.class);
    private Morphia morphia = null;
    private Datastore datastore = null;

    public MongoStorage(Mongo mongo, String databaseName) {
        try {
            connectToMongo(mongo, databaseName);
            map(HistoricHand.class);
            map(HistoricTournament.class);
        } catch (UnknownHostException e) {
            log.warn("Could not connect to mongo.");
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public void map(Class classType) {
        if (morphia != null && datastore != null) {
            morphia.map(classType);
        }
    }

    public Query createQuery(Class classType)
    {
        return datastore.createQuery(classType);
    }

    private void connectToMongo(Mongo mongo, String databaseName) throws UnknownHostException {
        morphia = new Morphia();
        morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);
        datastore = morphia.createDatastore(mongo, databaseName);
    }

    public void disconnect() {
        if (datastore != null && datastore.getMongo() != null) {
            log.info("Closing mongo.");
            datastore.getMongo().close();
        }
    }
}