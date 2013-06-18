/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.service;

/**
 * A small bean used for service contract configuration. It contains
 * the contract class and the implementation class.
 * 
 * @author larsan
 */
@SuppressWarnings("rawtypes")
public class ContractsConfig {

	private final Class[] contracts;
	private final Class service;
	
	/**
	 * @param service Service class, must not be null
	 * @param contracts Contract classes, must not be null
	 */
	public ContractsConfig(Class service, Class...contracts) {
		this.service = service;
		this.contracts = contracts;
	}
	
	public Class[] getContracts() {
		return contracts;
	}
	
	public Class getService() {
		return service;
	}
}
