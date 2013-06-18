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
package com.cubeia.firebase.api.game.activator;

/**
 * This exception should be thrown to indicate that a table request has 
 * been denied. The activator should set a status code, and an optional 
 * string message, which will be forwarded to the clients.
 * 
 * @author larsan
 */
public class CreationRequestDeniedException extends Exception {

	private static final long serialVersionUID = 2932225799844167487L;
	
	private final int code;
	
	/**
	 * @param code Error code, will be forwarded to the client
	 */
	public CreationRequestDeniedException(int code) {
		this.code = code;
	}
	
	/**
	 * @return The error code of this exception
	 */
	public int getCode() {
		return code;
	}
}
