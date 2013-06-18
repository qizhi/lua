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

import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * This module configures the game activator support for Firebase. It
 * binds the activator context, and the activator implementation. This
 * module will throw an illegal state exception if no activator class
 * is configured.
 * 
 * @author larsan
 */
public class ActivatorModule extends AbstractModule {

	private final GuiceActivator activator;

	/**
	 * @param activator Parent activator, must not be null
	 */
	public ActivatorModule(GuiceActivator activator) {
		this.activator = activator;
	}

	@Override
	protected void configure() {
		bind(ActivatorContext.class).toInstance(activator.getContext());
		Class<? extends GameActivator> a = activator.getActivatorClass();
		if(a != null) {
			bind(GameActivator.class).to(a).in(Scopes.SINGLETON);
		} else {
			throw new IllegalStateException("Missing activator class");
		}
	}
}
