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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A transaction is a collection of related entries. The sum of all entries is zero.
 * In case of a multi currency transaction the sums of all entries for each currency are zero.
 * @author w
 */
@XmlRootElement(name="transaction")
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Set<Entry> entries;
    private Date timestamp;
    private String comment;
    private Map<String, String> attributes;
    
    public Transaction() {
    }

    public Transaction(Long id, Date timestamp, String comment, Map<String, String> attributes, Entry... entries) {
        this.id = id;
        this.comment = comment;
        this.entries = new HashSet<Entry>();
        this.entries.addAll(Arrays.asList(entries));
        this.attributes = attributes;
        this.timestamp = timestamp;
    }
    
    /**
     * Transaction id. Assigned by the service.
     * @return transaction id
     */
    @XmlElement
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Entries in this transaction.
     * @return entries
     */
    @XmlElement(name="entries")
    public Set<Entry> getEntries() {
        return entries;
    }
    
    void setEntries(Set<Entry> entries) {
        this.entries = entries;
    }
    
    @XmlElementWrapper(name = "attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * Transaction timestamp.
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
     * @return comment
     */
    @XmlElement(name="comment")
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((entries == null) ? 0 : entries.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
        Transaction other = (Transaction) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (entries == null) {
            if (other.entries != null)
                return false;
        } else if (!entries.equals(other.entries))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", entries=" + entries + ", timestamp=" + timestamp + ", comment=" + comment
            + ", attributes=" + attributes + "]";
    }
    
}
