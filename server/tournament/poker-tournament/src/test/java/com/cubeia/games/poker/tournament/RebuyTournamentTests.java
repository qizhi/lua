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

package com.cubeia.games.poker.tournament;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.cashgame.dto.BalanceUpdate;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.guice.tournament.TournamentAssist;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.common.time.DefaultSystemTime;
import com.cubeia.games.poker.io.protocol.TournamentOut;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructureFactory;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.games.poker.tournament.configuration.lifecycle.ScheduledTournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructureParser;
import com.cubeia.games.poker.tournament.messages.AddOnRequest;
import com.cubeia.games.poker.tournament.messages.PokerTournamentRoundReport;
import com.cubeia.games.poker.tournament.messages.RebuyResponse;
import com.cubeia.games.poker.tournament.messages.RebuyTimeout;
import com.cubeia.games.poker.tournament.rebuy.RebuySupport;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.cubeia.network.users.firebase.api.UserServiceContract;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.ON_BREAK;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.PREPARING_BREAK;
import static com.cubeia.games.poker.tournament.status.PokerTournamentStatus.RUNNING;
import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RebuyTournamentTests {
    private static final Logger log = Logger.getLogger(RebuyTournamentTests.class);
    private PokerTournamentState pokerState;
    private PokerTournament tournament;
    private BlindsStructure blindsStructure = BlindsStructureFactory.createDefaultBlindsStructure();
    @Mock(answer = RETURNS_DEEP_STUBS)
    private MttInstance instance;
    @Mock
    private TournamentAssist support;
    @Mock
    private MTTStateSupport state;
    @Mock
    private TournamentHistoryPersistenceService historyService;
    @Mock
    private CashGamesBackendService backend;
    @Mock
    private ShutdownServiceContract shutdownService;
    @Mock
    private TournamentPlayerRegistry tournamentPlayerRegistry;
    @Mock
    private PacketSender sender;
    @Mock
    private PlayerRegistry registry;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private UserServiceContract userService;
    @Mock DomainEventsService domainEventService;
    @Captor
    private ArgumentCaptor<MttObjectAction> actionCaptor;
    private RebuySupport rebuySupport;

    @Before
    public void setup() {
        initMocks(this);
        when(state.getPlayerRegistry()).thenReturn(registry);
        pokerState = new PokerTournamentState();
        pokerState.setStatus(RUNNING);
        pokerState.setBuyIn(BigDecimal.valueOf(10));
        pokerState.setCurrency(new Currency("EUR",2));
        InputStream resourceAsStream = PayoutStructure.class.getResourceAsStream("simple.csv");
        PayoutStructure payouts = new PayoutStructureParser().parsePayouts(resourceAsStream);
        pokerState.setPayoutStructure(payouts, 2);
        BigDecimal thousand = new BigDecimal(1000);
        rebuySupport = new RebuySupport(true, thousand, thousand, 1000, thousand, true, 3, BigDecimal.valueOf(10), BigDecimal.valueOf(10));
    }

    @Test
    public void doNotStarNewHandWhenRebuysHaveBeenRequested() {
        // Given a tournament
        when(state.getTables()).thenReturn(of(1));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));
        prepareTournament();

        // When one player is out
        sendRoundReportToTournament(1, 1);

        // Then we should schedule a rebuy timeout (as opposed to starting the next hand)
        verify(instance.getScheduler()).scheduleAction(actionCaptor.capture(), anyLong());
        assertThat(actionCaptor.getValue().getAttachment() instanceof RebuyTimeout, is(true));
    }

    @Test
    public void doNotStartBreakUntilRebuysAreFinished() {
        // Given a tournament that is waiting for a rebuy and about to go on a break.
        when(state.getTables()).thenReturn(of(1, 2));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));
        prepareTournament();
        blindsStructure.insertLevel(1, new Level(new BigDecimal(80), new BigDecimal(120), BigDecimal.ZERO, 2, true));
        pokerState.increaseBlindsLevel();
        assertThat(pokerState.isOnBreak(), is(true));
        sendRoundReportToTournament(1, 1);

        log.debug("When another table finishes a hand.");
        sendRoundReportToTournament(2);

        log.debug("The break should not have started.");
        assertThat(pokerState.getStatus(), is(PREPARING_BREAK));

        log.debug("But when the rebuy is finished.");
        tournament.handleRebuyResponse(new RebuyResponse(1,1, BigDecimal.ZERO, true));
        tournament.handleReservationResponse(createReserveResponse(1));

        log.debug("The break starts.");
        assertThat(pokerState.getStatus(), is(ON_BREAK));
    }

    @Test
    public void spontaneousRebuyShouldNotTriggerHandStart() {
        // Given a tournament.
        when(state.getTables()).thenReturn(of(1));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));
        prepareTournament();

        // When someone performs a spontaneous rebuy.
        tournament.handleRebuyResponse(new RebuyResponse(1, 2, new BigDecimal(50), true));
        tournament.handleReservationResponse(createReserveResponse(2));

        // We should not start a new hand.
        verify(support, never()).sendRoundStartActionToTables(isA(MTTStateSupport.class), anyListOf(Integer.class));
    }

    @Test
    public void doNotStartNewHandWhenRebuyTimeoutOccursIfThereAreOutstandingRequests() {
        // Given a tournament where one player is out and we are waiting for a backend response.
        when(state.getTables()).thenReturn(of(1));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));
        prepareTournament();
        sendRoundReportToTournament(1, 1);
        tournament.handleRebuyResponse(new RebuyResponse(1, 1, BigDecimal.ZERO, true));

        // When the rebuy timeout occurs.
        tournament.handleRebuyTimeout(1);

        // We should not start a new hand.
        verify(support, never()).sendRoundStartActionToTables(isA(MTTStateSupport.class), anyListOf(Integer.class));

        // But when the backend response comes.
        tournament.handleReservationResponse(createReserveResponse(1));

        // We should start the hand.
        verify(support).sendRoundStartActionToTables(isA(MTTStateSupport.class), anyListOf(Integer.class));
    }

    @Test
    public void removePendingRebuyPlayersOnTimeout() {
        // Given a tournament where one player is out and we are waiting for a backend response.
        when(state.getTables()).thenReturn(of(1));
        when(state.getPlayersAtTable(1)).thenReturn(of(1, 2, 3));
        when(registry.getPlayers()).thenReturn(of(new MttPlayer(1)));
        prepareTournament();
        sendRoundReportToTournament(1, 1);

        // When the rebuy timeout occurs.
        tournament.handleRebuyTimeout(1);

        // We should remove player 1 and start a new hand.
        verify(sender).sendPacketToPlayer(isA(TournamentOut.class), eq(1));
        verify(support).sendRoundStartActionToTables(isA(MTTStateSupport.class), anyListOf(Integer.class));
    }

    @Test
    public void performingRebuyShouldIncreasePrizeMoney() {
        // Given a rebuy tournament with two registered players (and there money added to the prize pool).
        prepareTournament();
        when(state.getRegisteredPlayersCount()).thenReturn(2);
        when(state.getMinPlayers()).thenReturn(2);
        pokerState.addBuyInToPrizePool();
        pokerState.addBuyInToPrizePool();
        BigDecimal prizePoolBeforeRebuy = pokerState.getPrizePool();
        BigDecimal firstPrize = pokerState.getPayouts().getPayoutsForPosition(1);

        // When someone performs a rebuy.
        tournament.handleRebuyResponse(new RebuyResponse(1, 2, new BigDecimal(50), true));
        tournament.handleReservationResponse(createReserveResponse(2));

        // The prize pool should be updated.
        BigDecimal prizePoolAfterRebuy = pokerState.getPrizePool();
        assertThat(prizePoolAfterRebuy.compareTo(prizePoolBeforeRebuy) > 0, is(true));
        assertThat(pokerState.getPayouts().getPayoutsForPosition(1).compareTo(firstPrize) > 0, is(true));
    }

    @Test
    public void performingAddOnShouldIncreasePrizeMoney() {
        // Given a rebuy tournament with two registered players (and there money added to the prize pool).
        prepareTournament();
        rebuySupport.startAddOnPeriod();
        when(state.getRegisteredPlayersCount()).thenReturn(2);
        when(state.getMinPlayers()).thenReturn(2);
        pokerState.addBuyInToPrizePool();
        pokerState.addBuyInToPrizePool();
        BigDecimal prizePoolBeforeRebuy = pokerState.getPrizePool();
        BigDecimal firstPrize = pokerState.getPayouts().getPayoutsForPosition(1);

        // When someone performs a rebuy.
        tournament.handleAddOnRequest(new AddOnRequest(1, 1));
        tournament.handleReservationResponse(createReserveResponse(1));

        // The prize pool should be updated.
        BigDecimal prizePoolAfterRebuy = pokerState.getPrizePool();
        assertThat(prizePoolAfterRebuy.compareTo(prizePoolBeforeRebuy) > 0, is(true));
        assertThat(pokerState.getPayouts().getPayoutsForPosition(1).compareTo(firstPrize) > 0, is(true));
    }

    private ReserveResponse createReserveResponse(int playerId) {
        Money money = new Money(BigDecimal.ONE, new Currency("EUR", 2));
        BalanceUpdate balanceUpdate = new BalanceUpdate(new PlayerSessionId(playerId, "1"), money, 1);
        return new ReserveResponse(balanceUpdate, money);
    }

    private void sendRoundReportToTournament(int tableId, int ... playersOut) {
        MttRoundReportAction action = mock(MttRoundReportAction.class);
        Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();
        for (int playerOut : playersOut) {
            balances.put(playerOut, BigDecimal.ZERO);
        }
        PokerTournamentRoundReport.Level level = new PokerTournamentRoundReport.Level(BigDecimal.TEN, blindsStructure.getBlindsLevel(0).getBigBlindAmount(), BigDecimal.ZERO);
        PokerTournamentRoundReport report = new PokerTournamentRoundReport(balances, level);

        when(action.getTableId()).thenReturn(tableId);
        when(action.getAttachment()).thenReturn(report);
        tournament.processRoundReport(action);
    }

    private TournamentLifeCycle prepareTournament() {
        DateTime startTime = new DateTime(2011, 7, 5, 14, 30, 0);
        DateTime openRegistrationTime = new DateTime(2011, 7, 5, 14, 0, 0);
        ScheduledTournamentLifeCycle lifeCycle = new ScheduledTournamentLifeCycle(startTime, openRegistrationTime);
        pokerState.setLifecycle(lifeCycle);
        pokerState.setRebuySupport(rebuySupport);
        pokerState.setBlindsStructure(blindsStructure);
        pokerState.setTournamentSessionId(new TournamentSessionId("4"));
        tournament = new PokerTournament(pokerState);
        tournament.injectTransientDependencies(instance, support, state, historyService, backend, new DefaultSystemTime(), shutdownService,
                tournamentPlayerRegistry, sender, userService, domainEventService);
        return lifeCycle;
    }

}
