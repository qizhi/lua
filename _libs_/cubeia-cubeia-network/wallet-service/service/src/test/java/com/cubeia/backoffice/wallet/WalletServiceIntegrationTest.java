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

import static com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus.CLOSED;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.SYSTEM_ACCOUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.DepositResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.WithdrawResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.util.WalletDTOFactory;


public class WalletServiceIntegrationTest extends BaseTest {
    
	@Test
	public void testCreateSession() throws Exception {
		MetaInformation meta = new MetaInformation();
		meta.setGameId(2L);
		meta.setName("n");
		meta.setObjectId("3");
		meta.setUserName("u");
		CreateAccountResult result = createAccount(meta, AccountType.SESSION_ACCOUNT);
		assertNotNull(result);
		
		Account account = accountingManager.getAccount(result.getAccountId());
		
		assertNotNull(account);
		assertEquals(AccountStatus.OPEN, account.getStatus());
		assertEquals(AccountType.SESSION_ACCOUNT.name(), account.getType());
	}

	
	@Test
    public void testCreateAccount() throws Exception {
        MetaInformation meta = new MetaInformation();
        meta.setGameId(2L);
        meta.setName("n");
        meta.setObjectId("3");
        meta.setUserName("u");
        
        CreateAccountResult result = createAccount(meta, AccountType.STATIC_ACCOUNT);
        
        assertNotNull(result);
        
        Account account = accountingManager.getAccount(result.getAccountId());
        
        assertNotNull(account);
        assertEquals(AccountStatus.OPEN, account.getStatus());
        assertEquals(AccountType.STATIC_ACCOUNT.toString(), account.getType());
        assertEquals("2", account.getAttribute(WalletDTOFactory.GAME_ID_KEY));
        assertEquals("3", account.getAttribute(WalletDTOFactory.OBJECT_ID_KEY));
    }
	
	@Test
	public void testCreateAndCloseSession() throws Exception {
        MetaInformation meta = new MetaInformation();
        meta.setGameId(2L);
        meta.setName("n");
        meta.setObjectId("3");
        meta.setUserName("u");
        CreateAccountResult result = createAccount(meta, AccountType.SESSION_ACCOUNT);
		
		walletService.closeAccount(result.getAccountId());
	}
	
	@Test
	public void testCloseSessionWithNonZeroBalance() throws AccountNotFoundException, com.cubeia.backoffice.wallet.api.dto.exception.AccountClosedException {
        long s1 = createNewSession(1);
        long s2 = createNewSession(2);
        assertEquals(0, walletService.getAccountBalance(s1).getBalance().getAmount().intValueExact());
        assertEquals(0, walletService.getAccountBalance(s2).getBalance().getAmount().intValueExact());
	    
	    Transaction tx = accountingManager.createTransaction(null, null, BigDecimal.ONE, s1, s2, null);
        assertNotNull(tx);
	    
	    // close session
        walletService.closeAccount(s1);
	    
        // check that account is marked as closed with non zero balance
        // Account a = accountingManager.getAccount(s1);
        // assertEquals(WalletAccountStatus.CLOSED_WITH_NON_ZERO_BALANCE, a.getStatus());
        
        // creating new transactions against the closed account should fail
        try {
            tx = accountingManager.createTransaction(null, null, BigDecimal.ONE, s1, s2, null);
            fail("exception should have been thrown");
        } catch (com.cubeia.backoffice.accounting.core.AccountClosedException e) {
            // this should happen
        }
	}
	
	@Test
	public void testFailingCloseSession() {
        // close non existing session
        try {
            walletService.closeAccount(9292929l);
            fail("an account error should have been thrown");
        } catch (AccountNotFoundException e) {
            // this should happen
        } catch (Throwable t) {
            fail("wrong exception thrown");
        }
	}
	
	@Test
	public void testWithdrawFromRemoteWalletToSession() {
	    long s = createNewSession(1);
	    UUID uuid = UUID.randomUUID();
	    
        WithdrawResult wr = walletService.withdrawFromRemoteWalletToAccount(uuid, 1, s, 99,  new Money("XXX", 3, new BigDecimal(100)));
        assertEquals(uuid, wr.getRequestId());
        assertTrue(wr.getTransactionId() > 0);
        
        assertEquals(100, mockExternalWalletImpl.withdrawAmount.intValueExact());
        assertEquals(99, mockExternalWalletImpl.withdrawLicenseeId);
        // cannot test this yet, it is mocked
        //assertEquals(1, mockExternalWalletImpl.withdrawExternalUserId);
	}
	
