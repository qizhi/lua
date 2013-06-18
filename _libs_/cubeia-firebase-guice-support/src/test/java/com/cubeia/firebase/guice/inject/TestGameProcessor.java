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

import static com.cubeia.firebase.guice.inject.ReEntrantTableListenerTestGame.PLAYER_ID;
import static com.cubeia.firebase.guice.inject.ReEntrantTableListenerTestGame.TABLE_ID;
import static junit.framework.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.api.util.ResourceLocator;

public class TestGameProcessor {

	@BeforeMethod
	public void setup() {
		PLAYER_ID.set(0);
		TABLE_ID.set(0);
	}

	@Test
	public void testIdInsertionOnHandleDataAction() throws Exception {
		Table t = new TestTable();
		ReEntrantTableListenerTestGame game = new ReEntrantTableListenerTestGame();
		game.init(new Context());
		GameDataAction gda = new GameDataAction(985, t.getId());
		game.getGameProcessor().handle(gda, t);
		assertEquals(985, PLAYER_ID.get());
		assertEquals(5, TABLE_ID.get());
	}
	
	@Test
	public void testIdInsertionOnHandleObjectAction() throws Exception {
		Table t = new TestTable();
		ReEntrantTableListenerTestGame game = new ReEntrantTableListenerTestGame();
		game.init(new Context());
		GameObjectAction goa = new GameObjectAction(t.getId());
		game.getGameProcessor().handle(goa, t);
		assertEquals(-1, PLAYER_ID.get());
		assertEquals(5, TABLE_ID.get());
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
