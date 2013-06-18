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
package com.cubeia.firebase.service.conn;

/**
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public class ClusterException extends Exception {

	private static final long serialVersionUID = 5534178909706431817L;

	public ClusterException() {
	}

	public ClusterException(String message) {
		super(message);
	}

	public ClusterException(Throwable cause) {
		super(cause);
	}

	public ClusterException(String message, Throwable cause) {
		super(message, cause);
	}

}
