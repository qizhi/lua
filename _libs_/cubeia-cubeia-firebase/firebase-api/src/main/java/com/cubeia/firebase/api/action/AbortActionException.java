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
package com.cubeia.firebase.api.action;

/**
 * Exception to throw from GameProcessor handler to rollback transaction silently 
 */
public class AbortActionException extends RuntimeException {

	private static final long serialVersionUID = 8753877160262618657L;

	public AbortActionException() {}

	public AbortActionException(String message) {
		super(message);
	}

	public AbortActionException(Throwable cause) {
		super(cause);
	}

	public AbortActionException(String message, Throwable cause) {
		super(message, cause);
	}
}
