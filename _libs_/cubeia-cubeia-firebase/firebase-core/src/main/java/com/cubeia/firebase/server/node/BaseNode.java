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
package com.cubeia.firebase.server.node;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.Handshake;
import com.cubeia.firebase.server.commands.Leave;
import com.cubeia.firebase.server.commands.Promotion;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.master.ClusterParticipant;
import com.cubeia.firebase.server.master.NodeId;
import com.cubeia.firebase.server.master.ServerId;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandResponse;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;


/*
 * This base class automatically sends a handshake on init() and waits
 * for the master to respond with the cluster configuration. Should the master
 * not respond within CONFIG_MAX_WAIT an illegal state exception is raised.
 */

public abstract class BaseNode<T extends NodeContext> implements Node<T> {
	
	// private static final long CONFIG_MAX_WAIT = 10000;
	
	
	/// --- INSTANCE MEMBERS --- ///

	protected final ClusterRole role;
	protected T con;
	protected final Logger log;
	protected volatile State state;

	private final String id;
	
	// private final AtomicBoolean hasConfig = new AtomicBoolean(false);
	protected final AtomicBoolean isCoordinator = new AtomicBoolean(false);
	protected final AtomicBoolean isHalted = new AtomicBoolean(false);
	
	public BaseNode(String id, ClusterRole role) {
		Arguments.notNull(role, "role");
		Arguments.notNull(id, "id");
		log = Logger.getLogger(getClass());
		state = State.STOPPED;
		this.role = role;
		this.id = id;
	}
	
	public void halt() {
		isHalted.set(true);
	}
	
	public boolean isHalted() {
		return isHalted.get();
	}
	
	public void resume() {
		isHalted.set(false);
	}
	
	public String getStateDescription() {
		return state.toString();
	}
	
	public void start() {
		state = State.STARTED;
	}
	
	public void stop() {
		state = State.STOPPED;
	}
	
	public String getId() {
		return id;
	}
	
	public void destroy() {
		if(sendAutomaticHandshake()) {
			sendLeave();
		}
		con = null;
	}

	public void init(T con) throws SystemException {
		Arguments.notNull(con, "context");
		this.con = con;
		if(listenForPromotions()) {
			startPromotionListening();
		}
		if(sendAutomaticHandshake()) {
			sendHandshake();
		}
	}
	
	
	/// --- COMMON MBEAN METHODS --- //
	
	public String getServerId() {
    	return con.getServerId().toString();
    }
	
	public String getNodeId() {
		return getId();
	}
	
	public boolean isCoordinator() {
		return isCoordinator.get();
	}
	
	
	/// --- PROTECTED METHODS --- ///
	
	/*protected abstract ClusterConnection getClusterConnection();
	
	protected abstract SocketAddress getNodeAddress();*/
	
	protected final ClusterConnection getClusterConnection() {
		ConnectionServiceContract service = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		return service.getSharedConnection();
	}
	
	protected boolean sendAutomaticHandshake() {
		return true;
	}
	
	protected boolean listenForPromotions() { return false; }
	
	/*
	 * This method will be called if the current node 
	 * get promoted to coordinator for its type.
	 */
	protected void promoted() { }
	
    /*protected void sendHandshake() throws SystemCoreException {
    	Handshake h = new Handshake();
    	h.setAttachment(createParticipant());
    	ClusterConnection conn = getClusterConnection();
    	try {
			conn.getCommandDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, h);
		} catch (ClusterException e) {
			throw new SystemCoreException("Failed to send handshake", e);
		}
	}*/
    
