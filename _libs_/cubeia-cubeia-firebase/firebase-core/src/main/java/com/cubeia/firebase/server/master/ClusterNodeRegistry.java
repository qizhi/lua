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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.util.Lists;

/**
 * This register keeps track of all known nodes in the cluster. It also
 * knows the coordinators for each cluster role. 
 * 
 * @author lars.j.nilsson
 */
class ClusterNodeRegistry implements ClusterNodeRegistryMBean {
	
	private final Map<ClusterRole, ClusterParticipant> coords;
	private final Map<NodeId, ClusterParticipant> nodes;
	private final Map<SocketAddress, List<ClusterParticipant>> serverMap;
	
	private final MBeanServer mbs;
	// private final GameObjectSpace<DefaultTable, GameAction> tables;
	// private final ClusterParticipant self;
	// private final String jmsURi;
	
	ClusterNodeRegistry(MBeanServer mbs) { //ClusterParticipant self) {
		this.mbs = mbs;
		nodes = new HashMap<NodeId, ClusterParticipant>(5);
		coords = new EnumMap<ClusterRole, ClusterParticipant>(ClusterRole.class);
		serverMap = new HashMap<SocketAddress, List<ClusterParticipant>>(5);
		// this.jmsURi = jmsURi;
		// this.tables = tables;
		// this.self = self;
		// populate the referring master node itself
		doClean();
		initJmx();
	}
	
	/*public String findServerId(Partition p) {
		EventType type = p.getType();
		SocketAddress a = p.getSocketId();
		for (NodeId id : nodes.keySet()) {
			ClusterParticipant node = nodes.get(id);
			MBusDetails det = node.getMBusDetails();
			if(det.getSocketIdFor(type).equals(a)) {
				return id.server.id;
			}
		}
		return null;
	}*/
	
	private void initJmx() {
		if(mbs == null) return; // SANITY CHECK
		try {
			ObjectName name = new ObjectName("com.cubeia.firebase:type=ClusterNodeRegistry");
			mbs.registerMBean(this, name);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("Failed to mound mbean", e);
		} 
	}

	@Override
	public String toString() {
		boolean first = true;
		StringBuilder b = new StringBuilder();
		b.append("ClusterNodeRegistry [");
		for (ClusterParticipant p : getAllNodes()) {
			if(!first) b.append(" ");
			b.append(p);
			first = false;
		}
		b.append("]");
		return b.toString();
	}
	
	public synchronized ClusterLayout getLayout() {
		return new SimpleLayout(nodes.values());
	}
	
	public synchronized ClusterParticipant getCoordinator(ClusterRole r) {
		Arguments.notNull(r, "r");
		return coords.get(r);
	}
	
	public synchronized boolean isCoordinator(ClusterParticipant p) {
		Arguments.notNull(p, "p");
		ClusterRole r = p.getRole();
		ClusterParticipant check = coords.get(r);
		return p.equals(check);
	}
	
	
	/**
	 * This method returns all node participants in no particular order. Each
	 * returned object will be marked with a correct boolean determining node
	 * coordinator.
	 * 
	 * @return The cluster nodes, with marked coordinators, never null
	 */
	public synchronized ClusterParticipant[] getAllNodes() {
		return Lists.toArray(nodes.values(), ClusterParticipant.class);
	}
	
	/*public synchronized boolean isGameNode(SocketAddress ad) {
		return gameNodes.containsKey(ad);
	}
	
	public synchronized boolean isMttNode(SocketAddress ad) {
		return mttNodes.containsKey(ad);
	}*/
	
	synchronized void registerAll(ClusterParticipant[] nodes, boolean doClean) {
		if(doClean) doClean();
		for (ClusterParticipant node : nodes) {
			registerUpOnServer(node);
			this.nodes.put(node.getNodeId(), node);
			if(node.isCoordinator()) {
				coords.put(node.getRole(), node);
			}
		}
	}

	/**
	 * @param node Node that just appeared
	 * @return True if the node is the new coordinator
	 */
	public synchronized boolean registerNodeUp(ClusterParticipant node) {
		Arguments.notNull(node, "node");
		nodes.put(node.getNodeId(), node);
		registerUpOnServer(node);
		return checkCoordinator(node);
	}
	
	
	/**
	 * @param ad The participant for a given address, never null
	 * @return The participant, or null if not found
	 */
	public synchronized ClusterParticipant getNode(NodeId id) {
		return nodes.get(id);
	}

