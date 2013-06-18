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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@SuppressWarnings("serial")
@XmlRootElement(name="EntryListResult")
public class EntriesQueryResult implements Serializable {
    private List<Entry> entries;
    private int totalQueryResultSize;
    private int queryOffset;
    private int queryLimit;
    private boolean ascending;
    
    public EntriesQueryResult() {
    }
    
    public EntriesQueryResult(int queryOffset, int queryLimit,
        int totalQueryResultSize, List<Entry> entries, boolean ascending) {
        
        this.queryLimit = queryLimit;
        this.queryOffset = queryOffset;
        this.totalQueryResultSize = totalQueryResultSize;
        this.entries = entries;
    }
    
    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
    
    @XmlElement(name="entry")
    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
    
    @XmlElement
    public int getTotalQueryResultSize() {
        return totalQueryResultSize;
    }
    
    public void setTotalQueryResultSize(int totalQueryResultSize) {
        this.totalQueryResultSize = totalQueryResultSize;
    }
    
    @XmlElement
    public int getQueryOffset() {
        return queryOffset;
    }
    
    public void setQueryOffset(int queryOffset) {
        this.queryOffset = queryOffset;
    }
    
    @XmlElement
    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }
    
    @XmlElement
    public boolean isAscending() {
        return ascending;
    }
    
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
