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
package com.cubeia.firebase.clients.java.connector;

import java.security.Key;

public class SessionKey implements Key {

	private static final long serialVersionUID = 1L;

	private final byte[] key;
	
	public SessionKey(byte[] key) {
		this.key = key;
	}
	
	public String getAlgorithm() {
		return "AES";
	}

	public byte[] getEncoded() {
		return key.clone();
	}

	public String getFormat() {
		return "RAW";
	}

}
