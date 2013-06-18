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

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.cubeia.backoffice.wallet.WalletService;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;

/**
 * 
 * @author Fredrik
 */
@Path("/currencies")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CurrenciesResource {
    
    private Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "wallet.service.adaptedWalletService")
	private WalletService walletService;
	
	/**
	 * Returns a collection of supported currencies. Won't return currencies
	 * marked as removed.
	 * @return collection of supported currencies
	 */
	@GET
	public CurrencyListResult getSupportedCurrencies() {
		return new CurrencyListResult(new ArrayList<Currency>(walletService.getSupportedCurrencies()));
	}
	
	/**
	 * Create or resurrect a currency.
	 * @param currency currency to add
	 */
    @POST
    public Response addCurrency(Currency currency) {
        log.info("adding currency: {}", currency);
        walletService.addSupportedCurrency(currency);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Update a currency.
     * @param currency
     * @return the result
     */
    @PUT
    @Profiled
    public Response updateCurrency(Currency currency){
        walletService.updateSupportedCurrency(currency);
        return Response.status(Response.Status.OK).build();
    }
}