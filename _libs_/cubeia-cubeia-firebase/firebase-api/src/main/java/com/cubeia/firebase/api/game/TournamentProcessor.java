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
package com.cubeia.firebase.api.game;

import com.cubeia.firebase.api.game.table.Table;



/**
 * The TournamentProcessor handles mtt events.
 * Implement this to provide game specific tournament logic.
 * 
 * @author Fredrik
 *
 */
public interface TournamentProcessor {
	
	/**
	 * Start a new tournament round.
	 * @param table the table
	 */
	public void startRound(Table table);

	/**
	 * The tournament manager requested that the
	 * round is stopped.
	 * @param table the table
	 */
	public void stopRound(Table table);

}
