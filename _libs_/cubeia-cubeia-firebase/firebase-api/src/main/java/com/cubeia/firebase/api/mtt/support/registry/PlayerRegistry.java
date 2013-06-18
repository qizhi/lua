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
package com.cubeia.firebase.api.mtt.support.registry;

import java.io.Serializable;
import java.util.Collection;

import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;

/**
 * <p>Holds all player currently registered or participating in a tournament.</p>
 *
 * @author Fredrik
 */
public interface PlayerRegistry extends Serializable {
	
	/**
	 * <p>Registers a player for this tournament.</p>
	 * 
	 * <p>
	 * The player will only be registered if all interceptors return {@link MttRegisterResponse#ALLOWED}.
	 * </p>
	 * <p>A player has only been registered in the tournament if the
	 * return value of is {@link MttRegisterResponse#ALLOWED}</p>
	 * <p>
	 * If the player is already registered he will be re-registered and all
	 * listeners will be called.
	 * </p>
	 * 
	 * @param instance the tournament instance
	 * @param request the context for the registration
	 * @return result of the registration
	 */
	public MttRegisterResponse register(MttInstance instance, MttRegistrationRequest request);
	
	/**
	 * <p>Unregisters a player from the tournament.</p>
	 * 
     * <p>
     * The player will only be unregistered if all interceptors returns {@link MttRegisterResponse#ALLOWED}.
     * </p>
	 * <p>A player has only been unregistered in the tournament if the
	 * return value is MttRegisterResponse.ALLOW</p>
	 * <p>
     * If the player is no registered nothing will happen but all listeners will
     * be called. To override this behavior you must provide your own interceptor(s).
	 * </p>
	 * 
	 * @param instance the tournament instance
	 * @param pid player id to unregister
	 * @return the result of the registration
	 */
	public MttRegisterResponse unregister(MttInstance instance, int pid);
	
	/**
	 * Removes a player from the registry, called when a player is out of the tournament.
	 * This method will not use any interceptor or call any listeners.
	 * @param pid the player to remove
	 */
	public void removePlayer(int pid);
	
	public boolean isRegistered(int pid);
	
	public void registerListener(PlayerListener listener);
	
	/**
	 * Unregisters a listener from this registry.
	 * 
	 * @param listener must not be <code>null</code>
	 */
	public void unregisterListener(PlayerListener listener);
	
	/**
	 * Registers a {@link PlayerInterceptor} for this registry.
	 * 
	 * @param interceptor must not be <code>null</code>
	 */
	public void registerInterceptor(PlayerInterceptor interceptor);
	
	/**
	 * Unregisters a {@link PlayerInterceptor} from this registry.
	 * 
	 * @param interceptor must not be <code>null</code>
	 */
	public void unregisterInterceptor(PlayerInterceptor interceptor);
	
	/** 
	 * Removes all listeners from this registry.
	 */
	public void clearListeners();

	/**
	 * Removes all interceptors from this registry.
	 */
	public void clearInterceptors();
	
	/**
	 * Gets the current number of players in this registry.
	 * 
	 * @return the current number of players in the registry.
	 */
	public int size();

	/**
	 * Gets all players currently contained in this registry
	 * 
	 * @return a {@link Collection} of {@link MttPlayer}s in this registry
	 */
    public Collection<MttPlayer> getPlayers();
	
}
