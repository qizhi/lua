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

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.TournamentGame;
import com.cubeia.firebase.api.game.handler.MttActionHandler;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.processor.ActionGameRegistry;

/**
 * This filter takes care of MTT rounds for the table. It accepts Firebase
 * tables and game actions.
 * 
 * @author Lars J. Nilsson
 */
public class FirebaseMttRoundActionFilter<T extends FirebaseTable, A extends GameAction> extends AbstractGameAccessFilter<T, A> {

	public FirebaseMttRoundActionFilter(ActionGameRegistry creator) {
		super(creator);
	}

	@Override
	public void process(A action, T data, ProcessorChain<T, A> filters) {
		checkHandleMttRounds(action, data);
		filters.next(action, data);
	}

	// --- PRIVATE METHODS --- //
	
	private void checkHandleMttRounds(A action, T table) {
		Game game = getGameForTable(table);
		if (game instanceof TournamentGame) {
			TournamentGame mttGame = (TournamentGame) game;
			MttActionHandler handler = new MttActionHandler(table, mttGame);
			action.visit(handler);
		}
	}
}
