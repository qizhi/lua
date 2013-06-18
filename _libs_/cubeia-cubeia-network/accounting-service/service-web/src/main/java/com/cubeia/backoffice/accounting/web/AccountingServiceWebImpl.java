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

package com.cubeia.backoffice.accounting.web;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.AccountDTO;
import com.cubeia.backoffice.accounting.api.AccountQueryResultDTO;
import com.cubeia.backoffice.accounting.api.AccountStatusDTO;
import com.cubeia.backoffice.accounting.api.AccountingService;
import com.cubeia.backoffice.accounting.api.AccountsOrderDTO;
import com.cubeia.backoffice.accounting.api.BalancedEntryQueryResultDTO;
import com.cubeia.backoffice.accounting.api.ClosedAccountException;
import com.cubeia.backoffice.accounting.api.CurrencyRateDTO;
import com.cubeia.backoffice.accounting.api.EntryDTO;
import com.cubeia.backoffice.accounting.api.EntryQueryResultDTO;
import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.accounting.api.NoSuchAccountException;
import com.cubeia.backoffice.accounting.api.NoSuchConversionRateException;
import com.cubeia.backoffice.accounting.api.NoSuchTransactionException;
import com.cubeia.backoffice.accounting.api.TransactionDTO;
import com.cubeia.backoffice.accounting.api.TransactionQueryResultDTO;
import com.cubeia.backoffice.accounting.api.TransactionsOrderDTO;
import com.cubeia.backoffice.accounting.api.UnbalancedTransactionException;

@Component("accounting.accountingServiceWebImpl")
@WebService(endpointInterface = "com.cubeia.backoffice.accounting.api.AccountingService")
public class AccountingServiceWebImpl implements AccountingService {

	private AccountingService delegate;
	
