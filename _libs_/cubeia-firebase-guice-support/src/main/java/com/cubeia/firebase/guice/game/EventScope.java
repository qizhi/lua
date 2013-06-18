/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.game;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableGameState;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.name.Names;

/**
 * Scope implementation for game event execution. Basically it
 * seeds members from the table given an execution, more precisely
 * it binds the following objects for each event execution:
 * 
 * <pre>
 *   TableGameState
 *   ExtendedDetailsProvider
 *   TableMetaData
 *   GameNotifier
 *   TablePlayerSet
 *   TableScheduler
 *   TournamentNotifier
 *   TableWatcherSet
 *   Table
 * </pre>
 * 
 * @author larsan
 */
public class EventScope implements Scope {
	
	private final Logger log = Logger.getLogger(getClass());

	private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
		public Object get() {
			throw new IllegalStateException("If you got here then it means that" +
					" your code asked for scoped object which should have been" +
					" explicitly seeded in this scope by calling" +
					" EventScope.seed(), but was not.");
		}
	};
	
	
	// --- INSTANCE MEMBERS --- //
	
	private final ThreadLocal<EntryKeeper> values = new ThreadLocal<EntryKeeper>();
	
	public EventScope() { }

	/**
	 * @param table Table bound to execution scope, must not be null
	 */
	public void enter(Integer playerId, Table table) {
		if(values.get() == null) {
			// Only seed values if we are outside a nested call
			values.set(new EntryKeeper());
			seedTableMembers(playerId, table);
		}
		values.get().incrementInvocations();
	}

	public void exit() {
		if(values.get() == null) {
			log.warn("Trying to clear non-existing scoped values. This indicates we have not be able to roll out of a nested call successfully");
		} else {
			if (values.get().decrementInvocations() <= 0) {
				values.remove();
			}
		}
	}

	/**
	 * @param key Object key, must not be null
	 * @param value Value, if null it will be ignored
	 */
	public <T> void seed(Key<T> key, T value) {
		if(value == null) return;
		Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
		if(scopedObjects.containsKey(key)) {
			log.warn("Re-seeding object for key: " + key);
		}
		scopedObjects.put(key, value);
	}

	/**
	 * @param clazz Object key, must not be null
	 * @param value Value, if null it will be ignored
	 */
	public <T> void seed(Class<T> clazz, T value) {
		seed(Key.get(clazz), value);
	}

	public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
		return new Provider<T>() {
			public T get() {
				Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
				@SuppressWarnings("unchecked")
				T current = (T) scopedObjects.get(key);
				if (current == null && !scopedObjects.containsKey(key)) {
					current = unscoped.get();
					scopedObjects.put(key, current);
				}
				return current;
			}
		};
	}

	@SuppressWarnings({"unchecked"})
	public static <T> Provider<T> seededKeyProvider() {
		return (Provider<T>) SEEDED_KEY_PROVIDER;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
		Map<Key<?>, Object> scopedObjects = values.get().values;
		if (scopedObjects == null) {
			throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
		}
		return scopedObjects;
	}
	
	private void seedTableMembers(Integer playerId, Table table) {
		seed(TableGameState.class, table.getGameState());
		seed(ExtendedDetailsProvider.class, table.getExtendedDetailsProvider());
		seed(TableMetaData.class, table.getMetaData());
		seed(GameNotifier.class, table.getNotifier());
		seed(TablePlayerSet.class, table.getPlayerSet());
		seed(TableScheduler.class, table.getScheduler());
		seed(TournamentNotifier.class, table.getTournamentNotifier());
		seed(TableWatcherSet.class, table.getWatcherSet());
		seed(Table.class, table);
		
		/*
		 * #682: Inject player ID, or -1 if not available
		 */
		seed(Key.get(Integer.class, Names.named("playerId")), playerId);
		
		/*
		 * #725: Inject table ID as well
		 */
		seed(Key.get(Integer.class, Names.named("tableId")), table.getId());
	}
	
	
	private static class EntryKeeper {
		
		private Map<Key<?>, Object> values = new HashMap<Key<?>, Object>(10);
		
		/** Keeps track of subsequent invocations so we don't clear the thread local on nested calls. */
		private int invocationCounter = 0;
		
		protected void incrementInvocations() {
			invocationCounter++;
		}
		protected int decrementInvocations() {
			invocationCounter--;
			return invocationCounter;
		}
	}
}
