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
package com.cubeia.firebase.server.processor.filter;

import static com.cubeia.firebase.api.util.Arguments.notNull;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributeMapper;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.trans.StandardPlayerSet;
import com.cubeia.firebase.game.table.trans.StandardWatcherSet;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyAttributeAccessor;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyTableAccessor;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class FirebaseLobbyUpdateFilter<T extends FirebaseTable, A extends Action> implements ProcessorFilter<T, A> {

	private final SystemStateServiceContract state;
	
	public FirebaseLobbyUpdateFilter(SystemStateServiceContract state) {
		notNull(state, "system state");
		this.state = state;
	}
	
	@Override
	public void process(A action, T table, ProcessorChain<T, A> filters) {
		filters.next(action, table);
		checkLobbyUpdate(table);
		updateLastModified(table);
	}

	
	// --- PRIVATE METHODS --- //
	
	/*
	 * Only update last modified if the table has no players.
	 * This will minimize the number of system state updates needed. 
	 */
	private void updateLastModified(FirebaseTable table) {
		if (table.getPlayerSet().getPlayerCount() == 0) {
			DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(state, table.getLobbyPath());
			DefaultTableAttributeMapper.updateLastModified(acc);
		}
	}
	
	private void checkLobbyUpdate(T table) {
    	/*
    	 * This method is called to update the seated/watching players
    	 * around a table. These two attributes (together with last modified, 
    	 * which is done elsewhere) are the only dynamic attributes we handle 
    	 * currently, so we're checking if they have been changed before actually
    	 * calling the update method.
    	 * 
    	 * This is a slight bit of a hack of course, but I'll leave it in for now. /LJN
    	 */
		if(isPlayerSetDirty(table) || isWatcherSetDirty(table)) {
			updateLobby(table);
		}
	}
	
    private boolean isWatcherSetDirty(T table) {
    	if (table.getWatcherSet() instanceof StandardWatcherSet) {
			StandardWatcherSet  playerSet = (StandardWatcherSet) table.getWatcherSet();
			return playerSet.getIsDirty();
        } else {
        	return true;
        }
	}
    
    private boolean isPlayerSetDirty(T table) {
    	if (table.getPlayerSet() instanceof StandardPlayerSet) {
			StandardPlayerSet  playerSet = (StandardPlayerSet) table.getPlayerSet();
			return playerSet.getIsDirty();
        } else {
        	return true;
        }
	}

	private void updateLobby(T table) {
		DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
		DefaultLobbyMutator mut = new DefaultLobbyMutator();
		mut.updateTable(acc, table);
    }
}
