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
package com.cubeia.firebase.server.gateway.event;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.processing.ReceiverEventDaemonBase;
import com.cubeia.firebase.server.gateway.GatewayConfig;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;

public class ReceivingClientEventDaemon extends ReceiverEventDaemonBase implements Initializable<ClientNodeContext> {

	// private static final int DEF_SCHEDULER_SIZE = 32;
	private static final String SCHEDULER_NAME = "ReceivingClientEventDaemon";
	
	private ClientNodeContext con;
	private ClientDispatcher dispatcher;
	
	// private final Logger log = Logger.getLogger(getClass());
	
	public ReceivingClientEventDaemon(String nodeId) {
		super(nodeId);
	}
	
	
	// -- INITIALIZABLE -- //
	
	public void init(ClientNodeContext con) throws SystemException {
		this.con = con;
		doSuperInit(con);
		initRegistry(con);
	}

	@Override
	public void destroy() {
		super.destroy();
	}
	
	
	// --- BASE METHODS --- //
	
	@Override
	protected void dispatch(RouterEvent event) {
		dispatcher.dispatch(event);
		stats.registerDispatchedEvent();
	}

	@Override
	protected String getSchedulerName() {
		return SCHEDULER_NAME;
	}
	
	public ClassLoader getTargetClassLaoder(Event<?> e) {
		return null;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void initRegistry(ClientNodeContext con) {
		ClientRegistry reg = con.getServices().getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
		dispatcher = new ClientDispatcher(reg);
	}

	private void doSuperInit(ClientNodeContext con) {
		MBeanServer mbs = con.getMBeanServer();
		ClientNodeRouter router = con.getNodeRouter();
		Receiver<RouterEvent> receiver = router.getClientEventReceiver();
		super.init(getSchedulerSize(), mbs, receiver);
	}
	
	private int getSchedulerSize() {
		ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		GatewayConfig conf = contr.getConfiguration(GatewayConfig.class, NodeRoles.getNodeNamespace(ClusterRole.CLIENT_NODE, nodeId));
		return conf.getEventDaemonThreads();
	}
}
