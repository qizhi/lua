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
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.handhistory.api.PlayerAction;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmbeddedMongoDatabaseStorageServiceTest {

    private static int PORT = 12345;

    private static String HOST = "localhost";

    static MongodProcess mongod;

    private DatabaseStorageService service;

    @Mock
    private DatabaseStorageConfiguration configuration;

    @Before
    public void setup() throws SystemException {
        initMocks(this);
        when(configuration.load(anyString())).thenReturn(configuration);
        when(configuration.getHost()).thenReturn(HOST);
        when(configuration.getPort()).thenReturn(PORT);
        when(configuration.getDatabaseName()).thenReturn("hands");
        service = new DatabaseStorageService() {
            @Override
            protected DatabaseStorageConfiguration getConfiguration(ServiceContext serviceContext) {
                return configuration;
            }
        };
        service.init(null);
        service.start();
    }

    @BeforeClass
    public static void initDb() throws Exception {
        MongodConfig config = new MongodConfig(Version.V2_1_1, PORT, Network.localhostIsIPv6());
        MongodExecutable prepared = MongoDBRuntime.getDefaultInstance().prepare(config);
        mongod = prepared.start();
    }

    @AfterClass
    public static void shutdownDb() {
        if (mongod != null) mongod.stop();
    }

    @Test
    public void testSimplePersist() throws Exception {
        HistoricHand historicHand = new HistoricHand("someHandId");
        historicHand.getEvents().add(new PlayerAction(99, PlayerAction.Type.ANTE));

        service.persist(historicHand);

        Mongo mongo = new Mongo(HOST, PORT);
        DB db = mongo.getDB("hands");
        DBCollection collection = db.getCollection("hands");

        DBCursor cursorDoc = collection.find();
        DBObject object = cursorDoc.next();
        assertThat(object.get("events").toString(), startsWith("[ { \"action\" : \"ANTE\""));
    }
}
