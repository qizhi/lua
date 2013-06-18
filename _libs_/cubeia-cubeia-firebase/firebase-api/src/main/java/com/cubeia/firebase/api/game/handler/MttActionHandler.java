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
package com.cubeia.firebase.api.game.handler;

import com.cubeia.firebase.api.action.StartMttRoundAction;
import com.cubeia.firebase.api.action.StopMttRoundAction;
import com.cubeia.firebase.api.game.TournamentGame;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.table.Table;

/**
 * Handles tournament specific table actions.
 *
 */
public class MttActionHandler extends AbstractActionHandler {

	private final TournamentGame game;
	private TournamentProcessor tournamentProcessor;
    private final Table table;

	public MttActionHandler(Table table, TournamentGame game) {
		this.table = table;
		this.game = game;
    }
	
	public TournamentProcessor getTournamentProcessor() {
		if(tournamentProcessor == null) {
			tournamentProcessor = game.getTournamentProcessor();
		}
		return tournamentProcessor;
	}
	  
    @Override
    public void visit(StartMttRoundAction action) {
    	if (getTournamentProcessor() != null) {
    		getTournamentProcessor().startRound(table);
    	}
    }
    
    @Override
    public void visit(StopMttRoundAction action) {
    	if (getTournamentProcessor() != null) {
    		getTournamentProcessor().stopRound(table);
    	}
    }
}
