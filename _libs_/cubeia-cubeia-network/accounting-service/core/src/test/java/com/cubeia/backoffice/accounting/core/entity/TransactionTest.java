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

package com.cubeia.backoffice.accounting.core.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.math.BigDecimal;

import org.junit.Test;

import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;

public class TransactionTest {

	@Test
	public void reverseTransactionSum() {
		Transaction t = new Transaction();
		t.getEntries().add(new Entry(null, new BigDecimal("11")));
		t.getEntries().add(new Entry(null, new BigDecimal("2")));
		
		Transaction t2 = t.reverse();
		assertThat(sum(t).negate(), is(sum(t2)));
	}
	
    @Test
    public void creation() {
        Account a1 = new Account("SEK", 2);
        Account a2 = new Account("SEK", 2);
        Entry e1 = new Entry(a1, new BigDecimal("10"));
        Entry e2 = new Entry(a2, new BigDecimal("-10"));
        
        Transaction tx = new Transaction("comment", e1, e2);
        assertThat(tx.getComment(), is("comment"));
        assertThat(tx.getEntries().size(), is(2));
        assertThat(tx.getEntries(), hasItems(e1, e2));
    }
	
	@Test(expected = TransactionNotBalancedException.class)
	public void errorOnNonBalanced() {
        Account a1 = new Account("SEK", 2);
        Account a2 = new Account("SEK", 2);
        Entry e1 = new Entry(a1, new BigDecimal("11"));
        Entry e2 = new Entry(a2, new BigDecimal("-10"));
	    
        new Transaction(null, e1, e2);
	}

	private BigDecimal sum(Transaction t) {
		BigDecimal sum = BigDecimal.ZERO;
		for (Entry e : t.getEntries()) {
			sum = sum.add(e.getAmount());
		}
		return sum;
	}
}
