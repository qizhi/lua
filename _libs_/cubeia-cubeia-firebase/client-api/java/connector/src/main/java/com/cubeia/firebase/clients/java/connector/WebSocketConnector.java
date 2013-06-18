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

import static com.cubeia.firebase.clients.java.connector.HttpConstants.HANDSHAKE_HTTP_HEADER;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

import se.cgbystrom.netty.http.websocket.WebSocketCallback;
import se.cgbystrom.netty.http.websocket.WebSocketClient;
import se.cgbystrom.netty.http.websocket.WebSocketClientFactory;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxJsonSerializer;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

// TODO Add Encryption
public class WebSocketConnector extends ConnectorBase {

	
	
	private final static WebSocketClientFactory factory = new WebSocketClientFactory();
	private final static StyxJsonSerializer serializer = new StyxJsonSerializer(new ProtocolObjectFactory());
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final String host;
	private final int port;
	private final String path;
	
	// private int maxTextMessageSize = DEF_MAX_MESSAGE_SIZE;
	
	/*private WebSocketClient client;
	private WebSocketClientFactory factory;
	private WebSocket.Connection connection;*/
	
	private EventHandler eventHandler;
	private WebSocketClient client;
	private ChannelFuture channelConnectionFuture;
	
	public WebSocketConnector(String host, int port, String path, boolean useHandshake, int handshakeSignature) {
		super(useHandshake, handshakeSignature);
		this.host = host;
		this.port = port;
		this.path = path;
	}
	
	public WebSocketConnector(String host, int port, String path) {
		this(host, port, path, false, -1);
	}

	@Override
	public void send(ProtocolObject packet) {
		try {
			eventHandler.latch.await();
			String json = serializer.toJson(packet);
			ChannelFuture fut = client.send(new DefaultWebSocketFrame(json));
			fut.await(); // OOOPS?!
			// connection.sendMessage(json);
		} catch(Exception e) {
			log.error("failed to send event", e);
		}
	}

	@Override
	public void connect() throws IOException, GeneralSecurityException {
		try {
			eventHandler = new EventHandler();
			client = factory.newClient(createUri(), eventHandler);
			channelConnectionFuture = client.connect();
		} catch (Exception e) {
			throw new IOException("failed to connect websocket", e);
		}
	}

	@Override
	public void disconnect() {
		try {
			client.disconnect();
			client = null;
			/*connection.close();
			factory.stop();*/
		} catch (Exception e) {
			log.error("failed to disconnect websocket", e);
		}
	}

	@Override
	public boolean isConnected() {
		try {
			channelConnectionFuture.await(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return client != null && channelConnectionFuture.isSuccess(); 
	}

	/*public void setMaxTextMessageSize(int maxTextMessageSize) {
		this.maxTextMessageSize = maxTextMessageSize;
	}
	
	public int getMaxTextMessageSize() {
		return maxTextMessageSize;
	}*/
	
	
	// --- PRIVATE METHODS --- //
	
	private URI createUri() throws URISyntaxException {
		if(useHandshake) {
			return new URI("ws://" + host + ":" + port + path + "?" + HANDSHAKE_HTTP_HEADER + "=" + handshakeSignature);
		} else {
			return new URI("ws://" + host + ":" + port + path);
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class EventHandler implements WebSocketCallback {

		private CountDownLatch latch = new CountDownLatch(1);
		
		@Override
		public void onConnect(WebSocketClient cl) {
			latch.countDown();
		}

		@Override
		public void onDisconnect(WebSocketClient cl) {
			// TODO ?
		}

		@Override
		public void onError(Throwable th) {
			// TODO ?
		}

		@Override
		public void onMessage(WebSocketClient cl, WebSocketFrame frame) {
			String json = frame.getTextData();
			if(json != null && json.length() > 0) {
				if(json.trim().startsWith("{")) {
					doFinalDispatch(serializer.fromJson(json));
				} else {
					for (ProtocolObject o : serializer.fromJsonList(json)) {
						doFinalDispatch(o);
					}
				}
			}
		}
		
		private void doFinalDispatch(final ProtocolObject packet) {
			dispatcher.submit(new Runnable() {
			
				public void run() {
					for (PacketListener v : listeners) {
						v.packetRecieved(packet);
					}
				}
			});
		}
	}
	
	
	/*private class EventHandler implements OnTextMessage {

		private CountDownLatch latch = new CountDownLatch(1);
		
		@Override
		public void onClose(int closeCode, String message) {
			// TODO ?
		}

		@Override
		public void onMessage(String json) {
			if(json != null && json.length() > 0) {
				if(json.trim().startsWith("{")) {
					doFinalDispatch(serializer.fromJson(json));
				} else {
					for (ProtocolObject o : serializer.fromJsonList(json)) {
						doFinalDispatch(o);
					}
				}
			}
		}
		
		private void doFinalDispatch(final ProtocolObject packet) {
			dispatcher.submit(new Runnable() {
			
				public void run() {
					for (PacketListener v : listeners) {
						v.packetRecieved(packet);
					}
				}
			});
		}

		@Override
		public void onOpen(Connection connection) {
			latch.countDown();
		}
	}*/
}
