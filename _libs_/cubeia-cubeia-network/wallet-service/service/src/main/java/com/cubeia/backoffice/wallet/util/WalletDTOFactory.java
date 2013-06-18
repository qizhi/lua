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

package com.cubeia.backoffice.wallet.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.entity.AccountAttribute;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.SupportedCurrency;
import com.cubeia.backoffice.accounting.core.entity.TransactionAttribute;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountInformation;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.Entry;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;

/**
 * Factory for conversion between entities and DTO objects.
 * @author w
 *
 */
@Component("wallet.service.walletDTOFactory")
public class WalletDTOFactory {

    public static final String GAME_ID_KEY = "gameId";
    public static final String OBJECT_ID_KEY = "objectId";
    public static final String GAME_NAME_KEY = "gameName";

	public AccountStatus createAccountStatusEntityFromDTO(
            com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus acDTO) {
        return acDTO == null ? null : AccountStatus.valueOf(acDTO.name());
    }

    public AccountsOrder createAccountsOrderEntityFromDTO(com.cubeia.backoffice.wallet.api.dto.AccountsOrder order) {
        if(order == null) {
        	return null;
        } else if(order == com.cubeia.backoffice.wallet.api.dto.AccountsOrder.ACCOUNT_NAME) {
        	return AccountsOrder.NAME;
        } else if(order == com.cubeia.backoffice.wallet.api.dto.AccountsOrder.USER_ID) {
        	return AccountsOrder.USER_ID;
        } else {
        	return AccountsOrder.valueOf(order.toString());
        }
    }

    public com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus createAccountStatusDTOFromEntity(
            AccountStatus status) {
        return status == null ? null : com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus.valueOf(status.name());
    }
    
    public com.cubeia.backoffice.accounting.core.domain.TransactionsOrder createTransactionOrderEntityFromDTO(TransactionsOrder order) {
        return order == null ? null : com.cubeia.backoffice.accounting.core.domain.TransactionsOrder.valueOf(order.name());
    }

    public List<Account> createAccountDTOsFromEntities(List<com.cubeia.backoffice.accounting.core.entity.Account> accounts) {
        ArrayList<Account> dtoList = new ArrayList<Account>();
        for (com.cubeia.backoffice.accounting.core.entity.Account a : accounts) {
            dtoList.add(createAccountDTOFromEntity(a));
        }
        return dtoList;
    }

    public Account createAccountDTOFromEntity(com.cubeia.backoffice.accounting.core.entity.Account a) {
        if (a == null) {
            return null;
        }
        
        Account aDTO = new Account();
        aDTO.setId(a.getId());
        aDTO.setCurrencyCode(a.getCurrencyCode());
        aDTO.setInformation(createAccountInformationDTOFromEntity(a));
        aDTO.setStatus(createAccountStatusDTOFromEntity(a.getStatus()));
        aDTO.setType(AccountType.valueOf(a.getType()));
        aDTO.setCreated(a.getCreated());
        aDTO.setClosed(a.getClosed());
        aDTO.setUserId(a.getUserId());        
        aDTO.setNegativeAmountAllowed(a.isNegativeBalanceAllowed());
        return aDTO;
    }

    public AccountInformation createAccountInformationDTOFromEntity(
    		com.cubeia.backoffice.accounting.core.entity.Account a) {
        if (a == null) {
            return null;
        }
        
        AccountInformation aiDto = new AccountInformation();
        aiDto.setGameId(getAccountIntAttribute(a, GAME_ID_KEY));
        aiDto.setName(a.getAttribute(GAME_NAME_KEY));
        aiDto.setObjectId(a.getAttribute(OBJECT_ID_KEY));
        
        return aiDto;
    }
    
    private int getAccountIntAttribute(com.cubeia.backoffice.accounting.core.entity.Account a, String key) {
    	String s = a.getAttribute(key);
    	if(s == null) {
    		return -1;
    	} else {
    		return Integer.parseInt(s);
    	}
    }

