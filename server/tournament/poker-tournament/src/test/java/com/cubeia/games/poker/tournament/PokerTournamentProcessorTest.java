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

package com.cubeia.games.poker.tournament;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.support.LobbyAttributeAccessorAdapter;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MttNotifierAdapter;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.time.DefaultSystemTime;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.activator.PokerTournamentCreationParticipant;
import com.cubeia.games.poker.tournament.activator.ScheduledTournamentCreationParticipant;
import com.cubeia.games.poker.tournament.activator.SitAndGoCreationParticipant;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentInstance;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.TournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.TournamentSchedule;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructureFactory;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructureParserTest;
import com.cubeia.games.poker.tournament.messages.PokerTournamentRoundReport;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.cubeia.games.poker.tournament.util.PacketSenderFactory;
import com.cubeia.network.users.firebase.api.UserServiceContract;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.*;

import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.OUT;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.STATUS;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.ANNOUNCED;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.REGISTERING;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests a poker tournament.
 * <p/>
 * Testing check list:
 * 1. Register a player, check that he is registered.
 * 2. Register enough players for the tournament to start, check that it starts.
 * 3. Send a round report indicating that two players are out, check that they are removed from the tournament.
 * 4. Send another round report and check that table balancing occurs.
 * 5. Check that the blinds are increased when a timeout is triggered.
 * 6. Check that the tournament finishes when there is only one player left.
 */
public class PokerTournamentProcessorTest extends TestCase {

    private static final Logger log = Logger.getLogger(PokerTournamentProcessorTest.class);

    // Class under test.
    private PokerTournamentProcessor tournamentProcessor;

    private MTTStateSupport state;

    @Mock
    private MttInstance instance;

    @Mock
    private Scheduler<MttAction> scheduler;

    @Mock
    private TournamentPlayerRegistry playerRegistry;

    @Mock
    private MttNotifier notifier;

    @Mock
    private ScheduledTournamentInstance instanceConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TournamentConfiguration configuration;

    @Mock
    private TournamentHistoryPersistenceService historyService;

    @Mock
    private LobbyAttributeAccessor tableLobbyAccessor;

    @Mock
    private CashGamesBackendService backend;

    @Mock
    private ShutdownServiceContract shutdownService;

    @Mock
    private TournamentSchedule schedule;

    @Mock
    private PacketSenderFactory senderFactory;

    @Mock
    private TournamentPlayerRegistry tournamentPlayerRegistry;

    @Mock
    private PacketSender sender;
    
    @Mock
    private UserServiceContract userService;
    
    @Mock DomainEventsService domainEventService;

    @Mock
    CashGamesBackendService cashGamesBackendService;

    private SystemTime dateFetcher = new DefaultSystemTime();

    private MockTournamentAssist support;

    private LobbyAttributeAccessor lobbyAccessor = new LobbyAttributeAccessorAdapter();

    private PokerTournamentState pokerState;

    private Random rng = new Random();

    private SystemTime systemTime = new DefaultSystemTime();

