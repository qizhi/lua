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
package com.cubeia.firebase.server.util;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.service.mcastgen.MCastGenerationService;

/**
 * This simple utility class can be used to wrap a jgroups config to 
 * provide generated mcast addresses.
 * 
 * @author Larsan
 */
public class MCastgenWrapper {

	private final ServiceRegistry reg;

	/**
	 * @param reg Service registry, must not be null
	 */
	public MCastgenWrapper(ServiceRegistry reg) {
		Arguments.notNull(reg, "service registry");
		this.reg = reg;
	}
	
	/**
	 * This method checks if the given jgroups config has an mcast address
	 * configured. Otherwise the serviceId will be used to generate an mcast
	 * address and wrap the config for returning.
	 * 
	 * @param parent Parent config, must not be null
	 * @param serviceId Service to check/generate address for, must not be null
	 * @return Either to parent config or a new wrapper with an mcast addres, never null
	 */
	public JGroupsConfig checkWrap(JGroupsConfig parent, String serviceId) {
		Arguments.notNull(parent, "parent");
		Arguments.notNull(serviceId, "serviceId");
		if(parent.getMcastAddress() != null) {
    		return parent;
    	} else {
	    	MCastGenerationService gen = reg.getServiceInstance(MCastGenerationService.class);
			try {
				final SocketAddress address = gen.getGeneratedAddress(serviceId);
				return new JGroupsConfigAdapter(parent) {
					
					@Override
					public SocketAddress getMcastAddress() {
						return address;
					}
				};
			} catch (UnknownHostException e) {
				Logger.getLogger(getClass()).error("Failed to generate mcast address", e);
				return parent;
			}
    	}
	}
}
