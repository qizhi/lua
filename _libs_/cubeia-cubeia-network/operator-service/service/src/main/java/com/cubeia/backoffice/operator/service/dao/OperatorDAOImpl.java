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
package com.cubeia.backoffice.operator.service.dao;


import com.cubeia.backoffice.operator.service.entity.Operator;
import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.util.Collection;
import java.util.List;

@Component("operator.operatorDAO")
public class OperatorDAOImpl implements OperatorDAO {

    @PersistenceContext(unitName = "operatorPersistenceUnit")
    protected EntityManager em;

    @Override
    public Operator save(Operator o) {
        em.persist(o);
        return o;
    }

    @Override
    public Operator update(Operator o) {
        return em.merge(o);
    }

    @Override
    public Operator getOperator(long id) {
        return em.find(Operator.class, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Operator> getOperators() {
        return em.createQuery("from Operator").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
	public Collection<Operator> findOperatorsHavingConfigValue(OperatorConfigParameter key, Object value) {
    	Query query =  em.createQuery("select op from Operator op where op.config['"+key.name()+"'] " 
    			+ (value == null ? "is null" : "= :value"));
    	if(value != null){
    		query.setParameter("value", value);
    	}
    	
    	return query.getResultList();
    }

}
