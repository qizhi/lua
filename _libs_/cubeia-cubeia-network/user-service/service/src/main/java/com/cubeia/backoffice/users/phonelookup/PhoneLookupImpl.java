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
package com.cubeia.backoffice.users.phonelookup;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.users.dao.PhoneLookupDAO;
import com.cubeia.backoffice.users.entity.UserInformation;

public class PhoneLookupImpl implements PhoneLookup {

	private static final transient Logger log = Logger.getLogger(PhoneLookupImpl.class);
	
	private PhoneLookupDAO phoneLookupDAO;
	
	@Override
	public UserInformation getUserInformation(String country, String number) {
		try {
			LookupStrategy strategy = getStrategy(country);
			if (strategy != null) {
				UserInformation info = strategy.lookup(number);
				info.setCellphone(number);
				return info;
			}
		} catch (Exception e) {
			log.warn("Error occured when looking up user information country["+country+"] number["+number+"]",e);
		}
		return null;
	}

	private LookupStrategy getStrategy(String country) {
		return phoneLookupDAO.getStrategyForCountry(country);
	}

	public void setPhoneLookupDAO(PhoneLookupDAO phoneLookupDAO) {
        this.phoneLookupDAO = phoneLookupDAO;
    }
	
}
