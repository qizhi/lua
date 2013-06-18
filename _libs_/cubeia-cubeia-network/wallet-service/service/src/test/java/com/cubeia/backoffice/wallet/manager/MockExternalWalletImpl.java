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

package com.cubeia.backoffice.wallet.manager;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component("wallet.service.mockExternalAccountManager")
public class MockExternalWalletImpl implements ExternalAccountManager {

    public BigDecimal depositAmount;
    public String depositExternalUserId;
    public long depositLicenseeId;
    public String depositReturnValue;
    
    public BigDecimal withdrawAmount;
    public String withdrawExternalUserId;
    public long withdrawLicenseeId;
    public String withdrawReturnValue;
    
    @Override
    public String deposit(BigDecimal amount, String externalUserId, long licenseeId) {
        depositAmount = amount;
        depositExternalUserId = externalUserId;
        depositLicenseeId = licenseeId;
        return depositReturnValue;
    }

    @Override
    public String withdraw(BigDecimal amount, String externalUserId, long licenseeId) {
        withdrawAmount = amount;
        withdrawExternalUserId = externalUserId;
        withdrawLicenseeId = licenseeId;
        return withdrawReturnValue;
    }
    
    public void clear() {
        depositAmount = BigDecimal.ZERO;
        depositExternalUserId = null;
        depositLicenseeId = 0;
        depositReturnValue = null;
        
        withdrawAmount = BigDecimal.ZERO;
        withdrawExternalUserId = null;
        withdrawLicenseeId = 0;
        withdrawReturnValue = null;
    }

}
