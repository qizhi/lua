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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.transaction.CoreTransactionManager;

public final class TransactionalTableFactory implements TableFactory<FirebaseTable> {
	
	private final static AtomicLong VERSION_COUNTER = new AtomicLong();

	
	
	// --- INSTANCE MEMBERS --- //
	
	private final Map<Integer, ClassLoader> gameLoaders;
	private final CoreTransactionManager manager;



	private final long tableWarnSize;
	
	public TransactionalTableFactory(CoreTransactionManager manager, long tableWarnSize) { 
		this.tableWarnSize = tableWarnSize;
		gameLoaders = new ConcurrentHashMap<Integer, ClassLoader>();
		this.manager = manager;
	}
	
	public TableData createTableData(InternalMetaData meta, int numSeats) {
		Arguments.notNull(meta, "meta");
		long version = VERSION_COUNTER.incrementAndGet();
		return new VersionedTableData(meta, version, numSeats);
	}

	public FirebaseTable createTable(TableData data) {
		Arguments.notNull(data, "data");
		VersionedTableData versioned = (VersionedTableData)data;
		checkTableLock(versioned);
		return create(versioned);
	}
	
	public TableData extractData(FirebaseTable table) {
		Arguments.notNull(table, "table");
		StandardTable tab = (StandardTable)table;
		return tab.getRealData();
	}
	
	public void setGameClassLoader(int gameId, ClassLoader load) {
		if(load != null) this.gameLoaders.put(gameId, load);
		else this.gameLoaders.remove(gameId);
	}

	
	// --- TABLE CALLBACKS --- //
	
	/*
	 * Verify version number.
	 */
	void verify(StandardTable table) { }
	
	/*
	 * Verify version and clean up.
	 */
	void cancel(StandardTable table) { }
	
	/*
	 * Clean up.
	 */
	void committed(StandardTable table) { }
	
	/*
	 * Get a game class loader, or null if not found
	 */
	public ClassLoader getGameClassLoader(int gameId) {
		return gameLoaders.get(gameId);
	}
	
	public long getTableWarnSize() {
		return tableWarnSize;
	}
	
	// --- PRIVATE METHODS --- //

	private FirebaseTable create(VersionedTableData data) {
		return new StandardTable(data, this, manager);
	}
	
	/*
	 * Check that this particular table has a read/write
	 * lock we can use.
	 */
	private void checkTableLock(VersionedTableData data) {
		synchronized(data) {
			Lock lock = data.getTransientLock();
			if(lock == null) {
				data.setTransientLock(new ReentrantLock());
			}
		}
	}
}
