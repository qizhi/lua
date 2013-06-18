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

package com.cubeia.backoffice.accounting.core.manager;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.cubeia.backoffice.accounting.core.util.ExecTimeCollector;

/**
 * This is the internal core accounting manager. It deals with
 * raw entities and should be used by services as the underlying 
 * domain logic handler, while the services deals with domain/core event
 * translation and extended domain logic. 
 * 
 * @author Lars J. Nilsson
 */
public interface AccountingManager {
	
	/**
	 * Get a set of accounts from the database. This is 
	 * a batch operation, but will still read single accounts
	 * separately. All accounts must exist.
	 * 
	 * @param ids ID to lookup, must exist, must not be null
	 * @return A map of ID to account, never null
	 */
	Map<Long, Account> getAccounts(Set<Long> ids);
	
	/**
	 * The default of the manager is to disallow transactions
	 * if any of the involved accounts are closed. Use this method
	 * reverse that behaviour.
	 * 
	 * @param allow True to allow transactions including closed accounts
	 */
	void setAllowTransactionsWithClosedAccounts(boolean allow);
    
	/**
	 * Update an account. The account must exist, and have all 
	 * needed properties.
	 * 
	 * @param account Account to update, must not be null
	 */
	void updateAccount(Account account);
	
	/**
	 * Create a new account from a template. This method will make
	 * sure status and creation date are set (to OPEN and the current
	 * date to be exact) before saving the entity.
	 * 
	 * @param account Account to create, must not be null
	 * @return The entity, with ID set, never null
	 */
    Account createAccount(Account account);
    
    /**
     * Create a new account, and include an initial balance. The initial balance
     * will be created with a so called "initial entry" which is not associated with
     * any transaction. This method will make sure status and creation date are set 
     * (to OPEN and the current date to be exact) before saving the entity.
     * 
     * @param account Account to create, must not be null
     * @param balance The initial balance of the account, must not be null
     * @return The new account, with ID set, never null
     */
    Account createAccountWithInitialBalance(Account account, BigDecimal balance);
    
    /**
     * Get an account from the database. If the account is not found
     * null will be returned. 
     * 
     * @param accountId ID of the account, must not be null
     * @return The given account, or null if not found
     */
    Account getAccount(Long accountId);
    
    /**
     * List all account associated with a user ID. The result is
     * not ordered and returns all account statuses.
     * 
     * @param userId User ID to look for, must not be null
     * @return A list of accounts, never null
     */
    Collection<Account> getAccountsByUserId(Long userId);
    
    /**
     * List all account associated with a user ID. The result is
     * not ordered and returns all account statuses. Both 
     * parameters are mandatory.
     * 
     * @param userId User ID to look for, must not be null
     * @param walletId Wallet ID to look for, must not be null
     * @return A list of accounts, never null
     */
    Collection<Account> getAccountsByUserAndWalletId(Long userId, Long walletId);
    
    /**
     * List all account associated with a user ID. The result is
     * not ordered and returns all account statuses. Only the user ID is
     * mandatory, the other parameters will be used if supplied. 
     * 
     * @param userId User ID to look for, must not be null
     * @param walletId Wallet ID to look for, may be null
     * @param type String type to look for, may be null
     * @param currency Currency code to look for, may be null
     * @return A list of accounts, never null
     */
    Collection<Account> getAccountsByUserIdWalletTypeAndCurrency(Long userId, Long walletId, String type, String currency);

    /**
     * List accounts per type and currency. The result is
     * not ordered and returns all account statuses. Both parameters
     * are mandatory.
     * 
     * @param type Account type, must not be null
     * @param currency Currency code, must not be null
     * @return A list of accounts, never null
     */
    Collection<Account> getAccountsByTypeAndCurrency(String type, String currency);
    
    /**
     * List accounts by user ID and currency. The result is
     * not ordered and returns all account statuses. Only the
     * user id is mandatory. 
     * 
     * @param userId User ID for the accounts, must not be null
     * @param currency Currency code to look for, may be null
     * @return A list of accounts, never null
     */
    Collection<Account> getAccountsByUserIdAndCurrency(Long userId, String currency);
    
    /**
     * Calculate the balance for an account. This operation effectively sums the
     * entries from the last balance checkpoint. 
     * 
     * @param accountId The account to get balance for, must not be null
     * @return The balance of the account, never null
     * @throws AccountNotFoundException
     */
    Money getBalance(Long accountId) throws AccountNotFoundException;
    
