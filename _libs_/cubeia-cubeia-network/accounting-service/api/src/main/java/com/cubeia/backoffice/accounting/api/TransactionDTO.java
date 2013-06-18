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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="transaction")
public class TransactionDTO implements Serializable {

	private static final long serialVersionUID = -5402346135147510972L;

	private Long id;
	private HashSet<EntryDTO> entries = new HashSet<EntryDTO>();
    private Date timestamp;
	private String comment;
	private String externalId;
	
	private HashMap<String, TransactionAttributeDTO> attributes = new HashMap<String, TransactionAttributeDTO>();
    
	@XmlElement
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@XmlElement
	public HashMap<String, TransactionAttributeDTO> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(HashMap<String, TransactionAttributeDTO> attributes) {
		this.attributes = attributes;
	}
	
	@XmlElement
	public String getExternalId() {
		return externalId;
	}
	
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@XmlElement
	public HashSet<EntryDTO> getEntries() {
		return entries;
	}
	
	public void setEntries(HashSet<EntryDTO> entries) {
		this.entries = entries;
	}
	
	@XmlElement
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	@XmlElement
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	@XmlTransient
	public Map<String, String> getAttributesAsStrings() {
	    HashMap<String, String> map = new HashMap<String, String>();
	    for (TransactionAttributeDTO tad : getAttributes().values()) {
	        map.put(tad.getKey(), tad.getValue());
	    }
	    return map;
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
