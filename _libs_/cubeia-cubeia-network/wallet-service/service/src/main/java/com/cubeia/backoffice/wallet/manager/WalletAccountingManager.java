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

import java.util.Set;

import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;

public interface WalletAccountingManager extends AccountingManager {
  
    Account getAccountByUserId(Long userId);

    /**
     * List accounts by the given search criterias/filters. A null or empty filter parameter is a wildcard.
     * @param accountId account id, null for all
     * @param userId user id, null for all
     * @param entityStatuses statuses to include, null for all
     * @param entityTypes account types to include, null for all
     * @param offset result set offset
     * @param limit result set max size
     * @param order sort order
     * @param ascending true for ascending, false for descending
     * @return the matching accounts and the size of the total result set without the size limit
     */
    QueryResultsContainer<Account> listAccounts(Long accountId, Long userId,
        Set<AccountStatus> entityStatuses, Set<String> entityTypes, int offset, int limit,
        AccountsOrder order, boolean ascending);

    Account getAccountByUserIdTypeAndCurrency(Long userId, String type, String currencyCode);
    
	Account getAccountByUserIdAndCurrency(Long userId, String currency);
	
	/**
	 * Get Account for user and currency combination that is not a SESSION ACCOUNT type.
	 * I.e. all Session Accounts will be filtered out.
	 * 
	 * @param userId
	 * @param currency
	 * @return
	 */
	public Account getNonSessionAccountByUserIdAndCurrency(Long userId, String currency);

}
