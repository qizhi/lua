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

package com.cubeia.backoffice.accounting.service;

import java.util.Currency;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.cubeia.backoffice.accounting.api.AccountingService;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountFactory;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;

@ContextConfiguration(locations = {"classpath:accounting-service-test.xml"})
@TransactionConfiguration(transactionManager = "accounting.transactionManager")
public abstract class BaseTest extends AbstractTransactionalJUnit4SpringContextTests {
	
    @Resource(name = "accounting.accountingService")
	protected AccountingService accountingService;

    @Resource(name = "accounting.accountingManager")
	protected AccountingManager accountingManager;
 
    protected Account createAccount(String currency) {
		Account a = AccountFactory.create(Currency.getInstance(currency));
		accountingManager.createAccount(a);
		return a;
	}
    
    protected Account createAccount(Long extId, String currency, int fractions, String type) {
		Account a = AccountFactory.create(extId, currency, fractions, type);
		accountingManager.createAccount(a);
		return a;
	}
}
