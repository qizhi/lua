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

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 * 
 */
@XmlRootElement(name="TransactionResult")
public class TransactionResult {
	
	private int errorCode = -1;
	
	private Long transactionId;
	
	private Collection<AccountBalanceResult> balances;
	
	/**
	 * @return String, a readable representation
	 */
	@Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
	
	/*------------------------------------------------
	 
		ACCESSORS & MUTATORS
		
		This are all bound to XML by JAXB annotations
		for marshalling and unmarshalling.
		
		Some attributes will use custom adapters for
		marshalling/unmarshalling. They are annotated
		with @XmlJavaTypeAdapter(class)

	 ------------------------------------------------*/

	@XmlElement(name="errorCode")
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	@XmlElement(name="balance")
	public Collection<AccountBalanceResult> getBalances() {
		return balances;
	}

	public void setBalances(Collection<AccountBalanceResult> balances) {
		this.balances = balances;
	}
	
	@XmlElement(name = "transactionId")
	public Long getTransactionId() {
        return transactionId;
    }
	
	public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
	
}
