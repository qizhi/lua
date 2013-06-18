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
 * This exception if thrown when the message bus sender objects
 * cannot route a message to any channel. 
 * 
 * @author Lars J. Nilsson
 */
public class ChannelNotFoundException extends SystemException {

	private static final long serialVersionUID = 1590320756702980505L;

	public ChannelNotFoundException() { }

	public ChannelNotFoundException(String message) {
		super(message);
	}

	public ChannelNotFoundException(Throwable cause) {
		super(cause);
	}

	public ChannelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
