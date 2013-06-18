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
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.api.util.ResourceLocator;

/**
 * This class check #663: It should be possible to configure
 * a table listener which *is not* a tournament listener...
 * 
 * @author larsan
 */
public class TestTableListener {
	
	@BeforeMethod
	public void setup() {
		PLAYER_ID.set(0);
		TABLE_ID.set(0);
	}

	@Test
	public void listenerDoesNotHavetoBeTournament() throws Exception {
		Table t = new TestTable();
		TableListenerTestGame game = new TableListenerTestGame();
		game.init(new Context());
		TableListener list = game.getTableListener(t);
		/*
		 * At this point "list" is a Proxy which will setup the event
		 * scopes and forward to the Guice initiated table listener. The
		 * next invocation fails in #663 as the code expects a tournament
		 * table listener.
		 */
		list.playerLeft(t, -1);
	}
	
	@Test
	public void testNullState() throws Exception {
		Table t = new TestTable();
		TableListenerTestGame game = new TableListenerTestGame();
		game.init(new Context());
		GameProcessor proc = game.getGameProcessor();
		assertNotNull(proc);
		proc.handle(new GameDataAction(1, 1), t);
	}
	
	@Test
	public void proxyReentrance() throws Exception {
		TestTable t = new TestTable();
		ReEntrantTableListenerTestGame game = new ReEntrantTableListenerTestGame();
		game.init(new Context());
		TableListener list = game.getTableListener(t);
		t.setListener(list);
		/*
		 * Ticket #706
		 * At this point "list" is a Proxy which will setup the event
		 * scopes and forward to the Guice initiated table listener.
		 * 
		 * Since the TableListener and other interfaces are available
		 * we need to keep track of the number of invocations for 
		 * cleanup purposes. 
		 * 
		 * Here we would get a warning and a clear of the thread local
		 * before since playerJoined calls itself again.
		 */
		GenericPlayer player = new GenericPlayer(123, "TestPlayer");
		list.playerJoined(t, player);
	}
	
	@Test
	public void testPlayerAndTableIdOnJoin() throws Exception {
		TestTable t = new TestTable();
		ReEntrantTableListenerTestGame game = new ReEntrantTableListenerTestGame();
		game.init(new Context());
		TableListener list = game.getTableListener(t);
		t.setListener(list);
		GenericPlayer player = new GenericPlayer(123, "TestPlayer");
		list.playerJoined(t, player);
		assertEquals(123, PLAYER_ID.get());
		assertEquals(5, TABLE_ID.get());
	}
	
	@Test
	public void testPlayerAndTableIdOnLeave() throws Exception {
		TestTable t = new TestTable();
		ReEntrantTableListenerTestGame game = new ReEntrantTableListenerTestGame();
		game.init(new Context());
		TableListener list = game.getTableListener(t);
		t.setListener(list);
		list.playerLeft(t, 643);
		assertEquals(643, PLAYER_ID.get());
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
