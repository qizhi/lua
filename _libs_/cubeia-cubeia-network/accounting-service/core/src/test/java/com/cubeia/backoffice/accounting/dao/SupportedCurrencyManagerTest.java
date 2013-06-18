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

package com.cubeia.backoffice.accounting.dao;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;

import com.cubeia.backoffice.accounting.BaseTest;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;

public class SupportedCurrencyManagerTest extends BaseTest {
	
	@Resource(name = "accounting.supportedCurrenciesManager")
	protected SupportedCurrencyManager supportedCurrencyManager;
	
	@Test
	public void getSupportedCurrencies() {
		supportedCurrencyManager.addCurrency(new SupportedCurrency("EUR", 2));
		supportedCurrencyManager.addCurrency(new SupportedCurrency("BID", 0));
		assertEquals(2, supportedCurrencyManager.getCurrencies().size());
	}
}