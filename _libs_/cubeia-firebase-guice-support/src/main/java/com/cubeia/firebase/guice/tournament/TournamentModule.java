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

package com.cubeia.firebase.guice.tournament;

import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.firebase.guice.inject.Log4jTypeListener;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;

/**
 * This is the configuration for the tournament support. It
 * binds {@link Log4j log4j} support to the context.
 * 
 * @author larsan
 */
public class TournamentModule extends AbstractModule {

	private final GuiceTournament parent;

	public TournamentModule(GuiceTournament parent) {
		this.parent = parent;
	}
	
	@Override
	protected void configure() {
		bindListener(Matchers.any(), new Log4jTypeListener());
		bind(TournamentAssist.class).toInstance(parent);
		Configuration conf = parent.getConfigurationHelp();
		if(conf != null && conf.getTournamentHandlerClass() != null) {
			bind(TournamentHandler.class).to(conf.getTournamentHandlerClass()).in(Scopes.SINGLETON);
		}
	}
}
