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

package com.cubeia.backoffice.wallet;

import javax.annotation.Resource;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.manager.MockExternalWalletImpl;
import com.cubeia.backoffice.wallet.manager.WalletAccountingManager;
import com.cubeia.backoffice.wallet.manager.WalletManager;

/**
 * IOC enabled base test case.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 *
 */
@ContextConfiguration(locations = {"classpath:wallet-service-app-test.xml"})
@TransactionConfiguration(transactionManager="accounting.transactionManager")
//@TransactionConfiguration(transactionManager="wallet.service.transactionManager")
public abstract class BaseTest extends AbstractTransactionalJUnit4SpringContextTests {
	
    @Resource(name = "wallet.service.accountingManager")
	protected WalletAccountingManager accountingManager;

	// @Resource(name = "wallet.service.accountingDAO")
	// protected AccountingDAO accountingDAO;
	
	@Resource(name = "wallet.service.walletManager")
	protected WalletManager walletManager;
	
	@Resource(name = "wallet.service.walletService")
	protected WalletService walletService;
	
	@Resource(name = "wallet.service.adaptedWalletService")
	protected WalletService adaptedWalletService;
	
	@Resource(name= "wallet.service.externalAccountManager")
	protected MockExternalWalletImpl mockExternalWalletImpl;
	
	@Resource(name = "accounting.supportedCurrenciesManager")
	protected SupportedCurrencyManager supportedCurrenciesManager;
	
//	@Resource(name= "accounting.supporetedCurrencyDAO")
//	protected SupportedCurrencyDAO supportedCurrenciesDAO;
	
	/*
	protected String[] getConfigLocations() {
		return new String[] { "classpath:wallet-service-app-test.xml" };
	}*/
	
	@Before
	public void setup() {
		supportedCurrenciesManager.addCurrency(new SupportedCurrency("EUR", 2));
	}
	
}
