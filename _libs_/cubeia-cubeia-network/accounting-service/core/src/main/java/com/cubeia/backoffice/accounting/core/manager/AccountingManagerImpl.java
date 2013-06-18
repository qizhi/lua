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

package com.cubeia.backoffice.accounting.core.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.accounting.api.NoSuchAccountException;
import com.cubeia.backoffice.accounting.core.AccountClosedException;
import com.cubeia.backoffice.accounting.core.AccountNotFoundException;
import com.cubeia.backoffice.accounting.core.TransactionNotBalancedException;
import com.cubeia.backoffice.accounting.core.TransactionNotFoundException;
import com.cubeia.backoffice.accounting.core.dao.AccountingDAO;
import com.cubeia.backoffice.accounting.core.dao.EntrySumAndCount;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.domain.TransactionsOrder;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountAttribute;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.BalanceCheckpoint;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.integrations.PluginManager;
import com.cubeia.backoffice.accounting.core.util.ExecTimeCollector;

/*
 *  NOTE: This component is transactional via the spring
 *  config, using AOP advises
 */
@Component("accounting.accountingManager")
public class AccountingManagerImpl implements AccountingManager {
	
    // public static final long CHECKPOINT_FLUSH_DELAY = 2000;
    private static final Object DUMMY_PAYLOAD = new Object();
    
    
    // --- INSTANCE MEMBERS --- // 
	
	/*
	 * Set to true to allow transactions involving closed accounts
	 */
	private AtomicBoolean allowClosedAccounts = new AtomicBoolean(false);

	/*
	 * Default checkpoint interval (per account).
	 */
    private int balanceCheckpointInterval = 20;
    
    /*
     * Your standard logger...
     */
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /*
     * Set to true to assume an external thread calls 
     * checkAndCreateCheckpoints() regularly.
     */
    private AtomicBoolean asyncCheckpointCreation = new AtomicBoolean(true);
    
    /*
     * This map contains account ID's mapped to dummy objects for accounts that are known
     * to have more than the limit of check point intervals. The dirty state is checked on 
     * "get balance" only.
     */
    private Map<Long, Object> dirtyAccountSet = new ConcurrentHashMap<Long, Object>();
    
    /*
     * Used by scheduled thread to mark failed checkpoints
     */
    private Set<Long> errorSet = Collections.synchronizedSet(new HashSet<Long>());
    
    private final AtomicReference<ExecTimeCollector> checkPointTime = new AtomicReference<ExecTimeCollector>();
    
    
    
    // --- DAOS -- //
    
    @Resource(name = "accounting.accountingDAO")
    private AccountingDAO accountingDAO;
    
    @Resource(name = "accounting.pluginManager")
    private PluginManager pluginManager;
    
    
    // --- PUBLIC METHODS --- //
    
    @Override
    public Transaction getTransactionByExternalId(String extId) {
    	return accountingDAO.getTransactionByExternalId(extId);
    }
    
    @Override
    public Map<Long, Account> getAccounts(Set<Long> ids) {
    	Map<Long, Account> map = new HashMap<Long, Account>(ids.size());
    	for (Long id : ids) {
    		Account a = accountingDAO.getAccount(id);
    		if(a == null) {
    			throw new NoSuchAccountException(id);
    		}
    		map.put(id, a);
    	}
    	return map;
    }
    
    @Override
    public BalanceCheckpoint getLastCheckpoint(Long accountId) {
    	return accountingDAO.getLatestBalanceCheckpoint(accountId, null);
    }
    
    @Override
    public void setAllowTransactionsWithClosedAccounts(boolean allow) {
    	log.info("Allow transactions with closed accounts set to: " + allow);
    	allowClosedAccounts.set(allow);
    }
    
    @Override
    public boolean getAllowTransactionsWithClosedAccounts() {
    	return allowClosedAccounts.get();
    }
    
    @Override
    public void setCheckpointExecTimeCollector(ExecTimeCollector col) {
    	checkPointTime.set(col);
    }
    
