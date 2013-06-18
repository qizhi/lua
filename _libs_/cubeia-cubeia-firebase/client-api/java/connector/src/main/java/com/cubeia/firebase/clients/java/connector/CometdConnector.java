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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ObjectNode;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.common.HashMapMessage;
import org.cometd.common.JacksonJSONContextClient;
import org.eclipse.jetty.client.HttpClient;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

public class CometdConnector extends ConnectorBase {
	
	private static final long EXCHANGE_WAIT = 200;
	private static final String CHANNEL = "/service/client";
	private static final String CLASS_ID_PROPERTY = "classId";
	
	private final ProtocolObjectFactory factory = new ProtocolObjectFactory();

	private final String host;
	private final String path;
	private final int port;
	
	private BayeuxClient client;
	private final HttpClient httpClient;
	
	private final Exchange exchange = new Exchange();

	public CometdConnector(HttpClient client, String host, int port, String path, boolean useHandshake, int handshakeSignature) {
		super(useHandshake, handshakeSignature);
		httpClient = client;
		this.host = host;
		this.port = port;
		this.path = path;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void send(ProtocolObject packet) {
		String id = UUID.randomUUID().toString();
		exchange.register(id);
		client.getChannel(CHANNEL).publish(packet, id); // THIS IS ASYNC!!
		try {
			exchange.waitFor(id);
		} catch (InterruptedException e) {
			// TODO ... ?
			e.printStackTrace();
		}
	}

	@Override
	public void connect() throws IOException, GeneralSecurityException {
		try {
			ClientTransport transport = getTransport();
			client = new Client(createUri(), transport);
			client.handshake(createHandshakeMap());
			boolean handshaken = client.waitFor(1000, BayeuxClient.State.CONNECTED);
			if (!handshaken) {
				this.client = null; // DISCONNECTED
			    throw new RuntimeException("Failed handshake");
			} else {
				client.getChannel(CHANNEL).subscribe(new Listener());
			}
		} catch (Exception e) {
			throw new IOException("failed to connect cometd", e);
		}
	}

	@Override
	public void disconnect() {
		client.disconnect();
	}

	@Override
	public boolean isConnected() {
		return client != null && client.isConnected();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private Map<String, Object> createHandshakeMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		if(useHandshake) {
			map.put("ext", Collections.singletonMap("handshakeSignature", handshakeSignature));
		}
		return map;
	}
	
	private String createUri() {
		return "http://" + host + ":" + port + path;
	}

	private LongPollingTransport getTransport() {
		HashMap<String, Object> options = new HashMap<String, Object>();
		options.put(ClientTransport.JSON_CONTEXT, new JsonContext());
		return LongPollingTransport.create(options, httpClient);
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class JsonContext extends JacksonJSONContextClient {
		
		public JsonContext() {
			ObjectMapper mapper = getObjectMapper();
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.getSerializationConfig().addMixInAnnotations(ProtocolObject.class, StyxClassIdJsonMixIn.class);
			SimpleModule module = new SimpleModule("ProtocolDeserializer Module", new Version(1, 0, 0, null));  
			module.addDeserializer(HashMapMessage.class, new StdDeserializer<HashMapMessage>(HashMapMessage.class) {
				
				@Override
				@SuppressWarnings("unchecked")
				public HashMapMessage deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
					ObjectMapper mapper = (ObjectMapper) jp.getCodec();  
				    ObjectNode root = (ObjectNode) mapper.readTree(jp); 
				    HashMapMessage s = new HashMapMessage();
				    if(hasProtocolObjectData(root)) {
				    	/*
				    	 * We have a data node with a classId which makes us comfortable
				    	 * this is a protocol object. So 1) remove the data; 2) find object
				    	 * class from classId; 3) create object; and 4) put back again...
				    	 */
				    	JsonNode node = root.remove("data");
				    	int classId = node.get("classId").asInt();
					    Class<? extends ProtocolObject> cl = factory.create(classId).getClass();
					    ProtocolObject p = mapper.readValue(node, cl);
					    s.setData(p);
				    }
				    /*
				     * Read the remainder as an ordinary map and put all into the
				     * server messages
				     */
				    HashMap<String, ?> values = mapper.readValue(root, HashMap.class);
				    s.putAll(values);
				    return s;
				}

				// --- PRIVATE METHODS --- //
				
				/*
				 * Return true if the object has a data field which has a "classId" field 
				 */
				private boolean hasProtocolObjectData(ObjectNode root) {
					JsonNode dataNode = root.get("data");
					JsonNode idNode = (dataNode == null ? null : dataNode.get(CLASS_ID_PROPERTY));
					return idNode != null;
				}
			});
			mapper.registerModule(module);
		}
	}
	
	/*
	 * Treat the classId as a property when serializing...
	 */
	private interface StyxClassIdJsonMixIn {

		@JsonProperty(CLASS_ID_PROPERTY) public int classId();
		
	}
	
	private class Client extends BayeuxClient {
		
		public Client(String uri, ClientTransport trans) {
			super(uri, trans);
		}
		
		@Override
		public void onSending(Message[] messages) {
			for (Message m : messages) {
				exchange.release(m.getId());
			}
		}
		
		@Override
		public void onFailure(Throwable x, Message[] messages) {
			for (Message m : messages) {
				exchange.release(m.getId());
			}
		}
	}
	
	private class Listener implements MessageListener {
		
		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			// System.out.println("ON_MESSAGE (" + Thread.currentThread().getName() + "): " + message.getData());
			ProtocolObject o = (ProtocolObject) message.getData();
			doFinalDispatch(o);
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
	
	private class Exchange {
		
		private Map<String, CountDownLatch> latches = new ConcurrentHashMap<String, CountDownLatch>();
		
		public void waitFor(String id) throws InterruptedException {
			CountDownLatch latch = latches.get(id);
			if(latch != null) {
				latch.await(EXCHANGE_WAIT, MILLISECONDS);
			}
		}

		public void register(String id) {
			latches.put(id, new CountDownLatch(1));
		}

		public void release(String id) {
			if(id != null) {
				CountDownLatch latch = latches.remove(id);
				if(latch != null) {
					latch.countDown();
				}
			}
		}
	}
}
