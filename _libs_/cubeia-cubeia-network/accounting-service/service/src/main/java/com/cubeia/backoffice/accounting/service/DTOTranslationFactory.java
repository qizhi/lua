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

package com.cubeia.backoffice.accounting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import com.cubeia.backoffice.accounting.api.AccountAttributeDTO;
import com.cubeia.backoffice.accounting.api.AccountDTO;
import com.cubeia.backoffice.accounting.api.AccountQueryResultDTO;
import com.cubeia.backoffice.accounting.api.AccountStatusDTO;
import com.cubeia.backoffice.accounting.api.AccountsOrderDTO;
import com.cubeia.backoffice.accounting.api.BalancedEntryDTO;
import com.cubeia.backoffice.accounting.api.BalancedEntryQueryResultDTO;
import com.cubeia.backoffice.accounting.api.CurrencyRateDTO;
import com.cubeia.backoffice.accounting.api.EntryDTO;
import com.cubeia.backoffice.accounting.api.EntryQueryResultDTO;
import com.cubeia.backoffice.accounting.api.TransactionAttributeDTO;
import com.cubeia.backoffice.accounting.api.TransactionDTO;
import com.cubeia.backoffice.accounting.api.TransactionQueryResultDTO;
import com.cubeia.backoffice.accounting.api.TransactionsOrderDTO;
import com.cubeia.backoffice.accounting.core.domain.AccountsOrder;
import com.cubeia.backoffice.accounting.core.domain.BalancedEntry;
import com.cubeia.backoffice.accounting.core.domain.QueryResultsContainer;
import com.cubeia.backoffice.accounting.core.domain.TransactionsOrder;
import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.entity.AccountAttribute;
import com.cubeia.backoffice.accounting.core.entity.AccountStatus;
import com.cubeia.backoffice.accounting.core.entity.CurrencyRate;
import com.cubeia.backoffice.accounting.core.entity.Entry;
import com.cubeia.backoffice.accounting.core.entity.Transaction;
import com.cubeia.backoffice.accounting.core.entity.TransactionAttribute;

public class DTOTranslationFactory {
	
	public DTOTranslationFactory() { }
	
	public AccountsOrder fromDTO(AccountsOrderDTO dto) {
		if(dto == null) return null;
		return AccountsOrder.valueOf(dto.toString());
	}
	
	public TransactionsOrder fromDTO(TransactionsOrderDTO dto) {
		if(dto == null) return null;
		return TransactionsOrder.valueOf(dto.toString());
	}
	
	public BalancedEntryDTO toDTO(BalancedEntry e) {
		if(e == null) return null;
		BalancedEntryDTO dto = new BalancedEntryDTO();
		copyMembers(e, dto, "account", "transaction");
		dto.setAccount(toDTO(e.getAccount()));
		if(e.getTransaction() != null) {
			Transaction tr = e.getTransaction();
			dto.setTransactionId(tr.getId());
			dto.setTransactionComment(tr.getComment());
			dto.setTransactionTimestamp(tr.getTimestamp());
			dto.setTransactionAttributes(toDTO(tr.getAttributes()));
		}
		return dto;
	}
	
	public BalancedEntryQueryResultDTO toDTOBalancedEntry(QueryResultsContainer<BalancedEntry> res) {
		if(res == null) return null;
		BalancedEntryQueryResultDTO dto = new BalancedEntryQueryResultDTO();
		dto.setTotalQueryResultSize(res.getTotalQueryResultSize());
		if(res.getResults() != null) {
			for (BalancedEntry e : res.getResults()) {
				dto.getResults().add(toDTO(e));
			}
		}
		return dto;
	}
	
	public TransactionQueryResultDTO toDTOTransaction(QueryResultsContainer<Transaction> res) {
		if(res == null) return null;
		TransactionQueryResultDTO dto = new TransactionQueryResultDTO();
		dto.setTotalQueryResultSize(res.getTotalQueryResultSize());
		if(res.getResults() != null) {
			for (Transaction tr : res.getResults()) {
				dto.getResults().add(toDTO(tr));
			}
		}
		return dto;
	}
	
