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

package com.cubeia.backoffice.wallet.api.dto.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 * 
 */
@XmlRootElement(name="TransactionRequest")
public class TransactionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Collection<TransactionEntry> entries = new ArrayList<TransactionEntry>();
    private String comment;
    private String externalId;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Set<Long> excludeReturnBalanceForAcconds = new HashSet<Long>(); // no balance returned for account
    
    /**
     * Creates an empty transaction request.
     */
    public TransactionRequest() {}
    
    /**
     * Exclude the balance from these accounts when returning
     * the transaction result. 
     */
    @XmlElement(name="excludeBalance")
    public Set<Long> getExcludeReturnBalanceForAcconds() {
		return excludeReturnBalanceForAcconds;
	}
    
    /**
     * Exclude the balance from these accounts when returning
     * the transaction result. 
     */
    public void setExcludeReturnBalanceForAcconds(Set<Long> excludeReturnBalanceForAcconds) {
		this.excludeReturnBalanceForAcconds = excludeReturnBalanceForAcconds;
	}

    /**
     * Creates a transaction request with the given comment and no attributes.
     * @param comment comment
     */
    public TransactionRequest(String comment) {
        this.comment = comment;
    }
    
    /**
     * Creates a transaction request with the given comment and attributes.
     * @param comment comment
     * @param attributes attributes
     */
    public TransactionRequest(String comment, Map<String, String> attributes) {
        this.comment = comment;
        this.attributes = attributes;
    }

    @XmlElement(name="entry")
    public Collection<TransactionEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(Collection<TransactionEntry> entries) {
        this.entries = entries;
    }
    
    @XmlElement(name="comment")
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @XmlElement(name = "externalId")
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    @XmlElementWrapper(name = "attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
