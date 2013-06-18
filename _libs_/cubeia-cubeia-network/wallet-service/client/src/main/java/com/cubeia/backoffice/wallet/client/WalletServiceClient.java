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
package com.cubeia.backoffice.wallet.client;

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListEntriesRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListTransactionsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;


/**
 * 
 * 
 * @author Fredrik
 */
public interface WalletServiceClient {

	AccountBalanceResult getAccountBalance(Long accountId) throws AccountNotFoundException;

	Account getAccountById(Long accountId);

	EntriesQueryResult listEntries(ListEntriesRequest request);

	AccountQueryResult listAccounts(ListAccountsRequest request);

	void updateAccount(Account account);

	void closeAccount(Long accountId) throws AccountNotFoundException;;

	void openAccount(Long accountId);

	Transaction getTransactionById(Long transactionId);

	TransactionQueryResult listTransactions(ListTransactionsRequest request);

	void transfer(Long accountId, TransferRequest request);

	CreateAccountResult createAccount(CreateAccountRequest request);
	
	/**
	 * Get (or create if missing) a static account by the user and currency.
	 * @param userId user id
	 * @param currencyCode currency
	 * @return the account
	 */
	Account getAccount(Long userId, String currencyCode);

	TransactionResult doTransaction(TransactionRequest transaction);

	void setBaseUrl(String baseUrl);

	String getBaseUrl();
	
    CurrencyListResult getSupportedCurrencies();
    
    void addCurrency(Currency currency);

    Currency getCurrency(String currencyCode);

    void updateCurrency(Currency currency);
    
    public void removeCurrency(String currencyCode);
    
    /**
     * Returns "pong" if the service is up and running.
     * @return "pong" 
     */
    public String ping();

    /**
     * Lookup the account balance from user and currency. If no account was found a new one
     * will be created.
     * 
     * @param userId
     * @param currencyCode
     * @return
     */
    AccountBalanceResult getAccountBalanceByUserAndCurrency(Long userId, String currencyCode);
}