    @Override
    public void checkAndCreateCheckpoints() {
    	// dirtyAccountSet.keySet()HashSet<Long> accountIds = new HashSet<Long>(dirtyAccountSet.keySet());
    	long time = System.currentTimeMillis();
    	// int size = dirtyAccountSet.keySet().size();
    	// int count = 0;
    	for (Long accountId : dirtyAccountSet.keySet()) {
        	/*
        	 * Roll through all dirty accounts, create checkpoint if the entry count
        	 * is larger than the configured interval, remove from dirty set and error
        	 * set.
        	 */
        	try {
        		createCheckpointIfNeeded(accountId);
        		dirtyAccountSet.remove(accountId);
        		errorSet.remove(accountId);
        		// count++;
        	} catch(Throwable th) {
        		if(errorSet.contains(accountId)) {
        			log.error("Failed to set consecutive checkpoint for account " + accountId, th);
        		} else {
        			log.warn("Failed to set first checkpoint for account " + accountId + ", will retry", th);
        			errorSet.add(accountId);
        		}
        	}
        }
        /*
         * Report timing if needed.
         */
    	long t = System.currentTimeMillis() - time;
        ExecTimeCollector col = checkPointTime.get();
        // log.debug("Scheduled checkpoint creation created " + count + " checkpoints (size before exec: " + size + ") in " + t + " millis");
        if(col != null) {
        	col.report(t);
        } 
    }
    
    @Override
    public void setTransactionExternalId(Long transactionId, String externalId) throws SecurityException {
    	Transaction trans = getTransactionById(transactionId);
    	if(trans == null) throw new IllegalArgumentException("No such transaction: " + transactionId);
    	if(trans.getExternalId() != null) {
    		throw new SecurityException("Transaction " + transactionId + " already have an extranal id, it is illegal to set this value twice.");
    	} else {
    		trans.setExternalId(externalId);
    		accountingDAO.saveOrUpdate(trans);
    	}
    }
    
    @Override
    public void updateAccount(Account account) {
    	accountingDAO.merge(account);
    }
    
    @Override
    public void removeAccountAttribute(Long accountId, String key) {
    	Account a = accountingDAO.getAccount(accountId);
    	checkThrowNotExists(a, accountId);
    	a.getAttributes().remove(key);
    }
    
    @Override
    public void removeTransactionAttribute(Long transactionId, String key) throws TransactionNotFoundException {
    	Transaction t = getTransactionById(transactionId);
    	checkThrowNotExists(t, transactionId);
    	t.getAttributes().remove(key);
    }
    
    @Override
    public void setAccountAttribute(Long accountId, String key, String value) {
    	Account a = accountingDAO.getAccount(accountId);
    	checkThrowNotExists(a, accountId);
    	AccountAttribute att = new AccountAttribute(a, key, value);
    	a.getAttributes().put(key, att);
    }
    
    @Override
    public void setTransactionAttribute(Long transactionId, String key, String value) throws TransactionNotFoundException {
    	setTransactionAttributes(transactionId, Collections.singletonMap(key, value));
    }
    
    @Override
    public void setTransactionAttributes(Long transactionId, Map<String, String> values) throws TransactionNotFoundException {
    	Transaction t = getTransactionById(transactionId);
    	checkThrowNotExists(t, transactionId);
    	t.setStringAttributes(values);
    }

	@Override
    public void setAccountStatus(Long accountId, AccountStatus status) {
    	Account a = accountingDAO.getAccount(accountId);
    	checkThrowNotExists(a, accountId);
    	a.setStatus(status);
    }
    
    @Override
    public Account createAccount(Account account) {
    	checkRequiredFields(account);
    	pluginManager.beforeCreate(account);
    	accountingDAO.saveOrUpdate(account);
    	pluginManager.afterCreate(account);
    	return account;
    }
    
    @Override
    public Account createAccountWithInitialBalance(Account account, BigDecimal balance) {
    	checkRequiredFields(account);
    	pluginManager.beforeCreate(account);
    	accountingDAO.saveOrUpdate(account);
    	accountingDAO.saveOrUpdate(new Entry(account, balance));
    	pluginManager.afterCreate(account);
    	return account;
    }

	@Override
    public Account getAccount(Long accountId) {
    	return accountingDAO.getAccount(accountId);
    }
    
    @Override
    public Collection<Account> getAccountsByUserIdAndCurrency(Long userId, String currency) {
    	return accountingDAO.findAccountsByUserId(userId, null, currency);
    }
    
    @Override
    public Collection<Account> getAccountsByUserAndWalletId(Long userId, Long walletId) {
    	return accountingDAO.findAccountsByUserAndWalletId(userId, walletId);
    }
    
    @Override
    public Collection<Account> getAccountsByUserIdWalletTypeAndCurrency(Long userId, Long walletId, String type, String currency) {
    	return accountingDAO.findAccountsByUserAndWalletId(userId, walletId, type, currency);
    }
    
