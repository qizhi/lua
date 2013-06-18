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


/**
 * The manager handles wallet operations against external systems.
 * This manager is responsible for routing the call to an
 * adapter that communicates with the licensee's system.
 * @author w
 */
public interface ExternalAccountManager {

	public String withdraw(BigDecimal amount, String externalUserId, long licenseeId);

	public String deposit(BigDecimal amount, String externalUserId, long licenseeId);
	
}
