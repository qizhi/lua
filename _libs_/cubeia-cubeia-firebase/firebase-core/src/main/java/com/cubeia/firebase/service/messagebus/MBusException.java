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
package com.cubeia.firebase.service.messagebus;

import com.cubeia.firebase.api.server.SystemException;

/**
 * @author Larsan
 */
public class MBusException extends SystemException {

	private static final long serialVersionUID = -420368837178024535L;

	public MBusException() {
		super();
	}

	public MBusException(String message, Throwable cause) {
		super(message, cause);
	}

	public MBusException(String message) {
		super(message);
	}

	public MBusException(Throwable cause) {
		super(cause);
	}
}
