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

package com.cubeia.backoffice.accounting.core.dao;

import java.util.List;

import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;

public interface SupportedCurrencyDAO {

	SupportedCurrency getCurrencyByCode(String currencyCode);
	
	List<SupportedCurrency> getCurrencies();
	
	void addCurrency(SupportedCurrency currency);

    void updateCurrency(SupportedCurrency currency);
}