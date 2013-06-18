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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.core.dao.SupportedCurrencyDAO;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;

@Component("accounting.supportedCurrenciesManager")
public class SupportedCurrencyManagerImpl implements SupportedCurrencyManager{

	@Resource(name= "accounting.supportedCurrencyDAO")
	protected SupportedCurrencyDAO supportedCurrenciesDAO;
	
	/**
	 * Returns the currency by the given code.
	 * The currency will be returned even if it is marked as removed.
	 * @return the currency
	 */
	@Override
	public SupportedCurrency getCurrencyByCode(String currencyCode) {
		return supportedCurrenciesDAO.getCurrencyByCode(currencyCode);
	}
	
	/**
	 * Returns a list of supported currencies. 
	 * Currencies marked as removed will not be returned.
	 * @return supported currencies
	 */
	@Override
	public List<SupportedCurrency> getCurrencies() {
		List<SupportedCurrency> currencies = new ArrayList<SupportedCurrency>();
		for (SupportedCurrency sc : supportedCurrenciesDAO.getCurrencies()) {
			if (!sc.isRemoved()) {
				currencies.add(sc);
			}
		}
		return currencies;
	}
	
	@Override
	public void addCurrency(SupportedCurrency currency) {
		supportedCurrenciesDAO.addCurrency(currency);
	}

    @Override
    public void updateCurrency(SupportedCurrency currency) {
        supportedCurrenciesDAO.updateCurrency(currency);
    }
}