	@Test
    public void testDepositFromSessionToRemoteWallet() {
        long s1 = createNewSession(1);
        long s2 = createNewSession(1);
        UUID uuid = UUID.randomUUID();

        // insert some money to withdraw
        Transaction tx = accountingManager.createTransaction(null, null, BigDecimal.ONE, s2, s1, null);
        assertNotNull(tx);
        
        DepositResult wr = walletService.depositFromAccountToRemoteWallet(uuid, 1, s1, 999, 
                new Money("XXX", 3, BigDecimal.ONE));
        assertEquals(uuid, wr.getRequestId());
        assertTrue(wr.getTransactionId() > 0);
        
        assertEquals(1, mockExternalWalletImpl.depositAmount.intValueExact());
        assertEquals(999, mockExternalWalletImpl.depositLicenseeId);
        // cannot test this yet, it is mocked
        //assertEquals(1, mockExternalWalletImpl.depositExternalUserId);
    }
    
	@Test
    public void testListAccountsFiltering() {
        Account a0 = accountingManager.createAccount(AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString()));
        Account a1 = accountingManager.createAccount(AccountFactory.create(1338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString()));
        Account a2 = accountingManager.createAccount(AccountFactory.create(1339l, "EUR", 2, AccountType.SYSTEM_ACCOUNT.toString()));
        assertNotNull(a0.getId());
        
        // no filter
        AccountQueryResult res = walletService.listAccounts(null, null, null, null, 0, 10, null, true);
        assertNotNull(res);
        assertEquals(3, res.getAccounts().size());
        
        // check sanity of result container
        assertEquals(10, res.getQueryLimit());
        assertEquals(0, res.getQueryOffset());
        assertEquals(3, res.getTotalQueryResultSize());
        
        // filter by account id
        res = walletService.listAccounts(a0.getId(), null, null, null, 0, 10, null, true);
        assertNotNull(res);
        assertEquals(1, res.getAccounts().size());
        assertEquals(a0.getId(), res.getAccounts().get(0).getId());
        
        // filter by user id
        res = walletService.listAccounts(null, a1.getUserId(), null, null, 0, 10, null, true);
        assertNotNull(res);
        assertEquals(1, res.getAccounts().size());
        assertEquals(a1.getId(), res.getAccounts().get(0).getId());

        // filter by type 
        res = walletService.listAccounts(null, null, null, Arrays.asList(SYSTEM_ACCOUNT), 0, 10, null, true);
        assertNotNull(res);
        assertEquals(1, res.getAccounts().size());
        assertEquals(a2.getId(), res.getAccounts().get(0).getId());
        
        // filter by status
        a0.setStatus(AccountStatus.CLOSED);
        accountingManager.setAccountStatus(a0.getId(), AccountStatus.CLOSED);
        a2.setStatus(AccountStatus.CLOSED);
        accountingManager.setAccountStatus(a2.getId(), AccountStatus.CLOSED);
        // accountingManager.updateAccount(a0);
        // accountingManager.updateAccount(a2);

