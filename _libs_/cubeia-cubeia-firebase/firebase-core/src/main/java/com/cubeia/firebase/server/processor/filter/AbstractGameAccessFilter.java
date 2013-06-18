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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.processor.ActionGameRegistry;

public abstract class AbstractGameAccessFilter<T extends FirebaseTable, A extends Action> implements ProcessorFilter<T, A> {

	protected final Logger log = Logger.getLogger(getClass());
	private final ActionGameRegistry creator;
	
	public AbstractGameAccessFilter(ActionGameRegistry creator) {
		notNull(creator, "creator");
		this.creator = creator;
	}
	
	protected Game getGameForTable(T table) {
		notNull(table, "table");
		int gameId = table.getMetaData().getGameId();
    	try {
    		Game g = creator.getGameInstance(gameId);
    		if(g == null) log.error("Failed to find game deployment/revision; gameId: " + gameId);
			return g;
    	} catch (Exception e) {
    		/*
    		 * Ugly catch all here... /LJN
    		 */
			log.error("Failed to instantiate game class", e);
			return null;
		} 
	}
}
