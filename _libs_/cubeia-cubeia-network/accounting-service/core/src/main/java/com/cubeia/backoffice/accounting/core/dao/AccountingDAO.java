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

package com.cubeia.backoffice.accounting.core.dao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.domain.TransactionsOrder;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.BalanceCheckpoint;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;

public interface AccountingDAO {

    void saveOrUpdate(Object object);

    /**
     * Returns a regular account.
     * @param accountId the account id
     * @return the account
     */
    Account getAccount(Long accountId);

    /**
     * Returns an account of the given type.
     * @param <T> The account type to get
     * @param clazz the class of the account
     * @param accountId the account id
     * @return the account
     */
    <T extends Account> T getAccount(Class<T> clazz, Long accountId);
    
    BigDecimal getBalanceNaive(Long accountId);

    /**
     * Returns the latest balance checkpoint for the given account that precedes or equals
     * the given entry id.
     * @param accountId the account
     * @param entryId the latest entry id to consider, null for all entries
     * @return the checkpoint, null if there is none
     */
    BalanceCheckpoint getLatestBalanceCheckpoint(Long accountId, Long entryId);

    /**
     * Calculates the sum of all entries in the given range.
     * @param accountId the account
     * @param firstEntryId the first entry id
     * @param lastEntryId the last entry id
     * @return the sum of entries and the number of entries summarized, 
     *   never null
     */
    EntrySumAndCount getEntrySumAndCount(Long accountId, Long firstEntryId, Long lastEntryId);

    /** 
     * Returns the entry with the given id.
     * @param entryId entry id
     * @return the entry
     */
    Entry getEntry(Long entryId);

    /**
     * Returns the transaction with the given id.
     * @param txId the tx id
     * @return the transaction
     */
    Transaction getTransaction(Long txId);

    /**
     * Returns all account by the given external id.
     * @param userId User id
     * @return matching accounts
     */
	Collection<Account> findAccountsByUserId(Long userId);
	
    /**
     * Returns all account by the given external id.
     * @param userId User id
     * @param walletId Wallet id
     * @return matching accounts
     */
	Collection<Account> findAccountsByUserAndWalletId(Long userId, Long walletId);
	
	/**
	 * Find accounts by the given parameters.
	 * @param userId User id, must not be null
	 * @param type Account type, may be null if currency is not null
	 * @param currency Currency code, may be null if type is not null
	 * @return matching accounts, never null
	 */
	Collection<Account> findAccountsByUserId(Long userId, String type, String currency);
	
	/**
	 * Find accounts by the given parameters.
	 * @param userId User id, must not be null
	 * @param type Account type, may be null if currency is not null
	 * @param currency Currency code, may be null if type is not null
	 * @return matching accounts, never null
	 */
	Collection<Account> findAccountsByUserAndWalletId(Long userId, Long walletId, String type, String currency);

    List<Account> listAccounts(Long accountId, Long userId, Long walletId, String currencyCode, Collection<AccountStatus> statuses, Collection<String> types, int offset, int limit, AccountsOrder order, boolean ascending);

    long countAccounts(Long accountId, Long userId, Long walletId, String currencyCode, Collection<AccountStatus> statuses, Collection<String> types);
    
    void merge(Account account);


    List<Transaction> listTransactions(
    		TransactionParticipant part1, 
    		TransactionParticipant part2, 
    		Date startDate, Date endDate, 
    		int offset, int limit, TransactionsOrder order, boolean ascending);

    int countTransactions(
    		TransactionParticipant part1, 
    		TransactionParticipant part2, 
    		Date startDate, Date endDate);
    
    /**
     * List entries matching the given search criterias.
     * @param accountId account id
     * @param offset offset
     * @param limit limit
     * @param ascending ascending order if true, descending if false
     * @return the matching entries
     */
    List<Entry> listEntries(Long accountId, int offset,
            int limit, boolean ascending);

    /**
     * Count entries.
     * @param accountId account id
     * @param includeBalances if true include the resulting balance after each entry
     * @return the number of matching entries
     */
    long countEntries(Long accountId);

    /**
     * Returns the most recent currency rate matching the given parameters.
     * @param sourceCurrencyCode source currency code
     * @param targetCurrencyCode target currency code
     * @param start start date, inclusive, must not be null
     * @param end end date, exclusive, must not be null
     * @return the currency rate or null if not found
     */
    CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date start, Date end);

    /**
     * This method returns a non-exclusive list of rates, which means
     * that duplicates can be included. The list will be ordered by time stamp
     * (ascending).
     * 
     * @param currencyCode Currency code to look for, or null for all
     * @param start Start date, inclusive, must not be null
     * @param end End date, exclusive, must not be null
     * @return A list of currency rates, never null
     */
    List<CurrencyRate> listCurrencyRates(String currencyCode, Date start, Date end);
    
	Collection<Account> findAccountsByTypeAndCurrency(String type, String currency);

	/**
	 * Returns the shortest chain of currency rates between the source and target rates.
	 * 
     * @param sourceCurrencyCode source currency code
     * @param targetCurrencyCode target currency code
     * @param start start date, inclusive
     * @param end end date, exclusive
     * @return The chain, empty list if no chain was found. Never null.
	 */
    List<CurrencyRate> getCurrencyRateChain(String targetCurrencyCode, String sourceCurrencyCode, Date start, Date end);

	Transaction getTransactionByExternalId(String extId);

    /* 
     * @param userId
     * @param currencyCode
     * @return
     */
	// Account getStaticAccountByUserIdAndCurrency(Long userId, String currencyCode);
}