        res = walletService.listAccounts(null, null, Arrays.asList(CLOSED), null, 0, 10, null, true);
        assertNotNull(res);
        assertEquals(2, res.getAccounts().size());
        assertTrue(
            a0.getId().equals(res.getAccounts().get(0).getId())  ||
            a0.getId().equals(res.getAccounts().get(1).getId()));
        assertTrue(
            a2.getId().equals(res.getAccounts().get(0).getId())  ||
            a2.getId().equals(res.getAccounts().get(1).getId()));
    }

	@Test
    public void testListAccountsOrdering() {
        
        /*WalletAccountInformation ai0 = new WalletAccountInformation();
        ai0.setName("a0");
        WalletAccountInformation ai1 = new WalletAccountInformation();
        ai1.setName("a1");
        WalletAccountInformation ai2 = new WalletAccountInformation();
        ai2.setName("a2");*/
        
    	Account a0 = AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        a0.setAttribute(WalletDTOFactory.GAME_NAME_KEY, "a0");
        a0.setName("a0");
        
        Account a1 = AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        a1.setAttribute(WalletDTOFactory.GAME_NAME_KEY, "a1");
        a1.setName("a1");
        
        Account a2 = AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        a2.setAttribute(WalletDTOFactory.GAME_NAME_KEY, "a2");
    	a2.setName("a2");
        
    	a0 = accountingManager.createAccount(a0);
        a1 = accountingManager.createAccount(a1);
        a2 = accountingManager.createAccount(a2);
        
        a2.setStatus(AccountStatus.CLOSED);
        a1.setStatus(AccountStatus.CLOSED);
        
        assertTrue(a0.getId() < a1.getId()  &&  a1.getId() < a2.getId());
        
        // order by id
        AccountQueryResult res = walletService.listAccounts(null, null, null, null, 0, 10, AccountsOrder.ID, true);
        List<com.cubeia.backoffice.wallet.api.dto.Account> accounts = res.getAccounts();
        assertEquals(a0.getId(), accounts.get(0).getId());
        assertEquals(a1.getId(), accounts.get(1).getId());
        assertEquals(a2.getId(), accounts.get(2).getId());
        res = walletService.listAccounts(null, null, null, null, 0, 10, AccountsOrder.ID, false);
        accounts = res.getAccounts();
        assertEquals(a0.getId(), accounts.get(2).getId());
        assertEquals(a1.getId(), accounts.get(1).getId());
        assertEquals(a2.getId(), accounts.get(0).getId());
        
        // order by user id
        res = walletService.listAccounts(null, null, null, null, 0, 10, AccountsOrder.USER_ID, true);
        accounts = res.getAccounts();
        assertEquals(a0.getId(), accounts.get(0).getId());
        assertEquals(a1.getId(), accounts.get(1).getId());
        assertEquals(a2.getId(), accounts.get(2).getId());
        
        // order by information.name descending
        res = walletService.listAccounts(null, null, null, null, 0, 10, AccountsOrder.ACCOUNT_NAME, false);
        accounts = res.getAccounts();
        assertEquals(a2.getId(), accounts.get(0).getId());
        assertEquals(a1.getId(), accounts.get(1).getId());
        assertEquals(a0.getId(), accounts.get(2).getId());
    }
    
	@Test
    public void testListAccountsLimits() {
        Account a0 = AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        Account a1 = AccountFactory.create(1338l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        Account a2 = AccountFactory.create(1339l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        
        a0 = accountingManager.createAccount(a0);
        a1 = accountingManager.createAccount(a1);
        a2 = accountingManager.createAccount(a2);
        
        AccountQueryResult res = walletService.listAccounts(null, null, null, null, 0, 10, null, true);
        assertEquals(3, res.getAccounts().size());
        assertEquals(3, res.getTotalQueryResultSize());
        
        res = walletService.listAccounts(null, null, null, null, 0, 1, AccountsOrder.ID, true);
        assertEquals(1, res.getAccounts().size());
        assertEquals(3, res.getTotalQueryResultSize());
        assertEquals(a0.getId(), res.getAccounts().get(0).getId());
        
        res = walletService.listAccounts(null, null, null, null, 1, 2, AccountsOrder.ID, true);
        assertEquals(2, res.getAccounts().size());
        assertEquals(3, res.getTotalQueryResultSize());
        assertEquals(a1.getId(), res.getAccounts().get(0).getId());
        assertEquals(a2.getId(), res.getAccounts().get(1).getId());
    }
    
	@Test
    public void testListTransactionsFiltering() throws InterruptedException {
        Account a0 = AccountFactory.create(1337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        Account a1 = AccountFactory.create(1338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString());
        Account a2 = AccountFactory.create(1339l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString());
        
        a0 = accountingManager.createAccount(a0);
        a1 = accountingManager.createAccount(a1);
        a2 = accountingManager.createAccount(a2);
        
        // create transactions
        Transaction tx0 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a0.getId(), a1.getId(), null);
        tx0.setTimestamp(new Date(100000));
        Transaction tx1 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a0.getId(), null);
        tx1.setTimestamp(new Date(200000));
        Transaction tx2 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a2.getId(), null);
        tx2.setTimestamp(new Date(300000));
        
        assertTrue(tx1.getTimestamp().after(tx0.getTimestamp()));
        assertTrue(tx2.getTimestamp().after(tx1.getTimestamp()));
        
        // no filter
        TransactionQueryResult res = walletService.listTransactions(
            null, false, false, 
            null, true, false, 
            null, null, 0, 10, null, true);
        assertEquals(3, res.getTransactions().size());
        
        // check sanity of result container
        assertEquals(10, res.getQueryLimit());
        assertEquals(0, res.getQueryOffset());
        assertEquals(3, res.getTotalQueryResultSize());
        
        // filter by account from account id
        res = walletService.listTransactions(
            a1.getId(), false, false, 
            null, true, false, 
            null, null, 0, 10, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx2.getId(), res.getTransactions().get(1).getId());

        res = walletService.listTransactions(
            a0.getId(), false, false, 
            null, true, false, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        
        res = walletService.listTransactions(
            null, false, false, 
            a2.getId(), true, false, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx2.getId(), res.getTransactions().get(0).getId());

        res = walletService.listTransactions(
            a1.getId(), false, false, 
            a0.getId(), true, false, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());

        // filter by user id
        res = walletService.listTransactions(
        	a1.getUserId(), false, true, 
            null, true, false, 
            null, null, 0, 10, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx2.getId(), res.getTransactions().get(1).getId());

        // filter by from user id to account id
        res = walletService.listTransactions(
        		a1.getUserId(), false, true, 
            a0.getId(), true, false, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());

        // filter by from account id to user id
        // FIXME: Fails for some reason
        /*
        res = walletService.listTransactions(
            a1.getId(), false, false, 
            a1.getUserId(), true, true, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        */
        
        // filter by debit/credit flags
        res = walletService.listTransactions(
            a1.getId(), true, false, 
            a0.getId(), false, false, 
            null, null, 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        
        res = walletService.listTransactions(
            null, true, false, 
            a1.getId(), false, false, 
            null, null, 0, 10, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx2.getId(), res.getTransactions().get(1).getId());
        
        res = walletService.listTransactions(
            a0.getId(), null, false, 
            a1.getId(), null, false, 
            null, null, 0, 10, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx1.getId(), res.getTransactions().get(1).getId());
        
        // filter by timestamp
        res = walletService.listTransactions(
            null, false, false, 
            null, true, false, 
            tx0.getTimestamp(), tx1.getTimestamp(), 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());

        res = walletService.listTransactions(
            null, false, false, 
            null, true, false, 
            new Date(tx0.getTimestamp().getTime() - 10000), tx1.getTimestamp(), 0, 10, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        
        res = walletService.listTransactions(
            null, false, false, 
            null, true, false, 
            tx0.getTimestamp(), new Date(tx1.getTimestamp().getTime() + 1000), 0, 10, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx1.getId(), res.getTransactions().get(1).getId());
    }
    
	@Test
    public void testListTransactionsLimits() throws InterruptedException {
        Account a0 = accountingManager.createAccount(AccountFactory.create(2337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString()));
        Account a1 = accountingManager.createAccount(AccountFactory.create(2338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString()));
        Account a2 = accountingManager.createAccount(AccountFactory.create(2338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString()));
        
        Transaction tx0 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a0.getId(), a1.getId(), null);
        Transaction tx1 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a0.getId(), null);
        Transaction tx2 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a2.getId(), null);
        
        TransactionQueryResult res = walletService.listTransactions(null, false, false, null, true, false, null, null, 0, 1, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx0.getId(), res.getTransactions().get(0).getId());
        assertEquals(3, res.getTotalQueryResultSize());
        
        res = walletService.listTransactions(null, false, false, null, true, false, null, null, 1, 1, null, true);
        assertEquals(1, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(3, res.getTotalQueryResultSize());
        
        res = walletService.listTransactions(null, false, false, null, true, false, null, null, 1, 2, null, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx2.getId(), res.getTransactions().get(1).getId());
        assertEquals(3, res.getTotalQueryResultSize());
    }
    
	@Test
    public void testListTransactionsOrdering() throws InterruptedException {
        Account a0 = accountingManager.createAccount(AccountFactory.create(2337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString()));
        Account a1 = accountingManager.createAccount(AccountFactory.create(2338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString()));
        Account a2 = accountingManager.createAccount(AccountFactory.create(2338l, "EUR", 2, AccountType.STATIC_ACCOUNT.toString()));
        
        Transaction tx0 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a0.getId(), a1.getId(), null);
        Transaction tx1 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a0.getId(), null);
        Transaction tx2 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a2.getId(), null);
        
        TransactionQueryResult res = walletService.listTransactions(null, false, false, null, true, false, null, null, 1, 2, TransactionsOrder.ID, true);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx2.getId(), res.getTransactions().get(1).getId());
        assertEquals(3, res.getTotalQueryResultSize());
        
        res = walletService.listTransactions(null, false, false, null, true, false, null, null, 1, 2, TransactionsOrder.ID, false);
        assertEquals(2, res.getTransactions().size());
        assertEquals(tx1.getId(), res.getTransactions().get(0).getId());
        assertEquals(tx0.getId(), res.getTransactions().get(1).getId());
        assertEquals(3, res.getTotalQueryResultSize());
    }
	
	@Test
    public void testGetTransactionById() {
        Account a0 = accountingManager.createAccount(AccountFactory.create(2337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString()));
        Account a1 = accountingManager.createAccount(AccountFactory.create(2338l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString()));
        
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put("key", "value");
        
        Transaction tx0 = accountingManager.createTransaction("comment", null, BigDecimal.ONE, a0.getId(), a1.getId(), attribs);
        accountingManager.createTransaction(null, null, BigDecimal.ONE, a1.getId(), a0.getId(), null);
        
        Transaction tx = accountingManager.getTransactionById(-12l);
        assertNull(tx);
        
        com.cubeia.backoffice.wallet.api.dto.Transaction txDTO = walletService.getTransactionById(tx0.getId());
        assertEquals(tx0.getId(), txDTO.getId());
        assertEquals("comment", txDTO.getComment());
        assertEquals(tx0.getTimestamp(), txDTO.getTimestamp());
        assertEquals(tx0.getAttribute("key"), txDTO.getAttributes().get("key"));
        // assertFalse(txDTO.isManual());
        assertEquals(2, txDTO.getEntries().size());
        Iterator<com.cubeia.backoffice.wallet.api.dto.Entry> eIter = txDTO.getEntries().iterator();
        com.cubeia.backoffice.wallet.api.dto.Entry e1 = eIter.next();
        com.cubeia.backoffice.wallet.api.dto.Entry e2 = eIter.next();
        if (!e1.getAccountId().equals(a0.getId())) {
            com.cubeia.backoffice.wallet.api.dto.Entry e = e1;
            e1 = e2;
            e2 = e;
        }
        assertEquals(e1.getAccountId(), a0.getId());
        assertEquals(e2.getAccountId(), a1.getId());
        assertEquals(new BigDecimal("-1").intValue(), e1.getAmount().getAmount().intValue());
        assertEquals(new BigDecimal("1").intValue(), e2.getAmount().getAmount().intValue());
        assertEquals(new BigDecimal("-1").intValue(), e1.getResultingBalance().getAmount().intValue());
        assertEquals(new BigDecimal("1").intValue(), e2.getResultingBalance().getAmount().intValue());
        assertEquals("EUR", e1.getAmount().getCurrencyCode());
        assertEquals("EUR", e2.getAmount().getCurrencyCode());
        assertEquals("EUR", e1.getResultingBalance().getCurrencyCode());
        assertEquals("EUR", e2.getResultingBalance().getCurrencyCode());
        assertEquals(tx0.getId(), e1.getTransactionId());
        assertEquals(tx0.getId(), e2.getTransactionId());
    }
    
	@Test
    public void testListEntries() throws InterruptedException {
        Account a0 = AccountFactory.create(2337l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        Account a1 = AccountFactory.create(2338l, "EUR", 2, AccountType.SESSION_ACCOUNT.toString());
        
        a0 = accountingManager.createAccount(a0);
        a1 = accountingManager.createAccount(a1);
        
        // create transactions
        Transaction tx0 = accountingManager.createTransaction(null, null, BigDecimal.ONE, a0.getId(), a1.getId(), null);
        tx0.setTimestamp(new Date(100000));
        Transaction tx1 = accountingManager.createTransaction(null, null, BigDecimal.TEN, a1.getId(), a0.getId(), null);
        tx1.setTimestamp(new Date(200000));
        
        assertTrue(tx1.getTimestamp().after(tx0.getTimestamp()));

        // no filter
        EntriesQueryResult res = walletService.listEntries(null, true, 0, 1000, true);
        assertEquals(4, res.getEntries().size());
        assertNull(res.getEntries().get(0).getResultingBalance());
        
        // filter by id
        res = walletService.listEntries(a0.getId(), false, 0, 1000, true);
        assertEquals(2, res.getEntries().size());
        assertEquals(new BigDecimal("-1.00"), res.getEntries().get(0).getAmount().getAmount());
        assertEquals(new BigDecimal("10.00"), res.getEntries().get(1).getAmount().getAmount());
        assertEquals(a0.getId(), res.getEntries().get(0).getAccountId());
        assertEquals(a0.getId(), res.getEntries().get(1).getAccountId());
        assertNull(res.getEntries().get(0).getResultingBalance());
        assertNull(res.getEntries().get(1).getResultingBalance());
        
        // check sanity of result container
        assertEquals(1000, res.getQueryLimit());
        assertEquals(0, res.getQueryOffset());
        assertEquals(2, res.getTotalQueryResultSize());
        
        // descending order
        res = walletService.listEntries(a0.getId(), false, 0, 1000, false);
        assertEquals(2, res.getEntries().size());
        assertEquals(new BigDecimal("10.00"), res.getEntries().get(0).getAmount().getAmount());
        assertEquals(new BigDecimal("-1.00"), res.getEntries().get(1).getAmount().getAmount());
        
        // limit
        res = walletService.listEntries(a0.getId(), false, 0, 1, false);
        assertEquals(1, res.getEntries().size());
        assertEquals(new BigDecimal("10.00"), res.getEntries().get(0).getAmount().getAmount());
        
        // resulting balances
        res = walletService.listEntries(a0.getId(), true, 0, 1000, true);
        assertEquals(2, res.getEntries().size());
        assertEquals(-1, res.getEntries().get(0).getResultingBalance().getAmount().intValueExact());
        assertEquals(9, res.getEntries().get(1).getResultingBalance().getAmount().intValueExact());

        // resulting balances, reverse order
        res = walletService.listEntries(a0.getId(), true, 0, 1000, false);
        assertEquals(2, res.getEntries().size());
        assertEquals(-1, res.getEntries().get(1).getResultingBalance().getAmount().intValueExact());
        assertEquals(9, res.getEntries().get(0).getResultingBalance().getAmount().intValueExact());
    }    

	@Test
	public void testGetSupportedCurrencies() {
		Collection<Currency> scl = walletService.getSupportedCurrencies();
		assertEquals(1, scl.size()); // EUR added in BaseTest
		
		walletService.addSupportedCurrency(new Currency("EUR-X", 2));
		walletService.addSupportedCurrency(new Currency("GBP-X", 2));
		walletService.addSupportedCurrency(new Currency("SEK-X", 2));
        int currenciesBeforeRemoving = walletService.getSupportedCurrencies().size();
		walletService.removeSupportedCurrency("EUR-X");
		
		scl = walletService.getSupportedCurrencies();
		assertEquals(currenciesBeforeRemoving - 1, scl.size());
		assertTrue(scl.contains(new Currency("GBP-X", 2)));
		assertTrue(scl.contains(new Currency("SEK-X", 2)));
	}
	
	@Test
	public void testAddSupportedCurrency() {
		walletService.addSupportedCurrency(new Currency("XXX", 5));
		Collection<Currency> curs = walletService.getSupportedCurrencies();
		assertTrue(curs.contains(new Currency("XXX", 5)));
	}
	
	@Test
	public void testAddSupportedCurrencyPreviouslyRemoved() {
		walletService.addSupportedCurrency(new Currency("YYY", 2));
		walletService.removeSupportedCurrency("YYY");
		assertNull(walletService.getSupportedCurrency("YYY"));
		
		walletService.addSupportedCurrency(new Currency("YYY", 2));
		Currency c = walletService.getSupportedCurrency("YYY");
		assertNotNull(c);
		assertEquals(2, c.getFractionalDigits());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testErrorOnReactivateCurrencyWithFractionalDigitsDiff() {
		walletService.addSupportedCurrency(new Currency("ZZZ", 2));
		walletService.removeSupportedCurrency("ZZZ");
		walletService.addSupportedCurrency(new Currency("ZZZ", 3));
	}
	
	private long createNewSession(long userId) {
	    MetaInformation meta = new MetaInformation();
	    meta.setGameId(1L);
	    meta.setName("u");
	    meta.setUserName("n");
	    meta.setObjectId("2");
	    CreateAccountResult result = createAccount(userId, meta, AccountType.SESSION_ACCOUNT);
		return result.getAccountId();
	}
	
	private CreateAccountResult createAccount(MetaInformation meta, AccountType type) {
		return createAccount(1l, meta, type);
	}
	
	private CreateAccountResult createAccount(Long userId, MetaInformation meta, AccountType type) {
		CreateAccountRequest request = new CreateAccountRequest(UUID.randomUUID(), userId, "EUR", type, meta);
		request.setNegativeBalanceAllowed(true);
		CreateAccountResult result = walletService.createAccount(request);
		return result;
	}
	
}