    @Override
    public Collection<Account> getAccountsByTypeAndCurrency(String type, String currency) {
    	return accountingDAO.findAccountsByTypeAndCurrency(type, currency);
    }

	@Override
	public Collection<Account> getAccountsByUserId(Long userId) {
		return accountingDAO.findAccountsByUserId(userId);
	}
	
	@Override
	public Money getBalance(Long accountId) {
		Account account = getAccount(accountId);
        checkThrowNotExists(account, accountId);
        
        // get latest balance checkpoint
        BalanceCheckpoint balanceCP = accountingDAO.getLatestBalanceCheckpoint(accountId, null);
        BigDecimal balance = calculateBalanceFromCheckpointToEntry(accountId, (long) Integer.MAX_VALUE, balanceCP);
        
        return new Money(account.getCurrencyCode(), account.getFractionalDigits(), balance);
	}
    
    @Override
    public Money getBalanceAfterEntry(Long accountId, Long entryId) {
        Account account = getAccount(accountId);
        checkThrowNotExists(account, accountId);
        
        // get previous balance checkpoint
        BalanceCheckpoint previousCP = accountingDAO.getLatestBalanceCheckpoint(accountId, entryId);
        BigDecimal balance = calculateBalanceFromCheckpointToEntry(accountId, entryId, previousCP);
        
        return new Money(account.getCurrencyCode(), account.getFractionalDigits(), balance);
    }
    
    @Override
    public Transaction createTransaction(
					        String comment,
					        String externalId,
					        BigDecimal amount, 
					        Long fromAccountId, 
					        Long toAccountId,
					        Map<String, String> atts) {
        
        Account fromAccount = getAccount(fromAccountId);
        Account toAccount = getAccount(toAccountId);
        
        if (toAccount == null) { 
        	throw new NoSuchAccountException(toAccountId);
        } if (fromAccount == null) {
        	throw new NoSuchAccountException(fromAccountId);
        }
        
        Entry fromEntry = new Entry(fromAccount, amount.negate());
        Entry toEntry = new Entry(toAccount, amount);
        
        return createTransactionInternal(comment, externalId, Arrays.asList(fromEntry, toEntry), atts);
    }
    
    @Override
    public Transaction createTransaction(String comment, String extId, List<Entry> entries, Map<String, String> attributes)
    		throws AccountNotFoundException, AccountClosedException, TransactionNotBalancedException {
    	
    	return createTransactionInternal(comment, extId, entries, attributes);
    }

    @Override
    public QueryResultsContainer<Account> listAccounts(Long accountId, Long userId, Long walletId, String currencyCode, Set<AccountStatus> statuses, Set<String> types, int offset, int limit, AccountsOrder order, boolean ascending) {
        List<Account> accounts = accountingDAO.listAccounts(accountId, userId, walletId, currencyCode, statuses, types, offset, limit, order, ascending);
        if(log.isTraceEnabled()) {
        	log.trace("List accounts from DAOs: "+accounts);
        }
        int size = (int) accountingDAO.countAccounts(accountId, userId, walletId, currencyCode, statuses, types);
        log.debug("List accounts Size from DAOs: "+size);
        return new QueryResultsContainer<Account>(size, accounts);
    }
    
    @Override
    public Transaction reverseTransaction(Long txId, String newExtId, Map<String, String> oldAttr, Map<String, String> newAttr) throws TransactionNotFoundException {
    	Transaction trans = accountingDAO.getTransaction(txId);
    	if(trans == null) {
    		// transaction must exist
    		throw new TransactionNotFoundException(txId);
    	}
    	
    	// reverse and set external id
    	Transaction reverser = trans.reverse();
    	if(newExtId != null) {
    		reverser.setExternalId(newExtId);
    	}
    	
    	// set new attributes on reversed transaction
    	if(oldAttr != null) {
	    	for (String key : oldAttr.keySet()) {
	    		if(trans.getAttributes().containsKey(key)) {
	    			throw new SecurityException("Transaction " + txId + " already has attribute by name " + key);
	    		} else {
	    			trans.setAttribute(key, oldAttr.get(key));
	    		}
	    	}
    	}
    	
    	// set new attributes on reverser
    	if(newAttr != null) {
	    	for (String key : newAttr.keySet()) {
	    		if(reverser.getAttributes().containsKey(key)) {
	    			throw new SecurityException("Transaction " + txId + " already has attribute by name " + key);
	    		} else {
	    			reverser.setAttribute(key, newAttr.get(key));
	    		}
	    	}
    	}
    	
    	// save new entries
    	for (Entry e : reverser.getEntries()) {
    		accountingDAO.saveOrUpdate(e);
    	}
    	
    	// save transactions
    	accountingDAO.saveOrUpdate(trans);
    	accountingDAO.saveOrUpdate(reverser);
    	
    	pluginManager.afterCreate(reverser);
    	
    	return reverser;
    }

