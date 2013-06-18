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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cubeia.firebase.api.game.table.TableGameState;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.transaction.ResourceException;

public class StandardTable extends FirebaseTable {

	private final VersionedTableData data;
	private final TransactionalTableFactory factory;

	private final CloneTableData clone;
	
	private GameStateDeltaChange currentObject;
	private SchedulerMapDeltaChange currentSchedule;
	
	private final List<DeltaChange> changes = new LinkedList<DeltaChange>();
	
	// Temporary objects
	private GameState gameState;
	private StandardPlayerSet plyrSet;
	private StandardWatcherSet watcherSet;
	private TableSchedulerImpl scheduler;
	private final CoreTransactionManager manager;
	
	StandardTable(VersionedTableData data, TransactionalTableFactory factory, CoreTransactionManager manager) {
		super(data);
		this.manager = manager;
		InternalMetaData meta = data.getMetaData();
		clone = new CloneTableData(meta);
		this.factory = factory;
		this.data = data;
		checkTransaction();
	}

	// --- VERSIONED DATA --- //

	VersionedTableData getVersionedTableData() {
		return data;
	}
	
	
	
	// --- SUBOBJECT ACCESS --- //
	
	SchedulerMapDeltaChange getCurrentSchedule() {
		return currentSchedule;
	}
	
	CloneTableData getDataClone() {
		return clone;
	}
	
	List<DeltaChange> getDeltaChanges() {
		return changes;
	}
	
	VersionedTableData getRealData() {
		return data;
	}
	
	
	// --- TRANSACTION CONTROL --- //

	public boolean begin(long timeout) throws InterruptedException {
		return tryAcquireLock(timeout);
	}
	
	/*public void prepare(long timeout) throws InterruptedException {
		factory.verify(this);
	}*/
	
	public void commit() {
		commitData();
		commitSchedule();
		commitDelta();
		//factory.commited(this);
		//tryReleaseLock();
	}
	
	public void rollback() {
		rollbackData();
		rollbackSchedule();
		rollbackDelta();
	}

	/*public void cancel() {
		factory.cancel(this);
		tryReleaseLock();private void rollbackData() {
		// TODO Auto-generated method stub
		
	}


	}*/

	public void release() {
		tryReleaseLock();
	}

	
	// --- TABLE --- //
	
	public TableGameState getGameState() {
		if(gameState == null) {
			gameState = new GameState();
		}
		return gameState;
	}

	public InternalMetaData getMetaData() {
		return meta;
	}

	public TablePlayerSet getPlayerSet() {
		if(plyrSet == null) {
			plyrSet = new StandardPlayerSet(this);
		}
		return plyrSet;
	}

	public TableScheduler getScheduler() {
		if(scheduler == null) {
			currentSchedule = new SchedulerMapDeltaChange(this.data, getClassLoader());
			scheduler = new TableSchedulerImpl(this);
		}
		return scheduler;
	}

	public TableWatcherSet getWatcherSet() {
		if(watcherSet == null) {
			watcherSet = new StandardWatcherSet(this);
		}
		return watcherSet;
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	/*private void addWatcherDelta(int playerId) {
		CollectionDeltaChange<Integer> delta = new CollectionDeltaChange<Integer>(data.getWatchingPlayers(), Type.ADD, playerId);
		changes.add(delta);
	}
	
	private void addScheduledGameActionDelta(UUID key, ScheduledAction value) {
		MapDeltaChange<UUID, ScheduledAction> delta = new MapDeltaChange<UUID, ScheduledAction>(data.getScheduledActions(), Type.ADD, key, value);
		changes.add(delta);
	}
	
	private void addPlayerDelta(GenericPlayer player, int id) {
		MapDeltaChange<Integer, GenericPlayer> delta = new MapDeltaChange<Integer, GenericPlayer>(data.getPlayers(), Type.ADD, id, player);
		changes.add(delta);
	}*/
	
	/*private Seat<SeatedPlayer> getCloneSeatForPlayerId(int playerId) {
		Seat<SeatedPlayer> result = null;
		for (Seat<SeatedPlayer> seat : clone.getSeats()) {
			if (seat.isOccupied() && seat.getPlayerId() == playerId) {
				result = seat;
			}
		}
		return result;
	}
	
	private void guardSeatPlayers() {
		guardPlayers();
		if(clone.getSeats() == null) {
			List<Seat<SeatedPlayer>> tmp = data.getSeats();
			List<Seat<SeatedPlayer>> list = new LinkedList<Seat<SeatedPlayer>>(tmp);
			clone.setSeats(list);
		}
	}
	
	private void guardScheduledActions() {
		if(clone.getScheduledActions() == null) {
			Map<UUID, ScheduledAction> tmp = data.getScheduledActions();
			Map<UUID, ScheduledAction> map = new HashMap<UUID, ScheduledAction>(tmp);
			clone.setScheduledActions(map);
		}
	}
	
	private void guardPlayers() {
		if(clone.getPlayers() == null) {
			Map<Integer, GenericPlayer> tmp = data.getPlayers();
			Map<Integer, GenericPlayer> map = new TreeMap<Integer, GenericPlayer>(tmp);
			clone.setPlayers(map);
		}
	}
	
	private void guardWatchers() {
		if(clone.getWatchingPlayers() == null) {
			Set<Integer> tmp = data.getWatchingPlayers();
			Set<Integer> map = new TreeSet<Integer>(tmp);
			clone.setWatchingPlayers(map);
		}
	}*/
	
	private void commitDelta() {
		for (DeltaChange change : changes) {
			change.commit();
		}
	}
	
	private void commitSchedule() {
		if(currentSchedule != null) {
			currentSchedule.commit();
		}
	}

	private void commitData() {
		if (currentObject != null) {
			currentObject.commit();
		}
	}
	
	private void rollbackData() {
		if(currentObject != null) {
			currentObject.rollback();
		}
	}

	private void rollbackDelta() {
		changes.clear();
	}

	private void rollbackSchedule() {
		if(currentSchedule != null) {
			currentSchedule.rollback();
		}
	}
	
	private boolean tryAcquireLock(long timeout) throws InterruptedException {
		return data.getTransientLock().tryLock(timeout, TimeUnit.MILLISECONDS);
	}

	private void tryReleaseLock() {
		data.getTransientLock().unlock();
	}
	
	private ClassLoader getClassLoader() {
		return factory.getGameClassLoader(data.getMetaData().getGameId());
	}
	
	private void checkTransaction() {
		if(manager == null) return; // EARlY RETURN
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new TableResource());
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class TableResource implements CoreResource {

		@Override
		public void commit() throws ResourceException {
			StandardTable.this.commit();
		}

		@Override
		public void prepare() throws ResourceException { }

		@Override
		public void rollback() {
			StandardTable.this.rollback();
		}
	}
	
	private class GameState implements TableGameState {
		
		public Object getState() {
			if(currentObject == null) {
				currentObject = new GameStateDeltaChange(data, getClassLoader(), factory.getTableWarnSize());
				currentObject.setSizeRecorder(sizeRecorder); // Trac #644
			}
			return currentObject.getNext();
		}
		
		public void setState(Object gameState) {
			currentObject = new GameStateDeltaChange(data, gameState, getClassLoader(), factory.getTableWarnSize());
			currentObject.setSizeRecorder(sizeRecorder); // Trac #644
		}
	}
}
