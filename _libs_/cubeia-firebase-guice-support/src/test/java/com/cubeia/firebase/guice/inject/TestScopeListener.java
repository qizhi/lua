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

import static com.cubeia.firebase.guice.inject.ScopeListenerTestGame.SUCCESS;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.api.util.ResourceLocator;

public class TestScopeListener {

	@BeforeMethod
	public void setup() {
		SUCCESS.set(false);
	}
	
	@Test
	public void testScopeCalles() throws Exception {
		Table t = new TestTable();
		ScopeListenerTestGame game = new ScopeListenerTestGame();
		game.init(new Context());
		GameDataAction gda = new GameDataAction(985, t.getId());
		game.getGameProcessor().handle(gda, t);
		assertTrue(SUCCESS.get());
	}
		
	private static class Context implements GameContext {

		@Override
		public ResourceLocator getResourceLocator() {
			return null;
		}

		@Override
		public ServiceRegistry getServices() {
			return new Registry();
		}
	}
	
	private static class Registry extends ServiceRegistryAdapter {  }
}
