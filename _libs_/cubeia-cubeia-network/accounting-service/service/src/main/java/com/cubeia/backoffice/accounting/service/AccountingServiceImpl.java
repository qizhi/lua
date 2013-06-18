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

package com.cubeia.backoffice.accounting.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
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
import com.cubeia.backoffice.accounting.core.AccountClosedException;
import com.cubeia.backoffice.accounting.core.AccountNotFoundException;
import com.cubeia.backoffice.accounting.core.BalanceNegativeException;
import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;
import com.cubeia.backoffice.accounting.core.TransactionNotFoundException;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;

@Component("accounting.accountingService")
public class AccountingServiceImpl implements AccountingService {

	private static final int CURRENCY_FALLBACK = 365;

	@Resource(name="accounting.accountingManager")
	private AccountingManager manager;
	
	private DTOTranslationFactory factory = new DTOTranslationFactory();
	
	private final Logger log = org.slf4j.LoggerFactory.getLogger(getClass());
	
	@Override
	public AccountDTO createAccount(AccountDTO account) {
		Account a = manager.createAccount(factory.fromDTO(account));
		return factory.toDTO(a);
	}
	
	@Override
	public void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException {
		manager.setTransactionExternalId(transactionId, externalId);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			Long fromAccountId, Long toAccountId, String conversionType)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NoSuchConversionRateException,
			NegativeBalanceException {
		
		return createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, toAccountId, conversionType, null);
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
	public TransactionDTO createMultiCurrencyTransaction(
			String comment,
			String externalId, 
			String currency, 
			BigDecimal amount,
			Long fromAccountId, 
			Long toAccountId, 
			String conversionType,
			Map<String, String> atts) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException {
		
		Account from = safeGetAccount(fromAccountId);
		Account to = safeGetAccount(toAccountId);
		
		// sanity check!!
		if(sameCurrency(from, to)) {
			log.debug("Shortcuting transaction, both accounts of the same currency; fromAccountId: " + fromAccountId + "; toAccountId: " + toAccountId);
			return createTransaction(comment, externalId, amount, fromAccountId, toAccountId);
		}
		
		Long fromConvId = safeGetConverstionAccount(conversionType, from.getCurrencyCode()); 
		Long toConvId = safeGetConverstionAccount(conversionType, to.getCurrencyCode());
		
		log.debug("Converion type tag '{}' returned the following conversion accounts: [{} account {}] [{} account {}]", new Object[] { conversionType, from.getCurrencyCode(), fromConvId, to.getCurrencyCode(), toConvId });
		
		return createMultiCurrencyTransaction(comment, externalId, currency, amount, fromAccountId, fromConvId, toAccountId, toConvId, atts);
	
	}

	@Override
	public TransactionDTO createMultiCurrencyTransaction(
			String comment,
			String externalId, 
			String currency, 
			BigDecimal amount,
			Long fromAccountId, 
			Long fromConversionId, 
			Long toAccountId,
			Long toConversionId,
			Map<String, String> atts) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException {
		
		Account from = safeGetAccount(fromAccountId);
		Account to = safeGetAccount(toAccountId);
		
		// sanity check!!
		if(sameCurrency(from, to)) {
			log.debug("Shortcuting transaction, both accounts of the same currency; fromAccountId: " + fromAccountId + "; toAccountId: " + toAccountId);
			return createTransaction(comment, externalId, amount, fromAccountId, toAccountId);
		}
		
		CurrencyRate rate = safeGetCurrencyRate(from.getCurrencyCode(), to.getCurrencyCode());
		
		log.debug("Found currency rate [{} / {}] for currency codes: {}, {}", 
				new Object[] { rate.getRate(), rate.getTimestamp(), from.getCurrencyCode(), to.getCurrencyCode() });
		
		return createMultiCurrencyTransaction(comment, externalId, currency, amount, factory.toDTO(rate), fromAccountId, fromConversionId, toAccountId, toConversionId, atts);
	}
	
