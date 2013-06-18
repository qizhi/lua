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

package com.cubeia.backoffice.wallet.manager;

import java.math.BigDecimal;

import com.cubeia.backoffice.accounting.core.entity.Transaction;

public interface WalletManager {
	
	/**
	 * Withdraw the given amount from the remote wallet of the given licensee to the
	 * given session account.
	 * @param amount the amount
	 * @param sessionId session id
	 * @param licenseeId licensee id (NOTE: this is not the licensee's account id!)
	 * @return the created transaction
	 */
	public Transaction withdraw(BigDecimal amount, long sessionId, long licenseeId);

	/**
	 * Deposit the given amount from the session account to the given remote wallet
	 * at the licensee site.
	 * @param amount the amount
	 * @param sessionId session id
	 * @param licenseeId licensee id (NOTE: this is not the licensee's account id!)
	 * @return the created transaction
	 */
	public Transaction deposit(BigDecimal amount, long sessionId, long licenseeId);


}
