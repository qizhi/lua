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
package com.cubeia.game.server.service.wallet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest.TransferType;
import com.cubeia.backoffice.wallet.client.WalletServiceClientHTTP;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.network.wallet.firebase.api.WalletServiceConfig;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import com.cubeia.network.wallet.firebase.domain.ResultEntry;
import com.cubeia.network.wallet.firebase.domain.RoundResultResponse;
import com.cubeia.network.wallet.firebase.domain.TransactionBuilder;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Implementation that talks to the Cubeia wallet.
 *
 * @author w
 */
public class CubeiaWalletService implements Service, WalletServiceContract {

	private final Logger log = Logger.getLogger(this.getClass());

	private WalletServiceClientHTTP walletClient;

	private String baseUrl;
	
	
	private LoadingCache<String, Currency> currencyCache = CacheBuilder.newBuilder()
			.maximumSize(10)
			.expireAfterWrite(60, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Currency>() {
			     public Currency load(String key) {
			         return getCurrencyFromWallet(key);
			       }
			     });

	@Override
	public void destroy() {
	}

	/**
	 * Can be used for programmatic initialization
	 */
	public void init(String baseUrl) throws SystemException {
		this.baseUrl = baseUrl;
	}

	/**
	 * Called by Firebase when the service is loaded.
	 * We will check for the  System property 'com.cubeia.network.walletservice.base-url',
	 * if no system property found then we will check Firebase configuration
	 * Last plan is the default fallback URL
	 */
	@Override
	public void init(ServiceContext con) throws SystemException {
		try {

			// Check System property
			baseUrl = System.getProperty("com.cubeia.network.walletservice.base-url");

			if (baseUrl == null) {
				// Check Firebase configuration
				ClusterConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
				WalletServiceConfig configuration = contr.getConfiguration(WalletServiceConfig.class, new Namespace(WalletServiceConfig.NAMESPACE));
				baseUrl = configuration.getBaseUrl();
			}
			log.info("Cubeia Wallet Service will connect to wallet service at this URL: " + baseUrl);

			if (baseUrl == null || !baseUrl.startsWith("http:")) {
				log.warn("The wallet service URL does not contain a full URL pattern, e.g. 'http://userservice:8080/wallet-service'");
			}


		} catch (ConfigurationException e) {
			log.error("Failed to read user service configuration. Will fall back on default value", e);
		}
	}

	@Override
	public void start() {
		ClassLoader originalClassLoader = switchClassLoader();
		walletClient = new WalletServiceClientHTTP(baseUrl);
		restoreClassLoader(originalClassLoader);
	}

	@Override
	public void stop() {
	}


	@Override
	public Long startSession(String currencyCode, int licenseeId, int userId, String objectId, int gameId, String userName, String accountName) {
		ClassLoader originalClassLoader = switchClassLoader();
		try {
			MetaInformation meta = new MetaInformation();
			meta.setGameId((long) gameId);
			meta.setUserName(userName);
			meta.setObjectId(String.valueOf(objectId));
			meta.setName(accountName);

			CreateAccountRequest request = new CreateAccountRequest();
			request.setRequestId(UUID.randomUUID());
			request.setCurrencyCode(currencyCode);
			request.setType(AccountType.SESSION_ACCOUNT);
			request.setUserId((long) userId);
			request.setInformation(meta);
			request.setNegativeBalanceAllowed(false);

			log.debug("Start wallet session for user[" + userId + ":" + userName + "]");
			CreateAccountResult response = walletClient.createAccount(request);

			return response.getAccountId();
		} finally {
			restoreClassLoader(originalClassLoader);
		}
	}

	@Override
	public AccountBalanceResult getBalance(long sessionId) {
		ClassLoader originalClassLoader = switchClassLoader();
		try {
			try {
				return walletClient.getAccountBalance(sessionId);
			} catch (AccountNotFoundException e) {
				throw new RuntimeException("Failed to get balance for session account with id: " + sessionId);
			}
		} finally {
			restoreClassLoader(originalClassLoader);
		}
	}

	@Override
	public void withdraw(Money amount, int licenseeId, long sessionId, String comment) {
		doTransfer(amount, licenseeId, sessionId, TransferType.CREDIT, comment);
	}


	@Override
	public void deposit(Money amount, int licenseeId, long sessionId, String comment) {
		doTransfer(amount, licenseeId, sessionId, TransferType.DEBIT, comment);
	}

