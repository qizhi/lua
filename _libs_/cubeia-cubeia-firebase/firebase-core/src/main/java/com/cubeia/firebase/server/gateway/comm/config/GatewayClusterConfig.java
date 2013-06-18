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
package com.cubeia.firebase.server.gateway.comm.config;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.util.StringList;

@Configurated(inheritance=Inheritance.ALLOW)
public interface GatewayClusterConfig extends Configurable {
	
	public static int DEFAULT_MAXPACKET_SIZE = 32768;
	
	@Property(defaultValue="true") public boolean isValidatePacketSize();
	@Property(defaultValue="false") public boolean isMinaLogginEnabled();
	@Property(defaultValue="false") public boolean isHandshakeEnabled();
	@Property(defaultValue="1128351298") public int getHandshakeSignature();
	@Property(defaultValue="32768") public int getMaxPacketSize();
	@Property(defaultValue="16384") public int getMaxNumberOfSessions();
	@Property(defaultValue="null") public StringList getCustomFilterChain();
	
}
