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

package com.cubeia.backoffice.accounting.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="balancedEntry")
public class BalancedEntryDTO implements Serializable {

    private static final long serialVersionUID = 5107181601532876616L;

	private Long id;
    private AccountDTO account;
    private BigDecimal amount;
    private Long transactionId;
    private BigDecimal balance;
    private Date transactionTimestamp;
    private String transactionComment;
    
    private HashMap<String, String> transactionAttributes = new HashMap<String, String>();
    
    @XmlElement
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@XmlElement
	public AccountDTO getAccount() {
		return account;
	}
	
	public void setAccount(AccountDTO account) {
		this.account = account;
	}
	
	@XmlElement
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	@XmlElement
	public Long getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	
	@XmlElement
	public BigDecimal getBalance() {
		return balance;
	}
	
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	@XmlElement
	public String getTransactionComment() {
		return transactionComment;
	}
	
	public void setTransactionComment(String transactionComment) {
	    this.transactionComment = transactionComment;
	}
	
	@XmlElement
	public Date getTransactionTimestamp() {
		return transactionTimestamp;
	}
	
	public void setTransactionTimestamp(Date transactionTimestamp) {
		this.transactionTimestamp = transactionTimestamp;
	}

    @XmlElement
    public HashMap<String, String> getTransactionAttributes() {
        return transactionAttributes;
    }
    
    public void setTransactionAttributes(HashMap<String, String> attributes) {
        this.transactionAttributes = attributes;
    }
	
	// --- OBJECT METHODS --- //
	
    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
