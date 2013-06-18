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

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.jws.WebParam;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.api.dto.Account;
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

/**
 * This is the business layer facade for the User Service.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface WalletService {
	
    /**
     * Creates an account.
     * @param request account request object
     */
    CreateAccountResult createAccount(CreateAccountRequest request);

    /**
     * Returns the account balance for the given account id.
     * @param accountId the account to get balance for
     * @return the balance
     * @throws AccountNotFoundException 
     */
    AccountBalanceResult getAccountBalance(long accountId) throws AccountNotFoundException;
    
    /**
     * Withdraw the given amount from the remote wallet of the given licensee to the
     * given session account.
     * @param amount the amount
     * @param sessionId session id
     * @param licenseeId licensee id (NOTE: this is not the licensee's account id!)
     * @return the created transaction
     */
    WithdrawResult withdrawFromRemoteWalletToAccount(
        UUID requestId,
        long userId,
        long sessionId,
        long licenseeId,
        Money amount);
    
    /**
     * Deposit the given amount from the session account to the given remote wallet
     * at the licensee site.
     * @param amount the amount
     * @param sessionId session id
     * @param licenseeId licensee id (NOTE: this is not the licensee's account id!)
     * @return the created transaction
     */
    DepositResult depositFromAccountToRemoteWallet(
        UUID requestId,
        long userId,
        long sessionId,
        long licenseeId,
        Money amount);
 
    /** List accounts by the given search criterias/filters. A null or empty filter parameter is a wildcard.
     * @param accountId account id, null for all
     * @param userId user id, null for all
     * @param statuses statuses to include, null for all
     * @param offset result set offset
     * @param limit result set max size
     * @param order sort order
     * @param ascending true for ascending, false for descending
     * @return a result container
     */
    AccountQueryResult listAccounts(
        Long accountId, 
        Long userId, 
        Collection<Account.AccountStatus> statuses,
        Collection<Account.AccountType> types,
        int offset, 
        int limit,
        AccountsOrder order,
        boolean ascending);
    
    /**
     * Get static account for the given user and currency.
     * 
     * @param userId
     * @param currencyCode
     * @return The account object
     */
    Account getStaticAccountForCurrency(Long userId, String currencyCode);

    /**
     * Returns a list of entries against the given account ordered by id (time).
     * @param accountId the account id, null for entries for all accounts
     * @param includeBalances if true calculate and return the resulting balance after each entry, this
     *   parameter is ignored if account id is null
     * @param offset result set offset
     * @param limit result set max size
     * @param ascending true for ascending, false for descending
     * @return a result container
     */
    EntriesQueryResult listEntries(
        Long accountId, 
        boolean includeBalances,
        int offset, 
        int limit,
        boolean ascending);
    
    
    /**
     * List transactions by the given search criterias/filters and limits. A null or empty filter parameter 
     * is a wild card.
     * @param id1 id 1, account or user id according to id selector parameter
     * @param id1IsUserId if true the id is a user id, if false id is an account id
     * @param id2 id 2, account or user id according to id selector parameter
     * @param id2IsUserId if true the id is a user id, if false id is an account id
     * @param startDate start date (inclusive), null for no limit
     * @param endDate end date (exclusive), null for no limit
     * @param offset result set offset
     * @param limit result set size limit
     * @param order sort order if null order will be by id
     * @param ascending true for ascending order, false for descending
     * @param id1credit true for credit, false for debit, null for both
     * @param id2credit true for credit, false for debit, null for both
     * @return a result container 
     */
    TransactionQueryResult listTransactions(
        Long id1, 
        Boolean id1credit,
        boolean id1IsUserId, 
        Long id2,
        Boolean id2credit, 
        boolean id2IsUserId, 
        Date startDate, 
        Date endDate,
        int offset,
        int limit, 
        TransactionsOrder order, 
        boolean ascending);

    /**
     * Returns the transaction with the given id.
     * @param transactionId transaction id
     * @return the transaction or null if not found
     */
    Transaction getTransactionById(@WebParam(name = "transactionId") Long transactionId);
    
    void updateAccount(Account account);

    void closeAccount(Long accountId) throws AccountNotFoundException; 

    void openAccount(Long accountId) throws AccountNotFoundException;

    Account getAccountById(Long accountId);
    
    /**
     * Deposit money from the given account to the users main account for the given currency.
     * If the user target account is missing it will be created.
     * @param userId user id
     * @param amount amount to deposit (credit)
     * @param accountId account to debit
     * @return the result
     */
    DepositResult deposit(Long userId, Money amount, Long accountId);

	/**
	 * This is for STATIC accounts only!
	 * 
	 * @param userId
	 * @param currencyCode
	 * @return The id of the Account
	 */
	Long getOrCreateStaticAccountIdForUserAndCurrency(Long userId, String currencyCode);
	
	/**
	 * Handle a transfer funds request for a given Account. This method will
	 * find the corresponding placeholder Account to interact with.
	 */
	void handleTransferFunds(Account account, TransferRequest request);

	TransactionResult doTransaction(TransactionRequest transaction) throws TransactionNotBalancedException;

	Collection<Currency> getSupportedCurrencies();

	/**
	 * Adds a new supported currency. If a removed currency with the given code
	 * exists it will be reactivated. It is an error to reactivate a currency 
	 * with a different number of fractional digits.
	 * @param currency currency to add
	 */
	void addSupportedCurrency(Currency currency);

	/**
	 * Mark a supported currency as removed.
	 * @param string currency code
	 */
	void removeSupportedCurrency(String string);

	/**
	 * Returns a supported currency by its currency code.
	 * This method will not return currencies marked as removed.
	 * @param currencyCode code
	 * @return currency
	 */
	Currency getSupportedCurrency(String currencyCode);

    void setSupportedCurrencyManager(SupportedCurrencyManager supportedCurrencyManager);

    void updateSupportedCurrency(Currency currency);
}
