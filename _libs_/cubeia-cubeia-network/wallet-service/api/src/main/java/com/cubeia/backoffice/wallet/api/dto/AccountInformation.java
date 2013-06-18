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

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="accountInformation")
public class AccountInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
	 * Contextual id for the game
	 * associated with the account.
	 */
	private Integer gameId;
	
	/**
	 * Contextual object id. 
	 * E.g. tableid, tournamentid or itemid. 
	 */
	private String objectId;
	
	/**
	 * Freeform name of the account.
	 */
	private String name;
	
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}
	
	@XmlElement(name="accountName")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    @XmlElement(name="gameId")
	public Integer getGameId() {
		return gameId;
	}

	public void setGameId(Integer gameId) {
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
