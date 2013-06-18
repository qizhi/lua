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

package com.cubeia.backoffice.accounting.perf;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.dao.AccountingDAO;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;

/**
 * Naive performance test. This test is excluded in the maven build.
 * To use it an external database is needed. See the accounting-app-perf-test.xml for details.
 * @author w
 */
@ContextConfiguration(locations = {"classpath:accounting-app-perf-test.xml"})
@TransactionConfiguration(transactionManager = "accounting.transactionManager")
public class PerfTest extends AbstractTransactionalJUnit4SpringContextTests {
	private Logger log = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "accounting.accountingManager")
	protected AccountingManager accountingManager;

	@Resource(name = "accounting.accountingDAO")
	protected AccountingDAO accountingDAO;
	
	@Resource(name = "accounting.transactionManager")
	protected PlatformTransactionManager txManager;
	
	private TransactionTemplate txTemplate;
	
	@PostConstruct
	public void init() {
	    txTemplate = new TransactionTemplate(txManager);
	}
	
	@Test
	public void dummy() {}
	
	// Enable MySQL Access for this test
	// @Test 
	@Rollback(false)
	public void naivePerfTest() {
	    int COUNT = 10000;
	    long time = System.currentTimeMillis();
	    
        Account a1 = accountingManager.createAccount(new Account("EUR", 2));
        Account a2 = accountingManager.createAccount(new Account("EUR", 2));
	    
        for (int i = 0; i < COUNT; i++) {
            doRandomTransaction(a1, a2);
            if (i % 100 == 0) {
                doGetBalance(a1);
                doGetBalance(a2);
            }
        }
        
        time = System.currentTimeMillis() - time;
        
        log.info("done in {} ms", time);
        log.info("{} tx/s", (1000 * COUNT / time));
	}
	
	private void doRandomTransaction(final Account a1, final Account a2) {
	    txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Entry e1 = new Entry(a1, new BigDecimal("1.00"));
                Entry e2 = new Entry(a2, new BigDecimal("-1.00"));
                List<Entry> entries = Arrays.asList(e1, e2);
                Transaction tx = accountingManager.createTransaction("comment", "tx-x", entries , null);
                log.debug("created tx: {}", tx.getId());
            }
        });
	}
	
    private void doGetBalance(final Account a) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Money b = accountingManager.getBalance(a.getId());
                log.debug("balance for aId = {}: {}", a.getId(), b);
            }
        });
    }
}
