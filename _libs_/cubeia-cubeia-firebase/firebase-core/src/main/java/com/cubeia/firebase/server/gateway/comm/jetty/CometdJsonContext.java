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
package com.cubeia.firebase.server.gateway.comm.jetty;

import java.io.IOException;
import java.util.HashMap;

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
import org.cometd.server.JacksonJSONContextServer;
import org.cometd.server.ServerMessageImpl;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

public class CometdJsonContext extends JacksonJSONContextServer {
	
	private static final String CLASS_ID_PROPERTY = "classId";
	private static final ProtocolObjectFactory factory = new ProtocolObjectFactory();
	
	public CometdJsonContext() {
		ObjectMapper mapper = getObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.getSerializationConfig().addMixInAnnotations(ProtocolObject.class, StyxClassIdJsonMixIn.class);
		SimpleModule module = new SimpleModule("ProtocolDeserializer Module", new Version(1, 0, 0, null));  
		module.addDeserializer(ServerMessageImpl.class, new StdDeserializer<ServerMessageImpl>(ServerMessageImpl.class) {
			
			@Override
			@SuppressWarnings("unchecked")
			public ServerMessageImpl deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				ObjectMapper mapper = (ObjectMapper) jp.getCodec();  
			    ObjectNode root = (ObjectNode) mapper.readTree(jp); 
			    ServerMessageImpl s = new ServerMessageImpl();
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
	
	
	// --- PRIVATE CLASSES --- //
	
	/*
	 * Treat the classId as a property when serializing...
	 */
	private interface StyxClassIdJsonMixIn {

		@JsonProperty(CLASS_ID_PROPERTY) public int classId();
		
	}
}
