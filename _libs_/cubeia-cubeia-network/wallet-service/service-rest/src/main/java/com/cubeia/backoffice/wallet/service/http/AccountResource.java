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

import java.io.IOException;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.perf4j.aop.Profiled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;

/**
 * 
 * @author Fredrik
 */
@Component
/* This component needs to be prototype to avoid concurrency issues. */
@Scope("prototype")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AccountResource {

	private Logger log = Logger.getLogger(getClass());
	
	@Resource(name = "wallet.service.adaptedWalletService")
	private WalletService walletService;
	
	@SuppressWarnings("unused")
	@Autowired
	private SupportedCurrencyManager supportedCurrencyManager;

	private Account account;

	public void setAccount(Account account) {
		this.account = account;
	}

	
	/* ---------------------------------
	 * DELEGATE RESOURCES
	 * ---------------------------------*/
	
	
	
	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/

	/**
	 * Returns the account by the given id.
	 * @param accountId The account id.
	 * @return the account
	 */
	@GET
	@Profiled
	public Account getAccountById() {
		return account;
	}
	
	/**
	 * Returns the current balance for the account.
	 * @return
	 */
	@Path("balance")
	@GET
	@Profiled
	public AccountBalanceResult getBalance() {
		try {
			return walletService.getAccountBalance(account.getId());
		} catch (AccountNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	/**
	 * Returns the current balance for the account in plain text on the format "[amount] [currency code]".
	 * Example:
	 * <code>134.56 EUR</code>
	 * @return
	 * @throws IOException
	 */
	@Path("balance/plain")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Profiled
	public String getBalanceAsPlainText() throws IOException {
		try {
			AccountBalanceResult balance = walletService.getAccountBalance(account.getId());
			return balance.getBalance().getAmount() + " " + balance.getBalance().getCurrencyCode();
		} catch (AccountNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	
	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/

	/**
	 * Debit or credit an account. 
	 * Depending on the account's type the system will
	 * determine which other account will be used in the transaction.
	 * <ul>
	 * <li>
	 * If the account is of type {@value AccountType#SESSION_ACCOUNT} the transfer will be against the
	 * user's corresponding {@value AccountType#STATIC_ACCOUNT}.
	 * </li>
	 * <li>
	 * If it is a {@value AccountType#STATIC_ACCOUNT} the transfer will be done against the user's remote wallet account.
	 * </li>
	 * </ul>
	 * @param request the transfer request
	 * @return the result
	 */
	@POST
	@Profiled
    public Response transferFunds(TransferRequest request){
		log.info("Transfer funds: "+request);
		try {
			walletService.handleTransferFunds(account, request);
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			log.error("Could not handle funds transfer.", e);
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
    }



	/* ---------------------------------
	 * PUT METHODS
	 * ---------------------------------*/
	
	/**
	 * Update an account.
	 * @param account the updated account dat
	 * @return the result
	 */
    @PUT
	@Profiled
    public Response updateAccount(Account account){
		walletService.updateAccount(account);
		return Response.status(Response.Status.OK).build();
    }
	
    /**
     * Open the given account.
     * @return the result
     */
	@Path("open")
    @PUT
	@Profiled
    public Response openAccount(){
		try {
			walletService.openAccount(account.getId());
			return Response.status(Response.Status.OK).build();
			
		} catch (AccountNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
    }
	
	/**
	 * Close the given account.
	 * @return the result
	 */
	@Path("close")
    @PUT
	@Profiled
    public Response closeAccount(){
    	try {
			walletService.closeAccount(account.getId());
			return Response.status(Response.Status.OK).build();
			
		} catch (AccountNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
    }
	

	/* ---------------------------------
	 * DELETE METHODS
	 * ---------------------------------*/

	
	
}
