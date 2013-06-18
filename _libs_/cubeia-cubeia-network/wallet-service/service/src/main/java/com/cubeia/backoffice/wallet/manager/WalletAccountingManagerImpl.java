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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.AccountClosedException;
import com.cubeia.backoffice.accounting.core.AccountNotFoundException;
import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;
import com.cubeia.backoffice.accounting.core.TransactionNotFoundException;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.domain.TransactionsOrder;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.BalanceCheckpoint;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;
import com.cubeia.backoffice.accounting.core.util.ExecTimeCollector;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;

@Component("wallet.service.accountingManager")
public class WalletAccountingManagerImpl implements WalletAccountingManager {
	
	@Resource(name="accounting.accountingManager")
	private AccountingManager manager;

	@Override
	public void updateAccount(Account account) {
		manager.updateAccount(account);
	}
	
	@Override
	public void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException {
		manager.setTransactionExternalId(transactionId, externalId);
	}
	
	
	@Override
	public void checkAndCreateCheckpoints() {
		manager.checkAndCreateCheckpoints();
	}
	
	@Override
	public BalanceCheckpoint getLastCheckpoint(Long accountId) {
		return manager.getLastCheckpoint(accountId);
	}
	
	@Override
	public QueryResultsContainer<BalancedEntry> listEntriesBalanced(Long accountId, int offset, int limit, boolean ascending) {
		return manager.listEntriesBalanced(accountId, offset, limit, ascending);
	}
	
	@Override
	public void removeAccountAttribute(Long accountId, String key) {
		manager.removeAccountAttribute(accountId, key);
	}
	
	@Override
	public void setAccountAttribute(Long accountId, String key, String value) {
		manager.setAccountAttribute(accountId, key, value);
	}
	
	@Override
	public void setAccountStatus(Long accountId, AccountStatus status) {
		manager.setAccountStatus(accountId, status);
	}
	
	@Override
	public Account createAccount(Account account) {
		return manager.createAccount(account);
	}
	
	@Override
	public Account getAccountByUserIdTypeAndCurrency(Long userId, String type, String currencyCode) {
		throw new RuntimeException("Not implemented yet");
		/*
		Collection<Account> accounts = manager.getAccountsByExternalIdTypeAndCurrency(userId.toString(), type, currencyCode);
        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() > 1) {
            throw new RuntimeException("multiple accounts found for external id = " + userId);
        } else {
            return accounts.iterator().next();
        }
        */
	}
	
	@Override
	public Account getNonSessionAccountByUserIdAndCurrency(Long userId, String currency) {
		Collection<Account> accounts = manager.getAccountsByUserIdAndCurrency(userId, currency);
		Collection<Account> filteredAccounts = new ArrayList<Account>();
		
		// Remove session accounts
		for (Account account : accounts) {
			if (!account.getType().equals(AccountType.SESSION_ACCOUNT.name())) {
				filteredAccounts.add(account);
			}
		}
		
        if (filteredAccounts.isEmpty()) {
            return null;
        } else if (filteredAccounts.size() > 1) {
            throw new RuntimeException("multiple accounts found for external id = " + userId);
        } else {
            return filteredAccounts.iterator().next();
        }
        
	}
	
