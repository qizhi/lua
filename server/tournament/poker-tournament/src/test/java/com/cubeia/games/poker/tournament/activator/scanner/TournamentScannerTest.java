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

package com.cubeia.games.poker.tournament.activator.scanner;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.mtt.MttFactory;
import com.cubeia.firebase.api.mtt.activator.ActivatorContext;
import com.cubeia.firebase.api.mtt.lobby.MttLobbyObject;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes;
import com.cubeia.games.poker.tournament.activator.ScheduledTournamentCreationParticipant;
import com.cubeia.games.poker.tournament.activator.SitAndGoCreationParticipant;
import com.cubeia.games.poker.tournament.activator.TournamentScanner;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentInstance;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.TournamentSchedule;
import com.cubeia.games.poker.tournament.configuration.provider.SitAndGoConfigurationProvider;
import com.cubeia.games.poker.tournament.configuration.provider.TournamentScheduleProvider;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TournamentScannerTest {

    @Mock
    private SitAndGoConfigurationProvider sitAndGoProvider;

    @Mock
    private TournamentScheduleProvider tournamentScheduleProvider;

    @Mock
    private ActivatorContext context;

    @Mock
    private SystemTime dateFetcher;

    @Mock
    private MttFactory factory;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private ShutdownServiceContract shutdownService;

    @Mock
    private CashGamesBackendService cashGamesBackendService;

    @Mock
    private TournamentHistoryPersistenceService tournamentHistoryPersistenceService;

    private TournamentScanner scanner;

    private TimeZone originalTimeZone;

    @Before
    public void setup() throws SystemException {
        initMocks(this);
        when(context.getServices()).thenReturn(serviceRegistry);
        when(serviceRegistry.getServiceInstance(ShutdownServiceContract.class)).thenReturn(shutdownService);
        when(serviceRegistry.getServiceInstance(CashGamesBackendService.class)).thenReturn(cashGamesBackendService);
        when(serviceRegistry.getServiceInstance(TournamentHistoryPersistenceService.class)).thenReturn(tournamentHistoryPersistenceService);
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
        scanner = new TournamentScanner(sitAndGoProvider, tournamentScheduleProvider, dateFetcher);
        scanner.init(context);
        scanner.setMttFactory(factory);
        
        
        when(factory.listTournamentInstances()).thenReturn(new MttLobbyObject[]{});
    }

    @After
    public void after() {
        TimeZone.setDefault(originalTimeZone);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(originalTimeZone));
    }

    @Test
    public void shouldCreateTournamentWhenAnnouncingTimeHasCome() {
        // Given a tournament that should start at 14.30 and be announced 30 minutes before.
        Date start = new DateTime(2011, 7, 5, 9, 0, 0).toDate();
        Date end = new DateTime(2013, 7, 5, 9, 0, 0).toDate();
        TournamentSchedule tournamentSchedule = new TournamentSchedule(start, end, "0 30 14 * * ?", 10, 20, 30);
        ScheduledTournamentConfiguration tournament = new ScheduledTournamentConfiguration(tournamentSchedule, "14.30", 1);
        when(tournamentScheduleProvider.getTournamentSchedule()).thenReturn(singletonList(tournament));

        // When we scan tournaments at 14.00.
        when(dateFetcher.date()).thenReturn(new DateTime(2012, 7, 5, 14, 0, 2));
        scanner.checkTournamentsNow();

        // Then we should create a tournament.
        verify(factory).createMtt(anyInt(), anyString(), isA(ScheduledTournamentCreationParticipant.class));
    }

    @Test
    public void shouldOnlyCreateOneInstancePerStartTime() {
        // Given a tournament that should start at 14.30 and be announced 30 minutes before.
        Date start = new DateTime(2011, 7, 5, 9, 0, 0).toDate();
        Date end = new DateTime(2013, 7, 5, 9, 0, 0).toDate();

        TournamentSchedule tournamentSchedule = new TournamentSchedule(start, end, "0 30 14 * * ?", 10, 20, 30);
        ScheduledTournamentConfiguration tournament = new ScheduledTournamentConfiguration(tournamentSchedule, "14.30", 1);
        when(tournamentScheduleProvider.getTournamentSchedule()).thenReturn(singletonList(tournament));

        // When we scan tournaments at 14:00.02 and 14:00.03.
        when(dateFetcher.date()).thenReturn(new DateTime(2012, 7, 5, 14, 0, 2)).thenReturn(new DateTime(2012, 7, 5, 14, 0, 3));
        scanner.checkTournamentsNow();

        // We should create one tournament.
        ArgumentCaptor<ScheduledTournamentCreationParticipant> captor = ArgumentCaptor.forClass(ScheduledTournamentCreationParticipant.class);
        verify(factory, times(1)).createMtt(anyInt(), anyString(), captor.capture());

        // And then check it again at 14.00.03 (resetting the factory so we can have it return the tournament we just created).
        reset(factory);
        ScheduledTournamentInstance instance = captor.getValue().getInstance();
        MttLobbyObject mttLobbyObject = tournamentWithNameAndIdentifier(instance.getName(), instance.getIdentifier());
        MttLobbyObject[] mttLobbyObjects = new MttLobbyObject[]{mttLobbyObject};
        when(factory.listTournamentInstances()).thenReturn(mttLobbyObjects);
        scanner.checkTournamentsNow();

        // Then we should not create any more tournaments (because the factory has been reset, the first invocation is gone).
        verify(factory, never()).createMtt(anyInt(), anyString(), isA(ScheduledTournamentCreationParticipant.class));
    }

    @Test
    public void onlyCreateOneInstancePerSitAndGo() {
        // Given that the provider supplies this configuration.
        String name = "my sit and go";
        SitAndGoConfiguration configuration = new SitAndGoConfiguration(name, 10);
        when(sitAndGoProvider.getConfigurations()).thenReturn(Collections.singleton(configuration));

        // When we check for tournaments.
        scanner.checkTournamentsNow();

        // Then a tournament should be created.
        verify(factory).createMtt(anyInt(), anyString(), isA(SitAndGoCreationParticipant.class));

        // But if we scan again, when this tournament already exists.
        reset(factory);
        MttLobbyObject mttLobbyObject = tournamentWithNameAndIdentifier(name, "");
        MttLobbyObject[] mttLobbyObjects = new MttLobbyObject[]{mttLobbyObject};
        when(factory.listTournamentInstances()).thenReturn(mttLobbyObjects);
        scanner.checkTournamentsNow();

        verify(factory, never()).createMtt(anyInt(), anyString(), isA(SitAndGoCreationParticipant.class));
    }

    @Test
    public void testFailedResurrectionShouldNotCrashServer() {
        HistoricTournament tournament = mock(HistoricTournament.class, RETURNS_DEEP_STUBS);
        when(tournamentHistoryPersistenceService.findTournamentsToResurrect()).thenReturn(singletonList(tournament));
        when(tournamentScheduleProvider.getScheduledTournamentConfiguration(anyInt())).thenThrow(new EntityNotFoundException());

        scanner.start();

        // Just checking that we are not throwing an exception. Not a best practice, but it's what we want here.
    }

    private MttLobbyObject tournamentWithNameAndIdentifier(String name, String identifier) {
        MttLobbyObject lobbyObject = mock(MttLobbyObject.class);
        Map<String, AttributeValue> map = Maps.newHashMap();
        map.put(PokerTournamentLobbyAttributes.IDENTIFIER.name(), AttributeValue.wrap(identifier));
        map.put(Enums.TournamentAttributes.NAME.name(), AttributeValue.wrap(name));
        map.put(Enums.TournamentAttributes.STATUS.name(), AttributeValue.wrap(PokerTournamentStatus.REGISTERING.name()));
        when(lobbyObject.getAttributes()).thenReturn(map);
        return lobbyObject;
    }
}
