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

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableGameState;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * This module is included in the Guice injector and binds table
 * members to scope key providers. It also binds the event scope itself.
 * 
 * @author larsan
 */
public class EventModule extends AbstractModule {

	public EventModule() { }
	
	@Override
	protected void configure() {
		configureScope();
		bindDummies();
	}

	private void bindDummies() {
		bind(TableGameState.class).toProvider(EventScope.<TableGameState>seededKeyProvider()).in(EventScoped.class);
		bind(ExtendedDetailsProvider.class).toProvider(EventScope.<ExtendedDetailsProvider>seededKeyProvider()).in(EventScoped.class);
		bind(TableMetaData.class).toProvider(EventScope.<TableMetaData>seededKeyProvider()).in(EventScoped.class);
		bind(GameNotifier.class).toProvider(EventScope.<GameNotifier>seededKeyProvider()).in(EventScoped.class);
		bind(TablePlayerSet.class).toProvider(EventScope.<TablePlayerSet>seededKeyProvider()).in(EventScoped.class);
		bind(TableScheduler.class).toProvider(EventScope.<TableScheduler>seededKeyProvider()).in(EventScoped.class);
		bind(TournamentNotifier.class).toProvider(EventScope.<TournamentNotifier>seededKeyProvider()).in(EventScoped.class);
		bind(TableWatcherSet.class).toProvider(EventScope.<TableWatcherSet>seededKeyProvider()).in(EventScoped.class);
		bind(Table.class).toProvider(EventScope.<Table>seededKeyProvider()).in(EventScoped.class);
		bind(Integer.class).annotatedWith(Names.named("playerId")).toProvider(EventScope.<Integer>seededKeyProvider()).in(EventScoped.class);
		bind(Integer.class).annotatedWith(Names.named("tableId")).toProvider(EventScope.<Integer>seededKeyProvider()).in(EventScoped.class);
	}

	private void configureScope() {
		EventScope scope = new EventScope();
		bindScope(EventScoped.class, scope);
		bind(EventScope.class).toInstance(scope);
	}
}
