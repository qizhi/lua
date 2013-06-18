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
package com.cubeia.firebase.server.game;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.server.node.NodeConfig;

@Configurated(inheritance=Inheritance.ALLOW)
public interface GameConfig extends NodeConfig { 
	
	/**
	 * @return The number of threads for the scheduler, defaults to 4
	 */
	@Property(defaultValue="64") 
	public int getEventDaemonThreads();
	
	/**
	 * @return True if the game activator should halt the system on errors in init, false if it should continue
	 */
	@Property(defaultValue="true") 
	public boolean getActivatorHaltOnInitError();

	/**
	 * @return Timeout in millis for a player reconnection, or -1 for default
	 */
	@Property(defaultValue="-1")
	public int getPlayerReconnectTimeout();

	/**
	 * @return Timeout in millis for a player reservation, or -1 for default
	 */
	@Property(defaultValue="-1")
	public int getPlayerReservationTimeout();

	/**
	 * @return True if the notifier should use a commit, false otherwise
	 */
	@Property(defaultValue="true")
	public boolean getUseNotifierCommit();

}
