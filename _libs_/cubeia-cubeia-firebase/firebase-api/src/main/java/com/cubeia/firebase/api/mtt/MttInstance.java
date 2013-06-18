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
package com.cubeia.firebase.api.mtt;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;

/**
 * A tournament instance. An instance represents a concrete 
 * tournament and encapsulates event specific data.
 */
public interface MttInstance extends Identifiable { 
	
    /**
     * @return The state holder for this tournament instance
     */
	public MTTState getState();
	
	/**
	 * @return The action scheduler for this instance.
	 */
	public Scheduler<MttAction> getScheduler();

	/**
	 * @return An accessor for the lobby attribute bound to this instance
	 */
	public LobbyAttributeAccessor getLobbyAccessor();
	
	/**
	 * Retrieve a table attribute accessor. This method only accesses table for
	 * the current tournament. If a table does not belong to the tournament or
	 * if the table cannot be found, null will be returned. 
	 * 
	 * @param tableId Table id for which to get an accessor
	 * @return An accessor for the lobby table, or null if not found
	 */
	public LobbyAttributeAccessor getTableLobbyAccessor(int tableId); 
	
	/**
	 * @return The service registry for the server
	 */
	public ServiceRegistry getServiceRegistry();

	/**
	 * This is a shortcut for retrieving the player registry. This
	 * registry should be used to inform the system of tournaments
	 * a player is registered in.  
	 * 
	 * @return The system tournament registry, never null
	 */
	public TournamentPlayerRegistry getSystemPlayerRegistry();
	
	/**
	 * @return The table creator for the tournament instance, never null
	 */
	public MttTableCreator getTableCreator();
	
	/**
	 * @return The notifier for the instance, never null
	 */
	public MttNotifier getMttNotifier();
	
}
