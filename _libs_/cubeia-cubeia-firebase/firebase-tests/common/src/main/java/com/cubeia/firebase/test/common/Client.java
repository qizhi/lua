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
package com.cubeia.firebase.test.common;

import static com.cubeia.firebase.test.common.Constants.COMETD_PATH;
import static com.cubeia.firebase.test.common.Constants.SOCKET_PATH;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.clients.java.connector.CometdConnectorFactory;
import com.cubeia.firebase.clients.java.connector.Connector;
import com.cubeia.firebase.clients.java.connector.ConnectorFactory;
import com.cubeia.firebase.clients.java.connector.Encryption;
import com.cubeia.firebase.clients.java.connector.PacketListener;
import com.cubeia.firebase.clients.java.connector.SecurityConfig;
import com.cubeia.firebase.clients.java.connector.SocketConnectorFactory;
import com.cubeia.firebase.clients.java.connector.WebSocketConnectorFactory;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.LogoutPacket;
import com.cubeia.firebase.test.common.rules.Builder;
import com.cubeia.firebase.test.common.rules.Expect;
import com.cubeia.firebase.test.common.rules.Expect.Action;
import com.cubeia.firebase.test.common.rules.impl.ExpectFailure;

/**
 * This class simulates a client connecting to a Firebase server. It 
 * has the ability to send and "expect" returns packets.
 * 
 * A normal usage pattern looks like this:
 * 
 * <pre>
 *   Client cl = new Client();
 *   cl.connect(hostname, port);
 *   cl.login(username, password, true);
 *   // do send + expect here
 *   cl.logout(true);
 * </pre>
 * 
 * <b>Sending</b><br />
 * The sending operation is synchronous in that the same thread writes to the TCP socket. 
 * This class only allows for Firebase protocol objects to be sent. In order to send game
 * specific objects, please use a subclass.
 * 
 * <p><b>Expecting></b><br />
 * The act of "expecting" a package is to wait for a significant event from the server. Which is
 * most like one or more packages resulting from a user operation. The wait is for <em>significant</em>
 * events as most {@link Expect expect} types will ignore non-matching packages. 
 * 
 * <p><b>Encryption</b><br />
 * Set one of the following system properties to "true" to specify encryption:
 * 
 * <pre>
 * 	"firebase.systest.ssl" - for normal SSL
 *  "firebase.systest.naive-ssl" - naive SSL accepting any server certificate
 *  "firebase.systest.native" - for native Firebase encryption
 * </pre>
 * 
 * @author larsan
 */
public class Client {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	protected long expectTimeout = 2000;
	
	protected int playerId = -1;
	
	protected Connector connector;
	protected BlockingQueue<ProtocolObject> packets;
	
	protected final ConnectorType connectorType;
	protected ConnectorFactory connectorFactory;

	private final String host;
	private final int port;
	
	
	/**
	 * @param connectorType Type of connector to use, must not be null
	 */
	public Client(ConnectorType connectorType, String host, int port) {
		this.connectorType = connectorType;
		this.host = host;
		this.port = port;
	}

	/**
	 * Connect the client to a remote host. This will initiate incoming packet queues 
	 * and the TCP connection.
	 * 
	 * @param connectorType Type of connector to use, or null for default
	 * @param hostname Host to connect to, address or IP, must not be null
	 * @param port Port to connect to, must be > 0
	 * @param handshakeSignature Handshake to use, or -1 for no handshake
	 * @throws UnknownHostException If the host cannot be resolved
	 * @throws IOException On general IO errors
	 * @throws GeneralSecurityException If an SSL exception occurs
	 */
	public void connect(int handshakeSignature) throws UnknownHostException, IOException, GeneralSecurityException {
		Connector connector = createConnector(handshakeSignature, getEncryption());
		connect(connector);
	}

