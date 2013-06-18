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
package com.cubeia.firebase.server.gateway;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.util.SocketAddress;

@Configurated(inheritance=Inheritance.ALLOW)
public interface ServerConfig extends Configurable {

	/**
	 * @return The address the client server connections should be made to, default to "0.0.0.0:4123"
	 */
	@Property(defaultValue="0.0.0.0:4123") 
	public SocketAddress getClientBindAddress();
		
	/**
	 * @return The address the client web connections should be made to, default to "0.0.0.0:8080"
	 */
	@Property(defaultValue="0.0.0.0:8080") 
	public SocketAddress getWebClientBindAddress();
	
	/**
	 * @return The address the client web SSL connections should be made to, default to "0.0.0.0:8443"
	 */
	@Property(defaultValue="0.0.0.0:8443") 
	public SocketAddress getWebClientSslBindAddress();
	
}
