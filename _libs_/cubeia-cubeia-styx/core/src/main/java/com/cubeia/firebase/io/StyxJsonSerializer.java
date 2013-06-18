package com.cubeia.firebase.io;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

/**
 * This is a JSON wire protocol serializer/deserializer. All protocol
 * object instances will contain a "classId" property which maps to the
 * protocol definition.
 * 
 * <p>All methods fails with an illegal argument exception if the JSON is
 * malformed or does not correspond correctly to the protocol objects. However,
 * unknown properties are ignored.
 * 
 * @author Lars J. Nilsson
 */
public class StyxJsonSerializer {

	private static final String CLASS_ID_PROPERTY = "classId";
	
	private final ObjectFactory factory;
	private final ObjectMapper objectMapper;
	
	/**
	 * @param factory Object factory, must not be null if factory is used for deserializing
	 */
	public StyxJsonSerializer(ObjectFactory factory) {
		this.objectMapper = createObjectMapper();
		this.factory = factory;
	}
	
	/**
	 * @param obj Object to pack, must not be null
	 * @return A JSON representation of the object, never null
	 */
	public String toJson(final ProtocolObject obj) {
		return doObjectMapperCall(new ObjectMapperCall<String>() {
			
			@Override
			public String call(ObjectMapper mapper) {
				try {
					return mapper.writeValueAsString(obj);
				} catch (Exception e) {
					throw new RuntimeException("error marshalling to json", e);
				} 
			}
		});
	}
	
	/**
	 * @param obj Object list to pack, must not be null
	 * @return A JSON representation of the object list, never null
	 */
	public String toJsonList(final List<ProtocolObject> obj) {
		return doObjectMapperCall(new ObjectMapperCall<String>() {
			
			@Override
			public String call(ObjectMapper mapper) {
				try {
					return mapper.writeValueAsString(obj);
				} catch (Exception e) {
					throw new RuntimeException("error marshalling to json", e);
				}
			}
		});
	}
	
	/**
	 * @param json JSON to unmarshall, must not be null
	 * @return A protocol object, never null
	 */
	public ProtocolObject fromJson(final String json) {
		return doObjectMapperCall(new ObjectMapperCall<ProtocolObject>() {
			
			@Override
			public ProtocolObject call(ObjectMapper mapper) {
				try {
					return mapper.readValue(json, ProtocolObject.class);
				} catch (Exception e) {
					throw new RuntimeException("error unmarshalling from json", e);
				}
			}
		});
	}
	
	/**
	 * @param json JSON array to unmarshall, must not be null
	 * @return A list of protocol objects, never null
	 */
	public List<ProtocolObject> fromJsonList(final String json) {
		return doObjectMapperCall(new ObjectMapperCall<List<ProtocolObject>>() {
			
			@Override
			public List<ProtocolObject> call(ObjectMapper mapper) {
				try {
					return mapper.readValue(json, new TypeReference<List<ProtocolObject>>() { });
				} catch (Exception e) {
					throw new RuntimeException("error unmarshalling from json", e);
				}		
			}
		});
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private ObjectMapper createObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		// Add a module with the deserializer for proper class ID resolution. 
		SimpleModule module = new SimpleModule("ProtocolDeserializer Module", new Version(1, 0, 0, null));  
		module.addDeserializer(ProtocolObject.class, new StdDeserializer<ProtocolObject>(ProtocolObject.class) {

			@Override
			public ProtocolObject deserialize(JsonParser jp, DeserializationContext ctxt) {
				ObjectMapper mapper = (ObjectMapper) jp.getCodec();  
			    ObjectNode root;
				try {
					root = (ObjectNode) mapper.readTree(jp);
					/*
					 * Get the class id and the refer to the factory to map the
					 * concrete class.
					 */
					int classId = root.get(CLASS_ID_PROPERTY).asInt();
					Class<? extends ProtocolObject> cl = factory.create(classId).getClass();
					return mapper.readValue(root, cl);
				} catch (Exception e) {
					throw new RuntimeException("error unmarshalling from json", e);
				} 
			}
		}); 
		// Add mix-in to mimic a real property for the class ID
		om.getSerializationConfig().addMixInAnnotations(ProtocolObject.class, StyxClassIdJsonMixIn.class);
		// Do not fail on unknown properties
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.enable(SerializationConfig.Feature.WRITE_ENUMS_USING_INDEX);		
		om.registerModule(module); 
		return om;
	}
	
	private <T> T doObjectMapperCall(ObjectMapperCall<T> call) {
		try {
			return call.call(objectMapper);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to handle JSON in object mapper", e);
		} 
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private interface StyxClassIdJsonMixIn {

		@JsonProperty(CLASS_ID_PROPERTY) public int classId();
		
	}
	
	private interface ObjectMapperCall<T> {
		public T call(ObjectMapper mapper) throws JsonProcessingException, JsonMappingException, IOException;
	}
}
