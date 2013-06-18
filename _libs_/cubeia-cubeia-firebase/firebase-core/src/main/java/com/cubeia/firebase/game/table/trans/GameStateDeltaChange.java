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
package com.cubeia.firebase.game.table.trans;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.table.GameObjectSizeRecorder;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.Serializer;

/**
 * Delta change class for the game state object.
 */
final class GameStateDeltaChange implements DeltaChange {

    private static transient Logger log = Logger.getLogger(GameStateDeltaChange.class);
    
	private final Object next;
	private final VersionedTableData table;
	private final ClassLoader loader;
	
	private final Serializer ser = new Serializer();
	
	private static final boolean FORCE_DESERIALIZE = Boolean.parseBoolean(System.getProperty("com.cubeia.forceDeserialization"));
	
	private final long objectWarnSize;
	
	private GameObjectSizeRecorder sizeRecorder;
	
	GameStateDeltaChange(VersionedTableData table, Object next, ClassLoader loader, long objectWarnSize) {
		this.objectWarnSize = objectWarnSize;
		this.loader = loader;
		this.next = next;
		this.table = table;
	}
	
	GameStateDeltaChange(VersionedTableData table, ClassLoader loader, long objectWarnSize) {
		this.loader = loader;
		this.objectWarnSize = objectWarnSize;
		/*
		 * Here's some magic, as we're only executing a table on one
		 * node we're safe to cache an already serialized transient object 
		 */
		Object test = table.getCachedGameState();
		if (test != null && !forceDeserialize()) {
            next = test;
        } else {
        	/*
        	 * If the cached game state was null, or if forced deserialization is enabled,
        	 * deserialize the game state.
        	 */ 
            next = deserialize(table.getStateData());
        }
		this.table = table;
	}
	
	public void setSizeRecorder(GameObjectSizeRecorder sizeRecorder) {
		this.sizeRecorder = sizeRecorder;
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
		table.setCachedGameState(null);
	}

	public void commit() {
		if (next == null) {
			table.setCachedGameState(null);
			table.setStateData(null);
		} else {
			/*if(table.getCachedGameState() != null) {
				table.setCachedGameState(next);
				return;
			}*/
			
			
			byte[] bytes = serialize(next);
			if (bytes != null) {
				/*
				 * Here's some magic, as we're only executing a table on one
				 * node we're safe to cache an already serialized transient object 
				 */
				table.setCachedGameState(next);
				table.setStateData(bytes);
				checkObjectSize(bytes);
				if (sizeRecorder != null) {
					/*
					 * Perhaps the warning above should be a part
					 * of the below call instead...
					 */
					sizeRecorder.recordGameObjectSize(bytes.length);
				}
			}
		}
	}


	// --- PRIVATE METHODS --- ///
	
	private void checkObjectSize(byte[] bytes) {
		if ((objectWarnSize != -1 && bytes.length > objectWarnSize) && log.isDebugEnabled()) {
		    log.debug("Large serialized gamestate detected. Class: "+next.getClass().getName()+" Size: "+bytes.length+" bytes.");
		}
	}
	
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