	public synchronized void registerNodeDown(NodeId id) {
		nodes.remove(id);
		registerDownOnServer(id);
		checkCoordinatorRem(id);
	}
	
	public synchronized void registerServerDown(SocketAddress ad) {
		List<ClusterParticipant> list = getNodesForServer(ad, false);
		for (ClusterParticipant p : list) {
			registerNodeDown(p.getNodeId());
		}
	}
	
	/**
	 * @param ad Address of the server, must not be null
	 * @param clone True to return a cloned list and not real references
	 * @return A list of the servers, this list is a copy, never null
	 */
	public synchronized List<ClusterParticipant> getNodesForServer(SocketAddress ad, boolean clone) {
		List<ClusterParticipant> list = serverMap.get(ad);
		if(list != null) {
			if(clone) {
				LinkedList<ClusterParticipant> l = new LinkedList<ClusterParticipant>();
				for (ClusterParticipant p : list) {
					l.add(new ClusterParticipant(p));
				}
				return l;
			} else {
				return new LinkedList<ClusterParticipant>(list);
			}
		} else {
			return new LinkedList<ClusterParticipant>();
		}
	}
	
	synchronized boolean haveGameOrMttNodes(SocketAddress server) {
		List<ClusterParticipant> list = serverMap.get(server);
		if(list == null) {
			return false;
		} else {
			return MasterNode.doPartitionChangesFor(list);
		}
	}

	
	// --- MBEAN --- //

	public synchronized int getRegistrySize() {
		return nodes.size();
	}
	
	public String[] printAllParticipants() {
		int count = 0;
		ClusterParticipant[] nodes = getAllNodes();
		String[] arr = new String[nodes.length];
		for (ClusterParticipant p : nodes) {
			arr[count++] = p.toString();
		}
		return arr;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void registerDownOnServer(NodeId id) {
		SocketAddress address = id.server.address;
		List<ClusterParticipant> list = serverMap.get(address);
		if(list != null) {
			for (Iterator<ClusterParticipant> it = list.iterator(); it.hasNext(); ) {
				ClusterParticipant tmp = it.next();
				if(tmp.getNodeId().equals(id)) {
					it.remove();
					break;
				}
			}
			if(list.size() == 0) {
				serverMap.remove(address);
			}
		}
	}
	
	private void registerUpOnServer(ClusterParticipant node) {
		SocketAddress address = node.getNodeId().server.address;
		List<ClusterParticipant> list = serverMap.get(address);
		if(list == null) {
			list = new LinkedList<ClusterParticipant>();
			serverMap.put(address, list);
		}
		list.add(node);
	}
	
	private void checkCoordinatorRem(NodeId id) {
		ClusterRole role = checkCoordinatorRole(id);
		if(role != null) {
			ClusterParticipant tmp = coords.remove(role);
			if(tmp != null) tmp.setIsCoordinator(false);
			ClusterParticipant next = findRole(role);
			if(next != null) {
				next.setIsCoordinator(true);
				coords.put(role, next);
			}
		}
	}

	private ClusterParticipant findRole(ClusterRole role) {
		for (ClusterParticipant p : nodes.values()) {
			if(p.getRole().equals(role)) {
				return p; // EARLY RETURN
			}
		}
		return null;
	}

	private ClusterRole checkCoordinatorRole(NodeId id) {
		ClusterRole role = null;
		for (ClusterRole r : coords.keySet()) {
			ClusterParticipant p = coords.get(r);
			if(p.getNodeId().equals(id)) {
				// Oooops, coordinator removed
				role = r;
				break;
			}
		}
		return role;
	}
	
	private boolean checkCoordinator(ClusterParticipant node) {
		ClusterRole role = node.getRole();
		if(!coords.containsKey(role)) {
			node.setIsCoordinator(true);
			coords.put(role, node);
			return true;
		} else return false;
	}
	
	private void doClean() {
		serverMap.clear();
		this.nodes.clear();
	}
}