    @Override
    public QueryResultsContainer<Transaction> listTransactions(
									    		TransactionParticipant part1, 
									    		TransactionParticipant part2,
									    		Date startDate, Date endDate, 
									    		int offset, int limit, TransactionsOrder order, boolean ascending) {
        
        List<Transaction> transactions = accountingDAO.listTransactions(
            part1, 
            part2, 
            startDate, endDate, 
            offset, limit, 
            order, ascending);
        
        int size = accountingDAO.countTransactions(part1, part2, startDate, endDate);
            
        return new QueryResultsContainer<Transaction>(size, transactions);
    }

    @Override
    public QueryResultsContainer<Entry> listEntries(Long accountId, int offset,  int limit, boolean ascending) {
        List<Entry> entries = accountingDAO.listEntries(accountId, offset, limit, ascending);
        int size = (int) accountingDAO.countEntries(accountId);
        return new QueryResultsContainer<Entry>(size, entries);
    }
    
    @Override
    public QueryResultsContainer<BalancedEntry> listEntriesBalanced(Long accountId, int offset, int limit, boolean ascending) {
        List<Entry> entries = accountingDAO.listEntries(accountId, offset, limit, ascending);
        int size = (int) accountingDAO.countEntries(accountId);
        List<BalancedEntry> tmp = new ArrayList<BalancedEntry>(entries.size());
        if(entries.size() > 0 && accountId != null) {
        	// Account a = getAccount(accountId);
        	Entry one = entries.get(0);
        	BigDecimal balance = getBalanceAfterEntry(accountId, one.getId()).getAmount();
        	if (ascending) {
                balance = balance.subtract(one.getAmount());
            }
        	for (Entry e : entries) {
                if (ascending) {
                    balance = balance.add(e.getAmount());
                }
                tmp.add(new BalancedEntry(e, balance));   
                if (!ascending) {
                    balance = balance.subtract(e.getAmount());
                }
        	}
        }
        return new QueryResultsContainer<BalancedEntry>(size, tmp);
    }
    
    /**
     * Set the number of entries before a balance checkpoint is made.
     * @param numberOfEntriesBeforeCheckpoint the checkpoint interval
     */
    public synchronized void setBalanceCheckpointInterval(int balanceCheckpointInterval) {
        this.balanceCheckpointInterval = balanceCheckpointInterval;
    }

    public synchronized int getBalanceCheckpointInterval() {
        return balanceCheckpointInterval;
    }
    
    public void setAsyncCheckpointCreation(boolean async) {
        log.info("setting async checkpoint creation to: {}", async);
		asyncCheckpointCreation.set(async);
	}
    
    @Override
    public boolean getAsyncCheckpointCreation() {
    	return asyncCheckpointCreation.get();
    }
    
    public boolean isAsyncCheckpointCreation() {
		return asyncCheckpointCreation.get();
	}

    @Override
    public Transaction getTransactionById(Long txId) {
        return accountingDAO.getTransaction(txId);
    }

    @Override
    public CurrencyRate addCurrencyRate(CurrencyRate rate) {
        accountingDAO.saveOrUpdate(rate);
        return rate;
    }
    
    @Override
    public List<CurrencyRate> listRatesForCurrency(String currencyCode, Date date, int maxAgeDays) {
    	
    	// calculate dates
    	DateMidnight dateMidnight = new DateTime(date).toDateMidnight();
        DateMidnight end = dateMidnight.plusDays(1);
        DateMidnight start = dateMidnight.minusDays(maxAgeDays);
        
        // list rates
        List<CurrencyRate> rates = accountingDAO.listCurrencyRates(currencyCode, start.toDate(), end.toDate());
    	
        // remove duplicates
        Map<String, CurrencyRate> map = new HashMap<String, CurrencyRate>();
        for (CurrencyRate rate : rates) {
        	String key = rate.getSourceCurrencyCode() + ":" + rate.getTargetCurrencyCode();
        	map.put(key, rate);
        }
        
        // re-sort results
        List<CurrencyRate> answer = new ArrayList<CurrencyRate>(map.values());
        Collections.sort(rates, new Comparator<CurrencyRate>() {
        	@Override
        	public int compare(CurrencyRate o1, CurrencyRate o2) {
        		int i = o1.getTimestamp().compareTo(o2.getTimestamp());
        		if(i != 0) {
        			return i;
        		} else {
        			return o1.getId().compareTo(o2.getId());
        		}
        	}
		});
        
        return answer;
    }

