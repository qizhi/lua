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

package com.cubeia.backoffice.accounting.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NoSuchConversionRateException;
import com.cubeia.backoffice.accounting.api.TransactionDTO;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;

public class AccountingServiceTest extends BaseTest {
	
	public final static BigDecimal SEK_EUR_RATE = new BigDecimal("0.09616");
	public final static BigDecimal SEK_EUR_INVERSE_RATE = new BigDecimal("10.39933");

	@Test
	public void testMultiCurrencyCalculationOne() throws Exception {
		Account from = createAccount("SEK");
		Account to = createAccount("EUR");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
	
		Money m = accountingService.calculateMultiCurrencyConvertion("SEK", new BigDecimal("100"), from.getId(), to.getId());
	
		Assert.assertEquals(m.getCurrencyCode(), "EUR");
		Assert.assertEquals(2, m.getFractionalDigits());
		Assert.assertEquals(new BigDecimal("9.61"), m.getAmount());
	}
	
	@Test
	public void testMultiCurrencyCalculationTwo() throws Exception {
		Account from = createAccount("SEK");
		Account to = createAccount("EUR");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
	
		Money m = accountingService.calculateMultiCurrencyConvertion("EUR", new BigDecimal("10"), from.getId(), to.getId());
	
		Assert.assertEquals(m.getCurrencyCode(), "SEK");
		Assert.assertEquals(2, m.getFractionalDigits());
		Assert.assertEquals(new BigDecimal("104.00"), m.getAmount());
		
	}
	
	@Test
	public void testMultiCurrencyTrans() throws Exception {
		Account from = createAccount("SEK");
		Account to = createAccount("EUR");
		
		Account eurConv = createAccount(new Long("1"), "EUR", 2, "CONV");
		Account sekConv = createAccount(new Long("2"), "SEK", 2, "CONV");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
		
		List<Long> cons = new LinkedList<Long>();
		cons.add(eurConv.getId());
		cons.add(sekConv.getId());
		
		accountingService.createMultiCurrencyTransaction("test1", null, "SEK", new BigDecimal("100"), from.getId(), to.getId(), "CONV");
		
		/*
		 * First, this should equal the given amount and currency we've specified
		 */
		Assert.assertEquals(new BigDecimal("100.00"), accountingManager.getBalance(sekConv.getId()).getAmount());
		Assert.assertEquals(new BigDecimal("-100.00"), accountingManager.getBalance(from.getId()).getAmount());
	
		/*
		 * This should equal the exchange rate *ROUNDED DOWN* in order for us not to loose
		 * money on the transaction.
		 */
		Assert.assertEquals(new BigDecimal("-9.61"), accountingManager.getBalance(eurConv.getId()).getAmount());
		Assert.assertEquals(new BigDecimal("9.61"), accountingManager.getBalance(to.getId()).getAmount());
	}
	
	@Test
	public void testMultiCurrencyTransInverse() throws Exception {
		Account from = createAccount("SEK");
		Account to = createAccount("EUR");
		
		Account eurConv = createAccount("EUR");
		Account sekConv = createAccount("SEK");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
		
		accountingService.createMultiCurrencyTransaction("test1", null, "EUR", new BigDecimal("10"), from.getId(), sekConv.getId(), to.getId(), eurConv.getId());
		
		/*
		 * First, this should equal the given amount and currency we've specified
		 */
		Assert.assertEquals(new BigDecimal("-10.00"), accountingManager.getBalance(eurConv.getId()).getAmount());
		Assert.assertEquals(new BigDecimal("10.00"), accountingManager.getBalance(to.getId()).getAmount());
	
		/*
		 * This should equal the exchange rate *ROUNDED UP* in order for us not to loose
		 * money on the transaction. Aka, 10 * <inverse rate> (round up).
		 */
		Assert.assertEquals(new BigDecimal("104.00"), accountingManager.getBalance(sekConv.getId()).getAmount());
		Assert.assertEquals(new BigDecimal("-104.00"), accountingManager.getBalance(from.getId()).getAmount());
	}
	
	@Test
	public void testMultiCurrencyTransShortcut() throws Exception {
		Account from = createAccount("EUR");
		Account to = createAccount("EUR");
		
		Account eurConv = createAccount("EUR");
		Account sekConv = createAccount("EUR");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
		
		TransactionDTO dto = accountingService.createMultiCurrencyTransaction("test1", null, "EUR", new BigDecimal("10"), from.getId(), sekConv.getId(), to.getId(), eurConv.getId());

		/*
		 * This should have created a shortcut to an ordinary transaction. Hence only
		 * two entries (instead of four).
		 */
		Assert.assertEquals(2, dto.getEntries().size());
	}
	
	@Test
	public void testMultiCurrencyNoSuchconversionRate() throws Exception {
		Account from = createAccount("EUR");
		Account to = createAccount("SEK");
		
		Account eurConv = createAccount("EUR");
		Account sekConv = createAccount("SEK");
		
		try {
			/*
			 * This should fail as there's no conversion rate in the system...
			 */
			accountingService.createMultiCurrencyTransaction("test1", null, "EUR", new BigDecimal("10"), from.getId(), sekConv.getId(), to.getId(), eurConv.getId());
			Assert.fail("Expected an exception due to missing conversion rate");
		} catch(NoSuchConversionRateException e) {
			// Expected
		}
	}
	
	@Test
	public void testMultiCurrencyCurrencyMismatch() throws Exception {
		Account from = createAccount("EUR");
		Account to = createAccount("SEK");
		
		Account eurConv = createAccount("EUR");
		Account sekConv = createAccount("SEK");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
		
		try {
			/*
			 * This should fail as neither accounts are in brittish pounds...
			 */
			accountingService.createMultiCurrencyTransaction("test1", null, "GPB", new BigDecimal("10"), from.getId(), sekConv.getId(), to.getId(), eurConv.getId());
			Assert.fail("Expected an exception due to currency mismatch");
		} catch(IllegalArgumentException e) {
			// Expected
		}
	}
	
	@Test
	public void testMultiCurrencyconversionAccountMismatch() throws Exception {
		Account from = createAccount("EUR");
		Account to = createAccount("SEK");
		
		Account eurConv = createAccount("GBP");
		Account sekConv = createAccount("SEK");
		
		accountingManager.addCurrencyRate(new CurrencyRate("SEK", "EUR", SEK_EUR_RATE, new Date()));
		
		try {
			/*
			 * This should fail the conversion accounts does not match the real account currencies...
			 */
			accountingService.createMultiCurrencyTransaction("test1", null, "EUR", new BigDecimal("10"), from.getId(), sekConv.getId(), to.getId(), eurConv.getId());
			Assert.fail("Expected an exception due to conversion account currency mismatch");
		} catch(IllegalArgumentException e) {
			// Expected
		}
	}
}
