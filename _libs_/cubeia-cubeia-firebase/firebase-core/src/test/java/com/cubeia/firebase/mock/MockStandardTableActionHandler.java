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
import com.cubeia.firebase.api.game.handler.StandardTableActionHandler;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.LobbyTableAccessor;
import com.cubeia.firebase.api.game.table.Table;

public class MockStandardTableActionHandler extends StandardTableActionHandler {

	private MockNotifier notifier = new MockNotifier();
	
	public MockStandardTableActionHandler(Table table, LobbyTableAccessor acc, DefaultLobbyMutator mut) {
		super(table, acc, mut);
	}
	
	@Override
	public GameNotifier getNotifier() {
		return notifier;
	}
	
}
