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

package com.cubeia.backoffice.accounting.core.domain;

import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.Direction.BOTH;
import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.Direction.CREDITED;
import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.Direction.DEBITED;
import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.IdentifiactionType.ACCOUNT_ID;
import static com.cubeia.backoffice.accounting.core.domain.TransactionParticipant.IdentifiactionType.USER_ID;

/**
 * This object is used to encapsulate search criteria for
 * one subject when searching for transactions.
 * 
 * @author Lars J. Nilsson
 */
public class TransactionParticipant {
	
	/**
	 * This method is for legacy access, it use the old parameters
	 * for creating a new participant.
	 * 
	 * @param id Id to look for, may be account or user ID, must not be null
	 * @param accountIsCredit True if the participant should credited, false for debited, and null for both
	 * @param idIsUserId True if the ID is a user ID, false for account ID
	 * @return A new participant, never null
	 */
	public static TransactionParticipant legacy(Long id, Boolean accountIsCredit, boolean idIsUserId) {
		Direction d = (accountIsCredit == null ? BOTH : (accountIsCredit.booleanValue() ? CREDITED : DEBITED));
		IdentifiactionType t = (idIsUserId ? USER_ID : ACCOUNT_ID);
		return new TransactionParticipant(id, t, d);
	}

	public enum IdentifiactionType {
		USER_ID,
		ACCOUNT_ID;
	}
	
	public enum Direction {
		CREDITED,
		DEBITED,
		BOTH;
	}
	
	private Long id;
	private IdentifiactionType idType;
	private Direction direction;
	
	public TransactionParticipant(Long id, IdentifiactionType type, Direction direction) {
		this.direction = direction;
		this.id = id;
		idType = type;
	}
	
	public TransactionParticipant() { }

	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public IdentifiactionType getIdType() {
		return idType;
	}
	
	public void setIdType(IdentifiactionType idType) {
		this.idType = idType;
	}	
}
