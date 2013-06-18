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

package com.cubeia.firebase.guice.inject;

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableGameState;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableWatcherSet;

class TestTable implements Table {

	private TableListener listener;
	
	public void setListener(TableListener listener) {
		this.listener = listener;
	}
	
	@Override
	public int getId() {
		return 5;
	}

	@Override
	public GameNotifier getNotifier() {
		return null;
	}

	@Override
	public TournamentNotifier getTournamentNotifier() {
		return null;
	}

	@Override
	public TableInterceptor getInterceptor() {
		return null;
	}

	@Override
	public TableListener getListener() {
		return listener;
	}

	@Override
	public ExtendedDetailsProvider getExtendedDetailsProvider() {
		return null;
	}

	@Override
	public TableScheduler getScheduler() {
		return null;
	}

	@Override
	public TableWatcherSet getWatcherSet() {
		return null;
	}

	@Override
	public TablePlayerSet getPlayerSet() {
		return null;
	}

	@Override
	public TableMetaData getMetaData() {
		return null;
	}

	@Override
	public TableGameState getGameState() {
		return new TableGameState() {
			
			@Override
			public Object getState() {
				return null;
			}
			
			@Override
			public void setState(Object gameState) { }
		};
	}

	@Override
	public LobbyTableAttributeAccessor getAttributeAccessor() {
		return null;
	}	
}