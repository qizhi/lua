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
package com.cubeia.firebase.server.processor;

import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.server.SystemException;

/**
 * The game registry is responsible for handling the lifetime of the games within
 * the context of a game node. 
 * 
 * @author Larsan
 * @date 2007 maj 14
 */
public interface ActionGameRegistry {

	/**
	 * This method returns a reference to a game instance. For games of the type
	 * game support this will return a new game instance for each call, all other invocations
	 * will return the same reference each time.
	 * 
	 * <p>If the game is not of game support and this is the first time the game is
	 * requested on the local node it needs to be created. Should it fail it initialization
	 * a system exception may be raised.
	 * 
	 * @param gameId Id of the game to get
	 * @return An instance of the game, or null if the game id is not known
	 * @throws ClassNotFoundException On game creation errors
	 * @throws InstantiationException On game creation errors
	 * @throws IllegalAccessException On game creation errors
	 * @throws SystemException If the game fails to initialize
	 */
	public Game getGameInstance(int gameId) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SystemException;

	
	/*
	 * This method can be used to create a new game context for a game. It should be
	 * used sparingly, but it is understood that it will be called for each invocation
	 * on a game support game.
	 * 
	 * @param gameId Id of the game to get a context for
	 * @param revisionId Revision id of the current game
	 * @return A game context, never null
	 */
	// public GameContext newGameContext(int gameId, int revisionId);
	
}
