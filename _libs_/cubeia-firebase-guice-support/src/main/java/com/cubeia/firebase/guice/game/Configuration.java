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

package com.cubeia.firebase.guice.game;

import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;

/**
 * Game configuration interface. This interface returns the 
 * classes implementing the table listeners, processors and interceptors.
 * The only mandatory interface is game processor. The interfaces
 * returned will be bound in a Guice injector.
 * 
 * @author larsan
 */
public interface Configuration {

	/**
	 * @return The game processor to use, must never return null
	 */
	public Class<? extends GameProcessor> getGameProcessorClass();
	
	/**
	 * @return The tournament processor to use, or null
	 */
	public Class<? extends TournamentProcessor> getTournamentProcessorClass();
	
	/**
	 * @return The table listener to use, or null
	 */
	public Class<? extends TableListener> getTableListenerClass();
	
	/**
	 * @return The table interceptor to use, or null
	 */
	public Class<? extends TableInterceptor> getTableInterceptorClass();
	
	/**
	 * @return The class of the game state, or null
	 */
	public Class<?> getGameStateClass();
	
}