	@Override
	public Money endSessionAndDepositAll(int licenseeId, long sessionId, String comment) {
		Money balance = getBalance(sessionId).getBalance();
		deposit(balance, licenseeId, sessionId, comment);
		endSession(sessionId);
		return balance;
	}

	private void doTransfer(Money amount, int licenseeId, long sessionId, TransferType type, String comment) {
		ClassLoader originalClassLoader = switchClassLoader();
		try {
			TransferRequest transfer = new TransferRequest();
			transfer.setTransferType(type);
			transfer.setAmount(amount.getAmount());
			transfer.setOperatorId(new Long(licenseeId));
			transfer.setRequestId(UUID.randomUUID());
			transfer.setComment(comment);
            walletClient.transfer(sessionId, transfer);
		} finally {
			restoreClassLoader(originalClassLoader);
		}
	}

	@Override
	public TransactionResult doTransaction(TransactionRequest txReq) {
		return walletClient.doTransaction(txReq);
	}

	@Override
	public void closeOpenSessionAccounts(Set<String> excludedAccountNames) {
		ListAccountsRequest listAccountsRequest = new ListAccountsRequest();
		listAccountsRequest.setTypes(Collections.singletonList(AccountType.SESSION_ACCOUNT));
		listAccountsRequest.setStatus(Account.AccountStatus.OPEN);

		AccountQueryResult accountQueryResult = walletClient.listAccounts(listAccountsRequest);

		List<Account> accounts = accountQueryResult.getAccounts();
		for (Account account : accounts) {
			if (excludedAccountNames != null && excludedAccountNames.contains(account.getInformation().getName())) {
				continue;
			}
			log.debug("Closing account " + account.getId());
			endSessionAndDepositAll(0, account.getId(),"Closing open session on startup");
		}
	}


	@Override
	public void endSession(long sessionId) {
		ClassLoader originalClassLoader = switchClassLoader();
		try {
			walletClient.closeAccount(sessionId);
		} catch (AccountNotFoundException e) {
			throw new RuntimeException("Failed to close session", e);
		} finally {
			restoreClassLoader(originalClassLoader);
		}
	}

	@Override
	public RoundResultResponse roundResult(long type, long contextId, long subContextId, Collection<ResultEntry> results, String description) {
		String info = "Report round " + results.size() + " results\n";
		for (ResultEntry entry : results) {
			info += "    " + entry.getSessionId() + " : " + entry.getAmount() + "\n";
		}
		info += "------------------------------------";
		log.debug(info);

		ClassLoader originalClassLoader = switchClassLoader();
		try {

			ResultEntry firstResult = results.iterator().next();
			String currencyCode = firstResult.getCurrencyCode();
			int fractionalDigits = firstResult.getFractionalDigits();

			TransactionBuilder tb = new TransactionBuilder(currencyCode, fractionalDigits);
			tb.comment(description);
			tb.attribute("type", "" + type).attribute("contextId", "" + contextId).attribute("subContextId", "" + subContextId);

			for (ResultEntry re : results) {
				tb.entry(re.getSessionId(), re.getAmount());
			}

			TransactionResult res = walletClient.doTransaction(tb.toTransactionRequest());

			RoundResultResponse response = new RoundResultResponse();

			response.setBalances(res.getBalances());
			return response;

		} catch (Exception e) {
			log.error("Failed to do transaction to remote wallet type["+type+"] contextId["+contextId+"] subContextId["+subContextId+"] entries: "+results);
			throw new RuntimeException("Failed to do transaction to remote wallet type["+type+"] contextId["+contextId+"] subContextId["+subContextId+"]", e);
		} finally {
			restoreClassLoader(originalClassLoader);
		}
	}

	@Override
	public Account getAccountById(long accountId) {
		return walletClient.getAccountById(accountId);
	}

	@Override
	public AccountQueryResult listAccounts(ListAccountsRequest lar) {
		return walletClient.listAccounts(lar);
	}

	protected static ClassLoader switchClassLoader() {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(CubeiaWalletService.class.getClassLoader());
		return originalClassLoader;
	}

	protected static void restoreClassLoader(ClassLoader originalClassLoader) {
		Thread.currentThread().setContextClassLoader(originalClassLoader);
	}

	@Override
	public Currency getCurrency(String currencyCode) {
		try {
			return currencyCache.get(currencyCode);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private Currency getCurrencyFromWallet(String currencyCode) {
		return walletClient.getCurrency(currencyCode);
	}

}
