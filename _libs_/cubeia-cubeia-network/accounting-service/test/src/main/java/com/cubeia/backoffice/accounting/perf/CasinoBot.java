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

package com.cubeia.backoffice.accounting.perf;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CasinoBot extends AbstractBot {
	
	private static final Random RAND = new Random();

	private Long accountId;
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private AccountingManager man;
	
	@Inject
	@Named("init-min-wait")
	private int initMinWait;

	@Inject
	@Named("init-max-wait")
	private int initMaxWait;
	
	@Inject
	@Named("min-deposit")
	private int minDeposit;

	@Inject
	@Named("max-deposit")
	private int maxDeposit;
	
	@Inject
	private Server parent;
	
	@Inject
	@Named("round-wait")
	private int roundWait;
	
	@Inject
	private Stats stats;
	
	@Override
	public void run() {
		doInitWait();
		checkAccount();
		while(true) {
			bet();
			if(RAND.nextInt(5) == 0) {
				win();
			}
			doWait();
			if(!haveMoney()) {
				deposit();
			}
		}
	}

	private void doWait() {
		log.debug(getAccountExternalId() + " waits " + roundWait + " millis");
		try {
			Thread.sleep(roundWait);
		} catch(InterruptedException e) {
			log.debug("Wait interrupted", e);
		}
	}

	private void win() {
		log.info(getAccountExternalId() + " wins 1");
		long systemAccId = parent.getSystemAccountId();
		String comm = getAccountExternalId() + " wins 1";
		long t = System.currentTimeMillis();
		man.createTransaction(comm, 
				null, 
				new BigDecimal("1"), 
				systemAccId, 
				accountId, 
				null);
		t = System.currentTimeMillis() - t;
		stats.reportWin(t);
	}

	private void bet() {
		log.debug(getAccountExternalId() + " betting");
		long systemAccId = parent.getSystemAccountId();
		String comm = getAccountExternalId() + " bets 1";
		long t = System.currentTimeMillis();
		man.createTransaction(comm, 
				null, 
				new BigDecimal("1"), 
				accountId, 
				systemAccId, 
				null);
		t = System.currentTimeMillis() - t;
		stats.reportBet(t);
	}

	private boolean haveMoney() {
		long t = System.currentTimeMillis();
		boolean b = man.getBalance(accountId).getAmount().intValue() != 0;
		t = System.currentTimeMillis() - t;
		stats.reportBalance(t);
		return b;
	}

	private void deposit() {
		int i = minDeposit + RAND.nextInt(maxDeposit - minDeposit);
		log.info(getAccountExternalId() + " depositing " + i + " SEK");
		long transferAccId = parent.getTransferAccountId();
		String comm = getAccountExternalId() + " deposits " + i;
		long t = System.currentTimeMillis();
		man.createTransaction(comm, 
				null, 
				new BigDecimal(String.valueOf(i)), 
				transferAccId, 
				accountId, 
				null);
		t = System.currentTimeMillis() - t;
		stats.reportDeposit(t);
	}
	

	// --- PRIVATE METHODS --- //
	
	private void doInitWait() {
		long w = initMinWait + RAND.nextInt(initMaxWait - initMinWait);
		log.debug(getAccountExternalId() + " waiting " + w + " millis");
		try {
			Thread.sleep(w);
		} catch(InterruptedException e) {
			log.debug("Wait interrupted", e);
		}
	}

	private void checkAccount() {
		long exitid = getAccountExternalId();
		Collection<Account> list = man.getAccountsByUserId(exitid);
		Account a = null;
		if(list == null || list.size() == 0) {
			log.debug(getAccountExternalId() + " creates account");
			a = man.createAccount(new Account(exitid, "SEK", 2));
			deposit();
		} else {
			a = list.iterator().next();
		}
		accountId = a.getId();
	}

	private int getAccountExternalId() {
		return getId();
	}
}