    /**
     * Calculate the balance for an account starting from a specific entry. This 
     * operation effectively finds the closest checkpoint to the entry, which could
     * be one and the same, and sums all entries up to the present after the entry.
     * 
     * @param accountId The account to get balance for, must not be null
     * @param entryId Entry to calculate from, must not be null
     * @return The balance of the account from the entry, never null
     * @throws AccountNotFoundException
     */
    Money getBalanceAfterEntry(Long accountId, Long entryId) throws AccountNotFoundException;

    /**
     * Create a non-manual two account transaction. The currency of the two accounts 
     * must be the same. Comment and external ID are optional. The external ID can be 
     * set later but only once and not if included in this method. 
     * 
     * <p>If transactions are not {{@link #setAllowTransactionsWithClosedAccounts(boolean) allowed} 
     * to involve closed accounts and one of the involved account is CLOSED an exception will be raised. 
     * 
     * @param comment Transaction comment, may be null
     * @param extId Transaction external id, may be null
     * @param amount The transaction amount, must not be null
     * @param fromAccountId The source account, must not be null
     * @param toAccountId The target account, must not be null
     * @param attributes Transaction attributes, may be null
     * @return The created transaction, never null
     * 
     * @throws AccountNotFoundException
     * @throws AccountClosedException 
     */
    Transaction createTransaction(
        String comment,
        String externalid,
        BigDecimal amount, 
        Long fromAccountId, 
        Long toAccountId,
        Map<String, String> attributes) throws AccountNotFoundException, AccountClosedException;

    /**
     * Create a transaction using manual entries, possibly spanning multiple
     * accounts. 
     * 
     * <p>If transactions are not {{@link #setAllowTransactionsWithClosedAccounts(boolean) allowed} 
     * to involve closed accounts and one of the involved account is CLOSED an exception will be raised. 
     * 
     * <p>Transactions must be "balanced" meaning that if the entries does not sum to zero
     * per involved currency an exception will be raised. 
     * 
     * @param comment Transaction comment, may be null
     * @param extId Transaction external id, may be null
     * @param entries A list of entries associated with this transaction, must not be empty or null
     * @param attributes Transaction attributes, may be null
     * @return The created transaction, never null
     * 
     * @throws AccountNotFoundException 
     * @throws TransactionNotBalancedException
     * @throws AccountClosedException
     */
    Transaction createTransaction(String comment, String extId, List<Entry> entries, Map<String, String> attributes) throws AccountNotFoundException, AccountClosedException, TransactionNotBalancedException;
    
    /**
     * Create the reverse of an existing transaction. In order to trace the action, you can set
     * attributes on the old, reversed transaction, as well as on the new, reversing, transaction, 
     * however these must not exist on the transaction, ie it is an error to try to overwrite 
     * any existing attributes, and attempt to do so will result in a security exception.
     * 
     * @param txId Id of transaction to reverse, must not be null
     * @param newExternalId External ID of the new transaction, null to copy old
     * @param oldAttr Attributes to set on old transaction, may be null
     * @param newAttr Attributes to set on new transaction, may be null
     * @throws TransactionNotFoundException
     * @return A new transaction, never null
     */
    Transaction reverseTransaction(Long txId, String newExternalId, Map<String, String> oldAttr, Map<String, String> newAttr) throws TransactionNotFoundException;
    
    /**
     * List accounts by the given search criteria/filters. A null or empty filter parameter is a wild card.
     * If of course, the account ID is specified, only one account will ever be returned, however this is useful
     * in search operations.
     * 
     * <p>The result set is paged, for all results use 0/Integer.MAX.
     * 
     * @param accountId Account id, null for all
     * @param userId User id, null for all
     * @param currencyCode Currency code to look for, or null for all
     * @param entityStatuses Statuses to include, null for all
     * @param entityTypes Account types to include, null for all
     * @param offset Result set page offset, mandatory
     * @param limit Result set max size, mandatory
     * @param order Sort order, or null
     * @param ascending True for ascending, false for descending
     * @return The matching accounts and the size of the total result set
     */
    QueryResultsContainer<Account> listAccounts(Long accountId, Long userId, Long walletId,
        String currencyCode, Set<AccountStatus> entityStatuses, Set<String> entityTypes, int offset, int limit,
        AccountsOrder order, boolean ascending);

    /**
     * List transactions by the given search criteria/filter. A null of empty filter parameter 
     * is a wild card. This method includes one or two parts in a transaction, and as such can
     * be used to list transactions between two accounts or user and accounts.
     * 
     * <p>The result set is paged, for all results use 0/Integer.MAX.
     * 
     * @param partOne Participant one, may be null
     * @param partOne Participant two, may be null
     * @param startDate Start date inclusive, null for no limit
     * @param endDate end Date exclusive, null for no limit
     * @param offset Result set offset, mandatory
     * @param limit Result set max size, mandatory
     * @param ascending True for ascending, false for descending
     * @param order Sort order, or null
     * @return The matching transactions and the size of the total result set size
     */
    QueryResultsContainer<Transaction> listTransactions(
    		TransactionParticipant partOne, 
    		TransactionParticipant partTwo,
    		Date startDate, Date endDate, int offset, int limit, 
    		TransactionsOrder order, boolean ascending);

