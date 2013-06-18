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

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManagerImpl;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest.TransferType;


public class TransfersTest extends BaseTest {
    
    @After
    public void restoreCurrencyManager() {
        walletService.setSupportedCurrencyManager(supportedCurrenciesManager);
    }
    
	@Test
	public void testCreateSession() throws Exception {
		
		SupportedCurrencyManagerImpl currMock = Mockito.mock(SupportedCurrencyManagerImpl.class);
		when(currMock.getCurrencyByCode("EUR")).
	            thenReturn(new SupportedCurrency("EUR", 2));
		walletService.setSupportedCurrencyManager(currMock);
		
		Long sessionId = createSessionAccount();
		Account sessionAccount = walletService.getAccountById(sessionId);
		Long staticAccountId = walletService.getOrCreateStaticAccountIdForUserAndCurrency(sessionAccount.getUserId(), "EUR");
		
		// Get balances before
		Assert.assertEquals(new BigDecimal("0.00"), walletService.getAccountBalance(sessionId).getBalance().getAmount());
		Assert.assertEquals(new BigDecimal("0.00"), walletService.getAccountBalance(staticAccountId).getBalance().getAmount());
		
		TransferRequest transfer = new TransferRequest();
		transfer.setAmount(new BigDecimal("10.00"));
		transfer.setOperatorId(1l);
		transfer.setTransferType(TransferType.CREDIT);
		
		walletService.handleTransferFunds(sessionAccount, transfer);
		
		Assert.assertEquals(new BigDecimal("10.00"), walletService.getAccountBalance(sessionId).getBalance().getAmount());
		Assert.assertEquals(new BigDecimal("-10.00"), walletService.getAccountBalance(staticAccountId).getBalance().getAmount());
		
	}

	

	private Long createSessionAccount() {
		MetaInformation meta = new MetaInformation();
		CreateAccountResult result = walletService.createAccount(new CreateAccountRequest(
				UUID.randomUUID(), 1, "EUR", AccountType.SESSION_ACCOUNT, meta));
		return result.getAccountId();
	}
}
