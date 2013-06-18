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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.users.entity.PhoneLookupStrategy;
import com.cubeia.backoffice.users.phonelookup.LookupStrategy;

public class PhoneLookupDAOImpl implements PhoneLookupDAO {

	private static final transient Logger log = Logger.getLogger(PhoneLookupDAOImpl.class);
	
    @PersistenceContext(unitName = "userServicePersistenceUnit")
    protected EntityManager em;
    
	@SuppressWarnings("unchecked")
	@Override
	public LookupStrategy getStrategyForCountry(String country) {
		try {
		 Query q = em.createQuery("from PhoneLookupStrategy as s where s.country = :country");
	        q.setParameter("country", country);
	        List<PhoneLookupStrategy> result = q.getResultList();
	        
	        if (result.size() >= 1) {
	        	PhoneLookupStrategy config = result.get(0);
	        	Object strategy = Class.forName(config.getClassname()).newInstance();
	        	if (strategy instanceof LookupStrategy) {
					LookupStrategy checked = (LookupStrategy) strategy;
					return checked; // RETURN found instance
				} else {
					log.warn("PhoneLookup Class ["+result.get(0)+"] is not an instance of "+LookupStrategy.class);
				}
	        } else {
	        	log.debug("No strategy defined for country ["+country+"]");
	        }
		} catch (Exception e) {
			log.error("Failed to get or create PhoneLookupStrategy for country: "+country, e);
		}
		return null;
	}

}
