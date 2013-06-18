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

package com.cubeia.backend.firebase;

import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.STATIC_ACCOUNT;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.SYSTEM_ACCOUNT;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.OPERATOR_ACCOUNT;

import static java.util.Arrays.asList;

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;

public class AccountLookupUtil {

    public long lookupPromotionsAccountId(WalletServiceContract walletService, String currencyCode) throws SystemException {
        return lookupSystemAccount(walletService, currencyCode, CashGamesBackendAdapter.PROMOTIONS_ACCOUNT_USER_ID);
    }

    public long lookupRakeAccountId(WalletServiceContract walletService, String currency) throws SystemException {
        return lookupSystemAccount(walletService, currency, CashGamesBackendAdapter.RAKE_ACCOUNT_USER_ID);
    }

    private long lookupSystemAccount(WalletServiceContract walletService, String currency, Long accountUserId) throws SystemException {
        ListAccountsRequest request = new ListAccountsRequest();
        request.setStatus(AccountStatus.OPEN);
        request.setTypes(asList(SYSTEM_ACCOUNT));
        request.setUserId(accountUserId);
        AccountQueryResult accounts = walletService.listAccounts(request);
        for (Account account : accounts.getAccounts()) {
            if (account.getCurrencyCode().equals(currency)) {
                return account.getId();
            }
        }
        throw new SystemException("Error getting rake account for currency " + currency + ". Looked for account matching: " + request);
    }

    /**
     * Gets the account id for the account with the given playerId and currency code.
     *
     * @param walletService the service to use for doing the remote call
     * @param playerId the id of the player who owns the account
     * @param currency the currency code that the account should have
     * @return the accountId of the matching account, or -1 if none found
     */
    public long lookupAccountIdForPlayerAndCurrency(WalletServiceContract walletService, long playerId, String currency) {
        ListAccountsRequest request = new ListAccountsRequest();
        request.setStatus(AccountStatus.OPEN);
        request.setTypes(asList(STATIC_ACCOUNT));
        request.setUserId(playerId);
        AccountQueryResult accounts = walletService.listAccounts(request);
        if (accounts.getAccounts() == null || accounts.getAccounts().size() < 1) {
            return -1;
        }

        for (Account account : accounts.getAccounts()) {
            if (account.getCurrencyCode().equals(currency)) {
                return account.getId();
            }
        }
        return -1;
    }

    public long lookupOperatorAccountId(WalletServiceContract walletService, long operatorId) throws SystemException {
        ListAccountsRequest request = new ListAccountsRequest();
        request.setLimit(1);
        request.setStatus(AccountStatus.OPEN);
        request.setTypes(asList(OPERATOR_ACCOUNT));
        request.setUserId(operatorId);
        AccountQueryResult accounts = walletService.listAccounts(request);
        if (accounts.getAccounts() == null || accounts.getAccounts().size() != 1 || accounts.getAccounts().iterator().next().getType() != OPERATOR_ACCOUNT) {
            throw new SystemException("Error getting operator account. Looked for account matching: " + request);
        }
        return accounts.getAccounts().iterator().next().getId();
    }


}