    protected void sendHandshake() throws SystemCoreException {
    	Handshake h = new Handshake();
    	h.setAttachment(createParticipant());
    	ClusterConnection conn = getClusterConnection();
    	// Listener l = startHandshakeListening(conn);
		try {
			CommandResponse[] rsps = conn.getCommandDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, h);
			checkHandshakeResponse(rsps);
			// waitForConfig();
		} catch (ClusterException e) {
			log.error("Failed to send handshake", e);
		} /*finally {
			stopHandshakeListening(conn, l);
		}*/
	}
    
	private void sendLeave() {
		Leave l = new Leave();
    	l.setAttachment(createParticipant());
    	ClusterConnection conn = getClusterConnection();
    	// Listener l = startHandshakeListening(conn);
		try {
			conn.getCommandDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, l);
			// conn.getCommandDispatcher().dispatch(l);
		} catch (ClusterException e) {
			log.error("Failed to send leave", e);
		} /*finally {
			stopHandshakeListening(conn, l);
		}*/	
	}
    
	private void checkHandshakeResponse(CommandResponse[] rsps) throws SystemCoreException {
		for (CommandResponse rsp : rsps) {
			Object[] arr = (Object[])rsp.getAnswer();
			if(rsp.isReceived() && arr != null && arr.length > 0) {
				for (Object s : arr) {
					if(s instanceof Boolean) {
						if(((Boolean)s).booleanValue()) {
							return; // SUCESSFULL ENTRANCE TO CLUSTER
						} else {
							throw new SystemCoreException("Master node denied entrance to cluster!");
						}
					}
				}
			}
		}
		throw new SystemCoreException("Master node failed to answer handshake!");
	}
    
	/*private void stopHandshakeListening(ClusterConnection conn, Listener l) {
		conn.getCommandReceiver().removeCommandListener(l);
	}

	private Listener startHandshakeListening(ClusterConnection conn) {
		Listener l = new Listener();
    	conn.getCommandReceiver().addCommandListener(l);
		return l;
	}*/
    
	protected MBusContract getMBus() {
		return con.getServices().getServiceInstance(MBusContract.class);
	}

	/*private void stopHandshakeListening(ClusterConnection conn, Listener l) {
		conn.getCommandReceiver().removeCommandListener(null, l);
	}

	private Listener startHandshakeListening(ClusterConnection conn) {
		Listener l = new Listener();
    	conn.getCommandReceiver().addCommandListener(null, l);
		return l;
	}*/
	
	private ClusterParticipant createParticipant() {
		return new ClusterParticipant(new NodeId(getRealServerId(), id), role, getMBus().getMBusDetails(), isCoordinator());
	}
    
    protected final ServerId getRealServerId() {
		return con.getServerId();
	}
	
	protected void startPromotionListening() {
		getClusterConnection().getCommandReceiver().addCommandListener(Constants.NODE_LIFETIME_COMMAND_CHANNEL, new CommandListener() {
		
			public Object commandReceived(CommandMessage c) {
				if(c.command instanceof Promotion) {
					Promotion p = (Promotion)c.command;
					ClusterParticipant att = p.getAttachment();
					if(equalsParticpant(att)) {
						isCoordinator.set(true);
						promoted();
					}
				}
				return null;
			}
		});
	}
    
	protected boolean equalsParticpant(ClusterParticipant att) {
		return att.getId().equals(id);
	}
	
    
    /// --- PRIVATE METHODS --- ///

	/*private void waitForConfig() throws SystemCoreException {
		if(shouldWaitForConfig()) {
			// MasterProxy proxy = con.getMasterProxy();
			long time = System.currentTimeMillis();
			long next = time + CONFIG_MAX_WAIT;
			while(!hasConfig.get() && next > System.currentTimeMillis()) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) { break; }
			}
			if(!hasConfig.get()) throw new SystemCoreException("Missing configuration; Cluster configuration provider service failed to respond within " + CONFIG_MAX_WAIT + " millis");	
			time = System.currentTimeMillis() - time;
			if(time > 1000) {
				log.warn("Node '" + getId() + "' waited " + time + " millis for configuration!");
			}
		}
	}

	private boolean shouldWaitForConfig() {
		return (!role.equals(ClusterRole.MASTER_NODE) || getClusterConnection().countMembers() > 1);
	}*/
	
	
	// --- PRIVATE CLASSES --- //
	
	/*private class Listener implements CommandListener {
		
		public Object commandReceived(CommandMessage c) {
			if(c.command instanceof Config) {
				hasConfig.set(((Config)c.command).getType().equals(Type.DELTA_INIT));
			}
			return null;
		}
	}*/
}
