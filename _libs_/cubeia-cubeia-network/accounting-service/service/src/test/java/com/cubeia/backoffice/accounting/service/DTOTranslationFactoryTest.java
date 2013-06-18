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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.junit.Test;

import com.cubeia.backoffice.accounting.api.AccountDTO;
import com.cubeia.backoffice.accounting.api.AccountStatusDTO;
import com.cubeia.backoffice.accounting.api.AccountsOrderDTO;
import com.cubeia.backoffice.accounting.api.BalancedEntryDTO;
import com.cubeia.backoffice.accounting.api.CurrencyRateDTO;
import com.cubeia.backoffice.accounting.api.EntryDTO;
import com.cubeia.backoffice.accounting.api.TransactionDTO;
import com.cubeia.backoffice.accounting.api.TransactionsOrderDTO;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.TransactionsOrder;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;

public class DTOTranslationFactoryTest {

	private final DTOTranslationFactory f = new DTOTranslationFactory();
	
	@Test
	public void testAccountsOrder() {
		AccountsOrder o = f.fromDTO(AccountsOrderDTO.CLOSE_DATE);
		assertEquals(AccountsOrder.CLOSE_DATE, o);
	}

    @Test
	public void testTransactionsOrder() {
		TransactionsOrder t = f.fromDTO(TransactionsOrderDTO.ID);
		assertEquals(TransactionsOrder.ID, t);
	}

    @Test
	public void testBalancedEntry() {
		Entry e = createEntry(10L, new BigDecimal("100"), createAccount(new Long("9487"), "EUR"), null);
		BalancedEntry be = new BalancedEntry(e, new BigDecimal("900"));
		Transaction txe = new Transaction();
		txe.setId(123L);
		txe.setTimestamp(new Date(1));
		txe.setComment("klyka");
        txe.setAttribute("a", "A");
        be.setTransaction(txe);
		
		BalancedEntryDTO dto = f.toDTO(be);
		assertThat(dto.getBalance(), is(be.getBalance()));
		assertThat(dto.getId(), is(be.getId()));
		assertThat(dto.getAmount(), is(be.getAmount()));
		assertThat(dto.getTransactionId(), is(123L));
		assertThat(dto.getTransactionComment(), is("klyka"));
		assertThat(dto.getTransactionTimestamp(), is(new Date(1)));
		assertThat(dto.getTransactionAttributes(), notNullValue());
        assertThat(dto.getTransactionAttributes().get("a"), is("A"));
	}

    @Test
	public void testTransaction() {
		Account a = createAccount(new Long("239874"), "EUR");
		Entry e1 = createEntry(11L, new BigDecimal("10"), a, null);
		Entry e2 = createEntry(12L, new BigDecimal("-10"), a, null);
		Transaction tr = new Transaction("comment", e1, e2);
		e1.setTransaction(tr);
		e2.setTransaction(tr);
		tr.setAttribute("namn", "kalle");
		TransactionDTO dto = f.toDTO(tr);
		Transaction test = f.fromDTO(dto);
		assertEquals(tr.getComment(), test.getComment());
		assertEquals(tr.getEntries().size(), test.getEntries().size());
		assertEquals(tr.getTimestamp(), test.getTimestamp());
		assertEquals(tr.getId(), test.getId());
		assertEquals(tr.getAttribute("namn"), test.getAttribute("namn"));
	}

    @Test
	public void testEntry() {
        Entry e = createEntry(10L, new BigDecimal("100"), createAccount(new Long("9475"), "EUR"), null);
        Transaction txe = new Transaction();
        txe.setId(123L);
        txe.setTimestamp(new Date(1));
        txe.setComment("klyka");
        txe.setAttribute("a", "A");
        e.setTransaction(txe);
        
        EntryDTO dto = f.toDTO(e);
        assertThat(dto.getId(), is(e.getId()));
        assertThat(dto.getAmount(), is(e.getAmount()));
        assertThat(dto.getTransactionId(), is(123L));
        assertThat(dto.getTransactionComment(), is("klyka"));
        assertThat(dto.getTransactionTimestamp(), is(new Date(1)));
        assertThat(dto.getTransactionAttributes(), notNullValue());
        assertThat(dto.getTransactionAttributes().get("a"), is("A"));
	}

    @Test
	public void testAccount() {
		Account a = new Account(new Long("321"), new Long("666"), "XYZ", 3);
		a.setNegativeBalanceAllowed(false);
		a.setAttribute("dummy", "dummyV");
		a.setId(109L);
		AccountDTO dto = f.toDTO(a);
		Account test = f.fromDTO(dto);
		// assertThat(a, is(test));
        assertThat(test.getId(), is(109L));
        assertThat(test.getUserId(), is(new Long("321")));
        assertThat(test.getWalletId(), is(new Long("666")));
        assertThat(test.getCurrencyCode(), is("XYZ"));
        assertThat(test.getFractionalDigits(), is(3));
        assertThat(test.getAttribute("dummy"), is("dummyV"));
	}

    @Test
	public void testAccountStatus() {
		AccountStatus st = AccountStatus.OPEN;
		AccountStatusDTO dto = f.toDTO(st);
		AccountStatus test = f.fromDTO(dto);
		assertEquals(st, test);
	}
    
    @Test
    public void testCurrencyRate() {
        CurrencyRate cr = new CurrencyRate("EUR", "USD", new BigDecimal("1.234"), new Date());
        CurrencyRateDTO crdto = f.toDTO(cr);
        CurrencyRate newCr = f.fromDTO(crdto);
        assertThat(cr, is(newCr));
    }
    

	// --- PRIVATE METHODS --- //
	
	private Entry createEntry(final Long id, BigDecimal amount, Account account, Transaction trans) {
		Entry e = new Entry(id);
		e.setAmount(amount);
		e.setAccount(account);
		e.setTransaction(trans);
		return e;
	}

	private Account createAccount(Long extId, String curr) {
	    Currency c = Currency.getInstance(curr);
		return AccountFactory.create(extId, curr, c.getDefaultFractionDigits(), null);
	}
}
