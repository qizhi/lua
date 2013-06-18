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

package com.cubeia.backoffice.accounting.manager;

import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.Direction.BOTH;
import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.IdentifiactionType.ACCOUNT_ID;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.cubeia.backoffice.accounting.BaseTest;
import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.accounting.core.AccountClosedException;
import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;

public class AccountingManagerImplTest extends BaseTest {

    @Resource(name = "accounting.transactionManager")
    private AbstractPlatformTransactionManager transactionManager;
    
    @After
    public void tearDown() throws Exception {
        accountingManager.setBalanceCheckpointInterval(10);
        accountingManager.setAsyncCheckpointCreation(false);
    }
	
    @Test
	public void testCreateAccount() {
		Account account = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account);
		Money balance = accountingManager.getBalance(account.getId());
		assertThat(balance.getCurrencyCode(), is("EUR"));
		assertThat(balance.getAmount().longValue(), is(0l));
	}
    
    @Test
	public void testCreateAccountWithInitialBalance() {
		Account account = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccountWithInitialBalance(account, new BigDecimal("10.00"));
		Money balance = accountingManager.getBalance(account.getId());
		assertThat(balance.getCurrencyCode(), is("EUR"));
		assertThat(balance.getAmount().longValue(), is(10L));
		QueryResultsContainer<BalancedEntry> con = accountingManager.listEntriesBalanced(account.getId(), 0, 10, true);
		assertThat(con.getTotalQueryResultSize(), is(1));
		assertThat(con.getResults().get(0).getBalance(), is(new BigDecimal("10.00")));
    }
    
    @Test
    public void testCreateAccountCustomCurrency() {
        Account account = AccountFactory.create(new Long("234123"), "BID", 0);
        accountingManager.createAccount(account);
        Money balance = accountingManager.getBalance(account.getId());
        assertThat(balance.getCurrencyCode(), is("BID"));
        assertThat(balance.getFractionalDigits(), is(0));
        assertThat(balance.getAmount().longValue(), is(0l));
    }
	
    @Test
	public void testAccountAttributes() throws Exception {
		Account a = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(a);
		
		assertEquals(0, a.getAttributes().size());
		
		accountingManager.setAccountAttribute(a.getId(), "kalle", "kalleV");
		accountingManager.setAccountAttribute(a.getId(), "kalle2", "kalle2V");	
	
		a = accountingManager.getAccount(a.getId());
		
		assertEquals(2, a.getAttributes().size());
		assertEquals("kalleV", a.getAttribute("kalle"));
		assertEquals("kalle2V", a.getAttribute("kalle2"));
		
		accountingManager.removeAccountAttribute(a.getId(), "kalle");
		
		a = accountingManager.getAccount(a.getId());
		
		assertEquals(1, a.getAttributes().size());
		assertEquals("kalle2V", a.getAttribute("kalle2"));
	}
    
    @Test
    public void testReverseTransaction() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		
		Map<String, String> atts = Collections.singletonMap("namn", "kalle");
		
		Transaction tr = accountingManager.createTransaction("testInsertMoney_1", null, new BigDecimal(100), account1.getId(), account2.getId(), atts);
	
		Map<String, String> oldAtts = Collections.singletonMap("isReversed", "true");
		Map<String, String> newAtts = Collections.singletonMap("reversing", tr.getId().toString());
		
		Transaction tr2 = accountingManager.reverseTransaction(tr.getId(), null, oldAtts, newAtts);
		tr = accountingManager.getTransactionById(tr.getId());
    
		assertEquals("true", tr.getAttribute("isReversed"));
		assertEquals(tr.getId().toString(), tr2.getAttribute("reversing"));
		
		assertEquals(sum(tr), sum(tr2).negate());
		
    }
    
    @Test
    public void testCreateTransactionDummyAccount() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
    	account1.setUserId(666L);
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		account2.setUserId(777L);
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		
		Entry e1 = new Entry();
		// using a dummy with only an id
		Account dummy1 = new Account();
		dummy1.setId(account1.getId());
		e1.setAccount(dummy1);
		e1.setAmount(new BigDecimal("10.00"));
		
		Entry e2 = new Entry();
		// using a dummy with only an id
		Account dummy2 = new Account();
		dummy2.setId(account2.getId());
		e2.setAccount(dummy2);
		e2.setAmount(new BigDecimal("-10.00"));
		
		List<Entry> l = new LinkedList<Entry>();
		l.add(e1);
		l.add(e2);
		
		accountingManager.createTransaction(null, null, l, null);
		
		// check that the account is not changed
		Account test1 = accountingManager.getAccount(account1.getId());
		Assert.assertEquals(666, test1.getUserId().intValue());
    }
    
	private BigDecimal sum(Transaction t) {
		BigDecimal sum = new BigDecimal("0");
		for (Entry e : t.getEntries()) {
			sum = sum.add(e.getAmount());
		}
		return sum;
	}
    
    @Test
    public void testTransactionAttributes() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		
		Map<String, String> atts = Collections.singletonMap("namn", "kalle");
		
		Transaction tr = accountingManager.createTransaction("testInsertMoney_1", null, new BigDecimal(100), account1.getId(), account2.getId(), atts);
	
		assertEquals(1, tr.getAttributes().size());
		assertEquals("kalle", tr.getAttribute("namn"));
		
		accountingManager.setTransactionAttribute(tr.getId(), "tel", "666");
		
		tr = accountingManager.getTransactionById(tr.getId());
		
		assertEquals(2, tr.getAttributes().size());
		assertEquals("666", tr.getAttribute("tel"));
		
		accountingManager.removeTransactionAttribute(tr.getId(), "namn");
   
		assertEquals(1, tr.getAttributes().size());
		assertEquals("666", tr.getAttribute("tel"));
    }
    
    @Test
    public void testNegativeBalanceFail() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		account1.setNegativeBalanceAllowed(false);
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
	
		try {
			accountingManager.createTransaction("test", null, new BigDecimal(100), account1.getId(), account2.getId(), null);
			fail("Expected negative balance exception...");
		} catch(NegativeBalanceException e) {
			// Expected...
		}
    }

    @Test
	public void testTransaction() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		
		Money balance1 = accountingManager.getBalance(account1.getId());
		Money balance2 = accountingManager.getBalance(account2.getId());
		
		assertEquals(0, balance1.getAmount().longValue());
		assertEquals(0, balance2.getAmount().longValue());
		
		accountingManager.createTransaction("testInsertMoney_1", null, new BigDecimal(100), account1.getId(), account2.getId(), null);
		
		balance1 = accountingManager.getBalance(account1.getId());
		balance2 = accountingManager.getBalance(account2.getId());
		
		assertEquals(-100, balance1.getAmount().longValue());
		assertEquals(100, balance2.getAmount().longValue());
	}
    
    @Test
    // @NotTransactional
    public void testCreateTransactionWithAsyncCheckpointCreation() throws Exception {
        accountingManager.setAsyncCheckpointCreation(true);
        
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        Money balance1 = accountingManager.getBalance(account1.getId());
        Money balance2 = accountingManager.getBalance(account2.getId());
        
        assertEquals(0, balance1.getAmount().longValue());
        assertEquals(0, balance2.getAmount().longValue());
        
        for (int i = 0; i < 500; i++) {
            accountingManager.createTransaction("testInsertMoney_1", null, new BigDecimal(100), account1.getId(), account2.getId(), null);
        }
        
        balance1 = accountingManager.getBalance(account1.getId());
        balance2 = accountingManager.getBalance(account2.getId());
        
        assertEquals(-100 * 500, balance1.getAmount().longValue());
        assertEquals(100 * 500, balance2.getAmount().longValue());
        
        /*
         * In order to use the test code below you need to 
         * 
         *  1. Un-comment the "@NotTransactional" for this method
         *  2. Use a real database (such as MySQL)
         *  3. Change the initial wit for the checkpoint job
         * 
         * God luck!
         */
        
        /*Thread.sleep(5000);
        
        BalanceCheckpoint c1 = accountingManager.getLastCheckpoint(account1.getId());
        BalanceCheckpoint c2 = accountingManager.getLastCheckpoint(account2.getId());
    
        assertNotNull(c1);
        assertNotNull(c2);
        
        balance1 = accountingManager.getBalance(account1.getId());
        balance2 = accountingManager.getBalance(account2.getId());
        
        assertEquals(-100 * 500, balance1.getAmount().longValue());
        assertEquals(100 * 500, balance2.getAmount().longValue());*/
        
    }
    

    /*@Test
    public void testTransactionFailOnDifferentCurrencies() throws Exception {
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("SEK"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        try {
            accountingManager.createTransaction("fail", BigDecimal.ONE, account1.getId(), account2.getId());
            fail("different currencies, should have failed");
        } catch (IllegalArgumentException e) {
            // this should happen
        }
    }*/
    
    @Test
    public void testGetTransactionByExtId() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
	
		accountingManager.createTransaction("test", "id1", new BigDecimal("100"), account1.getId(), account2.getId(), null);
		
		Transaction trans = accountingManager.getTransactionByExternalId("id1");
		
		assertNotNull(trans);
    }
    
    @Test
    public void testSetTransactionExtId() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
	
		Transaction trans = accountingManager.createTransaction("test", null, new BigDecimal("100"), account1.getId(), account2.getId(), null);

		Assert.assertNull(trans.getExternalId());
		
		accountingManager.setTransactionExternalId(trans.getId(), "kalle");
		
		trans = accountingManager.getTransactionById(trans.getId());
		
		assertEquals("kalle", trans.getExternalId());
    }
    
    public void testCreateTransactionOnClosedAccount() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		account2.setStatus(AccountStatus.CLOSED);
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
	
		try {
			// should fail, one account is closed
			accountingManager.createTransaction("test", null, new BigDecimal("100"), account1.getId(), account2.getId(), null);
			fail("Expected a AccountClosedException");
		} catch(AccountClosedException e) {
			// expected
		}

		accountingManager.setAllowTransactionsWithClosedAccounts(true);
		
		// should work
		accountingManager.createTransaction("test", null, new BigDecimal("100"), account1.getId(), account2.getId(), null);
		
    }
	
    @Test
    public void testSetTransactionExtIFail() throws Exception {
    	Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
	
		Transaction trans = accountingManager.createTransaction("test", "kalle", new BigDecimal("100"), account1.getId(), account2.getId(), null);

		Assert.assertNotNull(trans.getExternalId());
		
		try {
			accountingManager.setTransactionExternalId(trans.getId(), "kalle");
			fail("Expected a security exception...");
		} catch(SecurityException e) {
			// Expected
		}
	}
    
    @Test
	public void testMultiEntryTransaction() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account3 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		accountingManager.createAccount(account3);
		
		Money balance1 = accountingManager.getBalance(account1.getId());
		Money balance2 = accountingManager.getBalance(account2.getId());
		Money balance3 = accountingManager.getBalance(account2.getId());
		
		assertEquals(0, balance1.getAmount().longValue());
		assertEquals(0, balance2.getAmount().longValue());
		assertEquals(0, balance3.getAmount().longValue());
		
		List<Entry> entries = new ArrayList<Entry>();
		
		Entry e1 = new Entry(account1, new BigDecimal(-100));
		Entry e2 = new Entry(account2, new BigDecimal(-50));
		Entry e3 = new Entry(account3, new BigDecimal(150));
		
		entries.add(e1);
		entries.add(e2);
		entries.add(e3);
		
		accountingManager.createTransaction("testMultiInsertMoney", null, entries, null);
		
		balance1 = accountingManager.getBalance(account1.getId());
		balance2 = accountingManager.getBalance(account2.getId());
		balance3 = accountingManager.getBalance(account3.getId());
		
		assertEquals(-100, balance1.getAmount().longValue());
		assertEquals(-50, balance2.getAmount().longValue());
		assertEquals(150, balance3.getAmount().longValue());
	}
    
    @Test
    public void testMultiCurrencySumBalancing() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account3 = AccountFactory.create(Currency.getInstance("SEK"));
		Account account4 = AccountFactory.create(Currency.getInstance("SEK"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		accountingManager.createAccount(account3);
		accountingManager.createAccount(account4);
		

		List<Entry> entries = new ArrayList<Entry>();
		
		Entry e1 = new Entry(account1, new BigDecimal(-100));
		Entry e2 = new Entry(account2, new BigDecimal(100));
		Entry e3 = new Entry(account3, new BigDecimal(150));
		Entry e4 = new Entry(account4, new BigDecimal(-150));
		
		entries.add(e1);
		entries.add(e2);
		entries.add(e3);
		entries.add(e4);
		
		accountingManager.createTransaction("testMultiInsertMoney", null, entries, null);
		
		Money balance1 = accountingManager.getBalance(account1.getId());
		Money balance2 = accountingManager.getBalance(account2.getId());
		Money balance3 = accountingManager.getBalance(account3.getId());
		Money balance4 = accountingManager.getBalance(account4.getId());
		
		assertEquals(-100, balance1.getAmount().longValue());
		assertEquals(100, balance2.getAmount().longValue());
		assertEquals(150, balance3.getAmount().longValue());
		assertEquals(-150, balance4.getAmount().longValue());
    }
    
    @Test
    public void testListAccountsWithTransactions() throws Exception {
		Account account1 = AccountFactory.create(new Long("1"), Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(new Long("2"), Currency.getInstance("EUR"));
        Account account3 = AccountFactory.create(new Long("3"), Currency.getInstance("EUR"));
		
		accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        accountingManager.createAccount(account3);
		
		accountingManager.createTransaction("c", "x1", asList(
		    new Entry(account1, new BigDecimal("50")), 
		    new Entry(account2, new BigDecimal("50")),
		    new Entry(account3, new BigDecimal("-100"))), null);
        accountingManager.createTransaction("c", "x2", asList(
            new Entry(account1, new BigDecimal("-50")), 
            new Entry(account2, new BigDecimal("-50")),
            new Entry(account3, new BigDecimal("100"))), null);
        accountingManager.createTransaction("c", "x3", asList(
            new Entry(account1, new BigDecimal("10")), 
            new Entry(account2, new BigDecimal("10")),
            new Entry(account3, new BigDecimal("-20"))), null);

		account1.setClosed(new Date());
		account2.setClosed(new Date());
		accountingManager.updateAccount(account1);
		accountingManager.updateAccount(account2);
		
		QueryResultsContainer<Account> con = accountingManager.listAccounts(null, null, null, null, 
				new HashSet<AccountStatus>(asList(AccountStatus.OPEN, AccountStatus.CLOSED)), 
				null, 0, Integer.MAX_VALUE, AccountsOrder.CREATION_DATE, true);
		
		assertThat(con.getTotalQueryResultSize(), is(3));
    }
    
    @Test
    public void testListOpenAccounts() throws Exception {
		Account account1 = AccountFactory.create(new Long("1"), Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(new Long("1"), Currency.getInstance("EUR"));
		Account account3 = AccountFactory.create(new Long("1"), Currency.getInstance("SEK"));
		Account account4 = AccountFactory.create(new Long("1"), Currency.getInstance("SEK"));
		account4.setStatus(AccountStatus.CLOSED);
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		accountingManager.createAccount(account3);
		accountingManager.createAccount(account4);
		
		QueryResultsContainer<Account> con = accountingManager.listAccounts(null, new Long("1"), null, null, Collections.singleton(AccountStatus.OPEN), null, 0, Integer.MAX_VALUE, AccountsOrder.CREATION_DATE, true);
		
		assertEquals(3, con.getTotalQueryResultSize());
    }
    
    @Test
    public void testListAccountsByCurrency() throws Exception {
		Account account1 = AccountFactory.create(new Long("1"), Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(new Long("1"), Currency.getInstance("EUR"));
		Account account3 = AccountFactory.create(new Long("1"), Currency.getInstance("SEK"));
		Account account4 = AccountFactory.create(new Long("1"), Currency.getInstance("SEK"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		accountingManager.createAccount(account3);
		accountingManager.createAccount(account4);
		
		QueryResultsContainer<Account> con = accountingManager.listAccounts(null, null, null, "EUR", null, null, 0, Integer.MAX_VALUE, AccountsOrder.CREATION_DATE, true);
		
		assertEquals(2, con.getTotalQueryResultSize());
    }
    
    @Test
    public void testMultiCurrencySumUnbalancing() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account3 = AccountFactory.create(Currency.getInstance("SEK"));
		Account account4 = AccountFactory.create(Currency.getInstance("SEK"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		accountingManager.createAccount(account3);
		accountingManager.createAccount(account4);
		

		List<Entry> entries = new ArrayList<Entry>();
		
		Entry e1 = new Entry(account1, new BigDecimal(-100));
		Entry e2 = new Entry(account2, new BigDecimal(100));
		Entry e3 = new Entry(account3, new BigDecimal(150));
		Entry e4 = new Entry(account4, new BigDecimal(-121));
		
		entries.add(e1);
		entries.add(e2);
		entries.add(e3);
		entries.add(e4);

		try {
			accountingManager.createTransaction("testMultiInsertMoney", null, entries, null);
			fail("Transaction should have failed on unbalaced SEK accounts");
		} catch(TransactionNotBalancedException e) {
			// This is expected
		}
    }
    
    @Test
    public void testListTransactions() throws Exception {
		Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
		Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(account1);
		accountingManager.createAccount(account2);
		
		List<Entry> entries = new ArrayList<Entry>(2);
		
		Entry e1 = new Entry(account1, new BigDecimal(-100));
		Entry e2 = new Entry(account2, new BigDecimal(100));
		
		entries.add(e1);
		entries.add(e2);
		
		accountingManager.createTransaction("trans1", null, entries, null);
    
		entries = new ArrayList<Entry>(2);
		
		e1 = new Entry(account1, new BigDecimal(-100));
		e2 = new Entry(account2, new BigDecimal(100));
		
		entries.add(e1);
		entries.add(e2);
		
		accountingManager.createTransaction("trans2", null, entries, null);
    
		QueryResultsContainer<Transaction> transes = accountingManager.listTransactions(
		    new TransactionParticipant(account1.getId(), ACCOUNT_ID, BOTH), 
		    new TransactionParticipant(account2.getId(), ACCOUNT_ID, BOTH), 
		    null, null, 0, 10, null, true);
		
		assertEquals(2, transes.getResults().size());
    }
	
    @Test
    public void testListTransactionsDateRange() throws Exception {
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        List<Entry> entries = new ArrayList<Entry>(2);
        
        Entry e1 = new Entry(account1, new BigDecimal(-100));
        Entry e2 = new Entry(account2, new BigDecimal(100));
        
        entries.add(e1);
        entries.add(e2);
        
        Transaction tx = accountingManager.createTransaction("trans1", null, entries, null);
    
        entries = new ArrayList<Entry>(2);
        
        QueryResultsContainer<Transaction> transes = accountingManager.listTransactions(
            new TransactionParticipant(account1.getId(), ACCOUNT_ID, BOTH), 
            new TransactionParticipant(account2.getId(), ACCOUNT_ID, BOTH), 
            new Date(tx.getTimestamp().getTime() - 24 * 3600 * 1000), new Date(tx.getTimestamp().getTime() + 24 * 3600 * 1000), 
            0, 10, null, true);
        assertThat(transes.getResults(), Matchers.hasItem(tx));
        
        transes = accountingManager.listTransactions(
            new TransactionParticipant(account1.getId(), ACCOUNT_ID, BOTH), 
            new TransactionParticipant(account2.getId(), ACCOUNT_ID, BOTH), 
            new Date(tx.getTimestamp().getTime() - 2 * 24 * 3600 * 1000), new Date(tx.getTimestamp().getTime() - 24 * 3600 * 1000), 
            0, 10, null, true);
        assertThat(transes.getResults().isEmpty(), is(true));
    }
    
    @Test
	public void testGetAccountByUserId() {
		Collection<Account> accounts = accountingManager.getAccountsByUserId(new Long("1337"));
		assertThat(accounts.isEmpty(), is(true));
		
		Account a = AccountFactory.create(Currency.getInstance("EUR"));
		a.setUserId(new Long("1337"));
		accountingManager.createAccount(a);

		accounts = accountingManager.getAccountsByUserId(new Long("1337"));
		assertThat(accounts.size(), is(1));
		assertThat(accounts.iterator().next().getId(), is(a.getId()));
		
        Account a2 = AccountFactory.create(Currency.getInstance("EUR"));
        a2.setUserId(new Long("1337"));
        accountingManager.createAccount(a2);
        
        accounts = accountingManager.getAccountsByUserId(new Long("1337"));
        assertThat(accounts.size(), is(2));
	}
	
    @Test
	public void testListBalancedEntries() {
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        accountingManager.createTransaction("", null, new BigDecimal("100"), account1.getId(), account2.getId(), null);
        accountingManager.createTransaction("", null, new BigDecimal("50"), account1.getId(), account2.getId(), null);
        
        QueryResultsContainer<BalancedEntry> result = accountingManager.listEntriesBalanced(account1.getId(), 0, 100, true);
        assertEquals(2, result.getResults().size());
        assertEquals(new BigDecimal("-100.00"), result.getResults().get(0).getBalance());
        assertEquals(new BigDecimal("-150.00"), result.getResults().get(1).getBalance());
        
        result = accountingManager.listEntriesBalanced(account1.getId(), 0, 100, false);
        assertEquals(2, result.getResults().size());
        assertEquals(new BigDecimal("-150.00"), result.getResults().get(0).getBalance());
        assertEquals(new BigDecimal("-100.00"), result.getResults().get(1).getBalance());
	}
	
    @Test
	public void testGetBalanceAfterMultipleTransactions() throws InterruptedException {
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        assertThat(accountingManager.getBalance(account1.getId()).getAmount(), is(new BigDecimal("0.00")));
        assertThat(accountingManager.getBalance(account2.getId()).getAmount(), is(new BigDecimal("0.00")));
        	    
        accountingManager.setBalanceCheckpointInterval(10);
        
	    for (int i = 1; i <= 21; i++) {
	        accountingManager.createTransaction(null, null, BigDecimal.ONE, account1.getId(), account2.getId(), null);
	        assertEquals(BigDecimal.valueOf(-i).intValue(), accountingManager.getBalance(account1.getId()).getAmount().intValue());
	        assertEquals(BigDecimal.valueOf( i).intValue(), accountingManager.getBalance(account2.getId()).getAmount().intValue());
	    }
	    
	    // sleep for a while to let the executor finish
	    Thread.sleep(100);
	    
        assertEquals(BigDecimal.valueOf(-21).intValue(), accountingManager.getBalance(account1.getId()).getAmount().intValue());
        assertEquals(BigDecimal.valueOf( 21).intValue(), accountingManager.getBalance(account2.getId()).getAmount().intValue());
	}
	
	@Test
	public void testGetBalanceAfterEntry() throws InterruptedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {
	    TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
	    
        Account account1 = AccountFactory.create(Currency.getInstance("EUR"));
        Account account2 = AccountFactory.create(Currency.getInstance("EUR"));
        accountingManager.createAccount(account1);
        accountingManager.createAccount(account2);
        
        assertThat(accountingManager.getBalance(account1.getId()).getAmount(), is(new BigDecimal("0.00")));
        assertThat(accountingManager.getBalance(account2.getId()).getAmount(), is(new BigDecimal("0.00")));
        
        // create transactions 
        accountingManager.setBalanceCheckpointInterval(5);
        ArrayList<Transaction> txList = new ArrayList<Transaction>();
        int count = 51;
        for (int i = 1; i <= count; i++) {
            Transaction tx = accountingManager.createTransaction(
                null, null, BigDecimal.ONE, account1.getId(), account2.getId(), null);
            txList.add(tx);
        }
        
        // sleep for a while to let the executor finish
        Thread.sleep(100);
	    
        // check total balances
        assertEquals(BigDecimal.valueOf(-count).intValue(), accountingManager.getBalance(account1.getId()).getAmount().intValue());
        assertEquals(BigDecimal.valueOf( count).intValue(), accountingManager.getBalance(account2.getId()).getAmount().intValue());
        
        // check after each entry
        int b = 0;
        for (Transaction tx : txList) {
            b++;
            
            ArrayList<Entry> entries = new ArrayList<Entry>(tx.getEntries());
            Entry ea1;
            Entry ea2;
            if (entries.get(0).getAccount().equals(account1)) {
                ea1 = entries.get(0);
                ea2 = entries.get(1);
            } else {
                ea1 = entries.get(1);
                ea2 = entries.get(0);
            }
            
            Money ba1 = accountingManager.getBalanceAfterEntry(account1.getId(), ea1.getId());
            Money ba2 = accountingManager.getBalanceAfterEntry(account2.getId(), ea2.getId());
            
            assertEquals(BigDecimal.valueOf(-b).intValue(), ba1.getAmount().intValue());
            assertEquals(BigDecimal.valueOf( b).intValue(), ba2.getAmount().intValue());
        }
        
        transactionManager.commit(txStatus);
	}
	
    @Test
    public void testAddCurrencyRate() {
        // add and get
        CurrencyRate rate = new CurrencyRate("EUR", "USD", new BigDecimal("1.2345"), new Date(0));
        accountingManager.addCurrencyRate(rate);
        assertThat(rate.getId(), notNullValue());
        
        CurrencyRate result = accountingManager.getCurrencyRate("EUR", "USD", new Date(0), 10);
        assertThat(result, notNullValue());
        assertThat(result, is(rate));
        
        // add again with new rate
        CurrencyRate rateNew = new CurrencyRate("EUR", "USD", new BigDecimal("2.345"), new Date(0));
        accountingManager.addCurrencyRate(rateNew);
        assertThat(rate.getId(), notNullValue());
        
        // get, new rate should be returned
        result = accountingManager.getCurrencyRate("EUR", "USD", new Date(0), 10);
        assertThat(result, notNullValue());
        assertThat(result, is(rateNew));
    }
	
	@Test
	public void testGetCurrencyRate() {
	    DateTime date = new DateTime("2009-03-23T23:00");
	    
        CurrencyRate rate = new CurrencyRate("EUR", "GBP", new BigDecimal("1.2345"), date.toDate());
        accountingManager.addCurrencyRate(rate);
	    
        CurrencyRate result = accountingManager.getCurrencyRate("EUR", "GBP", date.toDate(), 1);
        assertThat(result, is(rate));
        
        result = accountingManager.getCurrencyRate("EUR", "GBP", date.minusDays(1).toDate(), 1);
        assertThat(result, nullValue());
        
        result = accountingManager.getCurrencyRate("EUR", "GBP", date.plusDays(1).toDate(), 1);
        assertThat(result, is(rate));
        
        result = accountingManager.getCurrencyRate("EUR", "GBP", date.plusDays(2).toDate(), 1);
        assertThat(result, nullValue());
	}
	
	@Test
	public void testListCurrencyRate() {
		
	    DateTime date = new DateTime("2009-03-23T23:00");
	    
        CurrencyRate rate = new CurrencyRate("EUR", "GBP", new BigDecimal("1.2345"), date.minusDays(2).toDate());
        CurrencyRate rate2 = new CurrencyRate("EUR", "GBP", new BigDecimal("1.2344"), date.minusDays(4).toDate());
        CurrencyRate rate3 = new CurrencyRate("SEK", "GBP", new BigDecimal("0.01"), date.toDate());
        accountingManager.addCurrencyRate(rate);
        accountingManager.addCurrencyRate(rate2);
        accountingManager.addCurrencyRate(rate3);
	    
        // test get all, should return 2 (no duplicates)
        List<CurrencyRate> list = accountingManager.listRatesForCurrency(null, date.toDate(), 10);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(rate3, list.get(0));
        Assert.assertEquals(rate, list.get(1));
        
        // test get GBP, should return two (no duplicates)
        list = accountingManager.listRatesForCurrency("GBP", date.toDate(), 2);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(rate3, list.get(0));
        Assert.assertEquals(rate, list.get(1));
        
        // test get GBP within 1 day, should return 1
        list = accountingManager.listRatesForCurrency("GBP", date.toDate(), 1);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(rate3, list.get(0));
        
	}
	
    @Test
    public void testGetCurrencyRateReverse() {
        DateTime date = new DateTime("2009-03-23T23:00");
        
        CurrencyRate rate = new CurrencyRate("EUR", "SEK", new BigDecimal("1.2345"), date.toDate());
        accountingManager.addCurrencyRate(rate);
        
        CurrencyRate result = accountingManager.getCurrencyRate("EUR", "SEK", date.toDate(), 1);
        assertThat(result, is(rate));
        
        result = accountingManager.getCurrencyRate("SEK", "EUR", date.toDate(), 1);
        assertThat(result, notNullValue());
        assertThat(result.getSourceCurrencyCode(), is("SEK"));
        assertThat(result.getTargetCurrencyCode(), is("EUR"));
        assertThat(result.getTimestamp(), is(rate.getTimestamp()));
        assertThat(result.getRate().doubleValue(), closeTo(1 / 1.2345, 0.005));
    }
    
    @Test
    public void testGetCurrencyRateChained() {
        DateTime date = new DateTime("2009-03-23T23:00");
        
        CurrencyRate rateEURtoSEK = new CurrencyRate("EUR", "SEK", new BigDecimal("10.07"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoSEK);
        CurrencyRate rateEURtoGBP = new CurrencyRate("EUR", "GBP", new BigDecimal("0.88"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoGBP);
        
        // 1 EUR = 10.07 SEK, 1 EUR = 0.88 GBP -> 100 SEK = 100/10.07 * 0.88 = 8.74 EUR -> Rate = 0.087 
        CurrencyRate result = accountingManager.getCurrencyRate("SEK", "GBP", date.toDate(), 1);
        assertThat(result, notNullValue());
        assertThat(result.getRate().doubleValue(), closeTo(1.0/10.07 * 0.88, 0.005));
        assertThat(result.isCalculated(), is(true));
    }
    
    @Test
    public void testGetCurrencyRateChainedWithOldValues() {
        DateTime date = new DateTime("2009-03-23T23:00");
        
        CurrencyRate rateEURtoSEK = new CurrencyRate("EUR", "SEK", new BigDecimal("666"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoSEK);
        CurrencyRate rateEURtoGBP = new CurrencyRate("EUR", "GBP", new BigDecimal("666"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoGBP);
        
        date = new DateTime("2009-03-24T23:00");
        
        rateEURtoSEK = new CurrencyRate("EUR", "SEK", new BigDecimal("10.07"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoSEK);
        rateEURtoGBP = new CurrencyRate("EUR", "GBP", new BigDecimal("0.88"), date.toDate());
        accountingManager.addCurrencyRate(rateEURtoGBP);
        
        /*
         * At this point we should make sure we have the latest and greatest
         * currency rates...
         * 
         * 1 EUR = 10.07 SEK, 1 EUR = 0.88 GBP -> 100 SEK = 100/10.07 * 0.88 = 8.74 EUR -> Rate = 0.087 
         */
        
        CurrencyRate result = accountingManager.getCurrencyRate("SEK", "GBP", date.toDate(), 1);
        assertThat(result, notNullValue());
        assertThat(result.getRate().doubleValue(), closeTo(1.0/10.07 * 0.88, 0.005));
        assertThat(result.isCalculated(), is(true));
    }
    
    @Test
    public void testGetCurrencyRateBaseline() {
        DateTime date = new DateTime("2009-03-23T23:00");
        
        CurrencyRate r1 = new CurrencyRate("EUR", "SEK", new BigDecimal("10.07"), date.toDate());
        accountingManager.addCurrencyRate(r1);
        CurrencyRate r2 = new CurrencyRate("EUR", "GBP", new BigDecimal("0.88"), date.toDate());
        accountingManager.addCurrencyRate(r2);
        CurrencyRate r3 = new CurrencyRate("NOK", "SEK", new BigDecimal("1.15"), date.toDate());
        accountingManager.addCurrencyRate(r3);
        CurrencyRate r4 = new CurrencyRate("NOK", "GBP", new BigDecimal("0.09"), date.toDate());
        accountingManager.addCurrencyRate(r4);
        CurrencyRate r5 = new CurrencyRate("USD", "NOK", new BigDecimal("8.02"), date.toDate());
        accountingManager.addCurrencyRate(r5);
        
        /*
         * 1 EUR = 10.07 SEK, 1 EUR = 0.88 GBP -> 100 SEK = 100/10.07 * 0.88 = 8.74 EUR -> Rate = 0.087 
         */
        CurrencyRate result = accountingManager.getCurrencyRate("SEK", "GBP", "EUR", date.toDate(), 1);
        
        assertThat(result, notNullValue());
        assertThat(result.getRate().doubleValue(), closeTo(1.0/10.07 * 0.88, 0.005));
        assertThat(result.isCalculated(), is(true));
        
        /*
         * 1 NOK = 1.15 SEK, 1 NOK = 0.09 GBP -> 100 SEK = 100/1.15 * 0.09 = 7.83 EUR -> Rate = 0.078 
         */
        result = accountingManager.getCurrencyRate("SEK", "GBP", "NOK", date.toDate(), 1);
        
        assertThat(result, notNullValue());
        assertThat(result.getRate().doubleValue(), closeTo(1.0/1.15 * 0.09, 0.005));
        assertThat(result.isCalculated(), is(true));
        
        // check no result
        result = accountingManager.getCurrencyRate("NOK", "EUR", "USD", date.toDate(), 1);
        assertNull(result);
    }
}
