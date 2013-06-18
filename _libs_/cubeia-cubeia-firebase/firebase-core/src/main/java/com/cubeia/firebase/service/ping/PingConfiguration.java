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
package com.cubeia.firebase.service.ping;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;

@Configurated(inheritance=Inheritance.ALLOW)
public interface PingConfiguration extends Configurable {

	/**
	 * @return Initial time before ping start in millis, defaults to 20000
	 */
	@Property(defaultValue="20000")
	public long getInitialPingDelay();
	
	/**
	 * @return Ping interval in millis, defaults to 5000
	 */
	@Property(defaultValue="5000")
	public long getPingInterval();
	
	/**
	 * @return Ping time to live in millis, defaults to 3000
	 */
	@Property(defaultValue="3000")
	public long getPingTimeout();
	
	/**
	 * @return How many pings may fail? Defaults to 1
	 */
	@Property(defaultValue="1")
	public int getFailureThreshold();

	/**
	 * @return Is ping enabled? Defaults to false
	 */
	@Property(defaultValue="false")
	public boolean isPingEnabled();

	/**
	 * @return Number of threads used for scheduling, defaults to 1
	 */
	@Property(defaultValue="1")
	public int getNumberOfThreads();
	
}
