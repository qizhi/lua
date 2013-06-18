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
package com.cubeia.firebase.server.master;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.server.node.NodeConfig;

@Configurated(inheritance=Inheritance.ALLOW) 
public interface MasterConfig extends NodeConfig { 
	
	// @Property(defaultValue="localhost:6880") public SocketAddress getCommAddress();
	
	@Property(defaultValue="1") public int getSchedulerPoolInitSize();
	
	@Property(defaultValue="500") public long getResumeCommandDelay();
	
	@Property(defaultValue="50") public long getConfigCommandDelay();
	
}
