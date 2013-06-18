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
package com.cubeia.firebase.server.instance;

import java.io.File;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;

@Configurated(inheritance=Inheritance.ALLOW)
public interface ServerConfig extends Configurable {
	
	public SocketAddress getClusterMcastAddress();

	/**
	 * The socket address should not contain a port (ie be -1). However, for
	 * legacy reasons this is permitted, but the port will be ignored.
	 * 
	 * @return The cluster bind address, should not contain port
	 */
	public SocketAddress getClusterBindAddress();
	
	/**
	 * @return The bind interface name (eg. 'eth1'), or null
	 */
	public String getClusterBindInterface();
	
	@Property(defaultValue="7800")
	public int getClusterBindAddressStartPort();
	
	@Property(defaultValue="7900")
	public int getClusterBindAddressEndPort();
	
	public StringList getAutostartServices();
	
	@Property(defaultValue="logs/") 
	public File getLogDirectory(); 
	
	@Property(defaultValue="500") 
	public long getClusterPingTimeout();
	
}