	@Override
	public Account getAccountByUserIdAndCurrency(Long userId, String currency) {
		Collection<Account> accounts = manager.getAccountsByUserIdAndCurrency(userId, currency);
		
        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() > 1) {
            throw new RuntimeException("multiple accounts found for external id = " + userId);
        } else {
            return accounts.iterator().next();
        }
        
	}

	@Override
	public Account getAccountByUserId(Long userId) {
		Collection<Account> accounts = manager.getAccountsByUserId(userId);
		if (accounts.isEmpty()) {
		    return null;
		} else if (accounts.size() > 1) {
		    throw new RuntimeException("multiple accounts found for external id = " + userId);
		} else {
		    return accounts.iterator().next();
		}
	}

	@Override
	public QueryResultsContainer<Account> listAccounts(Long accountId, Long userId, Set<AccountStatus> entityStatuses, Set<String> entityTypes, int offset, int limit, AccountsOrder order, boolean ascending) {
		return manager.listAccounts(accountId, userId, null, null, entityStatuses, entityTypes, offset, limit, order, ascending);
	}

	@Override
	public Transaction createTransaction(String comment, String externalId,
			BigDecimal amount, Long fromAccountId, Long toAccountId,
			Map<String, String> attributes) throws AccountNotFoundException,
			AccountClosedException, TransactionNotBalancedException {
		
		return manager.createTransaction(comment, externalId, amount, fromAccountId, toAccountId, attributes);
	}
	
	@Override
	public Transaction createTransaction(String comment, String extId,
			List<Entry> entries, Map<String, String> attributes)
			throws AccountNotFoundException, AccountClosedException,
			TransactionNotBalancedException {
		
		return manager.createTransaction(comment, extId, entries, attributes);
	}
	
	@Override
	public void removeTransactionAttribute(Long transactionId, String key)
			throws TransactionNotFoundException {
		
		manager.removeTransactionAttribute(transactionId, key);
	}

	@Override
	public void setTransactionAttribute(Long transactionId, String key,
			String value) throws TransactionNotFoundException {
		
		manager.setTransactionAttribute(transactionId, key, value);
	}
	
	@Override
	public void setTransactionAttributes(Long transactionId,
			Map<String, String> values) throws TransactionNotFoundException {
		
		manager.setTransactionAttributes(transactionId, values);
	}
	
	@Override
	public Account getAccount(Long accountId) {
		return manager.getAccount(accountId);
	}

	@Override
	public Collection<Account> getAccountsByTypeAndCurrency(String type, String currency) {
		return manager.getAccountsByTypeAndCurrency(type, currency);
	}

	@Override
	public Money getBalance(Long accountId) {
		return manager.getBalance(accountId);
	}

	@Override
	public Money getBalanceAfterEntry(Long accountId, Long entryId) {
		return manager.getBalanceAfterEntry(accountId, entryId);
	}

	@Override
	public Transaction getTransactionById(Long txId) {
		return manager.getTransactionById(txId);
	}

	@Override
	public QueryResultsContainer<Entry> listEntries(Long accountId, int offset, int limit, boolean ascending) {
		return manager.listEntries(accountId, offset, limit, ascending);
	}

	@Override
	public void setAsyncCheckpointCreation(boolean async) {
		manager.setAsyncCheckpointCreation(async);
	}

	@Override
	public void setBalanceCheckpointInterval(int balanceCheckpointInterval) {
		manager.setBalanceCheckpointInterval(balanceCheckpointInterval);
	}

    @Override
    public CurrencyRate addCurrencyRate(CurrencyRate rate) {
        return manager.addCurrencyRate(rate);
    }

    @Override
    public CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays) {
        return manager.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, date, fallbackDays);
    }

	@Override
	public Map<Long, Account> getAccounts(Set<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAllowTransactionsWithClosedAccounts(boolean allow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Account createAccountWithInitialBalance(Account account,
			BigDecimal balance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Account> getAccountsByUserId(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Account> getAccountsByUserAndWalletId(Long userId,
			Long walletId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Account> getAccountsByUserIdWalletTypeAndCurrency(
			Long userId, Long walletId, String type, String currency) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Account> getAccountsByUserIdAndCurrency(Long userId,
			String currency) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction reverseTransaction(Long txId, String newExternalId,
			Map<String, String> oldAttr, Map<String, String> newAttr)
			throws TransactionNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResultsContainer<Account> listAccounts(Long accountId,
			Long userId, Long walletId, String currencyCode,
			Set<AccountStatus> entityStatuses, Set<String> entityTypes,
			int offset, int limit, AccountsOrder order, boolean ascending) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResultsContainer<Transaction> listTransactions(
			TransactionParticipant partOne, TransactionParticipant partTwo,
			Date startDate, Date endDate, int offset, int limit,
			TransactionsOrder order, boolean ascending) {
		
		return manager.listTransactions(partOne, partTwo, startDate, endDate, offset, limit, order, ascending);
	}
	@Override
	public CurrencyRate getCurrencyRate(String sourceCurrencyCode,
			String targetCurrencyCode, String baslineCurrencyCode, Date date,
			int fallbackDays) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CurrencyRate> listRatesForCurrency(String currencyCode,
			Date date, int maxAgeDays) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCheckpointExecTimeCollector(ExecTimeCollector col) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getAllowTransactionsWithClosedAccounts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAsyncCheckpointCreation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBalanceCheckpointInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Transaction getTransactionByExternalId(String extId) {
		// TODO Auto-generated method stub
		return null;
	}
}
