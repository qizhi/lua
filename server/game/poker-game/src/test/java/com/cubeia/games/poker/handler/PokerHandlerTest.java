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

package com.cubeia.games.poker.handler;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.ReserveRequest;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.io.protocol.BuyInInfoRequest;
import com.cubeia.games.poker.io.protocol.BuyInRequest;
import com.cubeia.games.poker.io.protocol.BuyInResponse;
import com.cubeia.games.poker.io.protocol.Enums.BuyInResultCode;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.PlayerAction;
import com.cubeia.games.poker.io.protocol.PlayerSitinRequest;
import com.cubeia.games.poker.io.protocol.PlayerSitoutRequest;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.logic.TimeoutCache;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.settings.PokerSettings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.math.BigDecimal;

import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PokerHandlerTest {

    @Mock
    private PokerState state;
    @Mock
    private Table table;
    @Mock
    private GameNotifier notifier;
    @Mock
    private PokerPlayerImpl pokerPlayer;
    @Mock
    private CashGamesBackendService backend;
    // @Mock
    // private FirebaseCallbackFactory callbackFactory;
    @Mock
    private TimeoutCache timeoutCache;
    private PokerHandler pokerHandler;
    private int playerId = 1337;

    @Mock
    private PokerSettings settings;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        pokerHandler = new PokerHandler();
        pokerHandler.setPlayerId(playerId);
        pokerHandler.state = state;
        pokerHandler.table = table;
        pokerHandler.cashGameBackend = backend;
        pokerHandler.timeoutCache = timeoutCache;

        pokerHandler.actionTransformer = new ActionTransformer();

        FirebaseState state = Mockito.mock(FirebaseState.class);

        when(pokerPlayer.getId()).thenReturn(playerId);
        when(pokerHandler.state.getAdapterState()).thenReturn(state);
        when(pokerHandler.table.getNotifier()).thenReturn(notifier);
        when(pokerHandler.state.getSettings()).thenReturn(settings);
        when(settings.getCurrency()).thenReturn(new Currency("EUR",2));
        when(pokerHandler.state.getPokerPlayer(playerId)).thenReturn(pokerPlayer);
        when(pokerHandler.state.getMaxBuyIn()).thenReturn(new BigDecimal(60));
        when(pokerHandler.state.getMinBuyIn()).thenReturn(new BigDecimal(10));
        // when(backend.getCallbackFactory()).thenReturn(callbackFactory);
    }

    @Test
    public void testVisitPerformAction() {
        PerformAction performAction = new PerformAction();
        performAction.seq = 10;
        performAction.betAmount = "3434.00";
        performAction.action = new PlayerAction();

        FirebaseState adapterState = mock(FirebaseState.class);
        when(state.getAdapterState()).thenReturn(adapterState);
        when(adapterState.getCurrentRequestSequence()).thenReturn(performAction.seq);

        pokerHandler.visit(performAction);

        ArgumentCaptor<PokerAction> captor = ArgumentCaptor.forClass(PokerAction.class);
        verify(state).act(captor.capture());
        PokerAction pokerAction = captor.getValue();
        assertThat(pokerAction.getBetAmount().toPlainString(), is(performAction.betAmount));
    }

    @Test
    public void testVisitPerformActionTooManyDecimalpointsRoundDown() {
        PerformAction performAction = new PerformAction();
        performAction.seq = 10;
        performAction.betAmount = "3434.25302";
        performAction.action = new PlayerAction();

        FirebaseState adapterState = mock(FirebaseState.class);
        when(state.getAdapterState()).thenReturn(adapterState);
        when(adapterState.getCurrentRequestSequence()).thenReturn(performAction.seq);

        pokerHandler.visit(performAction);

        ArgumentCaptor<PokerAction> captor = ArgumentCaptor.forClass(PokerAction.class);
        verify(state).act(captor.capture());
        PokerAction pokerAction = captor.getValue();
        assertThat(pokerAction.getBetAmount().toPlainString(), is("3434.25"));
    }

    @Test
    public void testVisitPerformActionDecimalPointsAdded() {
        PerformAction performAction = new PerformAction();
        performAction.seq = 10;
        performAction.betAmount = "3434";
        performAction.action = new PlayerAction();

        FirebaseState adapterState = mock(FirebaseState.class);
        when(state.getAdapterState()).thenReturn(adapterState);
        when(adapterState.getCurrentRequestSequence()).thenReturn(performAction.seq);

        pokerHandler.visit(performAction);

        ArgumentCaptor<PokerAction> captor = ArgumentCaptor.forClass(PokerAction.class);
        verify(state).act(captor.capture());
        PokerAction pokerAction = captor.getValue();
        assertThat(pokerAction.getBetAmount().toPlainString(), is("3434.00"));
    }

    @Test
    public void testVisitPlayerSitinRequest() {
        PlayerSitinRequest sitInRequest = new PlayerSitinRequest();
        pokerHandler.visit(sitInRequest);
        verify(pokerHandler.state).playerIsSittingIn(playerId);
    }

    @Test
    public void testVisitPlayerSitoutRequest() {
        PlayerSitoutRequest sitOutRequest = new PlayerSitoutRequest();
        pokerHandler.visit(sitOutRequest);
        verify(pokerHandler.state).playerSitsOutNextHand(playerId);
    }

    @Test
    public void testVisitBuyInInfoRequest() throws IOException {

        BuyInInfoRequest packet = new BuyInInfoRequest();
        pokerHandler.visit(packet);

        verify(state).notifyBuyinInfo(playerId, false);
    }

    @Test
    public void testVisitBuyInRequest() throws IOException {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId, null);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);

        BigDecimal balance = new BigDecimal("0.34");
        when(pokerPlayer.getBalance()).thenReturn(balance);
        BigDecimal pending = new BigDecimal("0.44");
        when(pokerPlayer.getPendingBalanceSum()).thenReturn(pending);
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(BigDecimal.ZERO);

        String buyInAmountString = "40.00";
        BigDecimal buyInAmount = new BigDecimal("40.00");
        BuyInRequest buyInRequest = new BuyInRequest(buyInAmountString, true);
        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        verify(backend, never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(state).handleBuyInRequest(pokerPlayer, buyInAmount);
        verify(pokerPlayer).setSitInAfterSuccessfulBuyIn(true);
        verify(state).playerIsSittingIn(playerId);

        ArgumentCaptor<GameDataAction> captor = ArgumentCaptor.forClass(GameDataAction.class);
        verify(notifier).sendToClient(Mockito.eq(playerId), captor.capture());
        GameDataAction gameDataAction = captor.getValue();

        StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());
        BuyInResponse buyInResponse = (BuyInResponse) styx.unpack(gameDataAction.getData());
        assertThat(buyInResponse.amountBroughtIn, is("0"));
        assertThat(buyInResponse.balance, is(format(balance)));
        assertThat(buyInResponse.pendingBalance, is(format(pending)));
        assertThat(buyInResponse.resultCode, is(BuyInResultCode.PENDING));
    }


    @Test
    public void testVisitBuyInRequestScaleDown() throws IOException {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId, null);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);

        BigDecimal balance = new BigDecimal("0.34");
        when(pokerPlayer.getBalance()).thenReturn(balance);
        BigDecimal pending = new BigDecimal("0.44");
        when(pokerPlayer.getPendingBalanceSum()).thenReturn(pending);
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(BigDecimal.ZERO);

        String buyInAmountString = "40.23342";
        BigDecimal buyInAmount = new BigDecimal("40.23");
        BuyInRequest buyInRequest = new BuyInRequest(buyInAmountString, true);
        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        verify(backend, never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(state).handleBuyInRequest(pokerPlayer, buyInAmount);

    }


    @Test
    public void testVisitBuyInRequestAmountTooHigh() {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);
        when(pokerPlayer.getBalance()).thenReturn(BigDecimal.ZERO);
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(BigDecimal.ZERO);

        // Request more money than max buy in
        BuyInRequest buyInRequest = new BuyInRequest("140", true);

        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        // since amount is higher than max allowed we should never get a call to the backend
        verify(backend, never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(pokerPlayer, never()).addRequestedBuyInAmount(Mockito.any(BigDecimal.class));
        verify(state, never()).playerIsSittingIn(playerId);
        // verify(reserveCallback).requestFailed(Mockito.any(ReserveFailedResponse.class));
    }

    @Test
    public void testVisitBuyInRequestAmountTooLow() {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);
        when(pokerPlayer.getBalance()).thenReturn(BigDecimal.ZERO);
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(BigDecimal.ZERO);

        // Request more money than max buy in
        BuyInRequest buyInRequest = new BuyInRequest("61", true);

        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        // since amount is higher than max allowed we should never get a call to the backend
        verify(backend, Mockito.never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(pokerPlayer, never()).addRequestedBuyInAmount(Mockito.any(BigDecimal.class));
        verify(state, never()).playerIsSittingIn(playerId);
        // verify(reserveCallback).requestFailed(Mockito.any(ReserveFailedResponse.class));
    }

    @Test
    public void testVisitBuyInRequestAmountTooHighForCurrentBalance() {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);
        when(pokerPlayer.getBalance()).thenReturn(new BigDecimal(4000));
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(BigDecimal.ZERO);

        // Request more money than allowed, balance + buyin <= max buyin
        BuyInRequest buyInRequest = new BuyInRequest("3000", true);

        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        // since amount is higher than max allowed we should never get a call to the backend
        verify(backend, Mockito.never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(pokerPlayer, never()).addRequestedBuyInAmount(Mockito.any(BigDecimal.class));
        verify(state, never()).playerIsSittingIn(playerId);
        // verify(reserveCallback).requestFailed(Mockito.any(ReserveFailedResponse.class));
    }

    @Test
    public void testVisitBuyInRequestAmountTooHighForCurrentBalanceIncludingPendingBalance() {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);
        when(pokerPlayer.getBalance()).thenReturn(new BigDecimal(2000)); // balance is ok
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(new BigDecimal(4000)); // pending will make it fail

        // Request more money than allowed, pendingBalance + balance + buyin <= max buyin
        BuyInRequest buyInRequest = new BuyInRequest("30", true);

        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        // since amount is higher than max allowed we should never get a call to the backend
        verify(backend, Mockito.never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(pokerPlayer, never()).addRequestedBuyInAmount(Mockito.any(BigDecimal.class));
        verify(state, never()).playerIsSittingIn(playerId);
        // verify(reserveCallback).requestFailed(Mockito.any(ReserveFailedResponse.class));
    }

    @Test
    public void testVisitBuyInRequestAmountTooHighForCurrentBalanceIncludingPendingBalanceButJustSlightly() {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        when(pokerPlayer.getPlayerSessionId()).thenReturn(playerSessionId);
        when(pokerPlayer.getBalance()).thenReturn(new BigDecimal(30)); // balance is ok
        when(pokerPlayer.getBalanceNotInHand()).thenReturn(new BigDecimal(20)); // pending will make it fail

        // Request more money than allowed, pendingBalance + balance + buyin <= max buyin
        // the player can actually buy in 1000 but requests 2000
        BuyInRequest buyInRequest = new BuyInRequest("20", true);

        // ReserveCallback reserveCallback = mock(ReserveCallback.class);
        // when(callbackFactory.createReserveCallback(table)).thenReturn(reserveCallback);

        pokerHandler.visit(buyInRequest);

        // since amount is higher than max allowed we should never get a call to the backend
        verify(backend, Mockito.never()).reserveMoneyForTable(Mockito.any(ReserveRequest.class), Mockito.<TableId>any());
        verify(pokerPlayer, never()).addRequestedBuyInAmount(Mockito.any(BigDecimal.class));
        verify(state, never()).playerIsSittingIn(playerId);
        // verify(reserveCallback).requestFailed(Mockito.any(ReserveFailedResponse.class));
    }
}
