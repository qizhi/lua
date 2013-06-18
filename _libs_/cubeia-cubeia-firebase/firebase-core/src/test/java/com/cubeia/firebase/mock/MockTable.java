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
package com.cubeia.firebase.mock;

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
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.api.lobby.LobbyPath;

public class MockTable implements Table {

	public TablePlayerSet playerSet = new MockPlayerSet();
	public MockNotifier notifier = new MockNotifier();
	public MockDetailsProvider extendedDetailsProvider;

	public ExtendedDetailsProvider getExtendedDetailsProvider() {
		return extendedDetailsProvider;
	}
	
	public LobbyTableAttributeAccessor getAttributeAccessor() {
		// TODO Auto-generated method stub
		return null;
	}

	public TableGameState getGameState() {
		// TODO Auto-generated method stub
		return null;
	}

	public TableInterceptor getInterceptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public TableListener getListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public TableMetaData getMetaData() {
		return new TableMetaData() {

			public int getGameId() {
				// TODO Auto-generated method stub
				return 0;
			}

			public LobbyPath getLobbyPath() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getMttId() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			public TableType getType() {
				// TODO Auto-generated method stub
				return null;
			}

			/*public boolean isTournamentTable() {
				// TODO Auto-generated method stub
				return false;
			}*/
			
		};
	}

	public GameNotifier getNotifier() {
		return notifier;
	}

	public TablePlayerSet getPlayerSet() {
		return playerSet;
	}

	public TableScheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}

	public TournamentNotifier getTournamentNotifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public TableWatcherSet getWatcherSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