	public EntryQueryResultDTO toDTOEntry(QueryResultsContainer<Entry> res) {
		if(res == null) return null;
		EntryQueryResultDTO dto = new EntryQueryResultDTO();
		dto.setTotalQueryResultSize(res.getTotalQueryResultSize());
		if(res.getResults() != null) {
			for (Entry e : res.getResults()) {
				dto.getResults().add(toDTO(e));
			}
		}
		return dto;
	}
	
	public AccountQueryResultDTO toDTO(QueryResultsContainer<Account> res) {
		if(res == null) return null;
		AccountQueryResultDTO dto = new AccountQueryResultDTO();
		dto.setTotalQueryResultSize(res.getTotalQueryResultSize());
		if(res.getResults() != null) {
			for (Account a : res.getResults()) {
				dto.getResults().add(toDTO(a));
			}
		}
		return dto;
	}
	
	public Transaction fromDTO(TransactionDTO dto) {
		if(dto == null) return null; 
		Transaction tr = new Transaction();
		copyMembers(dto, tr, "entries", "attributes");
		attributesFromDTO(dto, tr);
		entriesToTransaction(dto, tr);
		return tr;
	}

	public TransactionDTO toDTO(Transaction trans) {
		if(trans == null) return null; 
		TransactionDTO dto = new TransactionDTO();
		copyMembers(trans, dto, "entries", "attributes");
		attributesToDTO(trans, dto);
		entriesToDTO(trans, dto);
		return dto;
	}
	
	public Entry fromDTO(EntryDTO dto) {
		if(dto == null) return null; 
		Entry e = new Entry();
		copyMembers(dto, e, "account", "transactionId");
		e.setAccount(fromDTO(dto.getAccount()));
		// e.setTransaction(fromDTO(dto.getTransaction()));
		return e;
	}
	
	public EntryDTO toDTO(Entry e) {
		if(e == null) return null; 
		EntryDTO dto = new EntryDTO();
		copyMembers(e, dto, "account", "transaction");
		dto.setAccount(toDTO(e.getAccount()));
		if(e.getTransaction() != null) {
			Transaction tr = e.getTransaction();
			dto.setTransactionId(tr.getId());
			dto.setTransactionComment(tr.getComment());
			dto.setTransactionTimestamp(tr.getTimestamp());
			dto.setTransactionAttributes(toDTO(tr.getAttributes()));
		}
		return dto;
	}
	
	public Collection<AccountDTO> toDTO(Collection<Account> accounts) {
        ArrayList<AccountDTO> aDTOs = new ArrayList<AccountDTO>();
        for (Account a : accounts) {
            aDTOs.add(toDTO(a));
        }
        return aDTOs;
	}
	
	public AccountDTO toDTO(Account acc) {
		if(acc == null) return null; 
		AccountDTO dto = new AccountDTO();
		copyMembers(acc, dto, "attributes", "status");
		attributesToDTO(acc, dto);
		dto.setStatus(toDTO(acc.getStatus()));
		return dto;
	}
	
	public AccountStatus fromDTO(AccountStatusDTO dto) {
		if(dto == null) return null;
		return AccountStatus.valueOf(dto.toString());
	}
	
	public AccountStatusDTO toDTO(AccountStatus a) {
		if(a == null) return null; 
		return AccountStatusDTO.valueOf(a.toString());
	}
	
	public Account fromDTO(AccountDTO dto) {
		if(dto == null) return null;
		Account a = new Account();
		copyMembers(dto, a, "attributes", "status");
		attributesFromDTO(dto, a);
		a.setStatus(fromDTO(dto.getStatus()));
		return a;
	}

	public AccountAttribute fromDTO(Account acc, AccountAttributeDTO dto) {
		if(dto == null) return null; 
		AccountAttribute a = new AccountAttribute();
		copyMembers(dto, a, "account");
		a.setAccount(acc);
		return a;
	}
	
	public AccountAttributeDTO toDTO(AccountAttribute a) {
		if(a == null) return null; 
		AccountAttributeDTO dto = new AccountAttributeDTO();
		copyMembers(a, dto, "account");
		return dto;
	}
	
