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

package com.cubeia.games.poker.admin.service.history;

import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.handhistory.api.Player;
import com.cubeia.poker.handhistory.api.Table;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.google.code.morphia.Datastore;
import com.mongodb.Mongo;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmbeddedMongoHistoryServiceImplTest {

    private static int PORT = 12345;

    private static MongodProcess mongoProcess;

    private HistoryServiceImpl service;

    private Mongo mongo;

    private MongoStorage storage;

    private Datastore datastore;

    @Before
    public void setup() throws UnknownHostException {
        initMocks(this);
        mongo = new Mongo("localhost", PORT);
        storage = new MongoStorage(mongo, "poker");
        service = new HistoryServiceImpl(storage);
        datastore = storage.getDatastore();
    }

    @After
    public void onTearDown() throws Exception {
        datastore.delete(datastore.createQuery(HistoricHand.class));
    }

    @BeforeClass
    public static void initDb() throws Exception {
        MongodConfig config = new MongodConfig(Version.V2_1_1, PORT, Network.localhostIsIPv6());
        MongodExecutable prepared = MongoDBRuntime.getDefaultInstance().prepare(config);
        mongoProcess = prepared.start();
    }

    @AfterClass
    public static void shutdownDb() {
        if (mongoProcess != null) {
            mongoProcess.stop();
        }
    }

    @Test
    public void testFindHandById() throws Exception {
        // Given that there is a hand with a given id.
        datastore.save(new HistoricHand("some-id"));

        // When we find by that id.
        HistoricHand handById = service.findHandById("some-id");

        // We should get a hand back.
        assertThat(handById, notNullValue());
    }

    @Test
    public void testFindHandHistory() throws Exception {
        // Given a hand with the following properties.
        String handId = "hand-id";
        DateTime handStartDate = new DateTime(2013, 5, 2, 15, 30, 2);
        DateTime handEndDate = new DateTime(2013, 5, 2, 15, 32, 59);
        int tableId = 5;
        String tableIntegrationId = "TABLE-INTEGRATION-ID";
        Player player = new Player(101, 1, new BigDecimal(5000), "name");

        HistoricHand hand = createHand(handId, handStartDate, handEndDate, tableId, tableIntegrationId, player);
        datastore.save(hand);

        // When we look for that hand, given the playerId, tableId and time range.
        List<HistoricHand> hands = service.findHandHistory(101, tableIntegrationId, handStartDate.minusSeconds(10).toDate(), handStartDate.plusSeconds(10).toDate());

        // We should find it.
        assertThat(hands.isEmpty(), is(false));
    }

    @Test
    public void testFindTournamentByHistoricId() throws Exception {
        // Given a tournament with a historic id.
        ObjectId id = new ObjectId();
        HistoricTournament historicTournament = new HistoricTournament();
        historicTournament.setId(id);
        datastore.save(historicTournament);

        // When we search for it.
        HistoricTournament tournamentByHistoricId = service.findTournamentByHistoricId(id.toString());

        // We should find it.
        assertThat(tournamentByHistoricId, notNullValue());
    }

    @Test
    public void testFindTournaments() throws Exception {
        // Given a tournament with a start time.
        DateTime startDate = new DateTime(2013, 5, 2, 15, 30, 0);
        ObjectId id = new ObjectId();
        HistoricTournament historicTournament = new HistoricTournament();
        historicTournament.setId(id);
        historicTournament.setStartTime(startDate.getMillis());
        datastore.save(historicTournament);

        // When we search for tournaments in that interval.
        List<HistoricTournament> tournaments = service.findTournaments(startDate.minusMinutes(2).toDate(), startDate.plusMinutes(2).toDate());

        // We should find it.
        assertThat(tournaments.isEmpty(), is(false));
    }

    private HistoricHand createHand(String handId, DateTime handStartDate, DateTime handEndDate, int tableId, String tableIntegrationId, Player player) {
        HistoricHand hand = new HistoricHand(handId);
        hand.setStartTime(handStartDate.getMillis());
        hand.setEndTime(handEndDate.getMillis());
        hand.setSeats(Arrays.asList(player));
        Table table = new Table();
        table.setTableId(tableId);
        table.setTableIntegrationId(tableIntegrationId);
        hand.setTable(table);
        return hand;
    }
}
