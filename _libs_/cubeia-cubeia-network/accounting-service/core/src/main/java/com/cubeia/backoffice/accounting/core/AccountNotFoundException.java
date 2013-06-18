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

package com.cubeia.backoffice.accounting.core;

public class AccountNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 2323884228190584947L;

	private final Long accountId;

	public AccountNotFoundException(Long accountId) {
		this("Account not found: " + accountId, accountId);
	}
	
	public AccountNotFoundException(String msg, Long accountId) {
		super(msg);
		this.accountId = accountId;
	}
	
	public AccountNotFoundException(String msg) {
		this(msg, -1L);
	}
	
	public Long getAccountId() {
		return accountId;
	}
}
