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
package com.cubeia.firebase.server.manager;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.node.BaseNode;
import com.cubeia.firebase.server.util.NodeRoles;

public class ManagerNode extends BaseNode<ManagerContext> {

	
	/// --- INSTANCE MEMBERS --- ///
	
	protected final Logger log;
	private ManagerConfig config;
	
	public ManagerNode(String id) {
		super(id, ClusterRole.MANAGER_NODE);
		log = Logger.getLogger(getClass());
	}
	
	public void destroy() { 
		// eventLogger.destroy();
	}

	public void init(ManagerContext context) throws SystemException {
		super.init(context);
		setupConf();
		// setupLog();
	}


	/// --- PROTECTED METHODS --- ///
	
	/// --- PRIVATE METHODS --- ///
	
	private void setupConf() {
		ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		config = contr.getConfiguration(ManagerConfig.class, NodeRoles.getNodeNamespace(ClusterRole.MANAGER_NODE, getId()));
		log.info("Configuration found for node: " + getId() + " (" + config.getName() + ")");
	}
}
