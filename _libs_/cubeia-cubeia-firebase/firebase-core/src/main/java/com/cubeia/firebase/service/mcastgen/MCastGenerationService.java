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
package com.cubeia.firebase.service.mcastgen;

import java.net.UnknownHostException;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.util.SocketAddress;

/**
 * This service generates mcast addresses for a server. It
 * must be configured, and will look for a configuration file
 * in this order:
 * 
 * <ol>
 *    <li>A configured {@link MCastGeneratorConfig#getServiceConfiguration() file}</li>
 * 	  <li>For the file "mcastgen.conf" in the server configuration dir</li>
 * 	  <li>The default config packaged with the service</li>
 * </ol>
 * 
 * The configuration should be a Java property file in the 
 * following format:
 * 
 * <pre>
 * 	  &lt;ipOffset&gt; [ $ &lt;portOffset&gt; ] = &lt;serviceId&gt;
 * </pre>
 * 
 * The IP offset is done via a pattern much like an ordinary IP mask but where
 * '*' marks 'no change' and any number is an offset. So, given the root address
 * '224.224.1.0' heres a list of possible offest configs:
 * 
 * <pre>
 * 		*.*.*.1 = 224.224.1.1
 * 		*.*.2.2 = 224.224.3.2
 * 		*.-1.-1.10 = 224.223.0.11
 * 		*.1.10.* = 224.225.11.1
 * </pre>
 * 
 * The port offset is optional but similarly counts up and down form a base value. 
 * 
 * <p>The config file may also take the following values:
 * 
 * <pre>
 * 		_basePort = first port to count offset from
 * 		_baseAddress = base mcast address to start from
 * 		_baseNaPort = first non-configured port (default: 8940)
 *      _baseNaAddress = base non-configured address (default: 224.224.50.1)
 * </pre>
 * 
 * The base port defaults to the end of the cluster connection port range if
 * not configured to something else. 
 * 
 * <p>The base address defaults to the cluster connection address if not 
 * configured to something else. 
 * 
 * <p>If a service is not configured the "_baseNaPort" and "_baseNaAddress"
 * values are used as basic values and the service will use a linear increase
 * on the port and the least significant byte in the address. 
 * 
 * @author Larsan
 */
public interface MCastGenerationService extends Contract { 
	
	/**
	 * This method generates an mcast address for a given service. 
	 * Services using this method should use their {@link ServiceContext}
	 * to get hold of their configured public id to use as a 
	 * parameter to this method. 
	 * 
	 * @param serviceId Service public id, must not be null
	 * @return A generated address, never null
	 * @throws UnknownHostException If the generated address is illegal
	 */
	public SocketAddress getGeneratedAddress(String serviceId) throws UnknownHostException;
	
}
