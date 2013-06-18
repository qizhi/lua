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

import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * @author Fredrik
 */
@Path("/currency")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CurrencyResource {
    
//    private Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "wallet.service.adaptedWalletService")
	private WalletService walletService;
	
	/**
	 * Returns a supported currency. Won't return currencies
	 * marked as removed.
	 * @return collection of supported currencies
	 */
	@Path("id/{currencyCode}")
	@GET
	public Currency getCurrency(@PathParam("currencyCode") String currencyCode) {
		return walletService.getSupportedCurrency(currencyCode);
	}
	
	/**
	 * Removes the currency with the given code. Note that this will only mark
	 * the currency as removed.
	 * @param currencyCode currency code
	 */
    @Path("id/{currencyCode}")
    @DELETE
    public Response removeCurrency(@PathParam("currencyCode") String currencyCode) {
        walletService.removeSupportedCurrency(currencyCode);
        return Response.status(Response.Status.OK).build();
    }
}
