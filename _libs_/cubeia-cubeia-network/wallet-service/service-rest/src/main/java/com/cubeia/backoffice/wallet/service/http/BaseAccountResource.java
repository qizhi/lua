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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Account;

/**
 * @author Fredrik
 */
@Path("/account")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class BaseAccountResource {

	@SuppressWarnings("unused")
	private Logger log = Logger.getLogger(getClass());

	@Autowired
	private AccountResource accountResource;
	
	@Resource(name = "wallet.service.adaptedWalletService")
	private WalletService walletService;

	
	/* ---------------------------------
	 * DELEGATE RESOURCES
	 * ---------------------------------*/

	/**
	 * @param accountId The account id.
	 */
	@Path("id/{accountId}")
	public AccountResource getAccountById(@PathParam("accountId") Long accountId) throws IOException {
		Account account = walletService.getAccountById(accountId);
		checkAccountFound(account);
		accountResource.setAccount(account);
		return accountResource;
	}

	

	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/


	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/


	
	
	/* ---------------------------------
	 * PRIVATE METHODS
	 * ---------------------------------*/
	
	private void checkAccountFound(Account account) {
		if (account == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
}
