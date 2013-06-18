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

package com.cubeia.backoffice.wallet.api.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cubeia.backoffice.accounting.api.Money;

/**
 * Accounting entry. An account's balance is the sum of it's entries.
 * @author w
 */
@XmlRootElement(name="entry")
public class Entry implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long accountId;
    private Long accountUserId;
    private Long transactionId;
    private Date timestamp;
    private Account.AccountType accountType;
    private Account.AccountStatus accountStatus;
    private Money amount;
    private Money resultingBalance;
    private String transactionComment;
    
    public Entry() {
    }
    
    public Entry(
        Long id,
        Long accountId, 
        Long accountUserId,
        Long transactionId,
        Date timestamp,
        String transactionComment, 
        Account.AccountType accountType,
        Account.AccountStatus accountStatus,
        Money amount,
        Money resultingBalance) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.accountUserId = accountUserId;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.transactionComment = transactionComment;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
    }

    /**
     * Entry id. Assigned by the service.
     * @return entry id
     */
    @XmlElement(name="id")
    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    /**
     * Account for this entry.
     * @return account id
     */
    @XmlElement(name="accountId")
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * Entry amount including currency.
     * @return the amount
     */
    @XmlElement(name="amount")
    public Money getAmount() {
        return amount;
    }
    
    public void setAmount(Money amount) {
        this.amount = amount;
    }
    
    /**
     * The resulting account balance after this entry. This value is optional and is only returned
     * if explicitly requested.
     * @return resulting balance after entry
     */
    @XmlElement(name="resultingBalance", required=false)
    public Money getResultingBalance() {
        return resultingBalance;
    }

    public void setResultingBalance(Money resultingBalance) {
        this.resultingBalance = resultingBalance;
    }

    /**
     * Account user id (owner).
     * @return user id
     */
    @XmlElement(name="userId")
    public Long getAccountUserId() {
        return accountUserId;
    }

    public void setAccountUserId(Long accountUserId) {
        this.accountUserId = accountUserId;
    }

    /**
     * Account type.
     * @return account type
     */
    @XmlElement(name="accountType")
    public Account.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }

    /**
     * Account status.
     * @return account status
     */
    @XmlElement(name="accountStatus")
    public Account.AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Account.AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * Transaction id for this entry.
     * @return transaction id
     */
    @XmlElement(name="txId")
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Timestamp of the entry.
     * @return timestamp
     */
    @XmlElement(name="timestamp")
    @XmlJavaTypeAdapter(com.cubeia.backoffice.wallet.api.util.DateAdapter.class) 
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Transaction comment.
     * @return transaction comment
     */
    @XmlElement(name = "txComment")
    public String getTransactionComment() {
        return transactionComment;
    }
    
    public void setTransactionComment(String transactionComment) {
        this.transactionComment = transactionComment;
    }
    
    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entry other = (Entry) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
