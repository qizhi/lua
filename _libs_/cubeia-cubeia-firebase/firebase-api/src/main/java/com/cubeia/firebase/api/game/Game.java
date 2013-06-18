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

import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.server.Initializable;

/**
 * <p>This is the main interface that should be implemented if you are implementing a game.</p>
 * 
 * </p>The Game interface only specifies one method, getGameProcessor(). This is enough to start
 * of with, but for added functionality you will need to implement further interfaces.</p>
 * 
 * <p>The interfaces that are of interest for a Game implementation are:
 * <ul>
 *  <li>{@link TableListenerProvider}</li>
 *  <li>{@link TableInterceptorProvider}</li>
 *  <li>{@link ExtendedDetailsProvider}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>TableListener / Provider</b><br />
 * The {@link TableListenerProvider} can be implemented to provide a {@link TableListener} which 
 * will be accessed by the Firebase server to receive notifications when players join, leave etc. This 
 * is a "dual access" interface which means that the game can choose to implement <em>either</em> the
 * provider interface, in which case the table listener will be accessed via a getter method, or
 * implement the {@link TableListener} interface directly, in which case the provider interface
 * is ignored. In other words, if you want the listener as a separate interface you should implement
 * the {@link TableListenerProvider provider}, and if you prefer it to be implemented in your game, have the game 
 * implement {@link TableListener listener} directly. If the game implements both the listener and the provider
 * interface, the provider interface takes precedence. 
 * </p>
 * 
 * <p>
 * <b>TableInterceptor / Provider</b><br />
 * The {@link TableInterceptorProvider} can be implemented to provide a {@link TableInterceptor} which 
 * will be accessed by the Firebase server to determine if a player is allowed to join, leave etc. This 
 * is a "dual access" interface which means that the game can choose to implement <em>either</em> the
 * provider interface, in which case the table interceptor will be accessed via a getter method, or
 * implement the {@link TableInterceptor} interface directly, in which case the provider interface
 * is ignored. In other words, if you want the interceptor as a separate interface you should implement
 * the {@link TableInterceptorProvider provider}, and if you prefer it to be implemented in your game, have the game 
 * implement {@link TableInterceptor interceptor} directly. If the game implements both the interceptor and the provider
 * interface, the provider interface takes precedence. 
 * </p>
 *
 * @author Fredrik
 */
public interface Game extends Initializable<GameContext> {
	
	/**
	 * Get the Game Data processor class for actions. This method will 
	 * be accessed concurrently, but only for one table at a time. By 
	 * returning new game processors for each call, the game processors
	 * does not need to be concurrent.
	 * 
	 * @return A game processor, never null
	 */
	public GameProcessor getGameProcessor();

}
