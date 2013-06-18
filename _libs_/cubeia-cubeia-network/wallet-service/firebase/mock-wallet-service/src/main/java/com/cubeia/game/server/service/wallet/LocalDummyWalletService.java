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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import com.cubeia.network.wallet.firebase.domain.DepositResponse;
import com.cubeia.network.wallet.firebase.domain.ResultEntry;
import com.cubeia.network.wallet.firebase.domain.RoundResultResponse;


/**
 * Dummy Firebase wallet service that will keep track of sessions and balances. New session will have 
 * an initial balance set to 0.
 * @author w
 */
public class LocalDummyWalletService implements Service, WalletServiceContract {
	private static final int FRACTIONS = 2;

	public static final BigDecimal INITIAL_BALANCE = BigDecimal.ZERO;
	
	private static Logger log = Logger.getLogger(LocalDummyWalletService.class);

	private Map<Long, AccountContainer> accountMap = new HashMap<Long, AccountContainer>();
	private static volatile int idSequence = 0;
	
    @Override
	public void destroy() {
	}

    @Override
	public void init(ServiceContext con) throws SystemException {
        log.info("Wallet Service Mock initialized");
	}

    @Override
	public void start() {
	}

    @Override
	public void stop() {
	}

	@Override
	public synchronized void deposit(Money amount, int licenseeId, long sessionId, String comment) {
		log.debug("deposit: amount = " + amount + ", licenseeId = " + licenseeId + ", sessionId = " + sessionId);
		DepositResponse response = new DepositResponse();
		if (!accountMap.containsKey(sessionId)) {
			throw new RuntimeException("error depositing, session not found: " + ToStringBuilder.reflectionToString(response));
		} 
		
		AccountContainer account = accountMap.get(sessionId);
		
        if (amount.getAmount().intValue() > account.getBalance().getAmount().intValue()) {
			throw new RuntimeException("deposit denied, insufficient funds, sessionId = " + sessionId);
		}
		
        AccountContainer newBalance = account.credit(amount.negate());
		accountMap.put(sessionId, newBalance);
		
		log.debug("balance updated, sessionId = " + sessionId + ", balance = " + newBalance.getBalance());
		response.setTxId(UUID.randomUUID());
	}

    @Override
	public synchronized void endSession(long sessionId) {
		log.debug("end session: sessionId = " + sessionId);
		if (!accountMap.containsKey(sessionId)) {
			throw new RuntimeException("error ending session, session not found: " + sessionId);
		}
		log.debug("session closed with balance = " + accountMap.get(sessionId));
		accountMap.remove(sessionId);
		log.debug("active sessions: " + accountMap);
	}
    
    @Override
    public Money endSessionAndDepositAll(int licenseeId, long sessionId, String comment) {
        Money balance = getBalance(sessionId).getBalance();
        deposit(balance, licenseeId, sessionId, comment);
        endSession(sessionId);
        return balance;
    }

	@Override
	public synchronized AccountBalanceResult getBalance(long sessionId) {
		log.debug("get balance: sessionId = " + sessionId);
		
		AccountBalanceResult response = new AccountBalanceResult();
		if (!accountMap.containsKey(sessionId)) {
			throw new RuntimeException("error getting balance, session not found: " + ToStringBuilder.reflectionToString(response));
		}
		AccountContainer account = accountMap.get(sessionId);
		
        response.setBalance(account.getBalance());
		return response;
	}

	@Override
	public synchronized Long startSession(String currencyCode, int licenseeId, int userId, String tableId, int gameId, String userName, String accountName) {
		long sessionId = nextId();
		
		Account acnt = new Account();
		acnt.setId(sessionId);
		acnt.setCreated(new Date());
		acnt.setCurrencyCode(currencyCode);
		acnt.setType(AccountType.SESSION_ACCOUNT);
		acnt.setStatus(AccountStatus.OPEN);
		
		accountMap.put(sessionId, new AccountContainer(acnt, new Money(currencyCode, FRACTIONS, INITIAL_BALANCE)));
		log.debug("start session: sessionId = " + sessionId + ", balance = " + INITIAL_BALANCE + " " + currencyCode);
		log.debug("active sessions: " + accountMap);
		return sessionId;
	}

	@Override
	public synchronized void withdraw(Money amount, int licenseeId, long sessionId, String comment) {
		log.debug("withdraw: amount = " + amount + ", licenseeId = " + licenseeId + ", sessionId = " + sessionId);
		if (!accountMap.containsKey(sessionId)) {
			throw new RuntimeException("error withdrawing, session not found");
		}
		
        AccountContainer newAccount = accountMap.get(sessionId).credit(amount);
		accountMap.put(sessionId, newAccount);
		
		log.debug("balance updated, sessionId = " + sessionId + ", balance = " + newAccount);
	}
	