	@Required
	@Resource(name="accounting.accountingService")
	public void setDelegate(AccountingService delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Money calculateMultiCurrencyConvertion(String currencyCode, BigDecimal amount, Long fromAccountId, Long toAccountId) throws NoSuchAccountException, ClosedAccountException, NoSuchConversionRateException {
		return delegate.calculateMultiCurrencyConvertion(currencyCode, amount, fromAccountId, toAccountId);
	}
	
	public AccountingService getDelegate() {
		return delegate;
	}
	
	@Override
	public void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException {
		delegate.setTransactionExternalId(transactionId, externalId);
	}
	
	@Override
	public TransactionDTO reverseTransaction(Long txId, String newExternalId, Map<String, String> oldAttr, Map<String, String> newAttr) throws NoSuchTransactionException {
		return delegate.reverseTransaction(txId, newExternalId, oldAttr, newAttr);
	}
	
	@Override
	public AccountDTO createAccount(AccountDTO account) {
		return delegate.createAccount(account);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			CurrencyRateDTO conversionRate, Long fromAccountId,
			Long fromConversionId, Long toAccountId, Long toConversionId)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException {
		
		return delegate.createMultiCurrencyTransaction(comment, externalId, currency, amount, conversionRate, fromAccountId, fromConversionId, toAccountId, toConversionId);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			Long fromAccountId, Long fromConversionId, Long toAccountId,
			Long toConversionId, Map<String, String> attributes)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NoSuchConversionRateException,
			NegativeBalanceException {
		
		return delegate.createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, fromConversionId, toAccountId, toConversionId, attributes);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			Long fromAccountId, Long toAccountId, String conversionType,
			Map<String, String> attributes) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NoSuchConversionRateException, NegativeBalanceException {
		
		return delegate.createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, toAccountId, conversionType, attributes);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			CurrencyRateDTO conversionRate, Long fromAccountId,
			Long fromConversionId, Long toAccountId, Long toConversionId,
			Map<String, String> attributes) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NegativeBalanceException {
		
		return delegate.createMultiCurrencyTransaction(comment, externalId, currency, amount, conversionRate, fromAccountId, fromConversionId, toAccountId, toConversionId, attributes);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			Long fromAccountId, Long fromConversionId, Long toAccountId,
			Long toConversionId) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NoSuchConversionRateException, NegativeBalanceException {
		
		return createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, fromConversionId, toAccountId, toConversionId, null);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			Long fromAccountId, Long toAccountId, String conversionType)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NoSuchConversionRateException {
		
		return delegate.createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, toAccountId, conversionType);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, BigDecimal amount,
			Long fromAccountId, Long toAccountId)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NegativeBalanceException {
		
		return delegate.createTransaction(comment, amount, fromAccountId, toAccountId);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment,
			List<EntryDTO> entries) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NegativeBalanceException {
		
		return delegate.createTransaction(comment, entries);
	}

	@Override
	public TransactionDTO createTransaction(String comment, String externalId,
			BigDecimal amount, Long fromAccountId, Long toAccountId)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NegativeBalanceException {
		
		return delegate.createTransaction(comment, externalId, amount, fromAccountId, toAccountId);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, String externalId,
			BigDecimal amount, Long fromAccountId, Long toAccountId,
			Map<String, String> attributes) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NegativeBalanceException {
		
		return delegate.createTransaction(comment, externalId, amount, fromAccountId, toAccountId, attributes);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, String externalId,
			List<EntryDTO> entries) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NegativeBalanceException {
		
		return delegate.createTransaction(comment, externalId, entries);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, String externalId,
			List<EntryDTO> entries, Map<String, String> attributes)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NegativeBalanceException {
		
		return delegate.createTransaction(comment, externalId, entries, attributes);
	}
	
	@Override
	public void removeTransactionAttribute(Long transactionId, String key)
			throws NoSuchTransactionException {
	
		delegate.removeTransactionAttribute(transactionId, key);
	}
	
	@Override
	public void setTransactionAttribute(Long transactionId, String key,
			String value) throws NoSuchTransactionException {
	
		delegate.setTransactionAttribute(transactionId, key, value);
	}
	
	@Override
	public AccountDTO getAccount(Long accountId) {
		return delegate.getAccount(accountId);
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserId(Long userId) {
		return delegate.getAccountsByUserId(userId);
	}
	
	@Override
	public Collection<AccountDTO> getAccountsByUserAndWalletId(Long userId, Long walletId) {
		return delegate.getAccountsByUserAndWalletId(userId, walletId);
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserIdAndCurrency(Long userId, String currency) {
		return delegate.getAccountsByUserIdAndCurrency(userId, currency);
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserIdTypeAndCurrency(Long userId, String type, String currency) {
		return delegate.getAccountsByUserIdTypeAndCurrency(userId, type, currency);
	}
	
	@Override
	public Collection<AccountDTO> getAccountsByUserIdWalletTypeAndCurrency(Long userId, Long walletId, String type, String currency) {
		return delegate.getAccountsByUserIdWalletTypeAndCurrency(userId, walletId, type, currency);
	}

	@Override
	public Money getBalance(Long accountId) throws NoSuchAccountException {
		return delegate.getBalance(accountId);
	}

	@Override
	public Money getBalanceAfterEntry(Long accountId, Long entryId) throws NoSuchAccountException {
		return delegate.getBalanceAfterEntry(accountId, entryId);
	}

	@Override
	public TransactionDTO getTransactionById(Long txId) {
		return delegate.getTransactionById(txId);
	}

	@Override
	public AccountQueryResultDTO listAccounts(
									Long accountId,
									Long userId, 
									Long walletId,
									String currencyCode,
									Set<AccountStatusDTO> entityStatuses,
									Set<String> entityTypes, 
									int offset, int limit,
									AccountsOrderDTO order, 
									boolean ascending) {
		
		return delegate.listAccounts(accountId, userId, walletId, currencyCode, entityStatuses, entityTypes, offset, limit, order, ascending);
	}

	@Override
	public EntryQueryResultDTO listEntries(
									Long accountId, 
									int offset, int limit, 
									boolean ascending) {
		
		return delegate.listEntries(accountId, offset, limit, ascending);
	}

	@Override
	public BalancedEntryQueryResultDTO listEntriesBalanced(
											Long accountId,
											int offset, int limit, 
											boolean ascending) {
		
		return delegate.listEntriesBalanced(accountId, offset, limit, ascending);
	}

	@Override
	public TransactionQueryResultDTO listTransactions(
										Long id1,
										Boolean account1credit, 
										boolean id1IsExternalId, 
										Long id2,
										Boolean account2credit, 
										boolean id2IsExternalId, 
										Date startDate,
										Date endDate, 
										int offset, int limit, 
										TransactionsOrderDTO order,
										boolean ascending) {
		
		return delegate.listTransactions(id1, account1credit, id1IsExternalId, id2, account2credit, id2IsExternalId, startDate, endDate, offset, limit, order, ascending);
	}

	@Override
	public void removeAccountAttribute(Long accountId, String key) throws NoSuchAccountException {
		delegate.removeAccountAttribute(accountId, key);
	}

	@Override
	public void setAccountAttribute(Long accountId, String key, String value) throws NoSuchAccountException {
		delegate.setAccountAttribute(accountId, key, value);
	}

	@Override
	public void setAccountStatus(Long accountId, AccountStatusDTO status) throws NoSuchAccountException {
		delegate.setAccountStatus(accountId, status);
	}

	@Override
	public void setAsyncCheckpointCreation(boolean async) {
		delegate.setAsyncCheckpointCreation(async);
	}

	@Override
	public void setBalanceCheckpointInterval(int balanceCheckpointInterval) {
		delegate.setBalanceCheckpointInterval(balanceCheckpointInterval);
	}

	@Override
	public void updateAccount(AccountDTO account) {
		delegate.updateAccount(account);
	}
	
	@Override
	public CurrencyRateDTO getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays) {
	    return delegate.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, date, fallbackDays);
	}
	
	@Override
	public CurrencyRateDTO getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode) {
		return delegate.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode);
	}
	
	@Override
	public void addCurrencyRate(CurrencyRateDTO rate) {
	    delegate.addCurrencyRate(rate);
	}
}
