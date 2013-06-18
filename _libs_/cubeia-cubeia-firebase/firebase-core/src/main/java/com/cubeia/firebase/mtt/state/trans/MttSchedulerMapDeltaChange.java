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
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.ScheduledAction;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.Serializer;

public final class MttSchedulerMapDeltaChange implements DeltaChange {

	private final Map<UUID, ScheduledAction<?>> next;
	private final VersionedStateData mtt;
	private final ClassLoader loader;
	
	private final Serializer ser = new Serializer();
	
	MttSchedulerMapDeltaChange(VersionedStateData mtt, Map<UUID, ScheduledAction<?>> next, ClassLoader loader) {
		this.loader = loader;
		this.next = next;
		this.mtt = mtt;
	}
	
	MttSchedulerMapDeltaChange(VersionedStateData mtt, ClassLoader loader) {
		this.loader = loader;
		/*
		 * Here's some magic, as we're only executing a table on one
		 * node we're safe to cache an already serialized transient object 
		 */
		Map<UUID, ScheduledAction<?>> test = mtt.getCachedScheduledActions();
		if (test == null) {
            test = deserialize(mtt.getScheduledActions());
        }
		if(test == null) {
			test = new TreeMap<UUID, ScheduledAction<?>>();
		}
		this.next = test;
		this.mtt = mtt;
	}
	
	public Map<UUID, ScheduledAction<?>> getNext() {
		return next;
	}
	
	public void rollback() {
		/*
		 * Force the cache clear as we're not certain of 
		 * the object state.
		 */
		mtt.setCachedScheduledActions(null);
	}

	public void commit() {
		if (next == null || next.size() == 0) {
			mtt.setCachedScheduledActions(null);
			mtt.setScheduledActions(null);
		} else {
			byte[] bytes = serialize(next);
			if (bytes != null) {
				/*
				 * Here's some magic, as we're only executing a table on one
				 * node we're safe to cache an already serialized transient object 
				 */
				mtt.setCachedScheduledActions(next);
				mtt.setScheduledActions(bytes);
			}
		}
	}
	
	
	// --- PRIVATE METHODS --- ///
	
	@SuppressWarnings("unchecked")
	private Map<UUID, ScheduledAction<?>> deserialize(final byte[] data) {
		if (data == null) {
            return null;
        } else {
			try {
				return (Map<UUID, ScheduledAction<?>>)Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Exception>() {
					public Object invoke() throws Exception {
						return ser.deserialize(data);
					}
				}, loader);
			} catch (Exception e) {
				String msg = "Failed to deserialize scheduled actions; Recevied msg: " + e.getMessage();
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
			String msg = "Failed to serialize scheduled actions for class '" + o.getClass().getName() + "'; Recevied msg: " + e.getMessage();
			Logger.getLogger(getClass()).fatal(msg, e);
			SystemLogger.error(msg);
			return null;
		}
	}
}
