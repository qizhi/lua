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
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
@SuppressWarnings("serial")
@XmlRootElement(name="CreateSessionResult")
public class CreateSessionResult implements Serializable {
	
	private UUID requestId;
	
	private long sessionId;
	
	/**
	 * @return String, a readable representation
	 */
	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	/*------------------------------------------------
	 
		ACCESSORS & MUTATORS
		
		This are all bound to XML by JAXB annotations
		for marshalling and unmarshalling.
		
		Some attributes will use custom adapters for
		marshalling/unmarshalling. They are annotated
		with @XmlJavaTypeAdapter(class)

	 ------------------------------------------------*/

	@XmlElement(name="requestId")
	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID txId) {
		this.requestId = txId;
	}

	@XmlElement(name="sessionId")
	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
}
