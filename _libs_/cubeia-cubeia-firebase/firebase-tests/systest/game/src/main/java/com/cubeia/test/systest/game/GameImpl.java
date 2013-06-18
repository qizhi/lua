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
package com.cubeia.test.systest.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TournamentGame;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TournamentTableListener;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.test.systest.dao.PlayerCount;

public class GameImpl implements Game, ExtendedDetailsProvider, TournamentGame, TableInterceptor, TournamentTableListener {

	private GameContext con;
	// private final Logger log = Logger.getLogger(getClass());
	private EntityManagerFactory factory;
	
	private static final Map<Integer, AtomicInteger> PLYR_COUNT = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	public GameProcessor getGameProcessor() {
		checkContextClassLoaderAndJNDI();
		return new GameProcessor() {

			public void handle(GameDataAction action, Table table) {
				checkContextClassLoaderAndJNDI();
				Data data = (Data) table.getGameState().getState();
				TestProcessor processor = data.getProcessor();
				processor.setGameContext(con);
				processor.handle(action, table);
				verifyAndIncrement(action.getPlayerId());
			}

			public void handle(GameObjectAction action, Table table) {
				checkContextClassLoaderAndJNDI();
				Data data = (Data) table.getGameState().getState();
				TestProcessor processor = data.getProcessor();
				processor.setGameContext(con);
				processor.handle(action, table);
			}			
		};
	}
	
	public List<Param> getExtendedDetails(Table table, int playerId, boolean fromLobby) {
		checkContextClassLoaderAndJNDI();
		List<Param> response = new ArrayList<Param>();
		if (table.getPlayerSet().getPlayer(playerId) != null) {
			response.add(ParameterUtil.createParam("balance", 100 * playerId));
			if (fromLobby) {
				response.add(ParameterUtil.createParam("interesting_value_from_lobby", "lobby_value"));
			} else {
				response.add(ParameterUtil.createParam("interesting_value_for_watcher", "watcher_value"));
			}
		}
		verifyAndIncrement(playerId);
		return response;
	}
	
	public TournamentProcessor getTournamentProcessor() {
		checkContextClassLoaderAndJNDI();
		return new TournamentProcessorImpl();
	}

	public void destroy() { }

	public void init(GameContext con) throws SystemException {
		factory = Persistence.createEntityManagerFactory("systest-jta");
		this.con = con; 
	}
	
	
	
	// --- INTERCEPTOR --- //
	
	/*
	 * Trac #587: All methods below returns null. This should work as "assent"
	 * and *not* create any NPE:s...
	 */
	
	@Override
	public InterceptionResponse allowJoin(Table table, SeatRequest request) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableInterceptor) {
			return ((TableInterceptor) processor).allowJoin(table, request);
		} else {
			return null;
		}
	}
	
	@Override
	public InterceptionResponse allowLeave(Table table, int playerId) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		verifyAndIncrement(playerId);
		if(processor instanceof TableInterceptor) {
			return ((TableInterceptor) processor).allowLeave(table, playerId);
		} else {
			return null;
		}

	}
	
	@Override
	public InterceptionResponse allowReservation(Table table, SeatRequest request) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		verifyAndIncrement(request.getPlayerId());
		if(processor instanceof TableInterceptor) {
			return ((TableInterceptor) processor).allowReservation(table, request);
		} else {
			return null;
		}
	}
	
	
	// --- TABLE LISTENER --- //
	
	public void tournamentPlayerJoined(Table table, GenericPlayer player, java.io.Serializable serializable) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TournamentTableListener) {
			((TournamentTableListener) processor).tournamentPlayerJoined(table, player, serializable);
		} 
		verifyAndIncrement(player.getPlayerId());
	}
	
	@Override
	public void tournamentPlayerRejoined(Table table, GenericPlayer player) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TournamentTableListener) {
			((TournamentTableListener) processor).tournamentPlayerRejoined(table, player);
		} 
		verifyAndIncrement(player.getPlayerId());
	}
	
	@Override
	public void tournamentPlayerRemoved(Table table, int playerId, Reason reason) {
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TournamentTableListener) {
			((TournamentTableListener) processor).tournamentPlayerRemoved(table, playerId, reason);
		} 	
		verifyAndIncrement(playerId);
	}
	
	@Override
	public void playerJoined(Table table, GenericPlayer player) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).playerJoined(table, player);
		} 
		verifyAndIncrement(player.getPlayerId());
	}
	
	@Override
	public void playerLeft(Table table, int playerId) {
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).playerLeft(table, playerId);
		} 
		verifyAndIncrement(playerId);
	}
	
	@Override
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).playerStatusChanged(table, playerId, status);
		} 
		verifyAndIncrement(playerId);
	}
	
	@Override
	public void seatReserved(Table table, GenericPlayer player) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).seatReserved(table, player);
		} 
		verifyAndIncrement(player.getPlayerId());
	}
	
	@Override
	public void watcherJoined(Table table, int playerId) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).watcherJoined(table, playerId);
		}
		verifyAndIncrement(playerId);
	}
	
	@Override
	public void watcherLeft(Table table, int playerId) {
		checkContextClassLoaderAndJNDI();
		Data data = (Data) table.getGameState().getState();
		TestProcessor processor = data.getProcessor();
		if(processor instanceof TableListener) {
			((TableListener) processor).watcherLeft(table, playerId);
		} 
		verifyAndIncrement(playerId);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkContextClassLoaderAndJNDI() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null || !cl.equals(getClass().getClassLoader())) {
			throw new IllegalStateException("Found class loader: " + cl + "; Expected: " + getClass().getClassLoader());
		}
		checkJndiAccess();
	}

	private void checkJndiAccess() {
		try {
			InitialContext con = new InitialContext();
			Object o = con.lookup("java:comp/env/jdbc/test-jta");
			DataSource src = (DataSource) o;
			if(src == null) {
				throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test-jta");
			}
		} catch(NameNotFoundException e) {
			throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test-jta");
		} catch (NamingException e) {
			throw new IllegalStateException("Failed to lookup data source java:comp/env/jdbc/test-jta", e);
		}
	}
	
	private void createCount(int plyr) {
		EntityManager em = factory.createEntityManager();
		// log.info("Creating count at 1 for player " + plyr);
		PlayerCount c = new PlayerCount();
		c.setCount(1);
		c.setPlayerId(plyr);
		em.persist(c);
		// Why do I need to flush here ? /LJN
		em.flush();
		// save for later
		PLYR_COUNT.put(plyr, new AtomicInteger(1));
	}

	private void verifyAndIncrement(int plyr) {
		if(!PLYR_COUNT.containsKey(plyr)) {
			createCount(plyr);
		} else {
			AtomicInteger C = PLYR_COUNT.get(plyr);
			EntityManager em = factory.createEntityManager();
			// log.info("Expecting count " + C.get() + " for player " + plyr);
			PlayerCount c = em.find(PlayerCount.class, plyr);
			if(c.getCount() != C.get()) {
				throw new IllegalStateException("Expected " + C.get() + ", found " + c.getCount());
			}
			C.incrementAndGet();
			c.increaseCount();
			// Why do I need to flush here ? /LJN
			em.flush();
		}
	}
}