	@Override
	public Money calculateMultiCurrencyConvertion(
				String currency, 
				BigDecimal amount, 
				Long fromAccountId, 
				Long toAccountId) throws NoSuchAccountException, ClosedAccountException, NoSuchConversionRateException {
		
		Account from = safeGetAccount(fromAccountId);
		Account to = safeGetAccount(toAccountId);
		
		// check that one of the accounts is in the currency
		checkTargetCurrency(currency, from, to);
		
		// sanity check!!
		if(sameCurrency(from, to)) {
			log.debug("Shortcuting calculation, both accounts of the same currency; fromAccountId: " + fromAccountId + "; toAccountId: " + toAccountId);
			return new Money(from.getCurrencyCode(), from.getFractionalDigits(), amount);
		}
		
		CurrencyRate rate = safeGetCurrencyRate(from.getCurrencyCode(), to.getCurrencyCode());
		
		if(currency.equals(from.getCurrencyCode())) {
			
			// TODO: Investigate the rounding mode...
			
			int fractions = to.getFractionalDigits();
			BigDecimal converted = amount.multiply(rate.getRate());
			converted = converted.setScale(fractions, BigDecimal.ROUND_DOWN);
			
			return new Money(to.getCurrencyCode(), fractions, converted);
			
		} else {
			
			// TODO: Investigate the rounding mode...
			
			rate = rate.invert();
			int fractions = from.getFractionalDigits();
			BigDecimal converted = amount.multiply(rate.getRate());
			converted = converted.setScale(fractions, BigDecimal.ROUND_UP);
			
			return new Money(from.getCurrencyCode(), fractions, converted);
			
		}
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(String comment,
			String externalId, String currency, BigDecimal amount,
			CurrencyRateDTO conversionRate, Long fromAccountId,
			Long fromConversionId, Long toAccountId, Long toConversionId)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NegativeBalanceException {
		
		return createMultiCurrencyTransaction(comment, externalId, currency, amount, conversionRate, fromAccountId, fromConversionId, toAccountId, toConversionId, null);
	}
	
	@Override
	public TransactionDTO createMultiCurrencyTransaction(
				String comment,
				String externalId, 
				String currency, 
				BigDecimal amount,
				CurrencyRateDTO rateDto,
				Long fromAccountId, 
				Long fromConvId, 
				Long toAccountId, 
				Long toConvId,
				Map<String, String> atts) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException {
		
		// get and throw exception if not found
		Account from = safeGetAccount(fromAccountId);
		Account to = safeGetAccount(toAccountId);
		
		// check that one of the accounts is in the currency
		checkTargetCurrency(currency, from, to);
		
		// sanity check!!
		if(sameCurrency(from, to)) {
			log.debug("Shortcuting transaction, both accounts of the same currency; fromAccountId: " + fromAccountId + "; toAccountId: " + toAccountId);
			return createTransaction(comment, externalId, amount, fromAccountId, toAccountId);
		}
		
		// find conversion accounts and throw exceptions if not found
		Account fromConv = safeGetAccount(fromConvId);
		Account toConv = safeGetAccount(toConvId);
		
		checkMatchingCurrency(from, fromConv);
		checkMatchingCurrency(to, toConv);

		List<Entry> entries = new LinkedList<Entry>();
		
		CurrencyRate rate = factory.fromDTO(rateDto);
		
		if(currency.equals(from.getCurrencyCode())) {
			
			int fractions = to.getFractionalDigits();
			BigDecimal converted = amount.multiply(rate.getRate());
			converted = converted.setScale(fractions, BigDecimal.ROUND_DOWN);
			
			log.debug("Transaction converting {} {} to {} {} using a rate of {} dated '{}' with {} fraction digits, rounded down.", 
					new Object[] { from.getCurrencyCode(), amount, to.getCurrencyCode(), converted, rate.getRate(), rate.getTimestamp(), fractions });
			
			entries.add(new Entry(from, amount.negate()));
			entries.add(new Entry(fromConv, amount));
			entries.add(new Entry(toConv, converted.negate()));
			entries.add(new Entry(to, converted));
			
		} else {
			
			rate = rate.invert();
			int fractions = from.getFractionalDigits();
			BigDecimal converted = amount.multiply(rate.getRate());
			converted = converted.setScale(fractions, BigDecimal.ROUND_UP);
			
			log.debug("Transaction converting {} {} to {} {} using an inverted rate of {} dated '{}' with {} fraction digits, rounded down.", 
					new Object[] { from.getCurrencyCode(), amount, to.getCurrencyCode(), converted, rate.getRate(), rate.getTimestamp(), fractions });
			
			entries.add(new Entry(from, converted.negate()));
			entries.add(new Entry(fromConv, converted));
			entries.add(new Entry(toConv, amount.negate()));
			entries.add(new Entry(to, amount));
		}
		
		try {
			Transaction tr = manager.createTransaction(comment, externalId, entries, atts);
			return factory.toDTO(tr);
		} catch(NegativeBalanceException e) {
			throw new BalanceNegativeException(e.getAccountId());
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		} catch(TransactionNotBalancedException e) {
			throw new UnbalancedTransactionException(e.getMessage());
		} catch(AccountClosedException e) {
			throw new ClosedAccountException(e.getAccountId());
		} 
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, BigDecimal amount,
			Long fromAccountId, Long toAccountId)
			throws NoSuchAccountException, ClosedAccountException,
			UnbalancedTransactionException, NegativeBalanceException {
		
		return createTransaction(comment, null, amount, fromAccountId, toAccountId);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment,
			List<EntryDTO> entries) throws NoSuchAccountException,
			ClosedAccountException, UnbalancedTransactionException,
			NegativeBalanceException {
		
		return createTransaction(comment, null, entries);
	}

	@Override
	public TransactionDTO createTransaction(String comment, String extId, BigDecimal amount, Long fromAccountId, Long toAccountId) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException {
		return createTransaction(comment, extId, amount, fromAccountId, toAccountId, null);
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, String extId, BigDecimal amount, Long fromAccountId, Long toAccountId, Map<String, String> attributes) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException {
		try {
			Transaction tr = manager.createTransaction(comment, extId, amount, fromAccountId, toAccountId, attributes);
			return factory.toDTO(tr);
		} catch(NegativeBalanceException e) {
			throw new BalanceNegativeException(e.getAccountId());
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		} catch(TransactionNotBalancedException e) {
			throw new UnbalancedTransactionException(e.getMessage());
		} catch(AccountClosedException e) {
			throw new ClosedAccountException(e.getAccountId());
		}
	}
	
	@Override
	public TransactionDTO createTransaction(String comment, String extId, List<EntryDTO> entries) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException {
		return createTransaction(comment, extId, entries, null);
	}

	@Override
	public TransactionDTO createTransaction(String comment, String extId, List<EntryDTO> entries, Map<String, String> atts) throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException {
		try {
			Transaction tr = manager.createTransaction(comment, extId, factory.toDTOs(entries), atts);
			return factory.toDTO(tr);
		} catch(NegativeBalanceException e) {
			throw new BalanceNegativeException(e.getAccountId());
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		} catch(TransactionNotBalancedException e) {
			throw new UnbalancedTransactionException(e.getMessage());
		} catch(AccountClosedException e) {
			throw new ClosedAccountException(e.getAccountId());
		}
	}
	
	@Override
	public TransactionDTO reverseTransaction(Long txId, String newExternalId, Map<String, String> oldAttr, Map<String, String> newAttr) throws NoSuchTransactionException {
		try {
			return factory.toDTO(manager.reverseTransaction(txId, newExternalId, oldAttr, newAttr));
		} catch(TransactionNotFoundException e) {
			throw new NoSuchTransactionException(txId);
		}
	}

	@Override
	public AccountDTO getAccount(Long accountId) {
		Account a = manager.getAccount(accountId);
		return factory.toDTO(a);
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserId(Long userId) {
		return factory.toDTO(manager.getAccountsByUserId(userId));
	}
	
	@Override
	public Collection<AccountDTO> getAccountsByUserAndWalletId(Long userId, Long walletId) {
		return factory.toDTO(manager.getAccountsByUserAndWalletId(userId, walletId));
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserIdAndCurrency(Long userId, String currency) {
		return factory.toDTO(manager.getAccountsByUserIdAndCurrency(userId, currency));
	}

	@Override
	public Collection<AccountDTO> getAccountsByUserIdTypeAndCurrency(Long userId, String type, String currency) {
		return factory.toDTO(manager.getAccountsByUserIdWalletTypeAndCurrency(userId, null, type, currency));
	}
	
	@Override
	public Collection<AccountDTO> getAccountsByUserIdWalletTypeAndCurrency(Long userId, Long walletId, String type, String currency) {
		return factory.toDTO(manager.getAccountsByUserIdWalletTypeAndCurrency(userId, walletId, type, currency));
	}

	@Override
	public Money getBalance(Long accountId) throws NoSuchAccountException {
		try {
			return manager.getBalance(accountId);
		} catch (AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		}
	}

	@Override
	public Money getBalanceAfterEntry(Long accountId, Long entryId) throws NoSuchAccountException {
		try {
			return manager.getBalanceAfterEntry(accountId, entryId);
		} catch (AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		}
	}

	@Override
	public TransactionDTO getTransactionById(Long txId) {
		Transaction tr = manager.getTransactionById(txId);
		return factory.toDTO(tr);
	}

	@Override
	public AccountQueryResultDTO listAccounts(Long accountId, Long userId, Long walletId, String currencyCode, Set<AccountStatusDTO> entityStatuses, Set<String> entityTypes, int offset, int limit, AccountsOrderDTO order, boolean ascending) {
		QueryResultsContainer<Account> res = manager.listAccounts(accountId, userId, walletId, currencyCode, factory.fromDTOs(entityStatuses), entityTypes, offset, limit, factory.fromDTO(order), ascending);
		return factory.toDTO(res);
	}

	@Override
	public EntryQueryResultDTO listEntries(Long accountId, int offset, int limit, boolean ascending) {
		QueryResultsContainer<Entry> res = manager.listEntries(accountId, offset, limit, ascending);
		return factory.toDTOEntry(res);
	}

	@Override
	public BalancedEntryQueryResultDTO listEntriesBalanced(Long accountId, int offset, int limit, boolean ascending) {
		QueryResultsContainer<BalancedEntry> res = manager.listEntriesBalanced(accountId, offset, limit, ascending);
		return factory.toDTOBalancedEntry(res);
	}

	@Override
	public TransactionQueryResultDTO listTransactions(Long id1, Boolean account1credit, boolean id1IsExternalId, Long id2, Boolean account2credit, boolean id2IsExternalId, Date startDate, Date endDate, int offset, int limit, TransactionsOrderDTO createTransactionOrderEntityFromDTO, boolean ascending) {
		QueryResultsContainer<Transaction> res = manager.listTransactions(toTransPart(id1, account1credit, id1IsExternalId), toTransPart(id2, account2credit, id2IsExternalId), startDate, endDate, offset, limit, factory.fromDTO(createTransactionOrderEntityFromDTO), ascending);
		return factory.toDTOTransaction(res);
	}

	private TransactionParticipant toTransPart(Long id, Boolean accountIscredit, boolean idIsExternalId) {
		return TransactionParticipant.legacy(id, accountIscredit, idIsExternalId);
	}

	@Override
	public void removeAccountAttribute(Long accountId, String key) throws NoSuchAccountException {
		try {
			manager.removeAccountAttribute(accountId, key);
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		}
	}

	@Override
	public void setAccountAttribute(Long accountId, String key, String value) throws NoSuchAccountException {
		try {
			manager.setAccountAttribute(accountId, key, value);
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		}
	}
	
	@Override
	public void removeTransactionAttribute(Long transactionId, String key) throws NoSuchTransactionException {
		try {
			manager.removeTransactionAttribute(transactionId, key);
		} catch(TransactionNotFoundException e) {
			throw new NoSuchTransactionException(e.getTransactionId());
		}
	}
	
	@Override
	public void setTransactionAttribute(Long transactionId, String key, String value) throws NoSuchTransactionException {
		try {
			manager.setTransactionAttribute(transactionId, key, value);
		} catch(TransactionNotFoundException e) {
			throw new NoSuchTransactionException(e.getTransactionId());
		}
	}

	@Override
	public void setAccountStatus(Long accountId, AccountStatusDTO status) throws NoSuchAccountException {
		try {
			manager.setAccountStatus(accountId, factory.fromDTO(status));
		} catch(AccountNotFoundException e) {
			throw new NoSuchAccountException(e.getAccountId());
		}
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
	public void updateAccount(AccountDTO account) {
		manager.updateAccount(factory.fromDTO(account));
	}
	
	@Override
	public CurrencyRateDTO getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode) {
		return factory.toDTO(manager.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, new Date(), CURRENCY_FALLBACK));
	}

    @Override
    public CurrencyRateDTO getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays) {
        return factory.toDTO(manager.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, date, fallbackDays));
    }
    