	@Override
	public synchronized RoundResultResponse roundResult(long type, long contextId, long subContextId, Collection<ResultEntry> results, String description) {
		log.debug("round result: type = " + type + ", contextId = " + contextId + ", subContextId = " + subContextId + 
		    ", desc = '" + description + "'");
		
		RoundResultResponse response = new RoundResultResponse();
		Collection<AccountBalanceResult> balances = new ArrayList<AccountBalanceResult>();
		
		for (ResultEntry re : results) {
			long sessionId = re.getSessionId();
			log.debug("round result entry: sessionId = " + sessionId + ", amount = " + re.getAmount());
			
			AccountContainer acnt = accountMap.get(sessionId);
			if (acnt == null) {
				throw new RuntimeException("error handling round report, session not found: " + ToStringBuilder.reflectionToString(response));
			}

			acnt = acnt.credit(new Money(re.getCurrencyCode(), FRACTIONS, re.getAmount()));
			
			if (acnt.getBalance().getAmount().signum() < 0) {
				throw new RuntimeException("error handling round report, insufficient funds, sessionId = " + sessionId);
			}
			accountMap.put(sessionId, acnt);
			log.debug("balance updated, sessionId = " + sessionId + ", account = " + acnt);
			
			AccountBalanceResult accountBalance = new AccountBalanceResult(sessionId, acnt.getBalance());
			balances.add(accountBalance);
		}
		
		response.setBalances(balances);
		
		return response;
	}
	
    @Override
    public TransactionResult doTransaction(TransactionRequest txRequest) {
        log.debug("do transaction: " + txRequest);
        
        TransactionResult response = new TransactionResult();
        response.setTransactionId(-1L);
        Collection<AccountBalanceResult> balances = new ArrayList<AccountBalanceResult>();
        
        for (TransactionEntry te : txRequest.getEntries()) {
            long accountId = te.getAccountId();
            log.debug("tx entry: accountId = " + accountId + ", amount = " + te.getAmount());
            
            AccountContainer acnt = accountMap.get(accountId);
            if (acnt == null) {
                throw new RuntimeException("error handling round report, session not found: " + ToStringBuilder.reflectionToString(response));
            }

            acnt = acnt.credit(te.getAmount());
            
            if (acnt.getBalance().getAmount().signum() < 0) {
                throw new RuntimeException("error handling tx request, insufficient funds, sessionId = " + accountId);
            }
            accountMap.put(accountId, acnt);
            log.debug("balance updated, sessionId = " + accountId + ", balance = " + acnt);
            
            AccountBalanceResult accountBalance = new AccountBalanceResult(accountId, acnt.getBalance());
            balances.add(accountBalance);
        }
        
        response.setBalances(balances);
        
        return response;
    }

	@Override
	public void closeOpenSessionAccounts(Set<String> excludedAccountNames) {
		log.info("Close open session accounts called. Doing nothing.");
	}

    @Override
    public synchronized Account getAccountById(long accountId) {
        AccountContainer acnt = accountMap.get(accountId);
        return acnt == null ? null : acnt.getAccount();
    }
    
    @Override
    public synchronized AccountQueryResult listAccounts(ListAccountsRequest lar) {
        log.warn("listing accounts is not fully supported in dummy implementation: no sorting, no limiting");

        List<Account> accounts = new ArrayList<Account>();
        for (AccountContainer ac : accountMap.values()) {
            Account a = ac.getAccount();
            
            boolean accountIdMatch = lar.getAccountId() == null  ||  lar.getAccountId().equals(a.getId());
            boolean userIdMatch = lar.getUserId() == null  ||  lar.getUserId().equals(a.getUserId());
            boolean statusMatch = lar.getStatuses() == null  ||  lar.getStatuses().contains(a.getStatus());
            boolean typesMatch = lar.getTypes() == null  ||  lar.getTypes().contains(a.getType());
            
            if (accountIdMatch  &&  userIdMatch  &&  statusMatch  &&  typesMatch) {
                accounts.add(a);
            }
        }
        
        return new AccountQueryResult(lar.getOffset(), lar.getLimit(), accounts.size(), accounts, lar.getSortOrder(), lar.isAscending());
    }
    
	private static long nextId() {
		return idSequence++;
	}
	
	private static final class AccountContainer implements Serializable {
        private static final long serialVersionUID = 1L;
	    
        private final Account account;
	    private final Money balance;
	    
        private AccountContainer(Account account, Money balance) {
            this.account = account;
            this.balance = balance;
        }
        
        Money getBalance() {
            return balance;
        }
        
        Account getAccount() {
            return account;
        }
        
        AccountContainer credit(Money amount) {
            Money newBalance = balance.add(amount);
            return new AccountContainer(account, newBalance);
        }
        
	}

	@Override
	public Currency getCurrency(String currencyCode) {
		return new Currency(currencyCode, FRACTIONS);
	}

}
