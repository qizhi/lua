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
package com.cubeia.backoffice.wallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.WithdrawResult;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;

public class AdaptedWalletServiceTest extends BaseTest {

	@Test
	public void testWithdrawFromRemoteWalletToSession() {
	    long s = createNewSession(1);
	    UUID uuid = UUID.randomUUID();
	    
	    TestWalletAdapter.IS_METHOD_CALLED.set(false);
        WithdrawResult wr = adaptedWalletService.withdrawFromRemoteWalletToAccount(uuid, 1, s, 99,  new Money("XXX", 3, new BigDecimal(100)));
        assertEquals(uuid, wr.getRequestId());
        assertTrue(wr.getTransactionId() > 0);
        assertTrue(TestWalletAdapter.IS_METHOD_CALLED.get());
        
        assertEquals(100, mockExternalWalletImpl.withdrawAmount.intValueExact());
        assertEquals(99, mockExternalWalletImpl.withdrawLicenseeId);
	}
	
	private long createNewSession(long userId) {
	    MetaInformation meta = new MetaInformation();
	    meta.setGameId(1L);
	    meta.setName("u");
	    meta.setUserName("n");
	    meta.setObjectId("2");
	    CreateAccountResult result = createAccount(userId, meta, AccountType.SESSION_ACCOUNT);
		return result.getAccountId();
	}
	
	private CreateAccountResult createAccount(Long userId, MetaInformation meta, AccountType type) {
		CreateAccountResult result = walletService.createAccount(new CreateAccountRequest(UUID.randomUUID(), userId, "EUR", type, meta));
		return result;
	}
}
