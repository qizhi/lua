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
package com.cubeia.firebase.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;


/**
 * Provides serialization through JBoss Serialization which
 * is faster then the Java standard implementation. If needed 
 * a class loader can be supplied, which will be set as the
 * context class loader on de-serializing. 
 *
 * @author Fredrik
 */
public final class Serializer {

	private final ClassLoader deserializer;

	/**
	 * @param contextCl Context class loader to use when de-serializing, may be null
	 */
	public Serializer(ClassLoader contextCl) {
		this.deserializer = contextCl;	
	}
	
	public Serializer() {
		this(null);
	}
	
	/**
	 * @param o Object to serialize, must not be null
	 * @return The serialized bytes, never null
	 * @throws IOException
	 */
	public byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		BufferedOutputStream bo = new BufferedOutputStream(ba);
		JBossObjectOutputStream out = new JBossObjectOutputStream(bo);
		out.writeObject(o);
		out.flush();
		return ba.toByteArray();
	}
	
	/**
	 * @param arr Bytes to deserialize, must not be null
	 * @return The deserialized object, never null
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object deserialize(byte[] arr) throws IOException, ClassNotFoundException {
		if(deserializer == null) {
			return doUnmarshall(arr);
		} else {
			return switchDoUnmarshall(arr);
		}
	}
	
	
	// --- PRIVATE METHODS --- //

	private Object switchDoUnmarshall(final byte[] arr) throws IOException, ClassNotFoundException {
		try {
			/*
			 * Trac ticket #420
			 */
			return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Exception>() {
			
				public Object invoke() throws Exception {
					return doUnmarshall(arr);
				}
			
			}, deserializer);
		} catch (Exception e) {
			if(e instanceof IOException) {
				throw (IOException)e;
			} else if(e instanceof ClassNotFoundException) {
				throw (ClassNotFoundException)e;
			} else {
				Logger.getLogger(getClass()).error("Unexpected exception", e);
				return null;
			}
		}
	}

	private Object doUnmarshall(byte[] arr) throws IOException, ClassNotFoundException {
		ByteArrayInputStream ba = new ByteArrayInputStream(arr);
		BufferedInputStream bi = new BufferedInputStream(ba);
		JBossObjectInputStream in = new JBossObjectInputStream(bi);
		Object o = in.readObject();
		in.close();
		return o;
	}
}
