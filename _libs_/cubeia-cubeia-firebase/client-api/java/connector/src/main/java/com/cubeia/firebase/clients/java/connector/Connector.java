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

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.cubeia.firebase.io.ProtocolObject;

public interface Connector {

	/**
	 * @param handler Packet listener, must not be null
	 */
	public void addListener(PacketListener handler);

	/**
	 * @param handler Packet listener, must not be null
	 */
	public void removeListener(PacketListener handler);

	/**
	 * @param packet Packet to write to socket, must not be null
	 */
	public void send(ProtocolObject packet);

	/**
	 * Connect the connector. This method will wait for a key exchange
	 * if native firebase encryption is used.  
	 */
	public void connect() throws IOException, GeneralSecurityException;
	
	/**
	 * Close the connector. This will end the hand-off thread and
	 * close the socket and related objects.
	 */
	public void disconnect();

	/**
	 * @return True if the underlying socket is connected, false otherwise
	 */
	public boolean isConnected();

}