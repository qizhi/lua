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

package com.cubeia.backoffice.populate;

import java.math.BigDecimal;

import com.cubeia.backoffice.users.api.dto.AuthenticationResponse;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.CreationStatus;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.users.client.UserServiceClientHTTP;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest.TransferType;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.wallet.client.WalletServiceClientHTTP;

/**
 * Populate services with Bots and accounts.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class DemoPopulator {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: DemoPopulator <UserCount> <UserURL> <WalletURL>\nExample: DemoPopulator 100 http://localhost:8080/user-service-rest/rest http://localhost:8080/wallet-service-rest/rest");
			System.exit(0);
		}
		
		long start = System.currentTimeMillis();
		
		int count = Integer.parseInt(args[0]);
		UserServiceClient users = new UserServiceClientHTTP(args[1]);
		WalletServiceClient wallet = new WalletServiceClientHTTP(args[2]);
		
		try {
			
			for (int i = 1; i <= count; i++) {
				
				// CREATE USER
				System.out.println("Create Bot_"+i);
				User bot = new User("Bot_"+i);
				bot.setOperatorId(0l);
				
				UserInformation info = new UserInformation();
				info.setBillingAddress("RobotStrasse "+i);
				info.setCity("Stockholm");
				info.setCurrency("EUR");
				info.setEmail("bot"+i+"@mailinator.com");
				
				bot.setUserInformation(info);
				
				CreateUserRequest createUserRequest = new CreateUserRequest();
				createUserRequest.setPassword(i+"");
				createUserRequest.setUser(bot);
				CreateUserResponse created = users.createUser(createUserRequest);
				if (created.getStatus() != CreationStatus.OK) {
					throw new RuntimeException("Failed to create user["+i+"] . Status: "+created.getStatus());
				}

				// VERIFY AUTHENTICATION FOR CREATED USER
				AuthenticationResponse authenticate = users.authenticate(0l, "Bot_"+i, i+"");
				if (!authenticate.getAuthenticated()) {
					throw new RuntimeException("Failed to authenticate user["+i+"]");
				}
				
				// CREATE ACCOUNT
				CreateAccountRequest createAccount = new CreateAccountRequest();
				createAccount.setCurrencyCode("EUR");
				createAccount.setType(AccountType.STATIC_ACCOUNT);
				createAccount.setUserId(created.getUser().getUserId());
				
				CreateAccountResult accountResult = wallet.createAccount(createAccount);
				System.out.println("Created Account. "+created.getUser().getUserName()+" -> Account: "+accountResult.getAccountId());
				
				// MAKE 1st DEPOSIT
				TransferRequest transfer = new TransferRequest();
				transfer.setAmount(new BigDecimal(1000));
				transfer.setComment("Demo populator transfer");
				transfer.setOperatorId(0l);
				transfer.setTransferType(TransferType.CREDIT);
				wallet.transfer(accountResult.getAccountId(), transfer);
				
				AccountBalanceResult balance = wallet.getAccountBalance(accountResult.getAccountId());
				if (balance.getBalance().getAmount().longValue() != 1000l) {
					throw new RuntimeException("Wrong account balance: "+balance+" (should be 1000)");
				}
				
				System.out.println("Inital transfer complete");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long elapsed = System.currentTimeMillis() - start;
		
		System.out.println("FINISHED IN "+elapsed+" ms");
		
	}
	
}
