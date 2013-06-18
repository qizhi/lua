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
/**
 * 
 */
package com.cubeia.firebase.server.game;

import static com.cubeia.firebase.util.Classes.switchContextClassLoaderForInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.processor.ActionGameRegistry;
import com.cubeia.firebase.server.processor.DeferredResourceLocator;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.util.InternalComponentInvocationHandler;
import com.cubeia.firebase.server.util.InvocationHandlerAdapter;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * This class have been refactored out from the game event daemon in order to be
 * re-used. It maps game ID's to revision and deployments and caches already created
 * instances of games.
 * 
 * @author Larsan
 */
class GameRegistry implements ActionGameRegistry {
	
	private final DeploymentManager man; 
	private final Map<Integer, Game> realGames;
	private final JndiProvider eventContext;

	GameRegistry(DeploymentManager man, JndiProvider eventContext) {
		this.eventContext = eventContext;
		this.realGames = new ConcurrentHashMap<Integer, Game>();
		this.man = man;
	}

	public Game getGameInstance(int gameId) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SystemException {
		GameDeployment dep = man.getGameDeploymentForId(gameId);
    	GameRevision rev = (dep == null ? null : dep.getLatestRevision());
		if(rev != null) return doGetInstance(rev);
		else return null;
	}
	
	public void destroy() {
		destroyGames();
		realGames.clear();
	}	

	
	// --- PRIVATE METHODS --- //
	
	private GameContext newGameContext(int gameId, int revisionId) {
		ResourceLocator loc = new DeferredResourceLocator(gameId, revisionId);
		DefaultGameContext context = new DefaultGameContext(loc);
		// we'll return a proxy here for callbacks to the platform
		InvocationHandler root = new InvocationHandlerAdapter(context);
    	InvocationHandler switcher = new InternalComponentInvocationHandler(getClass().getClassLoader(), root);
    	return (GameContext) Proxy.newProxyInstance(context.getClass().getClassLoader(), new Class[] { GameContext.class }, switcher);
	}
	
	private void destroyGames() {
		for (Game g : realGames.values()) {
			try {
				final Game tmp = g;
				eventContext.wrapInvocation(new InvocationFacade<Throwable>() {
					
					public Object invoke() throws Exception {
						return switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
							@Override
							public Object invoke() throws RuntimeException {
								tmp.destroy();
								return null;
							}
						}, tmp.getClass().getClassLoader());
					}
				});
			} catch (Throwable th) {
				Logger.getLogger(getClass()).error("Unexpected throwable caught when destroying game", th);
			}
		}
	}

	private Game doGetInstance(GameRevision rev) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SystemException {
		/*if(rev.isGameSupport()) {
			return rev.newGameInstance();
		} else {*/
			return doGetRealInstance(rev);
		//}
	}

	private Game doGetRealInstance(GameRevision rev) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SystemException {
		Game g = realGames.get(rev.getGameDefinition().getId());
		if(g == null) {
			return newRealInstance(rev);
		} else {
			return g;
		}
	}

	private synchronized Game newRealInstance(GameRevision rev) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SystemException {
		int gameId = rev.getGameDefinition().getId();
		// First, double checked looking, someone might have been here before...
		Game g = realGames.get(gameId);
		if(g != null) return g; // EARLY RETURN
		g = rev.newGameInstance();
		checkInitRealGame(g, gameId, rev.getVersion());
		realGames.put(gameId, g);
		return g;
	}

	private void checkInitRealGame(final Game g, final int gameId, final int revisionId) throws SystemException {
		eventContext.wrapInvocation(new InvocationFacade<SystemException>() {
			
			public Object invoke() throws SystemException {
				return switchContextClassLoaderForInvocation(new InvocationFacade<SystemException>() {
					@Override
					public Object invoke() throws SystemException {
						g.init(newGameContext(gameId, revisionId));
						return null;
					}
				}, g.getClass().getClassLoader());
			}
		});
	}
}