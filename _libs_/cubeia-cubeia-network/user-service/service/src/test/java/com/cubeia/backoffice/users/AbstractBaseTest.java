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
package com.cubeia.backoffice.users;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * IOC enabled base test case.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 *
 */
@ContextConfiguration(locations = {
        "classpath:user-service-app-test.xml"})
@TransactionConfiguration(transactionManager="user.service.transactionManager")
public abstract class AbstractBaseTest extends AbstractTransactionalJUnit4SpringContextTests {
	
//	protected String[] getConfigLocations() {
//		return new String[] { "classpath:user-service-app-test.xml" };
//	}
}
