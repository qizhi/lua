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

package com.cubeia.backoffice.wallet.manager;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Test;

import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.wallet.BaseTest;

import static org.junit.Assert.*;

public class WalletManagerImplTest extends BaseTest {

	@Test
	public void testWithdraw() {
		WalletAccountingManager am = accountingManager;
		WalletManager wm = walletManager;
		
		Account la = AccountFactory.create(Currency.getInstance("EUR"));
		la.setUserId(12345l);
		am.createAccount(la);
		
		Account pa = AccountFactory.create(Currency.getInstance("EUR"));
		pa.setUserId(13l);
		am.createAccount(pa);
		
		assertEquals(0, am.getBalance(la.getId()).getAmount().intValue());
		assertEquals(0, am.getBalance(pa.getId()).getAmount().intValue());
		
		Transaction tx = wm.withdraw(new BigDecimal("1000"), pa.getId(), 12345L);
		assertEquals(2, tx.getEntries().size());
		
		Entry laEntry = findEntryByAccount(la.getId(), tx);
		assertEquals(-1000, laEntry.getAmount().intValue());
		Entry paEntry = findEntryByAccount(pa.getId(), tx);
		assertEquals(1000, paEntry.getAmount().intValue());
		
		assertEquals(-1000, am.getBalance(la.getId()).getAmount().intValue());
		assertEquals(1000, am.getBalance(pa.getId()).getAmount().intValue());
	}

	@Test
	public void testDeposit() {
		WalletAccountingManager am = accountingManager;
		WalletManager wm = walletManager;
		
		Account la = AccountFactory.create(Currency.getInstance("EUR"));
		la.setUserId(12346l);
		am.createAccount(la);
		
		Account pa = AccountFactory.create(Currency.getInstance("EUR"));
		pa.setUserId(14l);
		am.createAccount(pa);
		
		assertEquals(0, am.getBalance(la.getId()).getAmount().intValue());
		assertEquals(0, am.getBalance(pa.getId()).getAmount().intValue());
		
		Transaction tx = wm.deposit(new BigDecimal("1000"), pa.getId(), 12346L);
		assertEquals(2, tx.getEntries().size());
		
		Entry laEntry = findEntryByAccount(la.getId(), tx);
		assertEquals(1000, laEntry.getAmount().intValue());
		Entry paEntry = findEntryByAccount(pa.getId(), tx);
		assertEquals(-1000, paEntry.getAmount().intValue());
		
		assertEquals(1000, am.getBalance(la.getId()).getAmount().intValue());
		assertEquals(-1000, am.getBalance(pa.getId()).getAmount().intValue());
	}
	
	private Entry findEntryByAccount(Long aId, Transaction tx) {
		for (Entry e : tx.getEntries()) {
			if (e.getAccount().getId().equals(aId)) {
				return e;
			}
		}
		return null;
	}

}
