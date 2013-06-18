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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.defined.Parameter.Type;
import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.Enums.ParameterType;

/**
 * A small utility class for handling parameters and their IO
 * representation. Currently this class can convert {@link Parameter}
 * to {@link Param} and back.
 * 
 * @author Larsan
 */
public final class ParameterUtil {

	private ParameterUtil() { }
	
	/**
	 * This method creates a Param object given a key and a value. If the 
	 * value type is not known, an error will be logged a null returned.
	 * 
	 * @param key Param key, must not be null
	 * @param value Param value, must not be null
	 * @return A new Param, or null if the type is now known
	 */
	public static Param createParam(String key, Object value) {
		/*Param param = null;

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		PacketOutputStream writer = new PacketOutputStream(dataStream);*/
		
		if (value instanceof Integer) {
			return convert(new Parameter<Integer>(key, ((Integer)value).intValue(), Type.INT));
			
			/*param = new Param();
			Integer iValue = (Integer) value;
			
			param.type = BinaryData.intToByte(ParameterType.INT.ordinal());
			param.key = key;
			byte[] bs = BinaryData.intToBytes(iValue);
			param.value = bs;*/
		} else if (value instanceof String) {
			return convert(new Parameter<String>(key, (String)value, Type.STRING));
			
			/*try {
				param = new Param();
				String sValue = String.valueOf(value);
				param.type = BinaryData.intToByte(ParameterType.STRING.ordinal());
				param.key = key;
				param.value = writeString(sValue, byteStream, writer);
			} catch (Exception e) {
				log.error("Could not write string parameter: "+value, e);
			}*/
		} else if (value instanceof Date) {
			return convert(new Parameter<Date>(key, (Date)value, Type.DATE));
			
			/*try {
				param = new Param();
				Date dValue = (Date)value;
				param.type = BinaryData.intToByte(ParameterType.DATE.ordinal());
				param.key = key;
				param.value = BinaryData.dateToBytes(dValue);
			} catch (Exception e) {
				log.error("Could not write string parameter: "+value, e);
			}*/
		} else {
			if (value == null) {
				Logger.getLogger(ParameterUtil.class).error("Parameter value was null for key: "+key);
			} else {
				Logger.getLogger(ParameterUtil.class).error("Unknown parameter type: [key("+key+") - "+value.getClass()+" : "+value+"]");
			}
			return null;
		}
	}
	
	/**
	 * This method converts a Param to a Parameter. It logs an error
	 * and returns null if the parameter type is not known.
	 * 
	 * @param p Param to convert, must not be null
	 * @return A new parameter, or null if type is not known
	 */
	public static Parameter<?> convert(Param p) {
		PacketInputStream pin = new PacketInputStream(ByteBuffer.wrap(p.value));
		try {
			if(p.type == ParameterType.INT.ordinal()) {
				return convertAsInt(p, pin);
			} else if(p.type == ParameterType.DATE.ordinal()) {
				return convertAsDate(p, pin);
			} else if(p.type == ParameterType.STRING.ordinal()) {
				return convertAsString(p, pin);
			} else {
				Logger.getLogger(ParameterUtil.class).error("Unknown parameter type '" + p.type + "'");
				return null;
			}
		} catch(IOException e) {
			Logger.getLogger(ParameterUtil.class).fatal("Failed to convert internal parameter!", e);
			return null;
		}
	}
	
	/**
	 * This method converts a Param to a string Parameter. If the parameter
	 * type is not a string type this method throws an illegal argument exception.
	 * 
	 * @param p Param to convert, must not be null
	 * @return A new string parameter, never null
	 */
	@SuppressWarnings("unchecked")
	public static Parameter<String> convertAsString(Param p) {
		if(p.type != ParameterType.STRING.ordinal()) {
			throw new IllegalArgumentException("Parameter must be of type STRING; Found: " + p.type);
		}
		return (Parameter<String>) convert(p);
	}
	
	/**
	 * This method converts a Param to a date Parameter. If the parameter
	 * type is not a date type this method throws an illegal argument exception.
	 * 
	 * @param p Param to convert, must not be null
	 * @return A new date parameter, never null
	 */
	@SuppressWarnings("unchecked")
	public static Parameter<Date> convertAsDate(Param p) {
		if(p.type != ParameterType.DATE.ordinal()) {
			throw new IllegalArgumentException("Parameter must be of type DATE; Found: " + p.type);
		}
		return (Parameter<Date>) convert(p);
	}
	
	/**
	 * This method converts a Param to an int Parameter. If the parameter
	 * type is not an int type this method throws an illegal argument exception.
	 * 
	 * @param p Param to convert, must not be null
	 * @return A new int parameter, never null
	 */
	@SuppressWarnings("unchecked")
	public static Parameter<Integer> convertAsInt(Param p) {
		if(p.type != ParameterType.INT.ordinal()) {
			throw new IllegalArgumentException("Parameter must be of type INT; Found: " + p.type);
		}
		return (Parameter<Integer>) convert(p);
	}
	
	/**
	 * This method converts a Parameter to a Param. If the type is now
	 * known it will log an error and return null.
	 * 
	 * @param p Parameter to convert, must not be null
	 * @return A new param, or null if the type is not known
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Param convert(Parameter p) {
		
		Param param = new Param();
		param.key = p.getKey();
		Type type = p.getType();
		param.type = intToBytes(type.ordinal(), 4)[3];
		
		/*
		 * This feels heavy weight... /LJN
		 */
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		PacketOutputStream writer = new PacketOutputStream(dataStream);
		
		try {
			if (type == Type.STRING) {
	    		writer.saveString(((Parameter<String>)p).getValue());
	    	} else if (type == Type.INT) {
	    		writer.saveInt(((Parameter<Integer>)p).getValue());
	    	} else if (type == Type.DATE) {
	    		int i = (int)(((Parameter<Date>)p).getValue().getTime() / 1000);
	    		writer.saveInt(i);
	    	} else {
	    		Logger.getLogger(ParameterUtil.class).error("Unknown parameter type '" + type + "'");
	    		return null;
	    	}
			dataStream.flush();
		} catch(IOException e) {
			Logger.getLogger(ParameterUtil.class).fatal("Failed to convert internal parameter!", e);
		}
		
		param.value = byteStream.toByteArray();
		return param;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private static Parameter<String> convertAsString(Param p, PacketInputStream pin) throws IOException {
		if (p.value.length < 3) return null; // Not enough data
		Parameter<String> param = new Parameter<String>(p.key, pin.loadString(), Type.STRING);
		return param;
	}
	
	private static Parameter<Date> convertAsDate(Param p, PacketInputStream pin) throws IOException {
		Date d = new Date(pin.loadInt() * 1000L);
		Parameter<Date> param = new Parameter<Date>(p.key, d, Type.DATE);
		return param;
	}
	
	private static Parameter<Integer> convertAsInt(Param p, PacketInputStream pin) throws IOException {
		Parameter<Integer> param = new Parameter<Integer>(p.key, pin.loadInt(), Type.INT);
		return param;
	}
	
	private static byte[] intToBytes(int value, int b) {
		byte[] bytes = new byte[b];
		for( int i = 0; i < b; i++ ){
			bytes[b-1-i] = (byte)(value&0xff);
			value >>= 8;
		}
		return bytes;
	}
}
