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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

/**
 * Needs a Firebase running at ports 4123 for binary and 8080 for http.
 * 
 * @author Fredrik
 *
 */
public class ConnectorTestIntegrationTest {

	@Test
	public void testBinaryDisconnect() throws IOException, GeneralSecurityException, InterruptedException {
		SocketConnectorFactory socketConnectorFactory = new SocketConnectorFactory("localhost", 4123);
		Connector connector = socketConnectorFactory.createConnector();
		
		connector.connect();
		assertTrue(connector.isConnected());
		
		connector.disconnect();
		assertFalse(connector.isConnected());
	}
	
	@Test
	public void testWebsocketDisconnect() throws IOException, GeneralSecurityException, InterruptedException {
		WebSocketConnectorFactory socketConnectorFactory = new WebSocketConnectorFactory("localhost", 8080, "/socket");
		Connector connector = socketConnectorFactory.createConnector();
		connector.connect();
		
		assertTrue(connector.isConnected());
		
		Thread.sleep(50); // Need some time to setup connection
		
		connector.disconnect();
		assertFalse(connector.isConnected());
	}
	
	@Test
	public void testWebsocketBadConnection() throws IOException, GeneralSecurityException, InterruptedException {
		WebSocketConnectorFactory socketConnectorFactory = new WebSocketConnectorFactory("192.168.4.33", 45784, "/knark");
		Connector connector = socketConnectorFactory.createConnector();
		connector.connect();
		
		Thread.sleep(50); // Need some time to setup connection
		
		assertFalse(connector.isConnected());
	}
	
	
	@Test
	public void testCometdDisconnect() throws IOException, GeneralSecurityException, InterruptedException {
		CometdConnectorFactory socketConnectorFactory = new CometdConnectorFactory("localhost", 8080, "/cometd");
		socketConnectorFactory.start();
		Connector connector = socketConnectorFactory.createConnector();
		connector.connect();
		
		assertTrue(connector.isConnected());
		
		Thread.sleep(50); // Need some time to setup connection
		
		connector.disconnect();
		assertFalse(connector.isConnected());
	}
	
	@Test (expected=IOException.class)
	public void testCometdBadConnection() throws IOException, GeneralSecurityException {
		CometdConnectorFactory socketConnectorFactory = new CometdConnectorFactory("192.168.4.33", 45784, "/knark");
		socketConnectorFactory.setConnectTimeout(100);
		socketConnectorFactory.start();
		Connector connector = socketConnectorFactory.createConnector();
		connector.connect();
	}
	
	
}
