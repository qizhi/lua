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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionParticipant;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.manager.SupportedCurrencyManager;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.DepositResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.WithdrawResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.exception.TransactionNotBalancedException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest.TransferType;
import com.cubeia.backoffice.wallet.manager.WalletAccountingManager;
import com.cubeia.backoffice.wallet.manager.WalletManager;
import com.cubeia.backoffice.wallet.util.WalletDTOFactory;
import com.cubeia.backoffice.wallet.api.dto.Currency;

@Component("wallet.service.walletService")
public class WalletServiceImpl implements WalletService {
	
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "wallet.service.accountingManager")
    protected WalletAccountingManager accountingManager;
    
    @Resource(name = "wallet.service.walletManager")
    protected WalletManager walletManager;
    
    @Resource(name = "wallet.service.walletDTOFactory")
    protected WalletDTOFactory dtoFactory;
    
	@Resource(name = "accounting.supportedCurrenciesManager")
	protected SupportedCurrencyManager supportedCurrencyManager;
	
    @Override
    public void closeAccount(Long accountId) throws AccountNotFoundException {
    	Account account = accountingManager.getAccount(accountId);
        if (account == null) {
            throw new AccountNotFoundException("cannot close account, account not found, id = " + accountId);
        }
        
        //if (!checkBalanceIsZero(account.getId())) {
            account.setStatus(AccountStatus.CLOSED);
        /*} else {
            account.setStatus(AccountStatus.CLOSED);
        }*/
        account.setClosed(new Date());
        accountingManager.updateAccount(account);
    }
    
    @Override
    public void updateAccount(com.cubeia.backoffice.wallet.api.dto.Account account) {

        Account accountById = accountingManager.getAccount(account.getId());
        accountById.setUserId(account.getUserId());
        accountById.setNegativeBalanceAllowed(account.getNegativeAmountAllowed());
        accountById.setCurrencyCode(account.getCurrencyCode());
        accountById.setStatus(dtoFactory.createStatusFromDTO(account.getStatus()));
        accountById.setName(account.getInformation().getName());

        accountingManager.updateAccount(accountById);
    }

    @Override
    public void updateSupportedCurrency(Currency currency) {
        SupportedCurrency c = dtoFactory.createCurrencyEntityFromDTO(currency);
        supportedCurrencyManager.updateCurrency(c);
    }

    @Override
    public CreateAccountResult createAccount(CreateAccountRequest request) {
        CreateAccountResult cr = new CreateAccountResult();
		cr.setRequestId(request.getRequestId());
        
		com.cubeia.backoffice.wallet.api.dto.Currency currency = getSupportedCurrency(request.getCurrencyCode());
		if (currency == null) {
			throw new RuntimeException("Create account failed. Currency ("+request.getCurrencyCode()+") is not supported. Supported currencies are: "+getSupportedCurrencies()); 
		}
		
        Account account = new Account(currency.getCode(), currency.getFractionalDigits());
        account.setUserId(request.getUserId());
        account.setType(request.getType().name());
        account.setNegativeBalanceAllowed(request.isNegativeBalanceAllowed());
        
        // Populate meta data
        dtoFactory.setAccountInformationEntityFromDTO(account, request.getInformation());
        
        try {
            account = accountingManager.createAccount(account);
            
            if (account != null  &&  account.getId() != null) {
                cr.setAccountId(account.getId());
            } else {
                throw new RuntimeException("error creating account, request: "+request);
            }
        } catch (Throwable t) {
            throw new RuntimeException("error creating account,request: "+request);
        }
        
        return cr;
    }

    @Override
    public DepositResult depositFromAccountToRemoteWallet(UUID requestId,
        long userId, long sessionId, long licenseeId, Money amount) {
        
        DepositResult dr = new DepositResult();
        dr.setRequestId(requestId);
        
        Transaction tx = walletManager.deposit(amount.getAmount(), sessionId, licenseeId);
        dr.setTransactionId(tx.getId());
        
        return dr;
    }

    @Override
    public AccountBalanceResult getAccountBalance(long accountId) throws AccountNotFoundException {
        AccountBalanceResult balanceResponse = new AccountBalanceResult();
        
        Account account = accountingManager.getAccount(accountId);
        
        if (account != null) {
            com.cubeia.backoffice.accounting.api.Money balance = accountingManager.getBalance(accountId);
            balanceResponse.setBalance(new Money(account.getCurrencyCode(), account.getFractionalDigits(), 
                    balance.getAmount()));
            balanceResponse.setAccountId(account.getId());
        } else {
            throw new AccountNotFoundException("account not found, aId = " + accountId);
        }
        
        return balanceResponse;
    }
    
    
    @Override
    public WithdrawResult withdrawFromRemoteWalletToAccount(UUID requestId, long userId, long sessionId, long licenseeId, Money amount) {
        
        WithdrawResult wr = new WithdrawResult();
        wr.setRequestId(requestId);
        
        try {
            Transaction tx = walletManager.withdraw(amount.getAmount(), sessionId, licenseeId);
            wr.setTransactionId(tx.getId());
        } catch (Throwable th) {
            throw new RuntimeException("failed to withdraw, requestId = " + requestId, th);
        }
        
        return wr;
    }
    
    /*private boolean checkBalanceIsZero(long accountId) {
        BigDecimal balance = accountingManager.getBalance(accountId);
        if (balance.longValue() != 0) {
            log.warn("Closing account with a non-zero balance. Account: "+accountId+" Balance: "+balance);
            return false;
        } else {
            return true;
        }
    } */

    @Override
    public AccountQueryResult listAccounts(Long accountId, Long userId,
            Collection<com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus> statuses, 
            Collection<com.cubeia.backoffice.wallet.api.dto.Account.AccountType> types, 
            int offset, int limit, AccountsOrder order, boolean ascending) {
        
        Set<AccountStatus> entityStatuses = null;
        if (statuses != null) {
            entityStatuses = new HashSet<AccountStatus>();
            for (com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus acDTO : statuses) {
                entityStatuses.add(dtoFactory.createAccountStatusEntityFromDTO(acDTO));
            }
        }
        Set<String> entityTypes = null;
        if (types != null) {
            entityTypes = new HashSet<String>();
            for (com.cubeia.backoffice.wallet.api.dto.Account.AccountType acDTO : types) {
                entityTypes.add(acDTO.toString());
            }
        }
        
        QueryResultsContainer<Account> resultContainer = accountingManager.listAccounts(
            accountId, userId, entityStatuses, entityTypes, offset, limit, 
            dtoFactory.createAccountsOrderEntityFromDTO(order), ascending);
        
        AccountQueryResult result = new AccountQueryResult(offset, limit, resultContainer.getTotalQueryResultSize(), 
            dtoFactory.createAccountDTOsFromEntities(resultContainer.getResults()), order, ascending);
        
        return result;
    }
    
    @Override
    public TransactionQueryResult listTransactions(
        Long account1Id, Boolean account1credit, boolean id1IsUserId, 
        Long account2Id, Boolean account2credit, boolean id2IsUserId, 
        Date startDate, Date endDate,
        int offset, int limit, TransactionsOrder order, boolean ascending) {
    	
        QueryResultsContainer<Transaction> resultContainer = accountingManager.listTransactions(
    		TransactionParticipant.legacy(account1Id, account1credit, id1IsUserId), 
    		TransactionParticipant.legacy(account2Id, account2credit, id2IsUserId), 
            startDate, endDate, 
            offset, limit, dtoFactory.createTransactionOrderEntityFromDTO(order), ascending);
        
        ArrayList<com.cubeia.backoffice.wallet.api.dto.Transaction> txDTOs = 
            new ArrayList<com.cubeia.backoffice.wallet.api.dto.Transaction>();
        for (Transaction tx : resultContainer.getResults()) {
            txDTOs.add(dtoFactory.createTransactionDTOFromEntity(tx));
        }
        
        TransactionQueryResult txQueryResult = new TransactionQueryResult(
            offset, limit, resultContainer.getTotalQueryResultSize(), txDTOs, order, ascending);
        
        return txQueryResult;
    }

    @Override
    public EntriesQueryResult listEntries(Long accountId,
        boolean includeBalances, int offset, int limit, boolean ascending) {

        log.debug("getting entries for account id = {}", accountId);
        
        QueryResultsContainer<Entry> resultContainer = accountingManager.listEntries(
             accountId, offset, limit, ascending);
        
        ArrayList<com.cubeia.backoffice.wallet.api.dto.Entry> entryDTOs = 
            new ArrayList<com.cubeia.backoffice.wallet.api.dto.Entry>();
        for (Entry e : resultContainer.getResults()) {
            entryDTOs.add(dtoFactory.createEntryDTOFromEntity(e));
        }
        
        // fill in resulting balances
        if (includeBalances  &&  entryDTOs.size() > 0  &&  accountId != null) {
            Account a = accountingManager.getAccount(accountId);
            
            ListIterator<com.cubeia.backoffice.wallet.api.dto.Entry> iter = entryDTOs.listIterator();
            com.cubeia.backoffice.wallet.api.dto.Entry firstEntry = entryDTOs.get(0);
            
            // get balance before first entry (or if descending after entry)
            BigDecimal balance = accountingManager.getBalanceAfterEntry(accountId, firstEntry.getId()).getAmount();
            if (ascending) {
                balance = balance.subtract(firstEntry.getAmount().getAmount());
            }
            
            while (iter.hasNext()) {
                com.cubeia.backoffice.wallet.api.dto.Entry e = iter.next();
                
                if (ascending) {
                    balance = balance.add(e.getAmount().getAmount());
                }
                
                e.setResultingBalance(new Money(a.getCurrencyCode(), a.getFractionalDigits(), balance));
                
                if (!ascending) {
                    balance = balance.subtract(e.getAmount().getAmount());
                }
            }
        }
        
        // log.debug("found {} entries", entryDTOs.size());
        
        return new EntriesQueryResult(offset, limit, resultContainer.getTotalQueryResultSize(), entryDTOs, ascending);
    }
    
    @Override
    public com.cubeia.backoffice.wallet.api.dto.Transaction getTransactionById(Long txId) {
        Transaction tx = accountingManager.getTransactionById(txId);
        
        if (tx == null) {
            return null;
        }
        
        com.cubeia.backoffice.wallet.api.dto.Transaction txDTO = dtoFactory.
            createTransactionDTOFromEntity(tx);
        
        // fill in entry balances
        for (com.cubeia.backoffice.wallet.api.dto.Entry eDTO : txDTO.getEntries()) {
            Money balance = accountingManager.getBalanceAfterEntry(eDTO.getAccountId(), eDTO.getId());
            eDTO.setResultingBalance(balance);
        }
        
        return txDTO;
    }
    
    public void setWalletManager(WalletManager walletManager) {
        this.walletManager = walletManager;
    }
    
    public void setWalletDTOFactory(WalletDTOFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    @Override
    public com.cubeia.backoffice.wallet.api.dto.Account getAccountById(Long accountId) {
        log.debug("Getting account by id: " + accountId);
        return dtoFactory.createAccountDTOFromEntity(accountingManager.getAccount(accountId));
    }

    @Override
    public void openAccount(Long accountId) {
        Account a = accountingManager.getAccount(accountId);
        a.setStatus(AccountStatus.OPEN);
    }

	@Override
	public com.cubeia.backoffice.wallet.api.dto.Account getStaticAccountForCurrency(Long userId, String currencyCode) {
		return dtoFactory.createAccountDTOFromEntity(accountingManager.getAccountByUserIdTypeAndCurrency(userId, AccountType.STATIC_ACCOUNT.toString(), currencyCode));
		 
	}

	@Override
	public DepositResult deposit(Long userId, Money amount, Long fromAccountId) {
		Account userAccount = getOrCreateStaticAccountForUserAndCurrency(userId, amount.getCurrencyCode());
		Transaction tx = accountingManager.createTransaction(null, null, amount.getAmount(), fromAccountId, userAccount.getId(), null);
		
		DepositResult result = new DepositResult();
		result.setTransactionId(tx.getId());
		return result;
	}

	@Override
	public Long getOrCreateStaticAccountIdForUserAndCurrency(Long userId, String currencyCode) {
		return getOrCreateStaticAccountForUserAndCurrency(userId, currencyCode).getId();
	}
	
	private Account getOrCreateStaticAccountForUserAndCurrency(Long userId, String currencyCode) {
		Account userAccount = accountingManager.getNonSessionAccountByUserIdAndCurrency(userId, currencyCode);
		if (userAccount == null) {
			SupportedCurrency currency = supportedCurrencyManager.getCurrencyByCode(currencyCode);
			if (currency == null) {
				throw new RuntimeException("Unsupported currency["+currencyCode+"]");
			}
			
			userAccount = new Account(userId, currencyCode, currency.getFractionalDigits());
			userAccount.setType(AccountType.STATIC_ACCOUNT.name());
			accountingManager.createAccount(userAccount);
		}
		return userAccount;
	}
	
	/**
	 * 1. Check Account type
	 * 
	 * 2. If SESSION then transfer to corresponding STATIC account
	 * 
	 * 3. If STATIC then transfer to remote wallet
	 * 
	 * TODO: This method is both too specialized and too simplified to be generally useful.
	 *   If we provide this kind of high level interface the business rules must be configurable.
	 *   
	 */
	@Override
	public void handleTransferFunds(com.cubeia.backoffice.wallet.api.dto.Account account, TransferRequest request) {
		if (account.getType() == AccountType.SESSION_ACCOUNT) {
			handleTransferWithLocalAccount(account, request);
			
		}else if (account.getType() == AccountType.STATIC_ACCOUNT) {
			log.debug("Transfer funds between STATIC ACCOUNT and REMOTE WALLET: "+request);
			handleTransferWithRemoteWallet(account, request);
			
		} else {
			throw new RuntimeException("Transfer called on an unsupported account type. Request["+request+"] Account["+account+"]");
		}
	}

	private void handleTransferWithLocalAccount(com.cubeia.backoffice.wallet.api.dto.Account account, TransferRequest request) {
		Long staticAccountId = getOrCreateStaticAccountIdForUserAndCurrency(account.getUserId(), account.getCurrencyCode());
		if (request.getTransferType() == TransferType.CREDIT) {
			log.debug("Transfer funds from STATIC ACCOUNT to SESSION ACCOUNT: "+request);
			accountingManager.createTransaction(request.getComment(), null, request.getAmount(), 
			    staticAccountId, account.getId(), null);
			
		} else if (request.getTransferType() == TransferType.DEBIT) {
			log.debug("Transfer funds from SESSION ACCOUNT to STATIC ACCOUNT: "+request);
            accountingManager.createTransaction(request.getComment(), null, request.getAmount(), 
                account.getId(), staticAccountId, null);
			
		} else {
			throw new RuntimeException("Transfer called on an unsupported transfer type. Request["+request+"] Account["+account+"]");
		}
	}

	private void handleTransferWithRemoteWallet(com.cubeia.backoffice.wallet.api.dto.Account account, TransferRequest request) {
		Long licenseeId = request.getOperatorId();
		Money amount = new Money(java.util.Currency.getInstance(account.getCurrencyCode()), request.getAmount());
		if (request.getTransferType() == TransferType.CREDIT) {
			withdrawFromRemoteWalletToAccount(request.getRequestId(), account.getUserId(), account.getId(), licenseeId, amount);
			
		} else if (request.getTransferType() == TransferType.DEBIT) {
			depositFromAccountToRemoteWallet(request.getRequestId(), account.getUserId(), account.getId(), licenseeId, amount);
			
		} else {
			throw new RuntimeException("Transfer called on an unsupported transfer type. Request["+request+"] Account["+account+"]");
		}
	}

	@Override
	public void setSupportedCurrencyManager(SupportedCurrencyManager supportedCurrencyManager) {
		this.supportedCurrencyManager = supportedCurrencyManager;
	}
	
	
	@Override
	public TransactionResult doTransaction(TransactionRequest transaction) throws TransactionNotBalancedException {
        TransactionResult transactionResult = new TransactionResult();
        
        try {
            // Iterate and create entries
            List<Entry> entries = new ArrayList<Entry>();
            Map<Long, Account> accounts = new HashMap<Long, Account>();
            for (TransactionEntry roundEntry : transaction.getEntries()) {
            	Account account = accountingManager.getAccount(roundEntry.getAccountId());
            	accounts.put(roundEntry.getAccountId(), account);
                Entry entry = new Entry(account, roundEntry.getAmount().getAmount());
                entries.add(entry);
            }
            
            Transaction tx = accountingManager.createTransaction(transaction.getComment(), transaction.getExternalId(), entries, transaction.getAttributes());
            
            if (tx != null  &&  tx.getId() != null) {
                transactionResult.setTransactionId(tx.getId());
                
                Collection<AccountBalanceResult> balances = new ArrayList<AccountBalanceResult>();
                for (Long accountId : accounts.keySet()) {
                    if(!transaction.getExcludeReturnBalanceForAcconds().contains(accountId)) { // if not excluded
                    	Account account = accounts.get(accountId);
	                    com.cubeia.backoffice.accounting.api.Money balance = accountingManager.getBalance(accountId);
	                    AccountBalanceResult accountBalance = new AccountBalanceResult();
	                    accountBalance.setBalance(new Money(account.getCurrencyCode(), account.getFractionalDigits(), balance.getAmount()));
	                    accountBalance.setAccountId(accountId);
	                    balances.add(accountBalance);
                    }
                }
                transactionResult.setBalances(balances);
            } else {
                throw new RuntimeException("transaction creation denied. Round report ["+transaction+"] Tx["+tx+"]");
            }
        } catch (com.cubeia.backoffice.accounting.core.TransactionNotBalancedException tbe) {
            throw new TransactionNotBalancedException(tbe.getMessage());
        } catch (NegativeBalanceException e) { 
        	throw e;
        } catch (Throwable th) {
            throw new RuntimeException("failed to create transaction ["+transaction+"]", th);
        }
        
        return transactionResult;
    }
	
	@Override
	public Collection<com.cubeia.backoffice.wallet.api.dto.Currency> getSupportedCurrencies() {
		List<com.cubeia.backoffice.wallet.api.dto.Currency> currencies = 
			new ArrayList<com.cubeia.backoffice.wallet.api.dto.Currency>();
		for (SupportedCurrency sc : supportedCurrencyManager.getCurrencies()) {
			currencies.add(dtoFactory.createCurrencyDTOFromEntity(sc));
		}
		
		return currencies;
	}
	
	@Override
	public void addSupportedCurrency(com.cubeia.backoffice.wallet.api.dto.Currency newCurrency) {
		SupportedCurrency currency = supportedCurrencyManager.getCurrencyByCode(newCurrency.getCode());
		
		if (currency == null) {
		    log.debug("adding currency: {}", newCurrency);
		    supportedCurrencyManager.addCurrency(new SupportedCurrency(newCurrency.getCode(), newCurrency.getFractionalDigits()));
		} else {
		    if (currency.getFractionalDigits() != newCurrency.getFractionalDigits()) {
	            throw new UnsupportedOperationException("error, tried to add/reactivate '" + newCurrency.getCode() + 
	                "' with a different number of fractional digits");
		    } 
		    
		    if (currency.isRemoved()) {
		        log.debug("reactivating previously removed currency: {}", currency);
		        currency.setRemoved(false);
		    }
		}
	}
	
	/**
	 * Removes a supported currency by marking it as removed.
	 * This method cannot delete it from the database as there might be 
	 * transactions and accounts referring to it.
	 * @param currencyCode the currency code
	 */
	@Override
	public void removeSupportedCurrency(String currencyCode) {
        log.debug("Removing currency " + currencyCode);
		SupportedCurrency currency = supportedCurrencyManager.getCurrencyByCode(currencyCode);
		if (currency != null) {
			currency.setRemoved(true);
		} else {
            log.debug("No currency found.");
        }
	}
	
	@Override
	public com.cubeia.backoffice.wallet.api.dto.Currency getSupportedCurrency(String currencyCode) {
		SupportedCurrency c = supportedCurrencyManager.getCurrencyByCode(currencyCode);
		if (c == null  ||  c.isRemoved()) {
			return null;
		} else {
			return dtoFactory.createCurrencyDTOFromEntity(c);
		}
	}
}
