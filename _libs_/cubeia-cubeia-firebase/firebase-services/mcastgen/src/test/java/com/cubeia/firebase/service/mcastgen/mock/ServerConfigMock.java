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
package com.cubeia.firebase.service.mcastgen.mock;

import java.io.File;

import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.firebase.server.instance.ServerConfig;

public class ServerConfigMock implements ServerConfig {
	
	@Override
	public long getClusterPingTimeout() {
		return 0;
	}

	public StringList getAutostartServices() {
		return null;
	}

	public SocketAddress getClusterBindAddress() {
		return null;
	}

	public int getClusterBindAddressEndPort() {
		return 9999;
	}

	public int getClusterBindAddressStartPort() {
		return 0;
	}

	public SocketAddress getClusterMcastAddress() {
		return null;
	}

	public File getLogDirectory() {
		return null;
	}
	
	public String getClusterBindInterface() {
		return null;
	}
}
