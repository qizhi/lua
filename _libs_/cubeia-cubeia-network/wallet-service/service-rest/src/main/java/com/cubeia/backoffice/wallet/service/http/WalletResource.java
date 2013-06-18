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
package com.cubeia.backoffice.wallet.service.http;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.accounting.api.NoSuchAccountException;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.DepositRequest;
import com.cubeia.backoffice.wallet.api.dto.DepositResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.exception.TransactionNotBalancedException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListEntriesRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListTransactionsRequest;

/**
 * 
 * @author Fredrik
 */
@Path("/wallet")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class WalletResource {
    
    private Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "wallet.service.adaptedWalletService")
	private WalletService walletService;
	
	@Autowired
	private SupportedCurrencyManager supportedCurrencyManager;


	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/
	
	/**
	 * Returns a transaction by id.
	 * @param transactionId transaction id
	 * @return the transaction
	 */
	@Path("transaction/id/{transactionId}")
	@GET
	@Profiled
	public Transaction getTransactionById(@PathParam("transactionId") Long transactionId) {
		return walletService.getTransactionById(transactionId);
	}
	
	/**
	 * Lookup a static account by its owner (user id) and currency.
	 * If the account is missing it will be created and returned.
	 * @param userId the user id
	 * @param currencyCode the ISO 4217 currency code
	 * @return the account
	 */
	@Path("lookup/{userId}/{currency}")
	@GET
	@Profiled
	public Account getAccountByUserAndCurrency(@PathParam("userId") Long userId, @PathParam("currency") String currencyCode) {
		Long accountId = walletService.getOrCreateStaticAccountIdForUserAndCurrency(userId, currencyCode);
		return walletService.getAccountById(accountId);
	}
	
	/**
	 * Lookup a static account's balance by it's owner (user id) and currency.
	 * If the account is missing it will be created and zero balance returned.
	 * 
	 * @param userId the user id
	 * @param currencyCode the ISO 4217 currency code
	 * @return the account
	 */
	@Path("balance/{userId}/{currency}")
	@GET
	@Profiled
	public AccountBalanceResult getBalanceByUserAndCurrency(@PathParam("userId") Long userId, @PathParam("currency") String currencyCode) {
		Long accountId = walletService.getOrCreateStaticAccountIdForUserAndCurrency(userId, currencyCode);
		AccountBalanceResult accountBalance;
		try {
			accountBalance = walletService.getAccountBalance(accountId);
		} catch (AccountNotFoundException e) {
			throw new WebApplicationException(Status.NOT_FOUND); // This should not really happen since we create it above if not found
		}
		return accountBalance;
	}
	
	
	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/

	/**
	 * Creates a new account.
	 * @param request the create account request
	 * @return the result
	 */
	@Path("/accounts")
	@POST
	@Profiled
	public CreateAccountResult createAccount(CreateAccountRequest request) {
		log.info("Create account: "+request);
		return walletService.createAccount(request);
	}
	
	/**
	 * Perform a transaction.
	 * @param transaction the transaction specification
	 * @return the result
	 */
	@Path("/transaction")
	@POST
	@Profiled
	public TransactionResult doTransaction(TransactionRequest transaction) {
		 if(log.isDebugEnabled()) {
			 log.debug("ReportTransaction: "+transaction);
		 }
		 try {
			return walletService.doTransaction(transaction);
		} catch (TransactionNotBalancedException e) {
			log.error("Unbalanced transaction supplied: "+transaction);
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		} catch (NegativeBalanceException e) {
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}
	

	/* ---------------------------------
	 * PUT METHODS
	 * ---------------------------------*/

	/**
	 * List accounts.
	 * @param request account list parameters
	 * @return result
	 */
	@Path("/accounts")
	@PUT
	@Profiled
	public AccountQueryResult listAccounts(ListAccountsRequest request){
		log.debug("list accounts request: {}", request);
		AccountQueryResult accounts = walletService.listAccounts(
				request.getAccountId(), 
				request.getUserId(), 
				request.getStatuses(),
				request.getTypes(),
				request.getOffset(),
				request.getLimit(),
				request.getSortOrder(),
				request.isAscending());
		if(log.isTraceEnabled()) {
			log.trace("List accounts result: "+accounts);
		} else {
			log.debug("List accounts returns " + accounts.getAccounts().size() + " of a total " + accounts.getTotalQueryResultSize());
		}
		return accounts;
	}

	/**
	 * List transactions.
	 * @param request transaction list request
	 * @return result
	 */
	@Path("/transactions")
    @PUT
	@Profiled
    public TransactionQueryResult listTransactions(ListTransactionsRequest request){
		return walletService.listTransactions(
				request.getId1(),
				request.getId1credit(),
				request.isId1IsUserId(),
				request.getId2(),
				request.getId2credit(),
				request.isId2IsUserId(),
				request.getStartDate(),
				request.getEndDate(),
				request.getOffset(),
    			request.getLimit(),
    			request.getOrder(),
    			request.isAscending());
    }
	
	/**
	 * List entries.
	 * @param request entry list request
	 * @return result
	 */
	@Path("/entries")
    @PUT
	@Profiled
    public EntriesQueryResult listEntries(ListEntriesRequest request){
		return walletService.listEntries(
				request.getAccountId(),
				request.isIncludeBalances(),
				request.getOffset(),
    			request.getLimit(),
    			request.isAscending());
    }
	
	/**
	 * This method is deprecated. DO NOT USE.
	 * @param request request
	 * @return result
	 */
	@Path("/deposit")
    @POST
    @Deprecated
	@Profiled
    public DepositResult deposit(DepositRequest request){
		try {
			SupportedCurrency currency = supportedCurrencyManager.getCurrencyByCode(request.getCurrencyCode());
			
			if(currency == null) {
				log.error(request.getCurrencyCode() + "is not a supported currency.");
				throw new WebApplicationException(Response.Status.BAD_REQUEST);
			}
			
			Money amount = new Money(request.getCurrencyCode(), currency.getFractionalDigits(), new BigDecimal(request.getAmount()));
			return walletService.deposit(request.getUserId(), amount, request.getFromAccountId());
		} catch (NoSuchAccountException e) {
			log.error("No account found with id: " + e.getAccountId(), e);
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		} 
    }
	
	
	/* ---------------------------------
	 * PRIVATE METHODS
	 * ---------------------------------*/
	
}
