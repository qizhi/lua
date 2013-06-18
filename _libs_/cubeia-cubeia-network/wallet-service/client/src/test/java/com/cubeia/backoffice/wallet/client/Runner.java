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

import java.math.BigDecimal;
import java.util.UUID;

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListEntriesRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListTransactionsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest.TransferType;

public class Runner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WalletServiceClient client = new WalletServiceClientHTTP("http://localhost:9091/wallet-service-rest/rest");

			Currency currency = client.getCurrency("EUR");
            System.out.println("Currency: "+currency);
            System.out.println("--------------");
            
            client.addCurrency(new Currency("XXX", 1));
			
            CurrencyListResult supportedCurrencies = client.getSupportedCurrencies();
            System.out.println("Currencies: " + supportedCurrencies);
            System.out.println("--------------");
			
            client.removeCurrency("XXX");
            
            supportedCurrencies = client.getSupportedCurrencies();
            System.out.println("Currencies: "+supportedCurrencies);
            System.out.println("--------------");
            
			
			
			Account account = client.getAccountById(1l);
			System.out.println("Account: "+account);
			
			System.out.println("--------------");
			
			AccountBalanceResult balance = client.getAccountBalance(1l);
			System.out.println("Account Balance: "+balance);
			
			System.out.println("--------------");
			
			ListAccountsRequest request = new ListAccountsRequest();
			request.setOffset(0);
			request.setLimit(10);
			request.setSortOrder(AccountsOrder.ID);
			request.setAscending(true);
			AccountQueryResult listAccounts = client.listAccounts(request);
			System.out.println("List Accounts: "+listAccounts);
			
			System.out.println("--------------");
			
			Transaction tx = client.getTransactionById(1l);
			System.out.println("Transaction: "+tx);
			
			System.out.println("--------------");
			
			ListTransactionsRequest requestTx = new ListTransactionsRequest();
			requestTx.setOffset(0);
			requestTx.setLimit(10);
			requestTx.setOrder(TransactionsOrder.ID);
			requestTx.setAscending(true);
			TransactionQueryResult txs = client.listTransactions(requestTx);
			System.out.println("Transactions: "+txs);
			
			System.out.println("--------------");
			
			ListEntriesRequest requestEntries = new ListEntriesRequest();
			requestEntries.setIncludeBalances(true);
			requestEntries.setOffset(0);
			requestEntries.setLimit(10);
			requestEntries.setAscending(true);
			
			EntriesQueryResult entries = client.listEntries(requestEntries);
			System.out.println("Entries: "+entries);
			
			
	        CreateAccountRequest createSession = new CreateAccountRequest();
	        createSession.setRequestId(UUID.randomUUID());
	        createSession.setCurrencyCode("EUR");
	        createSession.setType(AccountType.SESSION_ACCOUNT);
	        createSession.setUserId(1l);
	        Long sessionId = client.createAccount(createSession).getAccountId();
	        
			TransferRequest transfer = new TransferRequest();
			transfer.setRequestId(UUID.randomUUID());
			transfer.setAmount(new BigDecimal(10));
			transfer.setOperatorId(1l);
			transfer.setTransferType(TransferType.CREDIT);
			
			client.transfer(sessionId, transfer);
			Account userAccount = client.getAccount(1l, "EUR");
			
			System.out.println("Transferred funds, " +
					"Session["+client.getAccountBalance(sessionId).getBalance()+"] " +
					"Account["+client.getAccountBalance(userAccount.getId()).getBalance()+"]");
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}