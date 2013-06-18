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

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;

import static com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus.OPEN;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.OPERATOR_ACCOUNT;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.SYSTEM_ACCOUNT;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountLookupUtilTest {
    @Mock
    private WalletServiceContract walletService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLookupRakeAccountId() throws SystemException {
        AccountLookupUtil acl = new AccountLookupUtil();

        ArgumentCaptor<ListAccountsRequest> requestCaptor = ArgumentCaptor.forClass(ListAccountsRequest.class);

        AccountQueryResult accountQueryResult = mock(AccountQueryResult.class);
        Account rakeAccount = mock(Account.class);
        Long rakeAccountId = -2000L;
        when(rakeAccount.getId()).thenReturn(rakeAccountId);
        when(rakeAccount.getCurrencyCode()).thenReturn("EUR");
        when(accountQueryResult.getAccounts()).thenReturn(asList(rakeAccount));
        when(walletService.listAccounts(requestCaptor.capture())).thenReturn(accountQueryResult);

        long lookupRakeAccountId = acl.lookupRakeAccountId(walletService, "EUR");
        assertThat(lookupRakeAccountId, is(rakeAccountId));

        ListAccountsRequest lar = requestCaptor.getValue();
        assertThat(lar.getStatuses(), is((Collection<AccountStatus>) asList(OPEN)));
        assertThat(lar.getTypes(), is((Collection<AccountType>) asList(SYSTEM_ACCOUNT)));
        assertThat(lar.getUserId(), is(CashGamesBackendAdapter.RAKE_ACCOUNT_USER_ID));
    }

    @Test
    public void testLookupOperatorAccountId() throws SystemException {
        AccountLookupUtil acl = new AccountLookupUtil();

        ArgumentCaptor<ListAccountsRequest> requestCaptor = ArgumentCaptor.forClass(ListAccountsRequest.class);

        AccountQueryResult accountQueryResult = mock(AccountQueryResult.class);
        Account operatorAccount = mock(Account.class);
        Long operatorAccountId = -2000L;
        Long operatorId = 5L;
        when(operatorAccount.getId()).thenReturn(operatorAccountId);
        when(operatorAccount.getUserId()).thenReturn(operatorId);
        when(operatorAccount.getType()).thenReturn(OPERATOR_ACCOUNT);
        when(accountQueryResult.getAccounts()).thenReturn(asList(operatorAccount));
        when(walletService.listAccounts(requestCaptor.capture())).thenReturn(accountQueryResult);

        long lookupRakeAccountId = acl.lookupOperatorAccountId(walletService, operatorId);
        assertThat(lookupRakeAccountId, is(operatorAccountId));

        ListAccountsRequest lar = requestCaptor.getValue();
        assertThat(lar.getLimit(), is(1));
        assertThat(lar.getStatuses(), is((Collection<AccountStatus>) asList(OPEN)));
        assertThat(lar.getTypes(), is((Collection<AccountType>) asList(OPERATOR_ACCOUNT)));
        assertThat(lar.getUserId(), is(operatorId));
    }

}
