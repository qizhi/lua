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

package com.cubeia.backoffice.wallet.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;

public class WalletDTOFactoryEnumTest  {
    private WalletDTOFactory dtoFactory;

    @Before
    public void setUp() throws Exception {        
        dtoFactory = new WalletDTOFactory();
    }
    
    /*public void testCreateEntityAccountStatusFromDTO() {
        assertEquals(
                com.cubeia.backoffice.wallet.manager.WalletAccountStatus.CLOSED,
                dtoFactory.createAccountStatusEntityFromDTO(AccountStatus.CLOSED));
        assertEquals(
                com.cubeia.backoffice.wallet.entity.WalletAccountStatus.CLOSED_WITH_NON_ZERO_BALANCE,
                dtoFactory.createAccountStatusEntityFromDTO(AccountStatus.CLOSED_WITH_NON_ZERO_BALANCE));
        assertEquals(
                com.cubeia.backoffice.wallet.entity.WalletAccountStatus.OPEN,
                dtoFactory.createAccountStatusEntityFromDTO(AccountStatus.OPEN));
    }*/

    @Test
    public void testCreateEntityAccountsOrderFromDTO() {
        assertEquals(
                AccountsOrder.ID,
                dtoFactory.createAccountsOrderEntityFromDTO(com.cubeia.backoffice.wallet.api.dto.AccountsOrder.ID));
        assertEquals(
                AccountsOrder.STATUS,
                dtoFactory.createAccountsOrderEntityFromDTO(com.cubeia.backoffice.wallet.api.dto.AccountsOrder.STATUS));
        assertEquals(
                AccountsOrder.USER_ID,
                dtoFactory.createAccountsOrderEntityFromDTO(com.cubeia.backoffice.wallet.api.dto.AccountsOrder.USER_ID));
    }

    /*public void testCreateDTOAccountTypeFromEntity() {
        assertEquals(
                AccountType.OPERATOR_ACCOUNT,
                dtoFactory.createAccountTypeDTOFromEntity(com.cubeia.backoffice.wallet.entity.AccountType.OPERATOR_ACCOUNT));
        assertEquals(
                AccountType.SESSION_ACCOUNT,
                dtoFactory.createAccountTypeDTOFromEntity(com.cubeia.backoffice.wallet.entity.AccountType.SESSION_ACCOUNT));
        assertEquals(
                AccountType.STATIC_ACCOUNT,
                dtoFactory.createAccountTypeDTOFromEntity(com.cubeia.backoffice.wallet.entity.AccountType.STATIC_ACCOUNT));
        assertEquals(
                AccountType.SYSTEM_ACCOUNT,
                dtoFactory.createAccountTypeDTOFromEntity(com.cubeia.backoffice.wallet.entity.AccountType.SYSTEM_ACCOUNT));
    }*/
    
    @Test
    public void testCreateDTOAccountStatusFromEntity() {
        assertEquals(
                AccountStatus.CLOSED, 
                dtoFactory.createAccountStatusDTOFromEntity(com.cubeia.backoffice.accounting.core.entity.AccountStatus.CLOSED));
        /*assertEquals(
                AccountStatus.CLOSED_WITH_NON_ZERO_BALANCE, 
                dtoFactory.createAccountStatusDTOFromEntity(com.cubeia.backoffice.accounting.entity.WalletAccountStatus.CLOSED_WITH_NON_ZERO_BALANCE));*/
        assertEquals(
                AccountStatus.OPEN, 
                dtoFactory.createAccountStatusDTOFromEntity(com.cubeia.backoffice.accounting.core.entity.AccountStatus.OPEN));
    }
}
