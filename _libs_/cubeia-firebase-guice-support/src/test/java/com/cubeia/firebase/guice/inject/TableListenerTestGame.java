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

import static org.testng.Assert.assertNull;

import javax.annotation.Nullable;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.guice.game.Configuration;
import com.cubeia.firebase.guice.game.ConfigurationAdapter;
import com.cubeia.firebase.guice.game.GuiceGame;
import com.google.inject.Inject;

/**
 * A simple game used to test #663.
 * 
 * @author larsan
 */
public class TableListenerTestGame extends GuiceGame {

	@Override
	public Configuration getConfigurationHelp() {
		return new ConfigurationAdapter() {
			
			@Override
			public Class<? extends TableListener> getTableListenerClass() {
				return Listener.class;
			}

			@Override
			public Class<? extends GameProcessor> getGameProcessorClass() {
				return Processor.class;
			}
			
			@Override
			public Class<?> getGameStateClass() {
				return TestState.class;
			}
		};
	}
	
	public static class Processor implements GameProcessor {
		
		@Inject
		@Nullable
		private TestState state;
		
		@Override
		public void handle(GameDataAction action, Table table) { 
			assertNull(state);
		}
		
		@Override
		public void handle(GameObjectAction action, Table table) { }
		
	}
	
	public static class Listener implements TableListener {

		@Override
		public void playerJoined(Table table, GenericPlayer player) { }

		@Override
		public void playerLeft(Table table, int playerId) { }

		@Override
		public void watcherJoined(Table table, int playerId) { }

		@Override
		public void watcherLeft(Table table, int playerId) { }

		@Override
		public void playerStatusChanged(Table table, int playerId, PlayerStatus status) { }

		@Override
		public void seatReserved(Table table, GenericPlayer player) { }
		
	}
}
