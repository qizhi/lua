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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.wallet.adapter.BaseWalletServiceExtension;
import com.cubeia.backoffice.wallet.adapter.WalletServiceAdapter;

@Component("wallet.service.adaptedWalletService")
public class AdaptedWalletService extends BaseWalletServiceExtension {

	@Autowired(required=false)
	private WalletServiceAdapter adapter;
	
	@Resource(name = "wallet.service.walletService")
	private WalletService rootService;
	
	@PostConstruct
	protected void init() {
		if(adapter == null) {
			adapter = new RootService();
		}
		super.setAdaptedService(adapter);
	}
	
	private class RootService extends BaseWalletServiceExtension implements WalletServiceAdapter {
		
		public RootService() {
			super.setAdaptedService(rootService);
		}
	}
}
