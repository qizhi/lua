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

package com.cubeia.backoffice.accounting.dao;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.cubeia.backoffice.accounting.BaseTest;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.BalanceCheckpoint;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;

public class AccountingDAOTest extends BaseTest {

    @Test
    public void testGetLatestBalanceCheckpoint() {
    	// create two accounts
    	Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
    	Account a1 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
		accountingDAO.saveOrUpdate(a0);
		accountingDAO.saveOrUpdate(a1);
    	assertNotNull(a0.getId());
    	assertNotNull(a1.getId());

    	BalanceCheckpoint cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), null);
    	assertNull(cp);
    	cp = accountingDAO.getLatestBalanceCheckpoint(a1.getId(), null);
    	assertNull(cp);
    	
    	// create entries and checkpoints
    	Entry e0a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e1a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e0a1 = new Entry(a1, BigDecimal.ONE);
    	accountingDAO.saveOrUpdate(e0a0);
    	accountingDAO.saveOrUpdate(e1a0);
    	accountingDAO.saveOrUpdate(e0a1);
    	
    	BalanceCheckpoint cp0a0 = new BalanceCheckpoint(e0a0, new BigDecimal(1));
    	BalanceCheckpoint cp1a0 = new BalanceCheckpoint(e1a0, new BigDecimal(2));
    	BalanceCheckpoint cp0a1 = new BalanceCheckpoint(e0a1, new BigDecimal(1));
    	accountingDAO.saveOrUpdate(cp0a0);
    	accountingDAO.saveOrUpdate(cp1a0);
    	accountingDAO.saveOrUpdate(cp0a1);
    	
    	// get latest checkpoints
    	cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), null);
    	assertEquals(cp1a0.getId(), cp.getId());

    	cp = accountingDAO.getLatestBalanceCheckpoint(a1.getId(), null);
    	assertEquals(cp0a1.getId(), cp.getId());
    }
    
    @Test
    public void testGetPreviousBalanceCheckpoint() {
    	// create two accounts
    	Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
    	Account a1 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
		accountingDAO.saveOrUpdate(a0);
		accountingDAO.saveOrUpdate(a1);
    	assertNotNull(a0.getId());
    	assertNotNull(a1.getId());
    	
    	// create entries and checkpoints
    	Entry e0a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e1a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e2a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e3a0 = new Entry(a0, BigDecimal.ONE);
    	Entry e0a1 = new Entry(a1, BigDecimal.ONE);
    	accountingDAO.saveOrUpdate(e0a0);
    	accountingDAO.saveOrUpdate(e1a0);
    	accountingDAO.saveOrUpdate(e2a0);
    	accountingDAO.saveOrUpdate(e3a0);
    	accountingDAO.saveOrUpdate(e0a1);
    	
    	BalanceCheckpoint cp1a0 = new BalanceCheckpoint(e1a0, new BigDecimal(2));
    	BalanceCheckpoint cp3a0 = new BalanceCheckpoint(e3a0, new BigDecimal(4));
    	accountingDAO.saveOrUpdate(cp1a0);
    	accountingDAO.saveOrUpdate(cp3a0);
    	
    	// check checkpoints
    	BalanceCheckpoint cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), e0a0.getId());
    	assertNull(cp);
    	
    	cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), e1a0.getId());
    	assertEquals(cp1a0.getId(), cp.getId());
    	
    	cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), e2a0.getId());
    	assertEquals(cp1a0.getId(), cp.getId());

    	cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), e3a0.getId());
    	assertEquals(cp3a0.getId(), cp.getId());
    	
    	cp = accountingDAO.getLatestBalanceCheckpoint(a1.getId(), e0a1.getId());
    	assertNull(cp);
    }

    @Test
    public void testGetPreviousBalanceCheckpointWhenOutOfOrder() {
        // create two accounts
        Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
        accountingDAO.saveOrUpdate(a0);
        assertNotNull(a0.getId());
        
        // create entries and checkpoints (out of order)
        
        // e0 - cp1 (latest cp)
        // e1 - cp0 (highest entry)
        // e2   <-- search for latest cp from here
        
        Entry e0 = new Entry(a0, BigDecimal.ONE);
        Entry e1 = new Entry(a0, BigDecimal.ONE);
        Entry e2 = new Entry(a0, BigDecimal.ONE);
        accountingDAO.saveOrUpdate(e0);
        accountingDAO.saveOrUpdate(e1);
        accountingDAO.saveOrUpdate(e2);
        
        BalanceCheckpoint cp0 = new BalanceCheckpoint(e1, new BigDecimal(2));
        BalanceCheckpoint cp1 = new BalanceCheckpoint(e0, new BigDecimal(1));
        accountingDAO.saveOrUpdate(cp0);
        accountingDAO.saveOrUpdate(cp1);
        
        // check checkpoints
        BalanceCheckpoint cp = accountingDAO.getLatestBalanceCheckpoint(a0.getId(), e2.getId());
        assertEquals(cp0.getId(), cp.getId());
    }
    
    @Test
    public void testFindByTypeAndCurrency() {
    	Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
    	accountingDAO.saveOrUpdate(a0);
        assertNotNull(a0.getId());
        assertThat(a0.getId(), notNullValue());
        
        Collection<Account> accounts = accountingDAO.findAccountsByUserId(a0.getUserId(), "system", "EUR");
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
        
        accounts = accountingDAO.findAccountsByUserId(a0.getUserId(), null, "EUR");
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
        
        accounts = accountingDAO.findAccountsByUserId(a0.getUserId(), "system", null);
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
    }
    
    @Test
    public void testFindByTypeWalletAndCurrency() {
    	Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
    	a0.setWalletId(666L);
    	
    	accountingDAO.saveOrUpdate(a0);
        assertNotNull(a0.getId());
        assertThat(a0.getId(), notNullValue());
        
        Collection<Account> accounts = accountingDAO.findAccountsByUserAndWalletId(a0.getUserId(), 666L, "system", "EUR");
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
        
        accounts = accountingDAO.findAccountsByUserId(a0.getUserId(), null, "EUR");
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
        
        accounts = accountingDAO.findAccountsByUserId(a0.getUserId(), "system", null);
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
        
        accounts = accountingDAO.findAccountsByUserAndWalletId(a0.getUserId(), 666L);
        assertThat(accounts.size(), is(1));
        assertThat(accounts.iterator().next().getId(), is(a0.getId()));
    }
    
    @Test
    public void testFindByTypeAndCurrency2() {
    	Account a0 = AccountFactory.create(new Long("1"), "EUR", 2, "system");
    	accountingDAO.saveOrUpdate(a0);
    	Account a1 = AccountFactory.create(new Long("2"), "EUR", 2, "kalle");
    	accountingDAO.saveOrUpdate(a1);
    	Account a2 = AccountFactory.create(new Long("3"), "EUR", 2, "system");
    	accountingDAO.saveOrUpdate(a2);
        
        Collection<Account> accounts = accountingDAO.findAccountsByTypeAndCurrency("system", "EUR");
        assertThat(accounts.size(), is(2));
    }
    
    @Test
    public void testFindCurrency() {
        Date now = new Date();
        
        List<CurrencyRate> crc = accountingDAO.listCurrencyRates("SEK", new Date(0), new Date(now.getTime() + 10000));
        assertThat(crc, notNullValue());
        assertThat(crc.isEmpty(), is(true));
        
        CurrencyRate r0 = new CurrencyRate("EUR", "SEK", new BigDecimal("10"), now);
        CurrencyRate r1 = new CurrencyRate("EUR", "GBP", new BigDecimal("0.8"), now);
        CurrencyRate r2 = new CurrencyRate("SEK", "GBP", new BigDecimal("0.01"), now);
        accountingDAO.saveOrUpdate(r0); 
        accountingDAO.saveOrUpdate(r1); 
        accountingDAO.saveOrUpdate(r2); 
        
        // find 2
        crc = accountingDAO.listCurrencyRates("SEK", new Date(0), new Date(now.getTime() + 10000));
        Assert.assertEquals(2, crc.size());
        
        // add an older rate
        CurrencyRate r3 = new CurrencyRate("SEK", "GBP", new BigDecimal("0.01"), new Date(now.getTime() - 100000L));
        accountingDAO.saveOrUpdate(r3); 
     
        // first check that we find the new one as well
        crc = accountingDAO.listCurrencyRates("SEK", new Date(0), new Date(now.getTime() + 10000));
        Assert.assertEquals(3, crc.size());
        // check order by timestamp+id
        Assert.assertEquals(r3, crc.get(0));
        Assert.assertEquals(r0, crc.get(1));
        Assert.assertEquals(r2, crc.get(2));
        
        // now narrow search and make sure we only get one
        crc = accountingDAO.listCurrencyRates("SEK", new Date(0), new Date(now.getTime() - 10000));
        Assert.assertEquals(1, crc.size());
        
        // test list all
        crc = accountingDAO.listCurrencyRates(null, new Date(0), new Date(now.getTime() + 10000));
        Assert.assertEquals(4, crc.size());
    }
    
    @Test
    public void getCurrencyRateChain() {
        Date now = new Date();
        
        List<CurrencyRate> crc = accountingDAO.getCurrencyRateChain("SEK", "GBP", new Date(0), new Date(now.getTime() + 10000));
        assertThat(crc, notNullValue());
        assertThat(crc.isEmpty(), is(true));
        
        CurrencyRate r0 = new CurrencyRate("EUR", "SEK", new BigDecimal("10"), now);
        CurrencyRate r1 = new CurrencyRate("EUR", "GBP", new BigDecimal("0.8"), now);
        accountingDAO.saveOrUpdate(r0); 
        accountingDAO.saveOrUpdate(r1); 
        
        crc = accountingDAO.getCurrencyRateChain("SEK", "GBP", new Date(0), new Date(now.getTime() + 10000));
        assertThat(crc, notNullValue());
        assertThat(crc.size(), is(2));
        assertThat(crc.contains(r0), is(true));
        assertThat(crc.contains(r1), is(true));
        
        crc = accountingDAO.getCurrencyRateChain("GBP", "SEK", new Date(0), new Date(now.getTime() + 10000));
        assertThat(crc, notNullValue());
        assertThat(crc.size(), is(2));
        assertThat(crc.contains(r0), is(true));
        assertThat(crc.contains(r1), is(true));
        
        // if there is a direct conversion it will not be returned
        CurrencyRate r3 = new CurrencyRate("SEK", "GBP", new BigDecimal("0.01"), now);
        accountingDAO.saveOrUpdate(r3); 
        
        crc = accountingDAO.getCurrencyRateChain("SEK", "GBP", new Date(0), new Date(now.getTime() + 10000));
        assertThat(crc, notNullValue());
        assertThat(crc.size(), is(2));
        assertThat(crc.contains(r3), is(false));
    }
    
    
}
