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

package com.cubeia.backoffice.accounting.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface AccountingService {

	/**
	 * Update an existing account.
	 */
    public void updateAccount(@WebParam(name = "account") AccountDTO account);

    /**
     * Create a new account.
     */
    public AccountDTO createAccount(@WebParam(name = "account") AccountDTO account);

    /**
     * Get existing account by id.
     */
    public AccountDTO getAccount(@WebParam(name = "accountId") Long accountId);

    /**
     * Get existing account by user id.
     */
    public Collection<AccountDTO> getAccountsByUserId(@WebParam(name = "userId") Long userId);
    
    /**
     * Get existing account by user id and wallet.
     */
    public Collection<AccountDTO> getAccountsByUserAndWalletId(
    								@WebParam(name = "userId") Long userId,
    								@WebParam(name = "walletId") Long walletId);
    
    
    /**
     * Get existing accounts by external id, type and currency.
     */
    public Collection<AccountDTO> getAccountsByUserIdTypeAndCurrency(@WebParam(name = "userId") Long userId,
            @WebParam(name = "type") String type, @WebParam(name = "currencyCode") String currency);


    /**
     * Get existing accounts by user id, wallet id, type and currency.
     */
    public Collection<AccountDTO> getAccountsByUserIdWalletTypeAndCurrency(@WebParam(name = "userId") Long userId,
    		@WebParam(name = "walletId") Long walletId, @WebParam(name = "type") String type, @WebParam(name = "currencyCode") String currency);

    
    /**
     * Get existing accounts by external id and currency.
     */
    public Collection<AccountDTO> getAccountsByUserIdAndCurrency(@WebParam(name = "userId") Long userId,
            @WebParam(name = "currencyCode") String currency);

    /**
     * @throws NoSuchAccountException
     */
    public Money getBalance(@WebParam(name = "accountId") Long accountId) throws NoSuchAccountException;

    /**
     * @throws NoSuchAccountException
     */
    public Money getBalanceAfterEntry(@WebParam(name = "accountId") Long accountId,
            @WebParam(name = "entryId") Long entryId) throws NoSuchAccountException;

    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionOne")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "toAccountId") Long toAccountId) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionTwo")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "toAccountId") Long toAccountId) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionThree")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "attributes") Map<String, String> attributes) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
  
    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionFour")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
            @WebParam(name = "entries") List<EntryDTO> entries) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionFive")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
            @WebParam(name = "entries") List<EntryDTO> entries) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionSix")
    public TransactionDTO createTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
            @WebParam(name = "entries") List<EntryDTO> entries,
            @WebParam(name = "attributes") Map<String, String> attributes) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * This method tries to calculate the resulting amount of a multi-currency
     * transaction. It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     * </ol>
     * 
     * <p>This method will search for currency rates 365 days back from the current date.
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws NoSuchConversionRateException
     */
    @WebMethod(operationName = "calculateMultiCurrencyConvertionOne")
    public Money calculateMultiCurrencyConvertion(
    		@WebParam(name = "currency") String currencyCode,
    		@WebParam(name = "amount") BigDecimal amount,
    		@WebParam(name = "fromAccountId") Long fromAccountId, 
    		@WebParam(name = "toAccountId") Long toAccountId) 
    throws NoSuchAccountException, ClosedAccountException, NoSuchConversionRateException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion
     * within the transaction. It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>There must be exist one account matching each currency and the "conversionAccountType".</li>
     * </ol>
     * 
     * If a system has multiple conversion accounts for a given currency, account ids can be used
     * in {@link #createMulticurrencyTransaction(String, String, String, BigDecimal, Long, Long, List) this} 
     * method instead. 
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * <p>This method will search for currency rates 365 days back from the current date.
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     * @throws NoSuchConversionRateException
     */
    @WebMethod(operationName = "createTransactionSeven")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "conversionAccountType") String conversionType,
            @WebParam(name = "attributes") Map<String, String> attributes) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion
     * within the transaction. It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>There must be exist one account matching each currency and the "conversionAccountType".</li>
     * </ol>
     * 
     * If a system has multiple conversion accounts for a given currency, account ids can be used
     * in {@link #createMulticurrencyTransaction(String, String, String, BigDecimal, Long, Long, List) this} 
     * method instead. 
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * <p>This method will search for currency rates 365 days back from the current date.
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     * @throws NoSuchConversionRateException
     */
    @WebMethod(operationName = "createTransactionEight")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "conversionAccountType") String conversionType) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion within the transaction. 
     * It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>Each conversion account must match the currency of its associated account.</li>
     * </ol>
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * <p>This method will search for currency rates 365 days back from the current date. This
     * is equivalent to using {@link #getCurrencyRate(String, String) this} method to get the 
     * conversion rate.
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     * @throws NoSuchConversionRateException
     */
    @WebMethod(operationName = "createTransactionNine")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "fromConversionId") Long fromConversionId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "toConversionId") Long toConversionId,
            @WebParam(name = "attributes") Map<String, String> attributes) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion within the transaction. 
     * It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>Each conversion account must match the currency of its associated account.</li>
     * </ol>
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionTen")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "currencyRate") CurrencyRateDTO conversionRate,
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "fromConversionId") Long fromConversionId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "toConversionId") Long toConversionId) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion within the transaction. 
     * It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>Each conversion account must match the currency of its associated account.</li>
     * </ol>
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     */
    @WebMethod(operationName = "createTransactionEleven")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "currencyRate") CurrencyRateDTO conversionRate,
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "fromConversionId") Long fromConversionId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "toConversionId") Long toConversionId,
            @WebParam(name = "attributes") Map<String, String> attributes) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NegativeBalanceException;
    
    /**
     * This method tries to do an automatic currency conversion within the transaction. 
     * It has the following pre-requisites:
     * 
     * <ol>
     *   <li>One of the accounts must be in the given currency.</li>
     *   <li>Each conversion account must match the currency of its associated account.</li>
     * </ol>
     * 
     * <p>This method will check the pre-requisites and the create a transaction consisting of 
     * two transfers, first the debit the "fromAccount" and credit the conversion account for the
     * "fromAccount" currency, and the debit the conversion account in for the "toAccount" currency
     * and credit the "toAccount".
     * 
     * <p>This method will search for currency rates 365 days back from the current date. This
     * is equivalent to using {@link #getCurrencyRate(String, String) this} method to get the 
     * conversion rate.
     * 
     * @throws NoSuchAccountException
     * @throws ClosedAccountException
     * @throws UnbalancedTransactionException
     * @throws NoSuchConversionRateException
     */
    @WebMethod(operationName = "createTransactionTwelve")
    public TransactionDTO createMultiCurrencyTransaction(
    		@WebParam(name = "comment") String comment,
    		@WebParam(name = "externalId") String externalId,
    		@WebParam(name = "currency") String currency,
            @WebParam(name = "amount") BigDecimal amount, 
            @WebParam(name = "fromAccountId") Long fromAccountId,
            @WebParam(name = "fromConversionId") Long fromConversionId,
            @WebParam(name = "toAccountId") Long toAccountId,
            @WebParam(name = "toConversionId") Long toConversionId) 
    throws NoSuchAccountException, ClosedAccountException, UnbalancedTransactionException, NoSuchConversionRateException, NegativeBalanceException;
    
    public AccountQueryResultDTO listAccounts(@WebParam(name = "accountId") Long accountId,
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "walletId") Long walletId,
            @WebParam(name = "currencyCode") String currencyCode,
            @WebParam(name = "entityStatuses") Set<AccountStatusDTO> entityStatuses,
            @WebParam(name = "entityTypes") Set<String> entityTypes, @WebParam(name = "offset") int offset,
            @WebParam(name = "limit") int limit, @WebParam(name = "order") AccountsOrderDTO order,
            @WebParam(name = "isAscending") boolean ascending);

    public TransactionQueryResultDTO listTransactions(@WebParam(name = "idOne") Long id1,
            @WebParam(name = "isAccountOneCredit") Boolean account1credit,
            @WebParam(name = "isIdOneUserId") boolean id1IsUserId, @WebParam(name = "idTwo") Long id2,
            @WebParam(name = "isAccountTwoCredit") Boolean account2credit,
            @WebParam(name = "isIdTwoUserId") boolean id2IsUserId,
            @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate,
            @WebParam(name = "offset") int offset, @WebParam(name = "limit") int limit,
            @WebParam(name = "order") TransactionsOrderDTO order, @WebParam(name = "isAscending") boolean ascending);

    public void setBalanceCheckpointInterval(@WebParam(name = "checkpointInterval") int balanceCheckpointInterval);

    public void setAsyncCheckpointCreation(@WebParam(name = "isAsynchronous") boolean async);

    public TransactionDTO getTransactionById(@WebParam(name = "transactionId") Long txId);

    public EntryQueryResultDTO listEntries(@WebParam(name = "accountId") Long accountId,
            @WebParam(name = "offset") int offset, @WebParam(name = "limit") int limit,
            @WebParam(name = "isAscending") boolean ascending);

    public BalancedEntryQueryResultDTO listEntriesBalanced(@WebParam(name = "accountId") Long accountId,
            @WebParam(name = "offset") int offset, @WebParam(name = "limit") int limit,
            @WebParam(name = "isAscending") boolean ascending);

    /**
     * @throws NoSuchAccountException
     */
    public void setAccountAttribute(@WebParam(name = "accountId") Long accountId, @WebParam(name = "key") String key,
            @WebParam(name = "value") String value) throws NoSuchAccountException;

    /**
     * @throws NoSuchAccountException
     */
    public void removeAccountAttribute(@WebParam(name = "accountId") Long accountId, @WebParam(name = "key") String key)
            throws NoSuchAccountException;
    
    /**
     * @throws NoSuchTransactionException
     */
    public void setTransactionAttribute(@WebParam(name = "transactionId") Long transactionId, @WebParam(name = "key") String key,
            @WebParam(name = "value") String value) throws NoSuchTransactionException;

    /**
     * @throws NoSuchTransactionException
     */
    public void removeTransactionAttribute(@WebParam(name = "transactionId") Long transactionId, @WebParam(name = "key") String key)
            throws NoSuchTransactionException;

    /**
     * @throws NoSuchAccountException
     */
    public void setAccountStatus(@WebParam(name = "accountId") Long accountId,
            @WebParam(name = "status") AccountStatusDTO status) throws NoSuchAccountException;

    
    /**
     * Returns the most recent currency day rate between the given currencies for the given date.
     * If no currency rate for the given date was found the the last rate found within 
     * the given number of days will be returned.
     * @param sourceCurrencyCode "from"-currency
     * @param targetCurrencyCode "to"-currency
     * @param date rate date
     * @param fallbackDays number of days to look back before giving up
     * @return the currency rate, null if not found
     */
    @WebMethod(operationName = "getCurrencyRateOne")
    public CurrencyRateDTO getCurrencyRate(
            @WebParam(name = "sourceCurrencyCode") String sourceCurrencyCode, 
            @WebParam(name = "targetCurrencyCode") String targetCurrencyCode,
            @WebParam(name = "date") Date date,
            @WebParam(name = "fallbackDays") int fallbackDays);

    /**
     * Returns the most recent currency day rate between the given currencies. This method
     * starts searching from the current date and 365 days back. 
     * 
     * @param sourceCurrencyCode "from"-currency
     * @param targetCurrencyCode "to"-currency
     * @return the currency rate, null if not found
     */
    @WebMethod(operationName = "getCurrencyRateTwo")
    public CurrencyRateDTO getCurrencyRate(
            @WebParam(name = "sourceCurrencyCode") String sourceCurrencyCode, 
            @WebParam(name = "targetCurrencyCode") String targetCurrencyCode);
    
    /**
     * Adds a currency exchange rate.
     * @param rate the rate to add
     */
    void addCurrencyRate(@WebParam(name = "currenyRate") CurrencyRateDTO rate);
    
    
    /**
     * This method updates a transaction with an external id. This method can
     * only be called if the transaction does not already have an external id. If
     * the transaction have an external id set a security exception will be thrown.
     * 
     * @param transactionId Transaction id
     * @param externalId Id to set, must not be null
     * @throws SecurityException If the external id is already set
     */
    void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException;
    
    
    /**
     * Create the reverse of an existing transaction. In order to trace the action, you can set
     * attributes on the old, reversed transaction, as well as on the new, reversing, transaction, 
     * however these must not exist on the transaction, ie it is an error to try to overwrite 
     * any existing attributes, and attempt to do so will result in a securiy exception.
     * 
     * @param txId Id of transaction to reverse, must not be null
     * @param newExternalId External ID of the new transaction, null to copy old
     * @param oldAttr Attributes to set on old transaction, may be null
     * @param newAttr Attributes to set on new transaction, may be null
     * @throws TransactionNotFoundException
     * @return A new transaction, never null
     */
    TransactionDTO reverseTransaction(Long txId, String newExternalId, Map<String, String> oldAttr, Map<String, String> newAttr) throws NoSuchTransactionException;
    
}
