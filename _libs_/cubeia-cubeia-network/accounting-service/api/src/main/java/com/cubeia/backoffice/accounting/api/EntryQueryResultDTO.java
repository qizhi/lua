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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="entryQueryResult")
public class EntryQueryResultDTO implements Serializable {

	private static final long serialVersionUID = 894850326088222332L;

	private List<EntryDTO> results = new ArrayList<EntryDTO>(5);
    private int totalQueryResultSize;
    
    @XmlElement
	public List<EntryDTO> getResults() {
		return results;
	}
	
	public void setResults(List<EntryDTO> results) {
		this.results = results;
	}
	
	@XmlElement
	public int getTotalQueryResultSize() {
		return totalQueryResultSize;
	}
	
	public void setTotalQueryResultSize(int totalQueryResultSize) {
		this.totalQueryResultSize = totalQueryResultSize;
	}
    
	
	// ---- OBJECT METHODS --- //
	
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