	/**
	 * Connect an existing connector. the connector will have a listener attached
	 * and will then be connected. 
	 * 
	 * @param connector Connector to use, must not be null
	 * @throws IOException On general IO errors
	 * @throws GeneralSecurityException If an SSL exception occurs
	 */
	public void connect(Connector connector) throws IOException, GeneralSecurityException {
		this.connector = connector;
		packets = new ArrayBlockingQueue<ProtocolObject>(1024);
		connector.addListener(new PacketListener() {
		
			public void packetRecieved(ProtocolObject packet) {
				packets.offer(packet);
			}
		});
		connector.connect();
	}

	/**
	 * Connect the client to a remote host. This will initiate incoming packet queues 
	 * and the TCP connection. This method of connection will not use a handshake.
	 * 
	 * @throws UnknownHostException If the host cannot be resolved
	 * @throws IOException On general IO errors
	 * @throws GeneralSecurityException If an SSL exception occurs
	 */
	public void connect() throws UnknownHostException, IOException, GeneralSecurityException {
		connect(-1);
	}

	/**
	 * Disconnect from the server.
	 */
	public void disconnect() {
		if (connector != null) {
			connector.disconnect();
		}
		if (connectorFactory != null) {
			connectorFactory.stop();
		}
	}

	/**
	 * Send a Firebase packet to the server. This is a blocking 
	 * operation. This method throws an exception if the object is
	 * not a Firebase protocol object.
	 * 
	 * @param p Packet to send, must not be null
	 * @throws IllegalArgumentException If the packet is not a Firebase protocol object
	 */
	public void sendFirebasePacket(ProtocolObject p) {
		Class<?> c = p.getClass();
		if (!isFirebasePacket(c)) {
			throw new IllegalArgumentException("Packet " + c.getPackage().getName() + " is not a Firebase packet");
		}
		connector.send(p);
	}
	
	/**
	 * @return True if the client is connected, false otherwise
	 */
	public boolean isConnected() {
		return (connector != null && connector.isConnected());
	}
	
	/**
	 * @return True if the client is connected (ie has a player id other than -1), false otherwise
	 */
	public boolean isLoggedIn() {
		return (this.playerId != -1);
	}

	/**
	 * Expect a single Firebase packet with the default expect 
	 * {@link #getExpectTimeout() timeout}. This method throws an exception
	 * if the class is not a Firebase protocol class.
	 * 
	 * @param <T> Type of the class / protocol object
	 * @param c Class to expect, must not be null
	 * @return A Firebase packet, never null
	 * @throws IllegalArgumentException If the packet is not a Firebase protocol object
	 * @throws ExpectFailure If the expect times out
	 */
	@SuppressWarnings("unchecked")
	public <T extends ProtocolObject> T expectFirebasePacket(Class<T> c) {
		assertPackage(c);		
		return (T) expect(Builder.expect(c), expectTimeout);
	}
	
	/**
	 * Expect on the incoming message queue. This method will return the 
	 * {@link Expect#result() result} of the expect object. If the expect 
	 * times out, a {@link TestFailure} is thrown.
	 * 
	 * @param e Expect to use, must not be null
	 * @param timeout Timeout to wait in millis, must be > 0
	 * @return The result of the expect
	 * @throws ExpectFailure If the expect times out
	 */
	public Object expect(Expect e, long timeout) {
		ProtocolObject packet = null;
		Action action = Action.PASS_THROUGH;
		long time = System.currentTimeMillis() + timeout;
		while(action == Action.PASS_THROUGH) {
			long wait = time - System.currentTimeMillis();
			packet = (wait > 0 ? doPoll(wait) : null);
			if(packet == null) {
				throw new ExpectFailure(e, timeout);
			} else {
				action = e.accept(packet);
				if(action == Action.FAIL) {
					throw new ExpectFailure(e);
				} else if(action == Action.PASS_THROUGH) {
					log.debug("Skipped packet during expect: " + packet);
				}
			}
		}
		return e.result();
	}
	
