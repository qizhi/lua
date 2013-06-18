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
package com.cubeia.firebase.service.messagebus.util;

import com.cubeia.firebase.service.messagebus.Channel;

/**
 * Simple interface for the game event deamon or a subcomponent
 * to be notified when a table is moved into or out of its partition.
 * 
 * @author Larsan
 */
public interface ChannelChangeListener {

	/**
	 * @param channels Added channels, never null
	 * @param knownMove True if this is known to be a "move", as opposed to an addition
	 */
	public void addition(Channel[] channels, boolean knownMove);
	
	/**
	 * @param channels removed channels, never null
	 * @param knownMove True if this is known to be a "move", as opposed to an removal
	 */
	public void removal(Channel[] channels, boolean knownMove);
	
}
