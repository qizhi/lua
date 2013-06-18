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
package com.cubeia.firebase.api.game.table;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;


/**
 * <p>Table is the interface towards the Firebase internal implementation of a table in the system.</p>
 * 
 * <p>A Table holds the game state as well as all players and watchers.</p>
 *	
 * <p><strong>Game State</strong><br>
 * The game state contained on the table is game specific. It should be set, either by the 
 * implemented Game Activator or the implementing Game Support. The game state must be serializable
 * since it will be serialized when replicated.</p>
 * 
 * <p><strong>Players</strong><br>
 * Players can join and leave the table. The player objects kept at the table are detached from the 
 * client's session (socket). If a client loses it's connection then the corresponding player's status will
 * be changed accordingly.
 * </p>
 * 
 * <p><strong>Watchers</strong><br>
 * Watchers can join and leave the table. Watchers will be automatically removed if the client loses it's connection.
 * </p>
 *
 * @author Fredrik
 */
public interface Table extends Identifiable {
	
	/**
	 * Gets a reference to the notifier for the table. Please note that
	 * the notifier is only available during event invocation, references
	 * to tables should not be kept outside event processing and there is no
	 * guarantee that the same notifier will be used between to invocations.
	 * In other words, the platform will insert a notifier on a neeed-to-have
	 * basis.
	 * 
	 * @return The game notifier, may be null between invocations
	 */
	public GameNotifier getNotifier();

	/**
	 * Returns the tournament notifier if applicable.
	 * @return The tournament notifier if this is a tournament table, otherwise null.
	 * TODO: move the tournament notifier from here, it is MTT specific and doesn't belong in a general table
	 */
	public TournamentNotifier getTournamentNotifier();
	
	/**
	 * @return The current table interceptor, or null
	 */
	public TableInterceptor getInterceptor();
	
	/**
	 * @return The current table listener, or null
	 */
	public TableListener getListener();
	
	/**
	 * 
	 * @return The {@link ExtendedDetailsProvider}, or null
	 */
	public ExtendedDetailsProvider getExtendedDetailsProvider();
	
	/**
	 * @return The scheduler representing this table, never null
	 */
	public TableScheduler getScheduler();
	
	
	/**
	 * @return The handler for watching players, never null
	 */
	public TableWatcherSet getWatcherSet();
	
	
	/**
	 * @return The player handler for the table, never null
	 */
	public TablePlayerSet getPlayerSet();
	
	
	/**
	 * @return The table meta data, never null
	 */
	public TableMetaData getMetaData();
	
	
	/**
	 * @return The game state handler, never null
	 */
	public TableGameState getGameState();
	
	
	/**
	 * @return The table lobby access interface, or null if not found
	 */
	public LobbyTableAttributeAccessor getAttributeAccessor();

}