    @Override
    protected void setUp() throws Exception {
        initMocks(this);
        tournamentProcessor = new PokerTournamentProcessor();
        support = new MockTournamentAssist();
        tournamentProcessor.setSupport(support);
        tournamentProcessor.setHistoryService(historyService);
        tournamentProcessor.setBackend(backend);
        tournamentProcessor.setShutdownService(shutdownService);
        tournamentProcessor.setDateFetcher(dateFetcher);
        tournamentProcessor.setSenderFactory(senderFactory);
        tournamentProcessor.setTournamentRegistryService(tournamentPlayerRegistry);
        tournamentProcessor.setUserService(userService);
        tournamentProcessor.domainEventService = domainEventService;
        
        state = new MTTStateSupport(1, 1);
        when(configuration.getBlindsStructure()).thenReturn(BlindsStructureFactory.createDefaultBlindsStructure());
        when(instance.getSystemPlayerRegistry()).thenReturn(playerRegistry);
        when(instance.getState()).thenReturn(state);
        when(instance.getLobbyAccessor()).thenReturn(lobbyAccessor);
        when(instance.getScheduler()).thenReturn(scheduler);
        when(instance.getMttNotifier()).thenReturn(notifier);
        when(instance.getTableLobbyAccessor(anyInt())).thenReturn(tableLobbyAccessor);
        when(instanceConfig.getConfiguration()).thenReturn(configuration);
        when(configuration.getBuyIn()).thenReturn(BigDecimal.valueOf(10));
        when(configuration.getPayoutStructure()).thenReturn(PayoutStructureParserTest.createTestStructure());
        when(configuration.getCurrency()).thenReturn("EUR");
        when(senderFactory.create(Mockito.<MttNotifier>any(), Mockito.<MttInstance>any())).thenReturn(sender);
        when(cashGamesBackendService.getCurrency(anyString())).thenReturn(new Currency("EUR",2));

        support.setTableCreator(new MockTableCreator(tournamentProcessor, instance));
        support.setMttNotifier(new MttNotifierAdapter());

        SitAndGoConfiguration config = new SitAndGoConfiguration("test", 20);
        config.setId(1);
        config.getConfiguration().setBuyIn(BigDecimal.valueOf(10));
        config.getConfiguration().setFee(BigDecimal.valueOf(1));
        config.getConfiguration().setBlindsStructure(BlindsStructureFactory.createDefaultBlindsStructure());
        config.getConfiguration().setPayoutStructure(PayoutStructureParserTest.createTestStructure());
        config.getConfiguration().setStartingChips(new BigDecimal(2000));
        config.getConfiguration().setCurrency("EUR");
        PokerTournamentCreationParticipant part = new SitAndGoCreationParticipant(config, historyService, systemTime, cashGamesBackendService);
        part.tournamentCreated(state, instance.getLobbyAccessor());

        pokerState = new PokerTournamentUtil().getPokerState(instance);
    }

    public void testRegister() {
        registerPlayer(1);
    }

    public void testSitAndGo() {
        assertEquals(REGISTERING.name(), instance.getLobbyAccessor().getStringAttribute(STATUS.name()));
        assertEquals(20, state.getMinPlayers());
        fillTournament();
        assertThat(instance.getLobbyAccessor().getStringAttribute(STATUS.name()), is(PokerTournamentStatus.RUNNING.name()));
        assertEquals(2, state.getTables().size());
        assertEquals(10, state.getPlayersAtTable(0).size());
    }

    public void testPlayerOut() {
        fillTournament();
        int remaining = state.getRemainingPlayerCount();
        simulatePlayersOut(1, state.getPlayersAtTable(1).iterator().next());
        assertEquals(remaining - 1, state.getRemainingPlayerCount());
    }

    public void testBalanceTables() {
        fillTournament();
        forceBalancing();
    }

    public void testScheduledTournamentSchedulesOpeningRegistrationWhenAskedTo() {
        // Given a scheduled tournament
        prepareScheduledTournament();

        // When the tournament receives an open registration trigger.
        MttObjectAction objectAction = new MttObjectAction(instance.getId(), TournamentTrigger.OPEN_REGISTRATION);
        tournamentProcessor.process(objectAction, instance);

        // Then the registration should be opened.
        assertEquals(REGISTERING, pokerState.getStatus());
    }

    private void prepareScheduledTournament() {
        when(instanceConfig.getStartTime()).thenReturn(new DateTime());
        when(instanceConfig.getOpenRegistrationTime()).thenReturn(new DateTime());
        when(instanceConfig.getConfiguration()).thenReturn(configuration);
        when(configuration.getFee()).thenReturn(BigDecimal.ZERO);
        when(instanceConfig.getSchedule()).thenReturn(schedule);
        when(instanceConfig.getIdentifier()).thenReturn("identifier");


        PokerTournamentCreationParticipant participant = new ScheduledTournamentCreationParticipant(instanceConfig, historyService, systemTime, cashGamesBackendService);
        participant.tournamentCreated(state, instance.getLobbyAccessor());
        pokerState = new PokerTournamentUtil().getPokerState(instance);
        assertEquals(ANNOUNCED, pokerState.getStatus());
    }

    private void forceBalancing() {
        int remaining = state.getRemainingPlayerCount();
        Collection<Integer> playersAtTable = state.getPlayersAtTable(0);
        Iterator<Integer> iterator = playersAtTable.iterator();
        simulatePlayersOut(0, iterator.next(), iterator.next());
        assertEquals(remaining - 2, state.getRemainingPlayerCount());

        // Another table finishes a hand.
        int playersAtTableTwo = state.getPlayersAtTable(1).size();
        sendRoundReport(1, new PokerTournamentRoundReport(new PokerTournamentRoundReport.Level(new BigDecimal(10), new BigDecimal(20), BigDecimal.ZERO)));
        assertEquals(playersAtTableTwo - 1, state.getPlayersAtTable(1).size());
    }

