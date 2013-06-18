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

package com.cubeia.backoffice.wallet.api.dto.request;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * NULL Values are considered wild cards. 
 * 
 * If you do not provide a limit then the response will not contain any Account
 * data but only the total count for the query.
 * 
 * @author Fredrik Johansson; Cubeia Ltd
 */
@XmlRootElement(name="ListTransactionsRequest")
public class ListTransactionsRequest {
	
	private Long id1; 
	private Boolean id1credit;
	private boolean id1IsUserId; 
	private Long id2;
	private Boolean id2credit; 
	private boolean id2IsUserId; 
	private Date startDate; 
	private Date endDate;
	private int offset;
	private int limit; 
	private TransactionsOrder order; 
	private boolean ascending;

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@XmlElement
	public Long getId1() {
		return id1;
	}

	public void setId1(Long id1) {
		this.id1 = id1;
	}

	@XmlElement
	public Boolean getId1credit() {
		return id1credit;
	}

	public void setId1credit(Boolean id1credit) {
		this.id1credit = id1credit;
	}

	@XmlElement
	public boolean isId1IsUserId() {
		return id1IsUserId;
	}

	public void setId1IsUserId(boolean id1IsUserId) {
		this.id1IsUserId = id1IsUserId;
	}

	@XmlElement
	public Long getId2() {
		return id2;
	}

	public void setId2(Long id2) {
		this.id2 = id2;
	}

	@XmlElement
	public Boolean getId2credit() {
		return id2credit;
	}

	public void setId2credit(Boolean id2credit) {
		this.id2credit = id2credit;
	}

	@XmlElement
	public boolean isId2IsUserId() {
		return id2IsUserId;
	}

	public void setId2IsUserId(boolean id2IsUserId) {
		this.id2IsUserId = id2IsUserId;
	}

	@XmlElement
    @XmlJavaTypeAdapter(com.cubeia.backoffice.wallet.api.util.DateAdapter.class) 
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@XmlElement
    @XmlJavaTypeAdapter(com.cubeia.backoffice.wallet.api.util.DateAdapter.class) 
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@XmlElement
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@XmlElement
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@XmlElement
	public TransactionsOrder getOrder() {
		return order;
	}

	public void setOrder(TransactionsOrder order) {
		this.order = order;
	}

	@XmlElement
	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	

}
