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
package com.cubeia.firebase.game.table;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.processor.TableActionScheduler;

/**
 * This is the basic implementation of a transactional system table. It
 * can be referenced by the rest of the server and contains the "per invocation"
 * objects from a table.
 * 
 * <p>The TableInterceptor, TableListener, GameNotifier and TableActionScheduler
 * should be set before event invocation and nullified afterwards.
 * 
 * @author Larsan
 */
public abstract class FirebaseTable implements TransactionalTable, Identifiable {
	
	private TableInterceptor interceptor;
	private TableListener listener;
	private GameNotifier notifier;
	private ExtendedDetailsProvider extendedDetailsProvider;
	private LobbyTableAttributeAccessor tableAccessor;
	private TournamentNotifier tournamentNotifier;
	
	protected TableActionScheduler scheduler;
	protected final InternalMetaData meta;
	
	protected GameObjectSizeRecorder sizeRecorder;
	
	protected FirebaseTable(TableData data) {
		Arguments.notNull(data, "data");
		meta = data.getMetaData();
	}
	
	public void setGameObjectSizeRecorder(GameObjectSizeRecorder rec) {
		this.sizeRecorder = rec;
	}
	
	
	// --- IMMUTABLE GAME DATA --- //

	/**
	 * @return The game id, shortcut for the meta information
	 */
	public int getGameId() {
		return meta.getGameId();
	}

	/**
	 * @return The lobby path, shortcut for the meta information
	 */
	public LobbyPath getLobbyPath() {
		return meta.getLobbyPath();
	}

	/**
	 * @return The table name, shortcut for the meta information
	 */
	public String getName() {
		return meta.getName();
	}
	
	
	// --- TRANSIENT TABLE DATA --- //
	
	/**
	 * @erturn The accessor for the table lobby data
	 */
	public LobbyTableAttributeAccessor getAttributeAccessor() {
		return tableAccessor;
	}
	
	/**
	 * @param tableAccessor The accessor for the table lobby data
	 */
	public void setTableAccessor(LobbyTableAttributeAccessor tableAccessor) {
		this.tableAccessor = tableAccessor;
	}
	
	/**
	 * @return The internal meta data
	 */
	public InternalMetaData getMetaData() {
		return meta;
	}
	
	/**
	 * @param interceptor Invocation interceptor
	 */
	public void setInterceptor(TableInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * @param listener Invocation table listener
	 */
	public void setListener(TableListener listener) {
		this.listener = listener;
	}
	
	/**
	 * @return Invocation notifier
	 */
	public GameNotifier getNotifier() {
		return notifier;
	}
	
	/**
	 * @param notifier Invocation notifier
	 */
	public void setNotifier(GameNotifier notifier) {
		this.notifier = notifier;
	}
	
	/**
	 * Returns the tournament notifier if applicable for this table.
	 * @return the tournament notifier or null if this is not a tournament table.
	 * FIXME: this notifier should be moved 
	 */
	public TournamentNotifier getTournamentNotifier() {
        return tournamentNotifier;
    }
	
	/**
	 * Set the tournament notifier.
	 * @param tournamentNotifier
	 * FIXME: this notifier should be moved
	 */
	public void setTournamentNotifier(TournamentNotifier tournamentNotifier) {
        this.tournamentNotifier = tournamentNotifier;
    }
	
	/**
	 * @return Invocation interceptor
	 */
	public TableInterceptor getInterceptor() {
		return interceptor;
	}
	
	/**
	 * @return Invocation listener
	 */
	public TableListener getListener() {
		return listener;
	}


	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.game.table.Table#getExtendedDetailsProvider()
	 */
	public ExtendedDetailsProvider getExtendedDetailsProvider() {
		return extendedDetailsProvider;
	}
	
	/**
	 * Sets an extended details provider.
	 * 
	 * @param provider an extended details provider, may be null
	 */
	public void setExtendedDetailsProvider(ExtendedDetailsProvider provider) {
		this.extendedDetailsProvider = provider;
	}	
	
	/**
	 * @return Invocation scheduler
	 */
	public TableActionScheduler getInternalScheduler() {
		return scheduler;
	}
	
	/**
	 * @param scheduler Invocation scheduler
	 */
	public void setInternalScheduler(TableActionScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public String toString() {
		return "FirebaseTable id[" + getId() + "] name[" + getMetaData().getName() + "] cap[" + getPlayerSet().getSeatingMap().getNumberOfSeats() + "]";
	}
	
	
	
	// --- IDENTIFIABLE --- //
	
	public int getId() {
		return meta.getTableId();
	}
}