    /**
     * Set the number entries that will be made between balance checkpoints. This
     * can be used to play with best ratio for performance. In short, the higher the 
     * interval, the higher throughput but also possible large lags when the checkpoint
     * should be written.
     * 
     * <p>Default interval is 100.
     * 
     * @param balanceCheckpointInterval Checkpoint interval, must be positive
     */
    void setBalanceCheckpointInterval(int balanceCheckpointInterval);
    
    /**
     * If set to true balance checkpoints will be asynchronously created. This speeds
     * up transaction creation, but must also be monitored so that the background thread does
     * not start lagging.
     * 
     * @param async True for lazy creation, false of eager
     */
    void setAsyncCheckpointCreation(boolean async);

    /**
     * Returns the transaction by its ID. This method returns
     * null if no transaction is found.
     * 
     * @param txId Transaction id, must not be null
     * @return A transaction, null if not full
     */
    Transaction getTransactionById(Long txId);
    
    /**
     * List entries by the given search criteria/filters. A null or empty filter parameter is a wild card.
     * The results are ordered by id (which is essentially the same as time).
     * 
     * <p>The result set is paged, for all results use 0/Integer.MAX.
     * 
     * @param accountId Account id, null for all
     * @param offset Result set offset, mandatory
     * @param limit Result set max size, mandatory
     * @param ascending True for ascending, false for descending
     * @return The matching accounts and the size of the total result size
     */
    QueryResultsContainer<Entry> listEntries(Long accountId, int offset, 
        int limit, boolean ascending);
    
    
    /**
     * List entries by the given search criteria/filters. A null or empty filter parameter is a wild card.
     * The results are ordered by id (which is essentially the same as time).
     * 
     * <p>This method returns entries which contains the balance after the entry, which is 
     * useful when listing the account history for a real user. 
     * 
     * <p>The result set is paged, for all results use 0/Integer.MAX.
     * 
     * @param accountId Account id, null for all
     * @param offset Result set offset, mandatory
     * @param limit Result set max size, mandatory
     * @param ascending True for ascending, false for descending
     * @return The matching accounts and the size of the total result size
     */
    QueryResultsContainer<BalancedEntry> listEntriesBalanced(Long accountId, int offset, 
            int limit, boolean ascending);
	
    /**
     * Set a single account attribute. Account attributes can be replaced. The
     * account itself must exist.
     * 
     * @param accountId Id to set attribute on, must not be null
     * @param key Attribute key, must not be null
     * @param value Attribute value, must not be null
     * @throws AccountNotFoundException
     */
    void setAccountAttribute(Long accountId, String key, String value) throws AccountNotFoundException;
    
    /**
     * Remove a single account attribute. The account itself must exist but
     * the method ignores missing attributes.
     * 
     * @param accountId Id to remove attribute from, must not be null
     * @param key Attribute key, must not be null
     * @throws AccountNotFoundException
     */
    void removeAccountAttribute(Long accountId, String key) throws AccountNotFoundException;
    
    /**
     * Set a single transaction attribute. Transaction attributes can be replaced. The
     * transaction itself must exist.
     * 
     * @param transactionId Id to set attribute on, must not be null
     * @param key Attribute key, must not be null
     * @param value Attribute value, must not be null
     * @throws TransactionNotFoundException
     */
    void setTransactionAttribute(Long transactionId, String key, String value) throws TransactionNotFoundException;
    
    /**
     * Set a multiple transaction attributes. Transaction attributes can be replaced. The
     * transaction itself must exist.
     * 
     * @param transactionId Id to set attribute on, must not be null
     * @param values Attribute map, must not be null
     * @throws TransactionNotFoundException
     */
    void setTransactionAttributes(Long transactionId, Map<String, String> values) throws TransactionNotFoundException;
    
    /**
     * Remove a single transaction attribute. TThe transaction itself must exist but
     * the method ignores missing attributes.
     * 
     * @param transactionId Id to remove attribute from, must not be null
     * @param key Attribute key, must not be null
     * @throws TransactionNotFoundException
     */
    void removeTransactionAttribute(Long transactionId, String key) throws TransactionNotFoundException;
    
    /**
     * Update the status for a single account. The account must exist
     * and both parameters are mandatory.
     * 
     * @param accountId Account to update, must not be null
     * @param status New account status, must not be null
     * @throws AccountNotFoundException
     */
    void setAccountStatus(Long accountId, AccountStatus status) throws AccountNotFoundException;

