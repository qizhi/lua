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

package com.cubeia.network.firebase.wallet.test;

import java.math.BigDecimal;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.game.server.service.wallet.CubeiaWalletService;

public class Runner {
	
	public static void main(String[] args) {
		System.out.println("Starting Firebase Wallet Service test");
		
		try {
			CubeiaWalletService wallet = new CubeiaWalletService();
			
			wallet.init("http://localhost:9091/wallet-service");
			wallet.start();
			
			// CREATE SESSION ACCOUNT
			Long sessionId = wallet.startSession("EUR", 1, 2, "3", 4, "tester", null);
			System.out.println("Started session account: "+sessionId);
			
			// CHECK BALANCE (Should be zero)
			AccountBalanceResult balance = wallet.getBalance(sessionId);
			System.out.println("Balance (0.00): "+balance.getBalance());
			
			// Fund the session account
			wallet.withdraw(new Money("EUR", 2, new BigDecimal(100)), 1, sessionId, null);
			
			// CHECK BALANCE (Should be 100.00)
			balance = wallet.getBalance(sessionId);
			System.out.println("Balance (100.00): "+balance.getBalance());
			
			wallet.deposit(new Money("EUR", 2, new BigDecimal(40)), 1, sessionId, null);
			
			// CHECK BALANCE (Should be 60.00)
			balance = wallet.getBalance(sessionId);
			System.out.println("Balance (60.00): "+balance.getBalance());
			
			// Close the session
			wallet.endSession(sessionId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
