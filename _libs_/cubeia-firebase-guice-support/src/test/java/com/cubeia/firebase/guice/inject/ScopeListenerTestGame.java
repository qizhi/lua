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

package com.cubeia.firebase.guice.inject;

import java.util.concurrent.atomic.AtomicBoolean;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.guice.game.Configuration;
import com.cubeia.firebase.guice.game.ConfigurationAdapter;
import com.cubeia.firebase.guice.game.GuiceGame;
import com.cubeia.firebase.guice.game.ScopeListener;
import com.google.inject.Injector;

public class ScopeListenerTestGame extends GuiceGame {
	
	public static AtomicBoolean SUCCESS = new AtomicBoolean();
	
	@Override
	public Configuration getConfigurationHelp() {
		return new ConfigurationAdapter() {
			
			public Class<? extends GameProcessor> getGameProcessorClass() {
				return Processor.class;
			}
		};
	}
	
	@Override
	protected void postInjectorCreation(Injector injector) {
		addScopeListener(injector.getInstance(Listener.class));
	}

	public static class Processor implements GameProcessor {
		
		@Override
		public void handle(GameDataAction action, Table table) { }
		
		@Override
		public void handle(GameObjectAction action, Table table) { }
		
	}
	
	public static class Listener implements ScopeListener {
		
		private ThreadLocal<String> local = new ThreadLocal<String>();
		
		@Override
		public void enter() {
			local.set("kalle");
		}
		
		@Override
		public void exit() {
			SUCCESS.set("kalle".equals(local.get()));
			local.remove();
		}
	}
}