    /**
     * Adds the given currency rate. Old currency rates will not be replaced, but search 
     * functions uses dates and fall back days to determine which rate to get. The timestamp
     * of the rate should be set.
     * 
     * @param rate The rate to add, must not be null
     */
    CurrencyRate addCurrencyRate(CurrencyRate rate);

    /**
     * Returns the most recent currency day rate between the given currencies for the given date.
     * If no currency rate for the given date was found the the last rate found within 
     * the given number of days will be returned.
     * 
     * <p>This method will calculate a rate if it does not find an exact match, but does find
     * a "first level" match on currency rate pairs. For example, if it doesn't find SEK -> EUR 
     * it will look for anything that matches SEK -> X -> EUR or EUR -> X -> SEK and combine 
     * the rates. To specify exactly what "baseline" currency to use as a bridge for the calculation
     * use {{@link #getCurrencyRate(String, String, String, Date, int) this} method.
     * 
     * @param sourceCurrencyCode Currency to get rate "from", must not be null
     * @param targetCurrencyCode Currency to exchange "to", must not be null
     * @param date Rate date, must not be null
     * @param fallbackDays Number of days to look back before giving up
     * @return The currency rate, null if not found
     */
    CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays);
   
    /**
     * Returns the most recent currency day rate between the given currencies for the given date
     * with an optional baseline. If no currency rate for the given date was found the the last rate found within 
     * the given number of days will be returned.
     * 
     * <p>This method will calculate a rate if it does not find an exact match, but does find
     * a "first level" match on the baseline currency rate. For example, if it doesn't find SEK -> EUR 
     * it will look for anything that matches SEK -> baseLine -> EUR or EUR -> baseLine -> SEK and combine 
     * the rates. 
     * 
     * <p>If the baseline is null, this method will return null if no direct match is found.
     * 
     * @param sourceCurrencyCode Currency to get rate "from", must not be null
     * @param targetCurrencyCode Currency to exchange "to", must not be null
     * @param baselineCurrencyCode Baseline to translate from if not direct match is found, may be null
     * @param date Rate date, must not be null
     * @param fallbackDays Number of days to look back before giving up
     * @return The currency rate, null if not found
     */
    CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, String baslineCurrencyCode, Date date, int fallbackDays);
    
    /**
     * This method lists currency rates for a given date. The max age is used to limit
     * the results. The returned list will not include duplicates; in other words only
     * the latest rate for a given currency pair will be returned. Also this list does
     * not contain any calculated rates, only rates actually found in the database. 
     * 
     * @param currencyCode Currency to search for, or null for all
     * @param date Rate date, must not be null
     * @param maxAgeDays Number of days to look back before giving up
     * @return A list of currencies, never null
     */
    List<CurrencyRate> listRatesForCurrency(String currencyCode, Date date, int maxAgeDays);
    
    /**
     * This method should be called regularly, say by a scheduled job. It checks for, and 
     * creates check points for "dirty accounts". Usually, it should be called by a spring 
     * scheduler.
     */
    void checkAndCreateCheckpoints();
    
    /**
     * Set a collector on the manager which will receive the execution time
     * each time a balance checkpoint operation is performed. Set to null to 
     * stop reporting.
     * 
     * @param col Collector to use, or null for no reporting
     */
    public void setCheckpointExecTimeCollector(ExecTimeCollector col);
    
    /**
     * Get the latest balance checkpoint for an account. The checkpoint is the sum of
     * all entries up the checkpoint entry itself for the account.
     * 
     * @param accountId The account to get a checkpoint for, must not be null
     * @return The last balance checkpoint, or null if not found
     */
    BalanceCheckpoint getLastCheckpoint(Long accountId);

    /**
     * This method updates a transaction with an external id. If the transaction
     * already have an external id set a security exception will be thrown.
     * 
     * @param transactionId Transaction id, must not be null
     * @param externalId Id to set, must not be null
     * @throws SecurityException If the external id is already set
     */
    void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException;

    /**
     * @return True if transactions over CLOSED accounts is permitted
     */
	boolean getAllowTransactionsWithClosedAccounts();

	/**
	 * @return True if checkpoints are created asyncronously by a separate background thread
	 */
	boolean getAsyncCheckpointCreation();

	/**
	 * @return The balance checkpoint interval
	 */
	int getBalanceCheckpointInterval();

	/**
	 * This method will result in an exception if the database query matches
	 * several objects. So only use on unique external ID.
	 * 
	 * @param extId External ID to look for, must not be null
	 * @return A transaction, or null if not found
	 */
	Transaction getTransactionByExternalId(String extId);

}
