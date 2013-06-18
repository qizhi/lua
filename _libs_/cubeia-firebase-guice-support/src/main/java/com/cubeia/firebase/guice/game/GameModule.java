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
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.google.inject.AbstractModule;

/**
 * This module configures the game injection context. It
 * binds the game context and the {@link Configuration configured}
 * listeners and interceptors.
 * 
 * @author larsan
 */
public class GameModule extends AbstractModule {

	private final GuiceGame game;
	
	/**
	 * @param game Parent game, must not be null
	 */
	public GameModule(GuiceGame game) {
		this.game = game;
	}
	
	@Override
	protected void configure() {
		bind(GameContext.class).toInstance(game.getContext());
		processConfiguration();
	}
	
	
	// --- PRIVATE METHODS --- //

	private void processConfiguration() {
		Configuration con = game.getConfigurationHelp();
		if(con != null) {
			conditionalBind(GameProcessor.class, con.getGameProcessorClass());
			conditionalBind(TableInterceptor.class, con.getTableInterceptorClass());
			conditionalBind(TableListener.class, con.getTableListenerClass());
			if(con.getGameStateClass() != null) {
				bindGameStateProvider(con.getGameStateClass());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void bindGameStateProvider(Class stateClass) {
		bind(stateClass).toProvider(StateProvider.class).in(EventScoped.class);
	}

	private <T> void  conditionalBind(Class<T> face, Class<? extends T> binding) {
		if(binding != null) {
			bind(face).to(binding).in(EventScoped.class);
		}
	}
}
