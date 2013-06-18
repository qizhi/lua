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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.adapter.BaseWalletServiceExtension;
import com.cubeia.backoffice.wallet.adapter.WalletServiceAdapter;
import com.cubeia.backoffice.wallet.api.dto.WithdrawResult;

@Component
public class TestWalletAdapter extends BaseWalletServiceExtension implements WalletServiceAdapter {

	public static final AtomicBoolean IS_METHOD_CALLED = new AtomicBoolean();
	
	@Resource(name = "wallet.service.walletService")
	private WalletService service;
	
	@PostConstruct
	protected void init() {
		super.setAdaptedService(service);
	}
	
	@Override
	public WithdrawResult withdrawFromRemoteWalletToAccount(UUID requestId, long userId, long sessionId, long licenseeId, Money amount) {
		IS_METHOD_CALLED.set(true);
		return super.withdrawFromRemoteWalletToAccount(requestId, userId, sessionId, licenseeId, amount);
	}
}
