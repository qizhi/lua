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
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component("wallet.service.externalAccountManager")
public class ExternalAccountManagerMockImpl implements ExternalAccountManager {
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public String deposit(BigDecimal amount, String externalUserId, long licenseeId) {
		String xid = UUID.randomUUID().toString();
		log.debug("deposit external, amount = " + amount + ", xUId = " + 
			externalUserId  + ", licenseeId = " + licenseeId + ", xTId = " + xid);
		return xid;
	}

	@Override
	public String withdraw(BigDecimal amount, String externalUserId, long licenseeId) {
		String xid = UUID.randomUUID().toString();
		log.debug("withdraw external, amount = " + amount + ", xUId = " + 
			externalUserId  + ", licenseeId = " + licenseeId + ", xTId = " + xid);
		return xid;
	}

}