    @Override
    public CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays) {
    	return getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, date, fallbackDays, true);
    }
    
    @Override
    public CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, String baslineCurrencyCode, Date date, int fallbackDays) {
    	CurrencyRate rate = getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, date, fallbackDays, false);
    	if(rate != null) {
    		// found a first level match, do return
    		return rate;
    	}
    	// Now look for an exact chain with baseline in the middle
    	CurrencyRate r1 = getCurrencyRate(sourceCurrencyCode, baslineCurrencyCode, date, fallbackDays, false);
    	CurrencyRate r2 = getCurrencyRate(baslineCurrencyCode, targetCurrencyCode, date, fallbackDays, false);
    	if(r1 == null || r2 == null) {
    		// no direct chain, return null
    		return null;
    	}
    	// now combine the rates
    	rate = r1.combine(r2);
    	return rate;
    }
    
    
    // --- PRIVATE METHODS --- //
    
    private void checkThrowNotExists(Account a, Long accountId) {
		if(a == null) {
			throw new AccountNotFoundException(accountId);
		}
	}
    
    private Transaction createTransactionInternal(String comment, String extId, List<Entry> entries, Map<String, String> atts) {
    	
    	// check that we indeed have the accounts
    	ensureAccountsNotNull(entries);
        
    	// create transaction (will check balance in constructor) and set attributes
    	Transaction tx = new Transaction(comment, entries.toArray(new Entry[entries.size()]));
        if(atts != null) {
        	tx.setStringAttributes(atts);
        }
        
        // set external id
        tx.setExternalId(extId);

        // save
        accountingDAO.saveOrUpdate(tx);

        // loop entries
        for (Entry entry : entries) {
            if(entry.getAccount() == null) throw new IllegalArgumentException("Entry " + entry.getId() + " does not contain an account");
            // check account status
            ensureAccountNotClosedAndExists(entry.getAccount());
            entry.setTransaction(tx);
            accountingDAO.saveOrUpdate(entry);
        }
        
        // check negative balances
        checkNegativeAccountBalances(entries);
        
        pluginManager.afterCreate(tx);

        return tx;
    }
    
    private BigDecimal calculateBalanceFromCheckpointToEntry(Long accountId, Long entryId, BalanceCheckpoint previousCP) {
        Long checkpointEntryId = -1l;
        BigDecimal checkpointBalance = BigDecimal.ZERO;
        if (previousCP != null) {
            checkpointBalance = previousCP.getBalance();
            checkpointEntryId = previousCP.getEntry().getId();
        }
        // sum checkpoint balance and all entries after it
        EntrySumAndCount entrySumAndCount = accountingDAO.getEntrySumAndCount(accountId, checkpointEntryId + 1, entryId);
        // update checkpoint count
        long count = entrySumAndCount.getCount();
        if(count > getBalanceCheckpointInterval()) {
        	if(isAsyncCheckpointCreation()) {
        		dirtyAccountSet.put(accountId, DUMMY_PAYLOAD);
        	} else {
        		doCreateCheckPoint(accountId, checkpointBalance, entrySumAndCount);
        	}
        }
        BigDecimal balance = checkpointBalance.add(entrySumAndCount.getSum());
        return balance;
    }
    
    private void checkNegativeAccountBalances(List<Entry> entries) {
		for (Entry e : entries) {
			Account a = e.getAccount();
			if(!a.isNegativeBalanceAllowed() && e.getAmount().compareTo(BigDecimal.ZERO) < 0) {
				Money balance = getBalance(a.getId());
				BigDecimal amount = balance.getAmount();
				if(amount.signum() == -1) {
					throw new NegativeBalanceException(a.getId());
				}
			}
		}
	}
    
    private void checkRequiredFields(Account account) {
    	if(account.getCreated() == null) {
    		account.setCreated(new Date());
    	}
    	if(account.getStatus() == null) {
    		account.setStatus(AccountStatus.OPEN);
    	}
	}
    
    private void ensureAccountNotClosedAndExists(Account a) {
        Account test = getAccount(a.getId());
        if(test == null) throw new AccountNotFoundException(a.getId());
    	if (test.getStatus() != AccountStatus.OPEN) {
            if(!allowClosedAccounts.get()) {
            	throw new AccountClosedException(a.getId());
            } else {
            	log.warn("Allowing transaction involving closed account: " + a.getId());
            }
        }
    }

    private void ensureAccountsNotNull(Collection<Entry> entries) {
        for (Entry e : entries) {
            if (e.getAccount() == null) {
                throw new NullPointerException("account is null, entry = " + e.toString());
            }
        }
    }

    /**
     * Creates a balance checkpoint if the number of entries on the account
     * after the latest checkpoint is higher than a predefined value.
     * @param accountId the account to check
     * @param entrySumAndCount entry information
     * @param balance the new balance
     */
    private void createCheckpointIfNeeded(Long accountId) {
        BalanceCheckpoint latestCP = accountingDAO.getLatestBalanceCheckpoint(accountId, null);
        long latestEntryId = (latestCP == null ? -1L : latestCP.getEntry().getId());
        BigDecimal oldBalance = (latestCP == null ? BigDecimal.ZERO : latestCP.getBalance());
        EntrySumAndCount entrySumAndCount = accountingDAO.getEntrySumAndCount(accountId, latestEntryId + 1, Long.MAX_VALUE);
        doCreateCheckPoint(accountId, oldBalance, entrySumAndCount);
    }

	private void doCreateCheckPoint(Long accountId, BigDecimal oldBalance, EntrySumAndCount entrySumAndCount) {
		BigDecimal newBalance = oldBalance.add(entrySumAndCount.getSum());
        Long entryCount = entrySumAndCount.getCount();
        Long lastEntryId = entrySumAndCount.getLastEntryId();
        if (entryCount > getBalanceCheckpointInterval()) { // double check
            Entry e = accountingDAO.getEntry(lastEntryId);
            BalanceCheckpoint newCP = new BalanceCheckpoint(e, newBalance);
            accountingDAO.saveOrUpdate(newCP);
            log.debug("Created new balance checkpoint: aId = " + accountId + ", count = " + entryCount + ", max eId = " + lastEntryId + ", balance = " + newBalance);
        } else {
        	log.debug("Skipped balance checkpoint creation: aId = " + accountId + ", count = " + entryCount + ", max eId = " + lastEntryId + ", balance = " + newBalance);
        }
	}
    
    private CurrencyRate getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, Date date, int fallbackDays, boolean allowSearch) {
    	DateMidnight dateMidnight = new DateTime(date).toDateMidnight();
        DateMidnight end = dateMidnight.plusDays(1);
        DateMidnight start = dateMidnight.minusDays(fallbackDays);
        
        CurrencyRate rate = accountingDAO.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode, start.toDate(), end.toDate());
        if (rate == null) {
        	
            // try opposite direction
            rate = accountingDAO.getCurrencyRate(targetCurrencyCode, sourceCurrencyCode, start.toDate(), end.toDate());
            
            if (rate != null) {
                rate = rate.invert();
            }
        }
        
        if (rate == null && allowSearch) {
            // look for a first level chain
            List<CurrencyRate> chain = accountingDAO.getCurrencyRateChain(targetCurrencyCode, sourceCurrencyCode, start.toDate(), end.toDate());
            
            if (!chain.isEmpty()) {
                // chain found, now we must put the rates in order and combine them
                CurrencyRate r0 = chain.get(0);
                CurrencyRate r1 = chain.get(1);
                
                CurrencyRate firstRate;
                CurrencyRate secondRate;
                
                if (r0.getSourceCurrencyCode().equals(sourceCurrencyCode)  ||  r0.getTargetCurrencyCode().equals(sourceCurrencyCode)) {
                    firstRate = r0;
                    secondRate = r1;
                } else {
                    firstRate = r1;
                    secondRate = r0;
                }
                
                if (!firstRate.getSourceCurrencyCode().equals(sourceCurrencyCode)) {
                    firstRate = firstRate.invert();
                }
                
                if (!secondRate.getTargetCurrencyCode().equals(targetCurrencyCode)) {
                    secondRate = secondRate.invert();
                }
                
                rate = firstRate.combine(secondRate);
            }
        }
        
        
        return rate;
    }
    
	private void checkThrowNotExists(Transaction t, Long transactionId) {
		if(t == null) {
			throw new TransactionNotFoundException(transactionId);
		}
	}
}
