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
package com.cubeia.core.space.jboss;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.util.JGroupsConfig;

@Configurated(inheritance=Inheritance.ALLOW)
public interface TreeCacheClusterConfig extends JGroupsConfig {

	// @Property(defaultValue="224.224.100.101:2122") 
	public SocketAddress getMcastAddress();
	
	@Property(defaultValue="REPL_ASYNC") public String getCacheMode();
	
	@Property(defaultValue="REPEATABLE_READ") public String getIsolationLevel();
	
	@Property(defaultValue="false") public boolean getUseReplQueue();
	
	@Property(defaultValue="100") public long getReplQueueInterval();

	@Property(defaultValue="false") public boolean getUseBuddyReplication();
	
	@Property(defaultValue="1") public int getNumberOfBuddies();
	
	@Property(defaultValue="2000") public long getBuddyCommTimeout();
	
	@Property(defaultValue="50") public long getReplQueueMaxElements();
	
	@Property(defaultValue="false") public boolean isJtaEnabled();
	
	@Property(defaultValue="true") public boolean isFailOverEnabled();
	
	@Property(defaultValue="5000") public long getLockAquisitionTimeout();
	
	@Property(defaultValue="20000") public long getSynchReplicationTimeout();
	
}