    public com.cubeia.backoffice.accounting.core.entity.Account createAccountEntityFromDTO(Account a) {
        com.cubeia.backoffice.accounting.core.entity.Account account = new com.cubeia.backoffice.accounting.core.entity.Account();
        account.setId(a.getId());
        account.setUserId(a.getUserId());
        account.setNegativeBalanceAllowed(a.getNegativeAmountAllowed());
        account.setCurrencyCode(a.getCurrencyCode());
        account.setStatus(createStatusFromDTO(a.getStatus()));
        account.setType(createTypeFromDTO(a.getType()));
        account.setCreated(a.getCreated());
        account.setClosed(a.getClosed());
        account.setName(a.getInformation().getName());
        return account;
    }

    public String createTypeFromDTO(AccountType type) {
        return type.name();
    }

    public AccountStatus createStatusFromDTO(Account.AccountStatus a) {
        if(a == Account.AccountStatus.OPEN) {
            return AccountStatus.OPEN;
        } else if(a == Account.AccountStatus.CLOSED){
            return AccountStatus.CLOSED;
        }
        return null;
    }

    public void setAccountInformationEntityFromDTO(
			com.cubeia.backoffice.accounting.core.entity.Account ae,
			AccountInformation i) {
		
    	if(i != null) {
    		Map<String, AccountAttribute> map = ae.getAttributes();
    		map.put(GAME_ID_KEY, new AccountAttribute(ae, GAME_ID_KEY, numberToStringOrNull(i.getGameId())));
    		map.put(OBJECT_ID_KEY, new AccountAttribute(ae, OBJECT_ID_KEY, i.getObjectId()));
    		map.put(GAME_NAME_KEY, new AccountAttribute(ae, GAME_NAME_KEY, i.getName()));
    	}
	}
    
	public void setAccountInformationEntityFromDTO(
			com.cubeia.backoffice.accounting.core.entity.Account ae,
			MetaInformation i) {
		
    	if(i != null) {
    		Map<String, AccountAttribute> map = ae.getAttributes();
    		map.put(GAME_ID_KEY, new AccountAttribute(ae, GAME_ID_KEY, numberToStringOrNull(i.getGameId())));
    		map.put(OBJECT_ID_KEY, new AccountAttribute(ae, OBJECT_ID_KEY, i.getObjectId()));
    		map.put(GAME_NAME_KEY, new AccountAttribute(ae, GAME_NAME_KEY, i.getName()));
    	}	
	}

	private String numberToStringOrNull(Number n) {
	    return n == null ? null : "" + n;
	}

    public Transaction createTransactionDTOFromEntity(com.cubeia.backoffice.accounting.core.entity.Transaction tx) {
        if (tx == null) {
            return null;
        }
        
        Collection <Entry> entryDTOs = new ArrayList<Entry>();
        for (com.cubeia.backoffice.accounting.core.entity.Entry e : tx.getEntries()) {
            entryDTOs.add(createEntryDTOFromEntity(e));
        }
        
        Map<String, String> attribs = new HashMap<String, String>();
        for (Map.Entry<String, TransactionAttribute> txAttrib : tx.getAttributes().entrySet()) {
            if (txAttrib.getValue() == null) {
                attribs.put(txAttrib.getKey(), "");
            } else {
                attribs.put(txAttrib.getKey(), txAttrib.getValue().getValue());
            }
        }
        
        return new Transaction(tx.getId(), tx.getTimestamp(), tx.getComment(), attribs, entryDTOs.toArray(new Entry[0]));
    }

    public Entry createEntryDTOFromEntity(com.cubeia.backoffice.accounting.core.entity.Entry e) {
        // NOTE: resulting entry is not known here
        if (e == null) {
            return null;
        } else {
            return new Entry(
                e.getId(), 
                e.getAccount().getId(), 
                e.getAccount().getUserId(), 
                e.getTransaction().getId(),
                e.getTransaction().getTimestamp(),
                e.getTransaction().getComment(),
                AccountType.valueOf(e.getAccount().getType()),
                createAccountStatusDTOFromEntity(e.getAccount().getStatus()),
                new Money(e.getAccount().getCurrencyCode(), e.getAccount().getFractionalDigits(), e.getAmount()), 
                null);
        }
    }
    
    public Currency createCurrencyDTOFromEntity(SupportedCurrency sc) {
    	return new Currency(sc.getCurrencyCode(), sc.getFractionalDigits());
    }

    public SupportedCurrency createCurrencyEntityFromDTO(Currency sc) {
        return new SupportedCurrency(sc.getCode(), sc.getFractionalDigits());
    }
}