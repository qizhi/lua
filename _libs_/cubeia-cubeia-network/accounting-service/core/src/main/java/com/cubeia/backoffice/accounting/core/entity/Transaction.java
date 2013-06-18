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

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;

@Entity
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Set<Entry> entries = new HashSet<Entry>();
    private Date timestamp;
    private String comment;
    private String externalId;
    
    private Map<String, TransactionAttribute> attributes = new HashMap<String, TransactionAttribute>();
    
    public Transaction() { }

    /**
     * Creates a transaction with the given entries. If the sum per currency is not zero a
     * {@link TransactionNotBalancedException} will be thrown.
     * @param comment transaction comment
     * @param entries entries
     * @throws TransactionNotBalancedException if the sum per currency is not zero
     */
    public Transaction(String comment, Entry... entries) throws TransactionNotBalancedException {
        ensureEntrySumIsZero(entries);
        this.comment = comment;
        this.entries.addAll(Arrays.asList(entries));
        this.timestamp = new Date();
    }
    
    private void ensureEntrySumIsZero(Entry[] entries) throws TransactionNotBalancedException {
        //BigDecimal sum = BigDecimal.ZERO;
        Map<String, BigDecimal> sums = new HashMap<String, BigDecimal>(2);
    	for (Entry e : entries) {
            //sum = sum.add(e.getAmount());
    		Account a = e.getAccount();
    		String curr = a.getCurrencyCode();
    		BigDecimal sum = sums.get(curr);
    		if(sum == null) {
    			sum = BigDecimal.ZERO;
    		}
    		sums.put(curr, sum.add(e.getAmount()));
    	}
        
    	for (String curr : sums.keySet()) {
    		BigDecimal sum = sums.get(curr);
    		if (sum.signum() != 0) {
                throw new TransactionNotBalancedException("Transaction balance is not zero for currency '" + curr + "', sum = " + sum 
                    + ". Entries: " + Arrays.toString(entries));
            }
    	}   
    }

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(nullable=true, unique=true)
    public String getExternalId() {
		return externalId;
	}
    
    public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
    
    @OneToMany(mappedBy="transaction",fetch=EAGER)
    public Set<Entry> getEntries() {
        return entries;
    }
    
    void setEntries(Set<Entry> entries) {
        this.entries = entries;
    }
   
    /*
     * Do not map the timestamp as a DATETIME as it will lose it's ms precision and tz.
     * See getTimestampLong().
     */
    @Transient
    @Column(nullable=false)
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Hibernate mapping of the timestamp attribute. We the time as a long to get milli seconds.
     * @return the timestamp as a long
     */
    @Column(name="timestamp")
    protected long getTimestampLong() {
        return timestamp.getTime();
    }
    
    protected void setTimestampLong(long timestamp) {
        setTimestamp(new Date(timestamp));
    }
    
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
	
    @Transient
    public String getAttribute(String key) {
    	TransactionAttribute a = attributes.get(key);
    	return (a == null ? null : a.getValue());
    }
    
    public void setAttribute(String key, String value) {
    	TransactionAttribute a = new TransactionAttribute(this, key, value);
    	attributes.put(key, a);
    }
    
    @MapKey(name="key")
    @OneToMany(mappedBy="transaction",cascade=ALL,fetch=EAGER)
    public Map<String, TransactionAttribute> getAttributes() {
		return attributes;
	}
    
    public void setAttributes(Map<String, TransactionAttribute> attributes) {
		this.attributes = attributes;
	}
    
	public void setStringAttributes(Map<String, String> values) {
		for (String key : values.keySet()) {
			String val = values.get(key);
			setAttribute(key, val);
		}
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 * This method returns a new transaction with the same external id, the same
	 * comment, the same attributes, a new time stamp and all entries reversed. 
	 * 
	 * @return A new transaction
	 */
	public Transaction reverse() {
		Transaction t = new Transaction();
		t.timestamp = new Date();
		t.comment = this.comment;
		t.externalId = this.externalId;
		
		// copy and reverse entries
		for (Entry e : entries) {
			Entry rev = e.reverse();
			rev.setTransaction(t);
			t.entries.add(rev);
		}
		
		// copy attributes
		for (TransactionAttribute a : attributes.values()) {
			t.setAttribute(a.getKey(), a.getValue());
		}
		
		return t;
	}
}