    @Override
    public void addCurrencyRate(CurrencyRateDTO rate) {
        manager.addCurrencyRate(factory.fromDTO(rate));
    }
    
    
    // --- PRIVATE METHODS --- //
  
	private void checkMatchingCurrency(Account account, Account conv) {
		if(!sameCurrency(account, conv)) {
			throw new IllegalArgumentException("Currency mismatch, account " + account.getId() + " is of currency " + account.getCurrencyCode() + ", but conversion account " + conv.getId() + " is of currency " + conv.getCurrencyCode() + ". They should be matching.");
		}
	}
	
	private boolean sameCurrency(Account one, Account two) {
		return one.getCurrencyCode().equals(two.getCurrencyCode());
	}
    
	/*private Account findConversionAccount(String currencyCode, List<Long> conversionAccounts) {
		for (Long id : conversionAccounts) {
			Account a = manager.getAccount(id);
			if(a != null && a.getCurrencyCode().equals(currencyCode)) {
				return a;
			}
		}
		throw new NoSuchAccountException("None of the given conversion accounts was in the given currency '" + currencyCode + "'");
	}*/
	
	private Long safeGetConverstionAccount(String type, String currency) {
		Collection<Account> col = manager.getAccountsByTypeAndCurrency(type, currency);
		if(col.size() != 0) return col.iterator().next().getId();
		else return null;
	}
    
	private Account safeGetAccount(Long accountId) {
		Account account = manager.getAccount(accountId);
		if(account == null) throw new NoSuchAccountException(accountId);
		return account;
	}
	
	private void checkTargetCurrency(String currency, Account from, Account to) {
		if(!from.getCurrencyCode().equals(currency) && !to.getCurrencyCode().equals(currency)) {
			throw new IllegalArgumentException("The given accounts does not match currency '" + currency + "'; fromAccount: " + from.getId() + "; toAccount: " + to.getId());
		}
	}
	
	private CurrencyRate safeGetCurrencyRate(String fromCurrency, String toCurrency) {
		CurrencyRate rate = factory.fromDTO(getCurrencyRate(fromCurrency, toCurrency));
		if(rate == null) throw new NoSuchConversionRateException("No conversion rate found for " + fromCurrency + " -> " + toCurrency + " within the last 365 days");
		return rate;
	}
}