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
package com.cubeia.firebase.mtt.state.trans;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.Serializer;

/**
 * Delta change class for the game state object.
 */
public final class MttStateDeltaChange implements DeltaChange {

    private static transient Logger log = Logger.getLogger(MttStateDeltaChange.class);
    
	private final Object next;
	private final VersionedStateData data;
	private final ClassLoader loader;
	
	private final Serializer ser = new Serializer();
	
	private static final boolean FORCE_DESERIALIZE = Boolean.parseBoolean(System.getProperty("com.cubeia.forceDeserialization"));

	private final long objectWarnSize;	
	
	MttStateDeltaChange(VersionedStateData data, Object next, ClassLoader loader, long objectWarnSize) {
		this.objectWarnSize = objectWarnSize;
		this.loader = loader;
		this.next = next;
		this.data = data;
	}
	
	MttStateDeltaChange(VersionedStateData data, ClassLoader loader, long objectWarnSize) {
		this.loader = loader;
		this.objectWarnSize = objectWarnSize;
		/*
		 * Here's some magic, as we're only executing a table on one
		 * node we're safe to cache an already serialized transient object 
		 */
		Object test = data.getCachedState();
		if (test != null && !forceDeserialize()) {
            next = test;
        } else {
        	/*
        	 * If the cached game state was null, or if forced deserialization is enabled,
        	 * deserialize the game state.
        	 */
            next = deserialize(data.getState());
        }
		this.data = data;
	}
	
	/**
	 * Checks if we should force deserialization of the game state.
	 * 
	 * @return <code>true</code> if we should force deserialization, <code>false</code> otherwise.
	 */
	private boolean forceDeserialize() {
		return FORCE_DESERIALIZE;
	}	
	
	public Object getNext() {
		return next;
	}
	
	public void rollback() {
		/*
		 * Force the cache clear as we're not certain of 
		 * the object state.
		 */
		data.setCachedState(null);
	}

	public void commit() {
		if (next == null) {
			data.setCachedState(null);
			data.setState(null);
		} else {
			byte[] bytes = serialize(next);
			if (bytes != null) {
				/*
				 * Here's some magic, as we're only executing a table on one
				 * node we're safe to cache an already serialized transient object 
				 */
				data.setCachedState((MTTState)next);
				data.setState(bytes);
				
				if ((objectWarnSize != -1 && bytes.length > objectWarnSize) && log.isDebugEnabled()) {
                    log.debug("Large serialized tournamentstate detected. Class: "+next.getClass().getName()+" Size: "+bytes.length+" bytes.");
                }
			}
		}
	}
	
	
	// --- PRIVATE METHODS --- ///
	
	private Object deserialize(final byte[] data) {
		if (data == null) {
            return null;
        } else {
			try {
				return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Exception>() {
					public Object invoke() throws Exception {
						return ser.deserialize(data);
					}
				}, loader);
			} catch (Exception e) {
				String msg = "Failed to deserialize game state; Recevied msg: " + e.getMessage();
				Logger.getLogger(getClass()).fatal(msg, e);
				SystemLogger.error(msg);
				return null;
			} 
		}
	}

	private byte[] serialize(Object o) {
		try {
			return ser.serialize(o);
		} catch (IOException e) {
			String msg = "Failed to serialize game state for class '" + o.getClass().getName() + "'; Recevied msg: " + e.getMessage();
			Logger.getLogger(getClass()).fatal(msg, e);
			SystemLogger.error(msg);
			return null;
		}
	}
}