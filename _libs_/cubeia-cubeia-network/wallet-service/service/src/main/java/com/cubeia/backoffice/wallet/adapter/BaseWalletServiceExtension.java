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
package com.cubeia.backoffice.wallet.adapter;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.DepositResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.WithdrawResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.exception.TransactionNotBalancedException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;

public class BaseWalletServiceExtension implements WalletService {

	protected WalletService base;
	
	protected void setAdaptedService(WalletService base) {
		this.base = base;
	}
	
	@Override
	public CreateAccountResult createAccount(CreateAccountRequest request) {
		return base.createAccount(request);
	}

	@Override
	public AccountBalanceResult getAccountBalance(long accountId) throws AccountNotFoundException {
		return base.getAccountBalance(accountId);
	}

	@Override
	public WithdrawResult withdrawFromRemoteWalletToAccount(UUID requestId, long userId, long sessionId, long licenseeId, Money amount) {
		return base.withdrawFromRemoteWalletToAccount(requestId, userId, sessionId, licenseeId, amount);
	}

	@Override
	public DepositResult depositFromAccountToRemoteWallet(UUID requestId, long userId, long sessionId, long licenseeId, Money amount) {
		return base.depositFromAccountToRemoteWallet(requestId, userId, sessionId, licenseeId, amount);
	}

	@Override
	public AccountQueryResult listAccounts(Long accountId, Long userId,
			Collection<AccountStatus> statuses, Collection<AccountType> types,
			int offset, int limit, AccountsOrder order, boolean ascending) {

		return base.listAccounts(accountId, userId, statuses, types, offset, limit, order, ascending);
	}

	@Override
	public Account getStaticAccountForCurrency(Long userId, String currencyCode) {
		return base.getStaticAccountForCurrency(userId, currencyCode);
	}

	@Override
	public EntriesQueryResult listEntries(Long accountId, boolean includeBalances, int offset, int limit, boolean ascending) {
		return base.listEntries(accountId, includeBalances, offset, limit, ascending);
	}

	@Override
	public TransactionQueryResult listTransactions(Long id1, Boolean id1credit,
			boolean id1IsUserId, Long id2, Boolean id2credit,
			boolean id2IsUserId, Date startDate, Date endDate, int offset,
			int limit, TransactionsOrder order, boolean ascending) {
		
		return base.listTransactions(id1, id1credit, id1IsUserId, id2, id2credit, id2IsUserId, startDate, endDate, offset, limit, order, ascending);
	}

	@Override
	public Transaction getTransactionById(Long transactionId) {
		return base.getTransactionById(transactionId);
	}

	@Override
	public void updateAccount(Account account) {
		base.updateAccount(account);
	}

	@Override
	public void closeAccount(Long accountId) throws AccountNotFoundException {
		base.closeAccount(accountId);
	}

	@Override
	public void openAccount(Long accountId) throws AccountNotFoundException {
		base.openAccount(accountId);
	}

	@Override
	public Account getAccountById(Long accountId) {
		return base.getAccountById(accountId);
	}

	@Override
	public DepositResult deposit(Long userId, Money amount, Long accountId) {
		return base.deposit(userId, amount, accountId);
	}

	@Override
	public Long getOrCreateStaticAccountIdForUserAndCurrency(Long userId, String currencyCode) {
		return base.getOrCreateStaticAccountIdForUserAndCurrency(userId, currencyCode);
	}

	@Override
	public void handleTransferFunds(Account account, TransferRequest request) {
		base.handleTransferFunds(account, request);
	}

	@Override
	public TransactionResult doTransaction(TransactionRequest transaction) throws TransactionNotBalancedException {
		return base.doTransaction(transaction);
	}

	@Override
	public Collection<Currency> getSupportedCurrencies() {
		return base.getSupportedCurrencies();
	}

	@Override
	public void addSupportedCurrency(Currency currency) {
		base.addSupportedCurrency(currency);
	}

	@Override
	public void removeSupportedCurrency(String string) {
		base.removeSupportedCurrency(string);
	}

	@Override
	public Currency getSupportedCurrency(String currencyCode) {
		return base.getSupportedCurrency(currencyCode);
	}

	@Override
	public void setSupportedCurrencyManager(SupportedCurrencyManager supportedCurrencyManager) {
		base.setSupportedCurrencyManager(supportedCurrencyManager);
	}

    @Override
    public void updateSupportedCurrency(Currency currency) {
        base.updateSupportedCurrency(currency);
    }
}
