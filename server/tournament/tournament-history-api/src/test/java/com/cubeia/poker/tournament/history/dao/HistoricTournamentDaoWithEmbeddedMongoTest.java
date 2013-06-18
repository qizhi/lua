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

package com.cubeia.poker.tournament.history.dao;

import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.api.TournamentEvent;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.apache.log4j.Logger;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HistoricTournamentDaoWithEmbeddedMongoTest {
    
    private static final Logger log = Logger.getLogger(HistoricTournamentDaoWithEmbeddedMongoTest.class);

    static int PORT = 12345;

    static String HOST = "localhost";

    static MongodProcess mongoProcess;

    @Mock
    DatabaseStorageConfiguration configuration;

    HistoricTournamentDao dao;

    private static Datastore datastore;

    @Before
    public void setup() throws UnknownHostException {
        initMocks(this);
        when(configuration.load(anyString())).thenReturn(configuration);
        when(configuration.getHost()).thenReturn(HOST);
        when(configuration.getPort()).thenReturn(PORT);
        when(configuration.getDatabaseName()).thenReturn("poker");
        dao = new HistoricTournamentDao(datastore);
    }

    @BeforeClass
    public static void initDb() throws Exception {
        MongodConfig config = new MongodConfig(Version.V2_1_1, PORT, Network.localhostIsIPv6());
        MongodExecutable prepared = MongoDBRuntime.getDefaultInstance().prepare(config);
        mongoProcess = prepared.start();
        Mongo mongo = new Mongo("localhost", PORT);
        datastore = new Morphia().createDatastore(mongo, "morphia");
    }

    @AfterClass
    public static void shutdownDb() {
        if (mongoProcess != null) mongoProcess.stop();
    }

    @Test
    public void testFindTournamentsToResurrect() throws Exception {
        HistoricTournament resurrectMe = new HistoricTournament();
        HistoricTournament notRegisteringYet = new HistoricTournament();
        HistoricTournament runningSitAndGo = new HistoricTournament();
        HistoricTournament ignoredBecauseStarted = new HistoricTournament();

        resurrectMe.getRegisteredPlayers().add(player(17, "p1"));
        resurrectMe.setStatus("REGISTERING");

        notRegisteringYet.setStatus("ANNOUNCED");

        runningSitAndGo.setStatus("RUNNING");
        runningSitAndGo.setSitAndGo(true);
        runningSitAndGo.setStartTime(new Date().getTime());

        ignoredBecauseStarted.getRegisteredPlayers().add(player(18, "p2"));
        ignoredBecauseStarted.setStartTime(new Date().getTime());
        ignoredBecauseStarted.setStatus("RUNNING");

        dao.store(resurrectMe);
        dao.store(notRegisteringYet);
        dao.store(runningSitAndGo);
        dao.store(ignoredBecauseStarted);

        List<HistoricTournament> tournamentsToResurrect = dao.findTournamentsToResurrect();
        assertThat(tournamentsToResurrect.size(), is(3));
    }

    private HistoricPlayer player(int id, String name) {
        return new HistoricPlayer(id, name, "");
    }

    @Test
    public void testCreateHistoricTournament() {
        assertThat(createHistoricTournament(), notNullValue());
    }
    
    @Test
    public void testFindHistoricTournament() throws Exception {
        String id = createHistoricTournament();
        log.debug("Id = " + id);
        HistoricTournament tournament = dao.getHistoricTournament(id);
        assertThat(tournament, notNullValue());
    }

    @Test
    public void testFindHistoricTournamentByKey() throws Exception {
        Key<HistoricTournament> id = dao.createHistoricTournamentKey();
        log.debug("Id = " + id);
        HistoricTournament tournament = dao.getHistoricTournamentByKey(id);
        assertThat(tournament, notNullValue());
    }

    @Test
    public void testUpdateTournament() throws Exception {
        String id = createHistoricTournament();

        dao.addEvent(id, new TournamentEvent(1L, "status changed", "ANNOUNCED"));
        dao.addEvent(id, new TournamentEvent(2L, "status changed", "REGISTERING"));

        HistoricTournament tournament = dao.getHistoricTournament(id);
        assertThat(tournament.getEvents().size(), is(2));
    }

    @Test
    public void testUnregisterPlayerRemovesHimFromTheSet() {
        String id = createHistoricTournament();
        dao.addRegisteredPlayer(id, new HistoricPlayer(1, "p1", "s1"));
        dao.addRegisteredPlayer(id, new HistoricPlayer(2, "p2", "s2"));
        dao.removeRegisteredPlayer(id, 2);
        assertEquals(1, dao.getHistoricTournament(id).getRegisteredPlayers().size());
    }

    @Test
    public void testSetStartDate() throws Exception {
        String id = createHistoricTournament();
        dao.setStartTime(id, new DateTime().getMillis());

        HistoricTournament tournament = dao.getHistoricTournament(id);
        log.debug("Start date: " + new Date(tournament.getStartTime()));
        assertThat(tournament.getStartTime(), not(0L));
    }

    @Test
    public void testAddTable() throws Exception {
        String id = createHistoricTournament();
        dao.addTable(id, "ext1");

        HistoricTournament tournament = dao.getHistoricTournament(id);
        log.debug("Tables: " + tournament.getTables());
        assertThat(tournament.getTables().size(), Is.is(1));
    }

    private String createHistoricTournament() {
        return dao.createHistoricTournament("name", 1, 11, false);
    }
}
