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
package com.cubeia.firebase.server.instance;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.mtt.MttNode;
import com.cubeia.firebase.server.mtt.MttNodeContext;
import com.cubeia.firebase.server.node.NodeContext;
import com.cubeia.firebase.server.routing.MttNodeRouter;
import com.cubeia.firebase.server.routing.impl.MttRouterImpl;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.service.messagebus.MBusContract;

public class MttNodeContextImpl extends RoutingNodeContextImpl<MttNode> implements MttNodeContext {

	private final MttRouterImpl impl;
	
	MttNodeContextImpl(ClusterRole role, MttNode node, ServerInstance inst) {
		super(role, node, inst);
		impl = new MttRouterImpl(node.getId(), "MTT Node " + node.getId());
	}
	
	@Override
	public void halt() {
		impl.halt();
	}
	
	@Override
	public void resume() {
		impl.resume();
	}
	
	@Override
	public void init(final NodeContext con) throws SystemException {
		super.init(con);
		impl.init(new RouterContext() {
			
			public String getServerId() {
				return con.getServerId().id;
			}
		
			public MBusContract getMessageBus() {
				return inst.getServiceRegistry().getServiceInstance(MBusContract.class);
			}
			
			public ServiceRegistry getServices() {
				return con.getServices();
			}
		
			public MBeanServer getMBeanServer() {
				return inst.getMBeanServer();
			}
		});
	}
	
	@Override
	public void destroy() {
		impl.destroy();
		super.destroy();
	}
	
	public MttNodeRouter getNodeRouter() {
		return impl;
	}
}
