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
package com.cubeia.firebase.api.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * A simple utility class representing an socket and an address.
 * 
 * @author lars.j.nilsson
 */
public final class SocketAddress implements Serializable {

	private static final long serialVersionUID = 870381562732313829L;
	
	private final int port;
	private final InetAddress address;
	
	
	/**
	 * String constructor which takes the format &lt;ip&gt;:&lt;port&gt;
	 * 
	 * @param s String representation of the address, must not be null
	 * @throws UnknownHostException If the host of the address is not known
	 * @throws IllegalArgumentException If the address is malformed
	 */
	
	public SocketAddress(String s) throws UnknownHostException {
		Arguments.notNull(s, "s");
		int i = s.lastIndexOf(':');
		// if(i == -1) throw new IllegalArgumentException("missing semicolon in address string '" + s + "'");
		if(i != -1) {
			port = Integer.parseInt(s.substring(i + 1));
			address = InetAddress.getByName(s.substring(0, i));
		} else {
			address = InetAddress.getByName(s);
			port = -1;
		}
	}
	
	
	/**
	 * @param host Host address, must not be null
	 * @param port Port to use
	 */
	public SocketAddress(InetAddress host, int port) {
		Arguments.notNull(host, "host");
		this.port = port;
		address = host;
	}
	
	
	/**
	 * @return The host address, never null
	 */
	public InetAddress getHost() {
		return address;
	}
	
	
	/**
	 * @return The specified port number
	 */
	public int getPort() {
		return port;
	}
	
	
	/**
	 * @return This address as a java.net object, never null
	 */
	public InetSocketAddress toInetSocketAddress() {
		return new InetSocketAddress(address, port);
	}
	
	
	// --- OBJECT CODE --- //
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SocketAddress)) return false;
		else {
			SocketAddress a = (SocketAddress)obj;
			return address.equals(a.address) && port == a.port;
		}
	}
	
	@Override
	public int hashCode() {
		return address.hashCode() ^ port;
	}
	
	@Override
	public String toString() {
		return address.getHostAddress() + ":" + getPort();
	}
}