    public void testStartingBalance() {
        fillTournament();
        assertEquals(new BigDecimal(2000), pokerState.getPlayerBalance(1));
    }

    public void testBalanceAfterMove() {
        fillTournament();
        support.setMttNotifier(new MttNotifier() {

            public void notifyPlayer(int playerId, MttAction action) {

            }

            public void notifyTable(int tableId, GameAction action) {
                log.debug("Received action: " + action);
                if (action instanceof SeatPlayersMttAction) {
                    SeatPlayersMttAction seat = (SeatPlayersMttAction) action;
                    assertEquals(new BigDecimal(2000), seat.getPlayers().iterator().next().getPlayerData());
                }
            }

        });
        forceBalancing();
    }

    // Note, this test can fail randomly, because it can simulate that the final x players all suddenly have zero chips (who took them? :).
    // Don't chase that goose.
    public void testStartToEnd() {
        fillTournament();

        int i = 0;
        while (pokerState.getStatus() != PokerTournamentStatus.FINISHED) {
            int randomTableId = getRandomTableId(state.getTables());

            if (randomTableId != -1) {
                sendRoundReport(randomTableId, createRoundReport(randomTableId));
            }
            if (i++ > 1000) {
                fail("Tournament should have been finished by now.");
            }
        }

        for (MttPlayer player : state.getPlayerRegistry().getPlayers()) {
            assertThat(player.getStatus(), is(OUT));
        }
    }

    private PokerTournamentRoundReport createRoundReport(int tableId) {
        PokerTournamentRoundReport report = new PokerTournamentRoundReport(new PokerTournamentRoundReport.Level(new BigDecimal(10), new BigDecimal(20), BigDecimal.ZERO));
        Collection<Integer> playersAtTable = state.getPlayersAtTable(tableId);
        int playersInTournament = state.getRemainingPlayerCount();
        log.debug("REMAINING: " + playersInTournament);

        for (Integer playerId : playersAtTable) {
            // Check so we don't kick all players out
            long randomBalance = getRandomBalance();
            if (randomBalance <= 0 && --playersInTournament == 0) {
                randomBalance = 1000; // Last player
            }
            report.setBalance(playerId, new BigDecimal(randomBalance));
        }

        return report;
    }

    private long getRandomBalance() {
        boolean out = rng.nextInt(100) < 40;
        if (out) {
            return 0;
        } else {
            return rng.nextInt(1000);
        }
    }

    private int getRandomTableId(Set<Integer> tables) {
        List<Integer> list = new ArrayList<Integer>(tables);
        if (list.size() > 0) {
            return list.get(new Random().nextInt(list.size()));
        }
        return -1;
    }

    private void simulatePlayersOut(int tableId, int... playerIds) {
        sendRoundReport(tableId, createPlayersOutRoundReport(playerIds));
    }

    private void sendRoundReport(int tableId, PokerTournamentRoundReport report) {
        MttRoundReportAction action = new MttRoundReportAction(1, tableId);
        action.setAttachment(report);
        tournamentProcessor.process(action, instance);
    }

    private PokerTournamentRoundReport createPlayersOutRoundReport(int... playerIds) {
        PokerTournamentRoundReport roundReport = new PokerTournamentRoundReport(new PokerTournamentRoundReport.Level(new BigDecimal(10), new BigDecimal(20), BigDecimal.ZERO));
        for (int playerId : playerIds) {
            roundReport.setBalance(playerId, BigDecimal.ZERO);
        }
        return roundReport;
    }

    private void fillTournament() {
        for (int i = 0; i < state.getMinPlayers(); i++) {
            registerPlayer(i);
        }
    }

    private void registerPlayer(int playerId) {
        MttPlayer player = new MttPlayer(playerId);
        MttRegistrationRequest request = new MttRegistrationRequest(player, new ArrayList<Attribute>());
        int before = state.getRegisteredPlayersCount();
        state.getPlayerRegistry().register(instance, request);
        tournamentProcessor.getPlayerListener(state).playerRegistered(instance, request);
        assertEquals(before + 1, state.getRegisteredPlayersCount());
        OpenSessionResponse response = new OpenSessionResponse(new PlayerSessionId(playerId), null);
        tournamentProcessor.process(new MttObjectAction(state.getId(), response), instance);
    }
}
