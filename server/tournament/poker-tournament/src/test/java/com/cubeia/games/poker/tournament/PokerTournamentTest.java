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
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.OpenTournamentSessionRequest;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.guice.tournament.TournamentAssist;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.time.DefaultSystemTime;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.games.poker.tournament.configuration.lifecycle.ScheduledTournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.messages.PokerTournamentRoundReport;
import com.cubeia.games.poker.tournament.rebuy.RebuySupport;
import com.cubeia.games.poker.tournament.state.PendingBackendRequests;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.cubeia.network.users.firebase.api.UserServiceContract;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

import static com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse.ErrorCode.UNSPECIFIED_ERROR;
import static com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructureFactory.createDefaultBlindsStructure;
import static com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructureParserTest.createTestStructure;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.*;
import static com.google.common.collect.ImmutableSet.of;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PokerTournamentTest {

    private PokerTournamentState pokerState;

    @Mock
    private SystemTime dateFetcher;

    @Mock
    private ShutdownServiceContract shutdownService;

    @Mock
    private MttInstance instance;

    @Mock
    private TournamentAssist support;

    @Mock
    private MTTStateSupport state;

    @Mock
    private MttNotifier notifier;

    @Mock
    private Scheduler<MttAction> scheduler;

    @Mock
    private LobbyAttributeAccessor lobbyAccessor;

    @Mock
    private CashGamesBackendService backend;

    @Mock
    private TournamentHistoryPersistenceService historyService;

    @Mock
    private TournamentLifeCycle mockLifeCycle;

    @Mock
    private MTTState mttState;

    @Mock
    private PlayerRegistry playerRegistry;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private PokerTournamentState mockPokerState;

//    @Mock
//    private TournamentLobby tournamentLobby;
    
    @Mock
    private UserServiceContract userService;

    @Mock
    private PacketSender sender;

    @Mock
    private PlayerRegistry mockRegistry;

    @Mock
    private TournamentPlayerRegistry tournamentPlayerRegistry;

    @Mock
    private PendingBackendRequests pendingRequests;
    
    @Mock DomainEventsService domainEventService;

    private PokerTournament tournament;

    private TournamentLifeCycle lifeCycle;

    @Before
    public void setup() {
        initMocks(this);
        pokerState = new PokerTournamentState();
        pokerState.setBuyIn(BigDecimal.valueOf(10));
        pokerState.setFee(BigDecimal.valueOf(1));
        pokerState.setPayoutStructure(createTestStructure(), 10);
        pokerState.setLifecycle(mockLifeCycle);
        pokerState.setCurrency(new Currency("EUR",2));
        when(instance.getScheduler()).thenReturn(scheduler);
        when(instance.getLobbyAccessor()).thenReturn(lobbyAccessor);
        when(instance.getState()).thenReturn(mttState);
        when(instance.getMttNotifier()).thenReturn(notifier);
        when(instance.getState().getCapacity()).thenReturn(10);
        when(instance.getState().getRegisteredPlayersCount()).thenReturn(0);
        when(state.getSeats()).thenReturn(10);
        when(state.getPlayerRegistry()).thenReturn(playerRegistry);
    }

    @Test
    public void registrationStartShouldBeScheduledWhenScheduledTournamentIsCreated() {
        // Given a scheduled tournament
        pokerState.setStatus(ANNOUNCED);
        prepareTournamentWithLifecycle();
        when(dateFetcher.date()).thenReturn(new DateTime(2011, 7, 5, 13, 30, 1));

        // When the tournament is created
        tournament.tournamentCreated();

        // Then registration opening should be scheduled
        ArgumentCaptor<MttAction> captor = ArgumentCaptor.forClass(MttAction.class);
        long timeToRegistrationStart = lifeCycle.getTimeToRegistrationStart(dateFetcher.date());
        verify(scheduler).scheduleAction(captor.capture(), eq(timeToRegistrationStart));
        assertEquals(TournamentTrigger.OPEN_REGISTRATION, ((MttObjectAction) captor.getValue()).getAttachment());
    }

    @Test
    public void shouldScheduleTournamentStartAfterOpeningRegistration() {
        // Given a scheduled tournament
        prepareTournamentWithLifecycle();
        when(dateFetcher.date()).thenReturn(new DateTime(2011, 7, 5, 14, 0, 1));

        // When registration is opened
        tournament.handleTrigger(TournamentTrigger.OPEN_REGISTRATION);

        // Then we should schedule tournament start
        ArgumentCaptor<MttAction> captor = ArgumentCaptor.forClass(MttAction.class);
        long timeToTournamentStart = lifeCycle.getTimeToTournamentStart(dateFetcher.date());
        verify(scheduler).scheduleAction(captor.capture(), eq(timeToTournamentStart));
        assertEquals(TournamentTrigger.START_TOURNAMENT, ((MttObjectAction) captor.getValue()).getAttachment());
    }

    @Test
    public void backendRequestShouldBeSentWhenRegistering() {
        // Given a tournament with a buy-in of 10+1.
        prepareTournamentWithLifecycle();
        pokerState.setStatus(PokerTournamentStatus.REGISTERING);
        pokerState.setTournamentSessionId(new TournamentSessionId("test"));

        // When we register a player
        tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));

        // A call to the backend should be made with 11.00.
        ArgumentCaptor<OpenTournamentSessionRequest> captor = ArgumentCaptor.forClass(OpenTournamentSessionRequest.class);
        verify(backend).openTournamentPlayerSession(captor.capture(), isA(TournamentSessionId.class));
        OpenTournamentSessionRequest request = captor.getValue();
        assertThat(request.getTournamentId().getInstanceId(), is(instance.getId()));
        assertThat(request.getOpeningBalance().getAmount(), is(new BigDecimal(11)));
    }

    @Test
    public void noBackendRequestShouldBeSentWhenRegisteringIfRegistrationIsNotOpen() {
        // Given a tournament that is not open for registration.
        prepareTournamentWithLifecycle();
        pokerState.setStatus(PokerTournamentStatus.ANNOUNCED);

        // When we register a player
        tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));

        // A call to the backend should be made with 11.00.
        verify(backend, never()).openTournamentSession(Mockito.<OpenTournamentSessionRequest>any());
    }

    @Test
    public void tournamentShouldNotStartWhenTimeHasComeIfThereArePendingRegistrations() {
        // Given a tournament with one registered (but pending) player.
        prepareTournamentWithLifecycle();
        pokerState.setStatus(PokerTournamentStatus.REGISTERING);
        tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
        assertThat(pokerState.hasPendingRegistrations(), is(true));

        // When the tournament is supposed to start.
        tournament.handleTrigger(TournamentTrigger.START_TOURNAMENT);

        // Then the tournament should not start.
        verify(support, never()).createTables(Mockito.<MTTStateSupport>any(), anyInt(), anyString(), Mockito.<Object>any());
    }
    
    @Test
    public void allowPlayerOnPrivateTournament() {
    	prepareTournamentWithMockLifecycle();
    	pokerState.getAllowedOperators().add(666L);
    	User user = mock(User.class);
    	when(user.getOperatorId()).thenReturn(666L); // Tournament public, but matching operator
    	when(userService.getUserById(anyInt())).thenReturn(user);
    	pokerState.setStatus(PokerTournamentStatus.REGISTERING);
    	MttRegisterResponse resp = tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
    	Assert.assertEquals(MttRegisterResponse.ALLOWED, resp);
    }
    
    @Test
    public void allowPlayerOnPublicTournament() {
    	prepareTournamentWithMockLifecycle();
    	User user = mock(User.class);
    	when(user.getOperatorId()).thenReturn(666L); // Tournament public, the operator ID shouldn't matter
    	when(userService.getUserById(anyInt())).thenReturn(user);
    	pokerState.setStatus(PokerTournamentStatus.REGISTERING);
    	MttRegisterResponse resp = tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
    	Assert.assertEquals(MttRegisterResponse.ALLOWED, resp);
    }
    
    @Test
    public void dontOpenNewSessionsIfTournamentIsFull() {
    	prepareTournamentWithMockLifecycle();
    	User user = mock(User.class);
    	when(user.getOperatorId()).thenReturn(666L); // Tournament public, the operator ID shouldn't matter
    	when(userService.getUserById(anyInt())).thenReturn(user);
    	when(instance.getState().getCapacity()).thenReturn(10);
    	when(instance.getState().getRegisteredPlayersCount()).thenReturn(10);
    	pokerState.setStatus(PokerTournamentStatus.REGISTERING);
    	MttRegisterResponse resp = tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
    	Assert.assertEquals(MttRegisterResponse.DENIED, resp);
    }
    
    @Test
    public void blockPlayerOnPrivateTournament() {
    	prepareTournamentWithMockLifecycle();
    	pokerState.getAllowedOperators().add(666L);
    	pokerState.getAllowedOperators().add(667L);
    	User user = mock(User.class);
    	when(user.getOperatorId()).thenReturn(1L); // Tournament public, but wrong operator, should deny
    	when(userService.getUserById(anyInt())).thenReturn(user);
    	pokerState.setStatus(PokerTournamentStatus.REGISTERING);
    	MttRegisterResponse resp = tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
    	Assert.assertEquals(MttRegisterResponse.DENIED, resp);
    }
    
    @Test
    public void tournamentShouldStartOnceAllPendingRegistrationsAreResolved() {
        // Given a tournament with one registered (but pending) player and that it's time to start the tournament.
        prepareTournamentWithMockLifecycle();
        pokerState.setStatus(PokerTournamentStatus.REGISTERING);
        MttPlayer tournamentPlayer = new MttPlayer(1);
        tournament.checkRegistration(new MttRegistrationRequest(tournamentPlayer, null));
        assertThat(pokerState.hasPendingRegistrations(), is(true));
        tournament.handleTrigger(TournamentTrigger.START_TOURNAMENT);
        when(mockLifeCycle.shouldStartTournament(Mockito.<DateTime>any(), anyInt(), anyInt())).thenReturn(true);
        when(state.getPlayerRegistry()).thenReturn(mockRegistry);
        when(mockRegistry.getPlayers()).thenReturn(Collections.singleton(tournamentPlayer));

        // When the registration is resolved.
        tournament.handleOpenSessionResponse(new OpenSessionResponse(new PlayerSessionId(1, "1"), null));

        // Then the tournament should start.
        verify(support).createTables(Mockito.<MTTStateSupport>any(), anyInt(), anyString(), Mockito.<Object>any());
    }
    
    @Test
    public void tournamentShouldBeCancelledOnceAllPendingRegistrationsAreResolvedAndThereAreNotEnoughPlayers() {
        // Given a tournament with one registered (but pending) player and that it's time to start the tournament.
        prepareTournamentWithMockLifecycle();
        pokerState.setStatus(PokerTournamentStatus.REGISTERING);
        tournament.checkRegistration(new MttRegistrationRequest(new MttPlayer(1), null));
        assertThat(pokerState.hasPendingRegistrations(), is(true));
        tournament.handleTrigger(TournamentTrigger.START_TOURNAMENT);
        when(mockLifeCycle.shouldStartTournament(Mockito.<DateTime>any(), anyInt(), anyInt())).thenReturn(false);
        when(mockLifeCycle.shouldCancelTournament(Mockito.<DateTime>any(), anyInt(), anyInt())).thenReturn(true);

        // When the registration is resolved.
        tournament.handleOpenSessionResponseFailed(new OpenSessionFailedResponse(UNSPECIFIED_ERROR, "error", 1));

        // Then the player should be removed and the tournament cancelled.
        verify(playerRegistry).removePlayer(1);
        assertThat(pokerState.getStatus(), is(CANCELLED));
    }

    @Test
    public void tournamentShouldScheduleTimeoutWhenBreakStarts() {
        // Given a tournament that is supposed to go on break.
        when(mockPokerState.getStatus()).thenReturn(RUNNING);
        when(mockPokerState.getRebuySupport()).thenReturn(RebuySupport.NO_REBUYS);
        when(mockPokerState.isOnBreak()).thenReturn(true);
        when(mockPokerState.getCurrentBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, true));
        when(mockPokerState.getNextLevelStartTime()).thenReturn(new DateTime());
        prepareTournamentWithMockTournamentState();

        // And that there is one table (so that the balancing doesn't crash)
        when(state.getPlayersAtTable(1)).thenReturn(of(1));
        when(state.getTables()).thenReturn(of(1));

        // When the last table finishes its hand (and all tables are ready)
        when(mockPokerState.allTablesReadyForBreak()).thenReturn(true);
        sendRoundReportToTournament(1);

        // Then the break should start and we should schedule a timeout to end the break.
        verify(scheduler).scheduleAction(Mockito.<MttObjectAction>any(), anyLong());
    }

    private void sendRoundReportToTournament(int tableId) {
        MttRoundReportAction action = mock(MttRoundReportAction.class);
        PokerTournamentRoundReport report = mock(PokerTournamentRoundReport.class);
        PokerTournamentRoundReport.Level level = mock(PokerTournamentRoundReport.Level.class);
        when(level.getBigBlindAmount()).thenReturn(BigDecimal.ZERO);
        when(level.getSmallBlindAmount()).thenReturn(BigDecimal.ZERO);
        when(level.getAnteAmount()).thenReturn(BigDecimal.ZERO);

        when(action.getTableId()).thenReturn(tableId);
        when(action.getAttachment()).thenReturn(report);
        when(report.getCurrentBlindsLevel()).thenReturn(level);
        tournament.processRoundReport(action);
    }

    @Test
    public void tournamentShouldStartAgainAfterBreakEnds() {
        // Given a tournament that is on break.
        when(mockPokerState.getRebuySupport()).thenReturn(RebuySupport.NO_REBUYS);
        when(mockPokerState.isOnBreak()).thenReturn(true);
        when(mockPokerState.getCurrentBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, true));
        when(mockPokerState.increaseBlindsLevel()).thenReturn(new Level(new BigDecimal(40), new BigDecimal(80), BigDecimal.ZERO, 5, false));
        prepareTournamentWithMockTournamentState();

        // When the break ends.
        tournament.handleTrigger(TournamentTrigger.INCREASE_LEVEL);

        // Then round start should be sent.
        verify(support).sendRoundStartActionToTables(state, state.getTables());

        // And break finished should be called.
        verify(mockPokerState).breakFinished();
    }

    @Test
    public void tournamentShouldGoOnBreakWhenOneTableIsWaitingForPlayersAndTheOtherTableIsFinished() {
        // Given a tournament where the current level is not a break, but the next level is a break.
        when(mockPokerState.getRebuySupport()).thenReturn(RebuySupport.NO_REBUYS);
        when(mockPokerState.isOnBreak()).thenReturn(true);
        when(mockPokerState.getCurrentBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, false));
        when(mockPokerState.increaseBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, true));
        when(mockPokerState.getNextLevelStartTime()).thenReturn(new DateTime());
        when(state.getTables()).thenReturn(of(1, 2));
        when(state.getPlayersAtTable(1)).thenReturn(of(1));
        when(state.getPlayersAtTable(2)).thenReturn(of(2, 3));
        prepareTournamentWithMockTournamentState();

        // Where one table finishes a round, and has only one player left at the table.
        sendRoundReportToTournament(1);

        // And then it's time for the break to start.
        when(mockPokerState.isOnBreak()).thenReturn(true);

        // And then the other table finishes its hand. (at this point the allTablesReadyForBreak must return true)
        when(mockPokerState.allTablesReadyForBreak()).thenReturn(true);
        sendRoundReportToTournament(2);

        // Then we should include the waiting table when asking if all tables are ready for the break.
        verify(mockPokerState, times(2)).allTablesReadyForBreak();

        // Then the break should start.
        verify(scheduler).scheduleAction(Mockito.<MttObjectAction>any(), anyLong());
    }

    @Test
    public void doNotSendRoundStartToTableWhenBreakShouldStart() {
        // Given a tournament that is supposed to go on a break.
        prepareTournamentWithMockTournamentState();
        when(mockPokerState.isOnBreak()).thenReturn(true);
        when(mockPokerState.getCurrentBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, true));
        when(state.getTables()).thenReturn(of(1));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));

        // When a round report is sent.
        sendRoundReportToTournament(1);

        // Then we should not start a new round.
        verify(support, never()).sendRoundStartActionToTables(Mockito.<MTTStateSupport>any(), Mockito.<Collection<Integer>>any());
    }

    @Test
    public void startRoundAtWaitingTableIfPlayerIsMovedThere() {
        // Given a tournament where one table only has one player.
        when(mockPokerState.getPendingRequests()).thenReturn(pendingRequests);
        when(mockPokerState.getRebuySupport()).thenReturn(RebuySupport.NO_REBUYS);
        when(state.getTables()).thenReturn(of(1, 2));
        when(state.getPlayersAtTable(1)).thenReturn(of(1));
        when(state.getPlayersAtTable(2)).thenReturn(of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        prepareTournamentWithMockTournamentState();

        // When a round report is sent from a table with >1 players still in.
        sendRoundReportToTournament(2);

        // Then a round start should be sent to the first table.
        verify(support).sendRoundStartActionToTables(state, of(1));
    }

    @Test
    public void doNotStartRoundAtWaitingTableIfBreakIsStarting() {
        // Given a tournament where one table only has one player.
        prepareTournamentWithMockTournamentState();
        when(state.getTables()).thenReturn(of(1, 2));
        when(mockPokerState.isOnBreak()).thenReturn(true);
        when(state.getPlayersAtTable(1)).thenReturn(of(1));
        when(state.getPlayersAtTable(2)).thenReturn(of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

        // When a round report is sent from a table with >1 players still in.
        sendRoundReportToTournament(2);

        // Then a round start should be sent to the first table.
        verify(support, never()).sendRoundStartActionToTables(state, of(1));
    }

    @Test
    public void testNumberOfTablesToCreate() {
        prepareTournamentWithMockTournamentState();
        when(state.getRegisteredPlayersCount()).thenReturn(75);
        when(state.getSeats()).thenReturn(10);

        // We need 8 tables.
        assertThat(tournament.numberOfTablesToCreate(), is(8));
    }

    @Test
    public void testSetNextStartTimeCalledBeforeGettingNextStartTimeWhenBreakIsFinished() {
        // If the call order is reversed, client will get an incorrect time to next level.
        when(mockPokerState.getRebuySupport()).thenReturn(RebuySupport.NO_REBUYS);
        when(mockPokerState.isOnBreak()).thenReturn(false);
        when(mockPokerState.getCurrentBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, true));
        when(mockPokerState.increaseBlindsLevel()).thenReturn(new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 5, false));
        when(mockPokerState.getNextLevelStartTime()).thenReturn(new DateTime(5000));
        when(state.getTables()).thenReturn(Collections.singleton(1));
        prepareTournamentWithMockTournamentState();

        tournament.increaseBlindsLevel();

        InOrder inOrder = inOrder(mockPokerState);
        inOrder.verify(mockPokerState).setNextLevelStartTime(isA(DateTime.class));
        inOrder.verify(mockPokerState).getNextLevelStartTime();
    }

    @Test
    public void testBreakWithFourTables() {
        // Given a tournament where the break should start.
        BlindsStructure blindsStructure = createDefaultBlindsStructure();
        blindsStructure.insertLevel(1, new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 1, true));
        pokerState.setBlindsStructure(blindsStructure);
        pokerState.increaseBlindsLevel();
        when(state.getTables()).thenReturn(of(1, 2, 3, 4));
        when(state.getPlayersAtTable(1)).thenReturn(of(1,2,3));
        when(state.getPlayersAtTable(2)).thenReturn(of(4,5,6));
        when(state.getPlayersAtTable(3)).thenReturn(of(7,8,9));
        when(state.getPlayersAtTable(4)).thenReturn(of(10,11,12));

        prepareTournament();


        // When each table sends its round report.
        pokerState.setStatus(RUNNING);
        sendRoundReportToTournament(1);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));
        sendRoundReportToTournament(2);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));
        sendRoundReportToTournament(3);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));

        // The break should start after the last table sends the report.
        sendRoundReportToTournament(4);
        assertThat(pokerState.getStatus(), is(ON_BREAK));
    }

    @Test
    public void testBreakShouldOnlyStartOnceWhenThereIsARebuyJustWhenBreakStarts() {
        // Given a tournament where the current level is not a break, but the next level is a break.
        BlindsStructure blindsStructure = createDefaultBlindsStructure();
        blindsStructure.insertLevel(1, new Level(new BigDecimal(20),new BigDecimal(40), BigDecimal.ZERO, 1, true));
        pokerState.setBlindsStructure(blindsStructure);
        pokerState.increaseBlindsLevel();
        when(state.getTables()).thenReturn(of(1, 2, 3, 4));
        when(state.getPlayersAtTable(1)).thenReturn(of(1,2,3));
        when(state.getPlayersAtTable(2)).thenReturn(of(4,5,6));
        when(state.getPlayersAtTable(3)).thenReturn(of(7,8,9));
        when(state.getPlayersAtTable(4)).thenReturn(of(10,11,12));
        prepareTournament();

        pokerState.setStatus(RUNNING);
        sendRoundReportToTournament(1);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));
        sendRoundReportToTournament(2);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));
        sendRoundReportToTournament(3);
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));
        sendRoundReportToTournament(4);
        assertThat(pokerState.getStatus(), is(ON_BREAK));
    }

    private TournamentLifeCycle prepareTournament() {
        DateTime startTime = new DateTime(2011, 7, 5, 14, 30, 0);
        DateTime openRegistrationTime = new DateTime(2011, 7, 5, 14, 0, 0);
        lifeCycle = new ScheduledTournamentLifeCycle(startTime, openRegistrationTime);
        pokerState.setLifecycle(lifeCycle);
        tournament = new PokerTournament(pokerState);
        tournament.injectTransientDependencies(instance, support, state, historyService, backend, new DefaultSystemTime(), shutdownService, tournamentPlayerRegistry, sender, userService, domainEventService);
        return lifeCycle;
    }

    private TournamentLifeCycle prepareTournamentWithLifecycle() {
        DateTime startTime = new DateTime(2011, 7, 5, 14, 30, 0);
        DateTime openRegistrationTime = new DateTime(2011, 7, 5, 14, 0, 0);
        lifeCycle = new ScheduledTournamentLifeCycle(startTime, openRegistrationTime);
        pokerState.setLifecycle(lifeCycle);
        tournament = new PokerTournament(pokerState);
        tournament.injectTransientDependencies(instance, support, state, historyService, backend, dateFetcher, shutdownService, tournamentPlayerRegistry, sender, userService, domainEventService);
        return lifeCycle;
    }

    private void prepareTournamentWithMockLifecycle() {
        pokerState.setLifecycle(mockLifeCycle);
        tournament = new PokerTournament(pokerState);
        tournament.injectTransientDependencies(instance, support, state, historyService, backend, dateFetcher, shutdownService, tournamentPlayerRegistry, sender, userService, domainEventService);
        pokerState.setBlindsStructure(createDefaultBlindsStructure());
    }

    private void prepareTournamentWithMockTournamentState() {
        tournament = new PokerTournament(mockPokerState);
        tournament.injectTransientDependencies(instance, support, state, historyService, backend, new DefaultSystemTime(), shutdownService,
                tournamentPlayerRegistry, sender, userService, domainEventService);
        pokerState.setBlindsStructure(createDefaultBlindsStructure());
    }
}