	/**
	 * Expect on the incoming message queue with the default expect 
	 * {@link #getExpectTimeout() timeout}. This method will return the 
	 * {@link Expect#result() result} of the expect object. If the expect 
	 * times out, a {@link TestFailure} is thrown.
	 * 
	 * @param e Expect to use, must not be null
	 * @return The result of the expect
	 * @throws ExpectFailure If the expect times out
	 */
	public Object expect(Expect e) {
		return this.expect(e, expectTimeout);
	}

	/**
	 * @param pid Id of the the player this client represent
	 */
	public void setPlayerId(int pid) {
		this.playerId = pid;
	}

	/**
	 * @return Id of the the player this client represent, or -1 if not set / not logged in
	 */
	public int getPlayerId() {
		return playerId;
	}
	
	/**
	 * @return The default time-out value to use in millis
	 */
	public long getExpectTimeout() {
		return expectTimeout;
	}
	
	/**
	 * @param expectTimeout The default time-out value to use in millis, must be > 0
	 */
	public void setExpectTimeout(long expectTimeout) {
		this.expectTimeout = expectTimeout;
	}
	
	
	// --- STANDARD ACTIONS --- //
	
	/**
	 * This method send a logout packet to the server. If the
	 * parameter is true the sent packet will request a leave for
	 * all tables. This method does not disconnect automatically.
	 * 
	 * @param leaveTables True to leave all tables, false otherwise
	 */
	public void logout(boolean leaveTables) {
		sendFirebasePacket(new LogoutPacket(leaveTables));
		// disconnect();
	}

	/**
	 * Login as a player. If the boolean parameter is true, this method will 
	 * automatically expect a login response and use a TestNG assert to verify
	 * the the login was successful.
	 * 
	 * @param user User name, must not be null
	 * @param password Password, must not be null
	 * @param expectResponse True to expect/verify a response automatically
	 */
	public void login(String user, String password, boolean expectResponse) {
		sendFirebasePacket(new LoginRequestPacket(user, password, 0, new byte[] { } ));
		if(expectResponse) {
			LoginResponsePacket login = expectFirebasePacket(LoginResponsePacket.class);
			assertEquals(Enums.ResponseStatus.OK, login.status);
			setPlayerId(login.pid);
		}
	}

	
	// --- PROTECTED METHODS --- //
	
	protected ProtocolObject doPoll(long timeout) {
		try {
			return packets.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new TestFailure("Unexpected interrupt", e);
		}
	}
	
	protected boolean isFirebasePacket(Class<?> c) {
		return "com.cubeia.firebase.io.protocol".equals(c.getPackage().getName());
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private Connector createConnector(int handshakeSignature, Encryption encryption) throws IOException, GeneralSecurityException {
		SecurityConfig conf = new SecurityConfig(handshakeSignature != -1, handshakeSignature, encryption);
		connectorFactory = createConnectorFactory(host, port);
		connectorFactory.start();
		return connectorFactory.createConnector(conf);
	}
	
	private ConnectorFactory createConnectorFactory(String host, int port) {
		switch(connectorType) {
			case SOCKET : return new SocketConnectorFactory(host, port);
			case WEB_SOCKET : return new WebSocketConnectorFactory(host, port, SOCKET_PATH);
			case COMETD: return new CometdConnectorFactory(host, port, COMETD_PATH);
			default : {
				throw new RuntimeException("Missing implementation for connector " + this);
			}
		}
	}
	
	private void assertPackage(Class<?> c) {
		if (!isFirebasePacket(c)) {
			throw new IllegalArgumentException("Expected packet " + c.getPackage().getName() + " is not a Firebase packet");
		}
	}
	
	private Encryption getEncryption() {
		if(Constants.USE_NAIVE_SSL) {
			return Encryption.NAIVE_SSL;
		} else if(Constants.USE_SSL) {
			return Encryption.SSL;
		} else if(Constants.USE_NATIVE) {
			return Encryption.FIREBASE_NATIVE;
		} else {
			return Encryption.NONE;
		}
	}
}