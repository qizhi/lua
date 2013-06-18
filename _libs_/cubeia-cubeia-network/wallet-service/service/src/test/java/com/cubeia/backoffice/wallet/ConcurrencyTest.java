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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;

public class ConcurrencyTest extends BaseTest {

	private static int INSERT_AMOUNT = 1;
	private static int INSERT_COUNT = 20;
	
//	private JpaTransactionManager transactionManager;

	private ExecutorService executor = Executors.newFixedThreadPool(20);
	
	private ExecutorService assertionThread = Executors.newFixedThreadPool(1);
	
	private CountDownLatch latch = new CountDownLatch(INSERT_COUNT);
	
	private AtomicBoolean failed = new AtomicBoolean(false);
	
	public ConcurrencyTest() {
		super();
	}
	
	@After
	public void onTearDown() throws Exception {
	    // as we must commit the transactions in this test we also have to clean up the mess
	    deleteFromTables(new String[] {"Entry", "AccountAttribute", "Account", "Transaction", "BalanceCheckpoint"});
	}
	/*
	@Override
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		super.setTransactionManager(transactionManager);
		this.transactionManager = (JpaTransactionManager) transactionManager;
	}
	*/

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Account createAccount(){
		Account a = AccountFactory.create(Currency.getInstance("EUR"));
		accountingManager.createAccount(a);
		return a;
	}
	
	@Test
	public void dummy(){}
	
	@Ignore
	public void testInserts() throws Exception {
		//setComplete(); // START TRANSACTION
		Account account = createAccount();//AccountFactory.create(Currency.getInstance("EUR"));
		Account debit = createAccount(); //AccountFactory.create(Currency.getInstance("EUR"));
		//WalletAccountingManager am = accountingManager;
		//am.createAccount(account);
		//am.createAccount(debit);
		//endTransaction(); // END TRANSACTION
		
		assertNotNull(account.getId());
		assertNotNull(debit.getId());
		
		List<Runnable> tasks = new ArrayList<Runnable>();
		
		// Create tasks
		for (int i = 0; i < INSERT_COUNT; i++) {
			tasks.add(new Insert(account, debit, i));
		}
		
		Assertion check = new Assertion(account.getId(), debit.getId());
		assertionThread.execute(check);
		
		// Execute tasks
		for (Runnable task : tasks) {
			executor.execute(task);
		}
		
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		
		assertionThread.shutdown();
		assertionThread.awaitTermination(10, TimeUnit.SECONDS);
		
		if (failed.get()) {
			fail();
		}
	}
	
	
	public class Insert implements Runnable {
		private final Account account;
		private final Account debit;
		private final int count;
		
		public Insert(Account account, Account debit, int count) {
			this.account = account;
			this.debit = debit;
			this.count = count;
		}

		@Override
		public void run() {
			try {
				//ConcurrencyTest.super.
				//TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
				accountingManager.createTransaction("Insert-"+count, null, new BigDecimal(INSERT_AMOUNT), debit.getId(), account.getId(), null);
				//transactionManager.commit(tx);
				latch.countDown();
			} catch (Throwable th) {
				th.printStackTrace();
                failed.set(true);
			}
		}
		
	}
	
	public class Assertion implements Runnable {
		
		private final long accountId;
		private final long debitId;
		
		public Assertion(long accountId, long debitId) {
			this.accountId = accountId;
			this.debitId = debitId;
		}
		
		@Override
		public void run() {
			try {
				latch.await(22, TimeUnit.SECONDS);
				
				BigDecimal balance = accountingManager.getBalance(accountId).getAmount();
				BigDecimal balanceDebit = accountingManager.getBalance(debitId).getAmount();
				
				assertEquals(INSERT_AMOUNT*INSERT_COUNT, balance.longValue());
				assertEquals(-INSERT_AMOUNT*INSERT_COUNT, balanceDebit.longValue());
				assertEquals(0, balanceDebit.longValue() + balance.longValue());
				
			} catch (AssertionFailedError error) {
				error.printStackTrace();
				failed.set(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				failed.set(true);
			} 
		}
	}
}
