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
package com.cubeia.space;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.server.util.JGroupsConfig;

@Configurated(inheritance=Inheritance.ALLOW)
public interface SpaceConfig extends JGroupsConfig {

	@Property(defaultValue="false") 
	public boolean isJtaEnabled();
	
	// @Property(defaultValue="true") 
	// public boolean isFailOverEnabled();
	
	@Property(defaultValue="10000") 
	public long getLockAquisitionTimeout();
	
	@Property(defaultValue="20000") 
	public long getSynchReplicationTimeout();
	
	@Property(defaultValue="REPL_SYNC") 
	public CacheMode getCacheMode();

}
