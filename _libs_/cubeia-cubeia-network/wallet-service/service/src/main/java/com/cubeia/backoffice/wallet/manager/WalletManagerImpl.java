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
import java.util.Currency;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;

@Component("wallet.service.walletManager")
public class WalletManagerImpl implements WalletManager {
    private Logger log = LoggerFactory.getLogger(getClass());
	
    @Resource(name = "wallet.service.accountingManager")
    private WalletAccountingManager accountingManager;

    @Resource(name = "wallet.service.externalAccountManager")
    private ExternalAccountManager extAccountManager;
    
	public Transaction withdraw(BigDecimal amount, long sessionId, long licenseeId) {
		// FIXME: We need an adapter implementation or something similar here
		log.warn("Wallet.Withdraw from operator method is using a mock implementation (it is not implemented yet)");
		
		Account licenseeAccount = getOrCreateLicenseeAccount(licenseeId);
		
		extAccountManager.withdraw(amount, "x-" + sessionId, licenseeId);
		
		Transaction tx = accountingManager.createTransaction(
			"withdraw from licensee", 
			null,
			amount, 
			licenseeAccount.getId(), 
			sessionId,
			null);
		
		log.trace("created transaction: {}", tx);
		return tx;
	}

	@Override
	public Transaction deposit(BigDecimal amount, long sessionId, long licenseeId) {
		// FIXME: We need an adapter implementation or something similar here
		log.warn("Wallet.Deposit from operator method is using a mock implementation (it is not implemented yet)");
		
		Account licenseeAccount = getOrCreateLicenseeAccount(licenseeId);
		
		extAccountManager.deposit(amount, "x-" + sessionId, licenseeId);
		
		Transaction tx = accountingManager.createTransaction(
			"deposit to licensee", 
			null,
			amount, 
			sessionId, 
			licenseeAccount.getId(),
			null);

        log.trace("created transaction: {}", tx);
		return tx;
	}
	
//	@Override
//	public Transaction transfer(BigDecimal amount, Long fromAccountId, Long toAccountId, String comment) {
//		// TODO: might need some more stuff here...
//		return accountingManager.createTransaction(comment, null, amount, fromAccountId, toAccountId, null);
//	}
//	
	private Account getOrCreateLicenseeAccount(long licenseeId) {
		Account licenseeAccount = accountingManager.getAccountByUserId(licenseeId);
		if (licenseeAccount == null) {
			licenseeAccount = AccountFactory.create(Currency.getInstance("EUR"));
			//licenseeAccount.setExternalId(String.valueOf(licenseeId)); FIXME
			licenseeAccount.setStatus(AccountStatus.OPEN);
			licenseeAccount.setType(AccountType.OPERATOR_ACCOUNT.toString());
			licenseeAccount.setUserId(licenseeId);
			accountingManager.createAccount(licenseeAccount);
			log.info("No operator account found for operator id: " + licenseeId + 
				". An account was created, Account ID: " + licenseeAccount.getId());
		}
		return licenseeAccount;
	}
}