	public TransactionAttribute fromDTO(Transaction acc, TransactionAttributeDTO dto) {
		if(dto == null) return null; 
		TransactionAttribute a = new TransactionAttribute();
		copyMembers(dto, a, "transaction");
		a.setTransaction(acc);
		return a;
	}
	
	public HashMap<String, String> toDTO(Map<String, TransactionAttribute> attribMap) {
	    HashMap<String, String> dtoMap = new HashMap<String, String>();
	    for (TransactionAttribute ta : attribMap.values()) {
	        dtoMap.put(ta.getKey(), ta.getValue());
	    }
	    return dtoMap;
	}
	
	public TransactionAttributeDTO toDTO(TransactionAttribute a) {
		if(a == null) return null; 
		TransactionAttributeDTO dto = new TransactionAttributeDTO();
		copyMembers(a, dto, "transaction");
		return dto;
	}
	
	public List<Entry> toDTOs(List<EntryDTO> entries) {
		if(entries == null) return null;
		List<Entry> list = new ArrayList<Entry>(entries.size());
		for (EntryDTO dto : entries) {
			list.add(fromDTO(dto));
		}
		return list;
	}
	
	public Set<AccountStatus> fromDTOs(Set<AccountStatusDTO> entries) {
		if(entries == null) return null;
		Set<AccountStatus> list = new HashSet<AccountStatus>(entries.size());
		for (AccountStatusDTO dto : entries) {
			list.add(fromDTO(dto));
		}
		return list;
	}
	
	public CurrencyRateDTO toDTO(CurrencyRate r) {
	    if (r == null) {
	        return null;
	    } else {
	        return new CurrencyRateDTO(r.getSourceCurrencyCode(), r.getTargetCurrencyCode(), r.getRate(), r.getTimestamp());
	    }
	}
	
	public CurrencyRate fromDTO(CurrencyRateDTO rdto) {
	    if (rdto == null) {
	        return null;
	    } else {
	        return new CurrencyRate(rdto.getSourceCurrencyCode(), rdto.getTargetCurrencyCode(), rdto.getRate(), rdto.getTimestamp());
	    }
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void entriesToTransaction(TransactionDTO dto, Transaction tr) {
		if(dto.getEntries() == null) return;
		for (EntryDTO d : dto.getEntries()) {
			tr.getEntries().add(fromDTO(d));
		}
	}
	
	private void entriesToDTO(Transaction tr, TransactionDTO dto) {
		if(tr.getEntries() == null) return;
		for (Entry e : tr.getEntries()) {
			dto.getEntries().add(toDTO(e));
		}
	}
	
	private void attributesFromDTO(AccountDTO dto, Account acc) {
		if(dto.getAttributes() == null) return;
		for (AccountAttributeDTO a : dto.getAttributes().values()) {
			AccountAttribute tmp = fromDTO(acc, a);
			acc.getAttributes().put(tmp.getKey(), tmp);
		}
	}
	
	private void attributesToDTO(Account acc, AccountDTO dto) {
		if(acc.getAttributes() == null) return;
		for (AccountAttribute a : acc.getAttributes().values()) {
			AccountAttributeDTO tmp = toDTO(a);
			dto.getAttributes().put(tmp.getKey(), tmp);
		}
	}
	
	private void attributesFromDTO(TransactionDTO dto, Transaction acc) {
		if(dto.getAttributes() == null) return;
		for (TransactionAttributeDTO a : dto.getAttributes().values()) {
			TransactionAttribute tmp = fromDTO(acc, a);
			acc.getAttributes().put(tmp.getKey(), tmp);
		}
	}
	
	private void attributesToDTO(Transaction acc, TransactionDTO dto) {
		if(acc.getAttributes() == null) return;
		for (TransactionAttribute a : acc.getAttributes().values()) {
			TransactionAttributeDTO tmp = toDTO(a);
			dto.getAttributes().put(tmp.getKey(), tmp);
		}
	}

	/**
	 * Copy instance members from source to target object. Exclude given
	 * members.
	 * 
	 * @param src Source object, must not be null
	 * @param target Target object, must not be null
	 * @param excludedMembers Exclude members, may be null
	 */
	private void copyMembers(Object src, Object target, String...excludedMembers) {
		BeanUtils.copyProperties(src, target, excludedMembers);
	}
}
