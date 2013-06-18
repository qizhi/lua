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

package com.cubeia.network.wallet.firebase.domain;

import java.math.BigDecimal;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.UnbalancedTransactionException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;

/**
 * Builder for transaction requests.
 * @author w
 */
public class TransactionBuilder {
	private final TransactionRequest tx;
	private final String currencyCode;
	private final int fractionalDigits;
	
	/**
	 * Creates a single currency transaction builder.
	 */
	public TransactionBuilder(String currencyCode, int fractionalDigits) {
	    this.currencyCode = currencyCode;
	    this.fractionalDigits = fractionalDigits;
        this.tx = new TransactionRequest();
	}

	/**
	 * Sets the transaction comment.
	 * @param comment comment
	 * @return this builder
	 */
	public TransactionBuilder comment(String comment) {
	    tx.setComment(comment);
	    return this;
	}
	
	/**
	 * Adds an attribute to the transaction.
	 * @param key key
	 * @param value value
	 * @return this builder
	 */
	public TransactionBuilder attribute(String key, String value) {
	    tx.getAttributes().put(key, value);
	    return this;
	}
	
	/**
	 * Adds an entry.
	 * @param accountId account id
	 * @param amount amount
	 * @return this builder
	 */
	public TransactionBuilder entry(long accountId, BigDecimal amount) {
	    tx.getEntries().add(new TransactionEntry(accountId, new Money(currencyCode, fractionalDigits, amount)));
	    return this;
	}
	
	/**
	 * Creates the final transaction request.
	 * After this method is called further usage of this builder is undefined.
	 * @return the transaction request object 
	 * @throws UnbalancedTransactionException if the transaction is unbalanced
	 */
	public TransactionRequest toTransactionRequest() throws UnbalancedTransactionException {
	    Money shouldBeZeroSum = new Money(currencyCode, fractionalDigits, BigDecimal.ZERO);
	    for (TransactionEntry te : tx.getEntries()) {
	        shouldBeZeroSum = shouldBeZeroSum.add(te.getAmount());
	    }
	    if (shouldBeZeroSum.getAmount().signum() != 0) {
	        throw new UnbalancedTransactionException("tx sum is not zero: " + shouldBeZeroSum.toString());
	    }
	    
	    return tx;
	}
	
}
