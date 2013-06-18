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
package com.cubeia.backoffice.users.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.cubeia.backoffice.users.entity.ResponseCache;

public class TransactionDAOImpl implements TransactionDAO {
    @PersistenceContext(unitName = "userServicePersistenceUnit")
    protected EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseCache getByTxId(UUID id) {
        Query q = em.createQuery("from ResponseCache as rc where rc.txId = :txId");
        q.setParameter("txId", id.toString());
        List<ResponseCache> result = q.getResultList();
        
        if (result.size() == 1) {
        	ResponseCache response = result.get(0);
        	return response;
        } else {
        	return null;
        }
	}

	@Override
	public void persist(ResponseCache tx) {
		em.persist(tx);
	}

}
