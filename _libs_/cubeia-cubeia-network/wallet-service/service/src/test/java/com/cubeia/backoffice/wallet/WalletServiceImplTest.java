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

package com.cubeia.backoffice.wallet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.exception.TransactionNotBalancedException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.manager.WalletAccountingManager;

public class WalletServiceImplTest {

    @Mock
    private WalletAccountingManager accountingManager;
    
    private WalletServiceImpl walletService;
    
    @Mock SupportedCurrencyManager supportedCurrencyManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        
        walletService = new WalletServiceImpl();
        walletService.accountingManager = accountingManager;
        walletService.supportedCurrencyManager = supportedCurrencyManager;
        
        SupportedCurrency euro = new SupportedCurrency("EUR", 2);
        List<SupportedCurrency> currencies = Collections.singletonList(euro);
        
        when(supportedCurrencyManager.getCurrencies()).thenReturn(currencies);
        when(supportedCurrencyManager.getCurrencyByCode("EUR")).thenReturn(euro);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDoTransaction() throws TransactionNotBalancedException {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("a1", "v1");
        attributes.put("a2", null);
        String comment = "tx comment";
        TransactionRequest txReq = new TransactionRequest(comment, attributes);
        String externalId = "abcAAA";
        txReq.setExternalId(externalId);
        Money amount = new Money("EUR", 2, new BigDecimal("123.45"));
        Money rake = new Money("EUR", 2, new BigDecimal("1.00"));
        long debitAccountId = 100L;
        long creditAccountId = 200L;
        long rakeAccountId = -1L;
        TransactionEntry debitEntry = new TransactionEntry(debitAccountId, amount.negate());
        TransactionEntry creditEntry = new TransactionEntry(creditAccountId, amount.subtract(rake));
        TransactionEntry rakeEntry = new TransactionEntry(rakeAccountId, rake);
        List<TransactionEntry> entries = Arrays.asList(debitEntry, creditEntry, rakeEntry);
        txReq.getExcludeReturnBalanceForAcconds().add(rakeAccountId);
        txReq.setEntries(entries);
        
        Account debitAccount = new Account(debitAccountId);
        debitAccount.setCurrencyCode("EUR");
        Account creditAccount = new Account(creditAccountId);
        creditAccount.setCurrencyCode("EUR");
        Account rakeAccount = new Account(rakeAccountId);
        creditAccount.setCurrencyCode("EUR");
        when(accountingManager.getAccount(debitAccountId)).thenReturn(debitAccount);
        when(accountingManager.getAccount(creditAccountId)).thenReturn(creditAccount);
        when(accountingManager.getAccount(rakeAccountId)).thenReturn(rakeAccount);

        Transaction tx = new Transaction();
        long txId = 1337;
        tx.setId(txId);
        ArgumentCaptor<List> entriesCaptor = ArgumentCaptor.forClass(List.class);
        when(accountingManager.createTransaction(Mockito.eq(comment), Mockito.eq(externalId), 
            entriesCaptor.capture(), Mockito.eq(attributes))).thenReturn(tx);
        
        Money debitAccountBalance = new Money("EUR", 2, new BigDecimal("500000"));
        when(accountingManager.getBalance(debitAccountId)).thenReturn(debitAccountBalance);
        Money creditAccountBalance = new Money("EUR", 2, new BigDecimal("1500000"));
        when(accountingManager.getBalance(creditAccountId)).thenReturn(creditAccountBalance);
        Money rakeAccountBalance = new Money("EUR", 2, new BigDecimal("666"));
        when(accountingManager.getBalance(rakeAccountId)).thenReturn(rakeAccountBalance);
        
        TransactionResult txRes = walletService.doTransaction(txReq);
        
        assertThat(txRes.getErrorCode(), is(-1));
        assertThat(txRes.getTransactionId(), is(txId));
        assertThat(txRes.getBalances().size(), is(2)); // NOT 3, rake account should be excluded
        ArrayList<AccountBalanceResult> balances = new ArrayList(txRes.getBalances());
        assertThat(balances.get(0).getAccountId(), is(debitAccountId));
        assertThat(balances.get(0).getBalance(), is(debitAccountBalance));
        assertThat(balances.get(1).getAccountId(), is(creditAccountId));
        assertThat(balances.get(1).getBalance(), is(creditAccountBalance));
        
        List<Entry> createdEntries = entriesCaptor.getValue();
        assertThat(createdEntries.size(), is(3));
        assertThat(createdEntries.get(0).getAccount(), is(debitAccount));
        assertThat(createdEntries.get(0).getAmount(), is(amount.getAmount().negate()));
        assertThat(createdEntries.get(1).getAccount(), is(creditAccount));
        assertThat(createdEntries.get(1).getAmount(), is(amount.getAmount().subtract(rake.getAmount())));
        assertThat(createdEntries.get(2).getAccount(), is(rakeAccount));
        assertThat(createdEntries.get(2).getAmount(), is(rake.getAmount()));
    }

}
