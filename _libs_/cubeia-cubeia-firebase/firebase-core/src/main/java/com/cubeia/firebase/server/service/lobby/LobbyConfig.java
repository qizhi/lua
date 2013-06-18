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
package com.cubeia.firebase.server.service.lobby;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;

/**
 * @author Larsan
 */
@Configurated(inheritance=Inheritance.ALLOW)
public interface LobbyConfig extends Configurable {

	/**
	 * This value determines the minimum frequency for 
	 * lobby access for a client. So if this method returns
	 * 500 no client can access the lobby more than once every
	 * 500 milliseconds.
	 * 
	 * @return Max frequency in millis, or -1 for no limit
	 */
	public long getMaxFixedAccessFrequency();
	
	/**
	 * The lobby can also restrict access over a period of milliseconds. 
	 * This method determines the interval length in milliseconds. This
	 * method is used together with {@link #getIntervalAccessFrequency()}.
	 * 
	 * @return The interval length in millis, or -1 for disabling
	 */
	public long getIntervalAccessFrequencyLength();
	
	/**
	 * The lobby can also restrict access over a period of milliseconds. 
	 * This method determines the number of accesses within the interval. This
	 * method is used together with {@link #getIntervalAccessFrequencyLength()}.
	 * 
	 * @return The number of accesses within the interval, or -1 for disabling
	 */
	public int getIntervalAccessFrequency();
	
}
