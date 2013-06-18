/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.service.dosprotect;

import com.cubeia.firebase.api.service.Contract;

/**
 * This service is used to perform rudimentary protection
 * from DOS attacks. Protection is performed with an "X
 * actions over Y time" algorithm which can only be set manually
 * at the moment.  
 * 
 * <p>Services using this service are identified with a string key, each
 * registration checks the frequency rules and returns true if the call
 * is allowed and false otherwise.
 * 
 * <p><b>NB:</b></p> All string identifiers starting with "_" are
 * reserved by Firebase. 
 * 
 * @author Larsan
 */
public interface DosProtector extends Contract {

	/**
	 * This method is used to manually configure the service
	 * for a particular identifier. It should be observed that
	 * this will clear all available data, ie reset the rules 
	 * for the identifier.
	 * 
	 * <p>NB: The rules will not be copied, so please don't keep
	 * the around or change them after invoking this method.
	 * 
	 * <p>To remove rules, set the rule set to null.
	 * 
	 * <p><b>NB:</b></p> All string identifiers starting with "_" are
	 * reserved by Firebase. 
	 * 
	 * @param key Rule access identifier, must not be null
	 * @param rules Rules to use, may be null to remove rules
	 */
	public void config(String key, Rule...rules);
	
	/**
	 * Check access for an identifier and a caller. If the access 
	 * is allowed by the current rule set.
	 * 
	 * @param key Rule access identifier, must not be null
	 * @param callerId Id of the caller which is to be checked, must not be null
	 * @return True if access is allowed, false otherwise
	 */
	public boolean allow(String key, Object callerId);

}
