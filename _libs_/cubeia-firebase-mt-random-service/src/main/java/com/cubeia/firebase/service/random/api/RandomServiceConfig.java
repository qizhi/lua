/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
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

package com.cubeia.firebase.service.random.api;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Property;

/**
 * Cluster configuration contract for the {@link RandomService random 
 * service}. Will be used to enable, disable and configure background 
 * polling and discarded draws.
 * 
 * @author Lars J. Nilsson
 */
@Configurated(namespace="com.cubeia.firebase.service.random")
public interface RandomServiceConfig extends Configurable {

	/**
	 * @return True to enable background polling, false to disable, defaults to true
	 */
	@Property(defaultValue="true")
	public boolean enableBackgroundPolling();
	
	/**
	 * @return Background polling interval in milliseconds, defaults to 1000
	 */
	@Property(defaultValue="1000")
	public long getBackgroundPollingInterval();
	
	/**
	 * @return Max number of discarded items from the sequence used by the background polling, defaults to 10
	 */
	@Property(defaultValue="10")
	public int getBackgroundPollingMaxDiscarded();

	/**
	 * @return True to enable discarded draws, false to disable, defaults to false
	 */
	@Property(defaultValue="false")
	public boolean enableDiscardedDraw();
	
	/**
	 * @return Max number of discarded items from the sequence used by the discarded drawing, defaults to 5
	 */
	@Property(defaultValue="5")
	public int getDiscardedDrawMaxDiscarded();

}
