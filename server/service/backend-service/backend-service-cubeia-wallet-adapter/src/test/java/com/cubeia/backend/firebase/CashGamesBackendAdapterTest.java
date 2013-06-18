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

package com.cubeia.backend.firebase;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.*;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.cashgame.exceptions.OpenSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.ReserveFailedException;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.UUID;

import static com.cubeia.backend.firebase.CashGamesBackendAdapter.GAME_ID;
import static com.cubeia.backend.firebase.CashGamesBackendAdapter.LICENSEE_ID;
import static com.cubeia.backend.firebase.CashGamesBackendService.MARKET_TABLE_REFERENCE_KEY;
import static com.cubeia.backend.firebase.CashGamesBackendService.MARKET_TABLE_SESSION_REFERENCE_KEY;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class CashGamesBackendAdapterTest {

    private CashGamesBackendAdapter backend;
    
    @Mock
    private AccountLookupUtil accountLookupUtil;
    
    @Mock
    private WalletServiceContract walletService;
    private com.cubeia.games.poker.common.money.Currency eur = new com.cubeia.games.poker.common.money.Currency( "EUR", 2);


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        backend = new CashGamesBackendAdapter(walletService, accountLookupUtil);
        backend.accountLookupUtil = accountLookupUtil;
    }

    @Test
    public void testGenerateHandId() {
        String handId = backend.generateHandId();
        assertThat(handId, notNullValue());
        UUID handIdUUID = UUID.fromString(handId);
        assertThat(handIdUUID.toString(), is(handId));
    }

    @Test
    public void testAllowJoinTable() {
        int playerId = 1235;
        AllowJoinResponse resp = backend.allowJoinTable(playerId);
        assertThat(resp.allowed, is(true));
        assertThat(resp.responseCode, is(-1));
    }

    @Test
    public void testAnnounceTable() {
        int platformTableId = 1337;
        AnnounceTableRequest request = new AnnounceTableRequest(new TableId(1, platformTableId));
        AnnounceTableResponse response = backend.announceTable(request);
        assertThat(response.getTableId(), notNullValue());
        assertThat(response.getProperty(MARKET_TABLE_REFERENCE_KEY), containsString("CUBEIA-TABLE-ID::"));
    }

    @Test
    public void testOpenSession() throws OpenSessionFailedException {
        int playerId = 3434;
        int tableIdInt = 8888;
        String integrationId = "tableIntegrationId1234";
        TableId tableId = new TableId(1, tableIdInt, integrationId);
        Money openingBalance = new Money(new BigDecimal(100),eur);
        OpenTableSessionRequest request = new OpenTableSessionRequest(playerId, tableId, openingBalance);
        long walletSessionId = 12234444L;
        when(walletService.startSession(openingBalance.getCurrencyCode(), LICENSEE_ID, playerId, integrationId, GAME_ID, "unknown-" + playerId, null))
                .thenReturn(walletSessionId);
        AccountBalanceResult balance = mock(AccountBalanceResult.class);
        when(balance.getBalance()).thenReturn(new com.cubeia.backoffice.accounting.api.Money(Currency.getInstance("EUR"), BigDecimal.valueOf(10)));
        when(walletService.getBalance(anyLong())).thenReturn(balance);
        OpenSessionResponse response = backend.openSession(request);
        PlayerSessionId playerSessionIdImpl = response.getSessionId();
        assertThat(playerSessionIdImpl.playerId, is(playerId));
        assertThat(playerSessionIdImpl.integrationSessionId, is("" + walletSessionId));
    }

    @Test
    public void testCloseSession() {
        int playerId = 5555;
        long sessionId = 3939393L;
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId, valueOf(sessionId));
        CloseSessionRequest request = new CloseSessionRequest(playerSessionId);
        backend.closeSession(request);
        verify(walletService).endSessionAndDepositAll(Mockito.eq(LICENSEE_ID), Mockito.eq(sessionId), Mockito.anyString());
    }

    @Test
    public void testReserve() throws ReserveFailedException {
        int playerId = 5555;
        long sessionId = 3939393L;
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId, valueOf(sessionId));
        Money amount = new Money(new BigDecimal(223), eur);
        ReserveRequest request = new ReserveRequest(playerSessionId, amount);

        AccountBalanceResult sessionBalance = mock(AccountBalanceResult.class);
        com.cubeia.backoffice.accounting.api.Money sessionBalanceMoney = new com.cubeia.backoffice.accounting.api.Money("EUR", 2, new BigDecimal("500"));
        when(sessionBalance.getBalance()).thenReturn(sessionBalanceMoney);
        when(walletService.getBalance(sessionId)).thenReturn(sessionBalance);

        ReserveResponse response = backend.reserve(request);

        assertThat(response.getPlayerSessionId(), is(playerSessionId));
        assertThat(response.getAmountReserved().getAmount(), is(amount.getAmount()));
        assertThat(response.getBalanceUpdate().getBalance().getAmount(), is(new BigDecimal("500.00")));
        assertThat(response.getReserveProperties().get(MARKET_TABLE_SESSION_REFERENCE_KEY), containsString("CUBEIA-MARKET-SID-"));
    }

    @Test
    public void testBatchHand() throws BatchHandFailedException, SystemException {
        long rakeAccountId = -5000L;
        backend.rakeAccounts.put("EUR", rakeAccountId);
        String handId = "xyx";
        TableId tableId = new TableId(1, 344);
        Money totalRake = money(BigDecimal.valueOf(1000));
        BatchHandRequest request = new BatchHandRequest(handId, tableId, totalRake);

        int player1Id = 1001;
        long session1Id = 1002L;
        PlayerSessionId playerSession1 = new PlayerSessionId(player1Id, "" + session1Id);
        int player2Id = 2001;
        long session2Id = 2002L;
        PlayerSessionId playerSession2 = new PlayerSessionId(player2Id, "" + session2Id);

        HandResult handResult1 = new HandResult(playerSession1, money(new BigDecimal("50.00")), money(new BigDecimal("90.00")), money(new BigDecimal("10.00")), 1, 0, money(new BigDecimal("50.00")));
        HandResult handResult2 = new HandResult(playerSession2, money(new BigDecimal("50.00")), money(BigDecimal.ZERO), money(BigDecimal.ZERO), 2, 0, money(new BigDecimal("50.00")));

        request.addHandResult(handResult1);
        request.addHandResult(handResult2);

        ArgumentCaptor<TransactionRequest> txCaptor = ArgumentCaptor.forClass(TransactionRequest.class);
        TransactionResult txResult = mock(TransactionResult.class);
        AccountBalanceResult sessionBalance1 = new AccountBalanceResult(session1Id, walletMoney("11.11"));
        AccountBalanceResult sessionBalance2 = new AccountBalanceResult(session2Id, walletMoney("22.22"));
        AccountBalanceResult rakeAccountBalance = new AccountBalanceResult(rakeAccountId, walletMoney("1232322.22"));
        when(txResult.getBalances()).thenReturn(Arrays.asList(sessionBalance1, sessionBalance2, rakeAccountBalance));

        when(walletService.doTransaction(txCaptor.capture())).thenReturn(txResult);
        when(accountLookupUtil.lookupOperatorAccountId(walletService, 0)).thenReturn(rakeAccountId);
        BatchHandResponse batchHandResponse = backend.batchHand(request);

        TransactionRequest txRequest = txCaptor.getValue();
        Collection<TransactionEntry> txEntries = txRequest.getEntries();
        assertThat(txEntries.size(), is(3));
        assertThat(findEntryByAccountId(session1Id, txEntries).getAmount().getAmount(), is(new BigDecimal("40.00")));
        assertThat(findEntryByAccountId(session2Id, txEntries).getAmount().getAmount(), is(new BigDecimal("-50.00")));
        assertThat(findEntryByAccountId(rakeAccountId, txEntries).getAmount().getAmount(), is(new BigDecimal("10.00")));

        assertThat(batchHandResponse.getResultingBalances().size(), is(2));
        assertThat(batchHandResponse.getResultingBalances().get(0).getBalance().getPlayerSessionId(), is(playerSession1));
        assertThat(batchHandResponse.getResultingBalances().get(0).getBalance().getBalance().getAmount(), is(new BigDecimal("11.11")));
        assertThat(batchHandResponse.getResultingBalances().get(1).getBalance().getPlayerSessionId(), is(playerSession2));
        assertThat(batchHandResponse.getResultingBalances().get(1).getBalance().getBalance().getAmount(), is(new BigDecimal("22.22")));
    }

    private TransactionEntry findEntryByAccountId(Long accountId, Collection<TransactionEntry> entries) {
        for (TransactionEntry e : entries) {
            if (accountId.equals(e.getAccountId())) {
                return e;
            }
        }
        return null;
    }


    /**
     * Creates a default money object with the given amount
     */
    private Money money(BigDecimal amount) {
        return new Money(amount, eur);
    }

    private com.cubeia.backoffice.accounting.api.Money walletMoney(String amount) {
        return new com.cubeia.backoffice.accounting.api.Money("EUR", 2, new BigDecimal(amount));
    }

    @Test
    public void testGetSessionBalance() throws GetBalanceFailedException {
        long sessionId = 3939393L;
        int playerId = 3939;
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId, "" + sessionId);

        com.cubeia.backoffice.accounting.api.Money balance = new com.cubeia.backoffice.accounting.api.Money("SEK", 2, new BigDecimal("343434"));
        AccountBalanceResult sessionBalance = new AccountBalanceResult(sessionId, balance);
        when(walletService.getBalance(sessionId)).thenReturn(sessionBalance);

        BalanceUpdate balanceUpdate = backend.getSessionBalance(playerSessionId);
        assertThat(balanceUpdate.getBalance(), is(new Money(new BigDecimal("343434.00"), new com.cubeia.games.poker.common.money.Currency("SEK", 2))));
        assertThat(balanceUpdate.getPlayerSessionId(), is(playerSessionId));
    }
}
