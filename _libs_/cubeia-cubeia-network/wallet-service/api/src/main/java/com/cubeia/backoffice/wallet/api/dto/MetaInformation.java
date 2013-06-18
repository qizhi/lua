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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * <p>Holds contextual Meta Data for accounts and sessions.</p>
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
@SuppressWarnings("serial")
@XmlRootElement(name="AccountInformation")
public class MetaInformation implements Serializable {
	
	private String name;
	private String userName;
	private Long gameId;
	private String objectId;
	
	/** 
	 * @return String, a readable representation
	 */
	public String toString() {
		return "name["+name+"] userName["+userName+"]";
	}
	
	/*------------------------------------------------
	 
		ACCESSORS & MUTATORS
		
		This are all bound to XML by JAXB annotations
		for marshalling and unmarshalling.
		
		Some attributes will use custom adapters for
		marshalling/unmarshalling. They are annotated
		with @XmlJavaTypeAdapter(class)

	 ------------------------------------------------*/
	
	@XmlElement(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="userName")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@XmlElement(name="gameId")
	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	@XmlElement(name="objectId")
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	
}
