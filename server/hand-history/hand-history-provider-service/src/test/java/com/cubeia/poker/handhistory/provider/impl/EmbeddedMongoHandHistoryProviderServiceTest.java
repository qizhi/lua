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

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.games.poker.common.mongo.MongoStorage;
import com.cubeia.poker.handhistory.api.*;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.junit.*;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmbeddedMongoHandHistoryProviderServiceTest {

    private static int PORT = 12345;

    private static MongodProcess mongoProcess;

    private HandHistoryProviderServiceImpl service;

    @Mock
    private DatabaseStorageConfiguration configuration;

    MongoStorage storage = null;

    @Before
    public void setup() throws SystemException {
        initMocks(this);
        when(configuration.load(anyString())).thenReturn(configuration);
        String HOST = "localhost";
        when(configuration.getHost()).thenReturn(HOST);
        when(configuration.getPort()).thenReturn(PORT);
        when(configuration.getDatabaseName()).thenReturn("poker");

        service = new HandHistoryProviderServiceImpl() {
            @Override
            protected DatabaseStorageConfiguration getConfiguration(ServiceContext context) {
                return configuration;
            }

            @Override
            protected MongoStorage getMongoStorage() {
                storage = new MongoStorage(configuration);
                return storage;
            }
        };

        service.init(null);
        service.start();
    }

    @After
    public void onTearDown() throws Exception {
        Datastore datastore = storage.getDatastore();
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
    public void testGetHandIds() throws Exception {
        createHandHistory(1, "hand1", 100L, 1, 2);
        createHandHistory(1, "hand2", 200L, 1, 2);
        createHandHistory(1, "hand3", 300L, 2, 3);
        createHandHistory(2, "hand4", 400L, 1, 2);

        List<Key<HistoricHand>> handIds = service.getHandIds(1, 1, 10, 0);
        assertEquals(2, handIds.size());

        assertEquals("hand2", handIds.get(0).getId());
        assertEquals("hand1", handIds.get(1).getId());
    }

    @Test
    public void testGetHand() throws Exception {
        int myPlayerId = 1;
        createHandHistory(1, "hand1", 100L, myPlayerId, 2);
        createHandHistory(1, "hand2", 200L, myPlayerId, 2);
        createHandHistory(1, "hand3", 300L, 2, 3);
        createHandHistory(2, "hand4", 400L, myPlayerId, 2);

        List<HistoricHand> hand1 = service.getHand("hand1", myPlayerId);
        assertEquals(1, hand1.size());
        HistoricHand hand = hand1.get(0);

        assertEquals("hand1", hand.getId());
        verifyOtherPlayersPrivateCardsNotExposed(myPlayerId, hand);
    }

    private void verifyOtherPlayersPrivateCardsNotExposed(int myPlayerId, List<HistoricHand> hands) {
        for (HistoricHand h : hands) {
            verifyOtherPlayersPrivateCardsNotExposed(myPlayerId, h);
        }
    }

    private void verifyOtherPlayersPrivateCardsNotExposed(int myPlayerId, HistoricHand hand) {
        List<HandHistoryEvent> events = hand.getEvents();

        for (HandHistoryEvent e : events) {
            if (e instanceof PlayerCardsDealt) {
                PlayerCardsDealt pc = (PlayerCardsDealt) e;
                if (pc.getPlayerId() != myPlayerId && pc.getCards().size() > 0) {
                    fail("Private cards for other players should not be shown");
                } else if (pc.getPlayerId() == myPlayerId && pc.getCards().size() == 0) {
                    fail("Private cards for your player should be shown");
                }
            }
        }
    }

    @Test
    public void testGetHandsByCount() throws Exception {
        int myPlayerId = 1;
        createHandHistory(1, "hand1", 100L, myPlayerId, 2);
        createHandHistory(1, "hand2", 200L, myPlayerId, 2);
        createHandHistory(1, "hand3", 300L, 2, 3);
        createHandHistory(2, "hand4", 400L, myPlayerId, 2);
        createHandHistory(1, "hand5", 500L, myPlayerId, 2);

        List<HistoricHand> hands = service.getHands(1, myPlayerId, 2, System.currentTimeMillis());
        assertEquals(2, hands.size());
        verifyOtherPlayersPrivateCardsNotExposed(myPlayerId, hands);
    }

    @Test
    public void testGetHandSummaries() throws Exception {
        createHandHistory(1, "hand1", 100L, 1, 2);
        createHandHistory(1, "hand2", 200L, 1, 2);
        createHandHistory(1, "hand3", 300L, 2, 3);
        createHandHistory(2, "hand4", 400L, 1, 2);

        List<HistoricHand> handSummaries = service.getHandSummaries(1, 2, 2, System.currentTimeMillis());
        assertEquals(2,handSummaries.size());
        for(HistoricHand h : handSummaries) {
            assertTrue(h.getId().startsWith("hand"));
            assertEquals(0, h.getEvents().size());
            assertEquals(null, h.getResults());
            assertEquals(0, h.getSeats().size());
        }
    }

    @Test
    public void testGetHandsNoResults() throws Exception {
        createHandHistory(1, "hand1", 100L, 1, 2);
        createHandHistory(1, "hand2", 200L, 1, 2);

        List<HistoricHand> hands = service.getHands(1, 3, 10, System.currentTimeMillis());
        assertEquals(0, hands.size());
    }

    @Test
    public void testGetHandsByTime() throws Exception {
        int myPlayerId = 2;
        createHandHistory(1, "hand1", 100L, 1, myPlayerId);
        createHandHistory(1, "hand2", 200L, 1, myPlayerId);
        createHandHistory(1, "hand3", 300L, myPlayerId, 3);
        createHandHistory(2, "hand4", 400L, 1, myPlayerId);

        List<HistoricHand> hands = service.getHands(1, myPlayerId, 0, 200L);
        assertEquals(2, hands.size());
        verifyOtherPlayersPrivateCardsNotExposed(myPlayerId, hands);
    }

    private void createHandHistory(int tableId, String handId, long startTime, int... playerIds) throws UnknownHostException {
        // Create a hand history.
        HistoricHand hand = new HistoricHand();
        hand.setStartTime(startTime);
        hand.setId(handId);
        Table table = new Table();
        table.setTableId(tableId);

        hand.setTable(table);
        hand.setEvents(new ArrayList<HandHistoryEvent>());

        for (int playerId : playerIds) {
            Player player = new Player(playerId, 1, new BigDecimal(400), "p" + playerId);
            hand.getSeats().add(player);
            PlayerCardsDealt pcd = new PlayerCardsDealt();
            pcd.setCards(new ArrayList<GameCard>());
            pcd.getCards().add(new GameCard(GameCard.Suit.CLUBS, GameCard.Rank.ACE));
            pcd.setPlayerId(playerId);
            hand.getEvents().add(pcd);
            PlayerCardsExposed pce = new PlayerCardsExposed(playerId);
            pce.setCards(new ArrayList<GameCard>());
            pce.getCards().add(new GameCard(GameCard.Suit.CLUBS, GameCard.Rank.ACE));
            hand.getEvents().add(pce);
        }
        table.setSeats(hand.getSeats().size());
        storage.persist(hand);
    }
}
