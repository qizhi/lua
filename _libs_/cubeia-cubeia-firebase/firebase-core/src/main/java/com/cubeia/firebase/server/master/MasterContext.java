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

import java.io.File;

import com.cubeia.firebase.server.instance.ServerConfig;
import com.cubeia.firebase.server.node.NodeContext;
import com.cubeia.firebase.server.routing.MasterNodeRouter;

public interface MasterContext extends NodeContext {

	/*
	 * @return true - If this is a single server installation, containing all node types in one VM
	 */
	// public boolean isSingleton();
	
	public File getConfigDirectory();
	
	public ServerConfig getClusterConfig();
	
	public MasterNodeRouter getNodeRouter();
	
	/**
	 * @param The current members as known by the master, may be null
	 * @return true - If this is the primary maser in the cluster, this value is dynamic
	 */
	// public boolean isPrimary(ClusterNode[] nodes);
}
