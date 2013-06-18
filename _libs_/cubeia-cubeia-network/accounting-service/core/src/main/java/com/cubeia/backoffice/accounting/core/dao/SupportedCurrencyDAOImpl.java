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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;

@Component("accounting.supportedCurrencyDAO")
public class SupportedCurrencyDAOImpl implements SupportedCurrencyDAO {

	@PersistenceContext(unitName = "accountingPersistenceUnit")
    protected EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public SupportedCurrency getCurrencyByCode(String currencyCode) {		
		List<SupportedCurrency> matchingCurrencies = (List<SupportedCurrency>)em.createQuery("from SupportedCurrency where currencyCode = :currencyCode")
			.setParameter("currencyCode", currencyCode)
			.getResultList();
		if(matchingCurrencies.isEmpty()) {
			return null;
		}
		return matchingCurrencies.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SupportedCurrency> getCurrencies() {
		return (List<SupportedCurrency>)em.createQuery("from SupportedCurrency").getResultList();
	}

	@Override
	public void addCurrency(SupportedCurrency currency) {
        em.persist(currency);
	}

    @Override
    public void updateCurrency(SupportedCurrency currency) {
        em.merge(currency);
    }
}