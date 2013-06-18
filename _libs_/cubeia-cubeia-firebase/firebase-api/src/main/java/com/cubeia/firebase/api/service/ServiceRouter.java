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
package com.cubeia.firebase.api.service;

import java.util.Collection;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;

/**
 * This is the contract for an object which knows how to dispatch
 * events from a particular services to either other services, or connected clients. 
 * In other words, it is the services connection with the message bus in the Firebase cluster. 
 * 
 * <p>A service that wishes to receive events should implement the {@link RoutableService}
 * interface. 
 * 
 * @author Larsan
 */
public interface ServiceRouter {
	
	/**
	 * Sends an action to a particular service. The data will be wrapped 
	 * and dispatched across the message bus asynchronously.
	 * 
	 * @param disc The service discriminator, must not be null
	 * @param action Action to include, must not be null
	 * @throws NoSuchRouteException If the service cannot be found or routed to
	 * @throws ChannelNotFoundException 
	 */
	public void dispatchToService(ServiceDiscriminator disc, ServiceAction action) throws NoSuchRouteException;
	
	
	/**
	 * <p>Sends a service action to a particular player. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously.</p>
	 * 
	 * @param playerId Player to send data to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToPlayer(int playerId, ServiceAction action);
	
	
	/**
	 * <p>This method can be used to send an event to multiple players. It
	 * also functions as a general broadcast if the given player id parameter
	 * is null in which case it send the action to all logged in players.</p>
	 * 
	 * <p>The data will be wrapped and dispatched across the message bus
	 * asynchronously.
	 * 
	 * @param players Players to send data to, or null for all logged in players
	 * @param action Action to send, must not be null
	 */
	public void dispatchToPlayers(int[] players, ServiceAction action);
	
	/**
	 * <p>Sends a game data action to a particular player. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously.</p>
	 * 
	 * @param playerId Player to send data to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToPlayer(int playerId, GameAction action);
	
	/**
	 * <p>Sends a collection of game data actions to a particular player. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously.</p>
	 * 
	 * @param playerId Player to send data to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToPlayer(int playerId, Collection<? extends GameAction> actions);

	
	/**
	 * Sends a game data action to a particular table. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously. The action must have a valid table id set for
	 * the event to be routed correctly.
	 * 
	 * @param gameId Id of the game to send action to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToGame(int gameId, GameAction action);
	
	
	/**
	 * Sends a game data action to a particular tournament. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously. The action must have a valid MTT id set for
	 * the event to be routed correctly.
	 * 
	 * @param mttInstanceId Id of the tournament to send action to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToTournament(int mttInstanceId, MttAction action);
	
	
	/**
	 * Sends an activator action to a particular tournament activator. The
	 * data will be wrapped and dispatched across the message bus asynchronously. 
	 * 
	 * @param mttId Tournament id
	 * @param action Action to send, must not be null
	 */
	public void dispatchToTournamentActivator(int mttId, ActivatorAction<?> action);
	
	
	/**
	 * Sends an activator action to a particular game activator. The
	 * data will be wrapped and dispatched across the message bus asynchronously. 
	 * 
	 * @param gameId Game id
	 * @param action Action to send, must not be null
	 */
	public void dispatchToGameActivator(int gameId, ActivatorAction<?> action);
	
}
