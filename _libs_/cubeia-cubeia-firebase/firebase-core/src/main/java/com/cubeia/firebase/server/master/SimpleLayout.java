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
/**
 * 
 */
package com.cubeia.firebase.server.master;

import java.util.Collection;

import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.util.ArrayUtil;

class SimpleLayout implements ClusterLayout {
	
private static final long serialVersionUID = 2805780967425559840L;
	
	// private TablePartition[] partitions = new TablePartition[0];
	private ClusterParticipant[] nodes = new ClusterParticipant[0];
	
	SimpleLayout() { }
	
	SimpleLayout(Collection<ClusterParticipant> mems) { 
		addNodes(mems);
	}
	
	/*synchronized void addPartition(TablePartition p) {
		partitions = ArrayUtil.add(partitions, p);
	}
	
	synchronized TablePartition getPartition(String id) {
		for (TablePartition p : partitions) {
			if(p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}
	
	synchronized TablePartition removePartition(String id) {
		TablePartition remed = null;
		List<TablePartition> list = toList(partitions);
		for (Iterator<TablePartition> it = list.iterator(); it.hasNext(); ) {
			TablePartition tmp = it.next();
			if(tmp.getId().equals(id)) {
				remed = tmp;
				it.remove();
				break;
			}
		}
		partitions = list.toArray(new TablePartition[list.size()]);
		return remed;
	}
	
	private List<TablePartition> toList(TablePartition[] parts) {
		List<TablePartition> list = new LinkedList<TablePartition>();
		for (TablePartition tmp : parts) {
			list.add(tmp);
		}
		return list;
	}

	synchronized void removePartition(TablePartition p) {
		partitions = ArrayUtil.rem(partitions, p);
	}*/
	
	synchronized void addNode(ClusterParticipant p) {
		nodes = ArrayUtil.add(nodes, p);
	}
	
	synchronized void addNodes(Collection<ClusterParticipant> members) {
		nodes = ArrayUtil.add(nodes, members);
	}
	
	synchronized void removeNode(ClusterParticipant p) {
		nodes = ArrayUtil.rem(nodes, p);
	}
	
	public ClusterParticipant[] getClusterNodes() {
		return nodes;
	}
	
	public synchronized SocketAddress getPrimaryMasterAddress() {
		for (ClusterParticipant node : nodes) {
			if(node.isMaster() && node.isCoordinator()) {
				return node.getServerId().address;
			}
		}
		// FIXME: Illegal state?
		throw new IllegalStateException("missing primary master");
	}
	
	/*public synchronized TablePartition[] getTablePartitions() {
		return partitions;
	}*/
	
	public synchronized String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Layout: Nodes: {");
		for(int i = 0; i < nodes.length; i++) {
			b.append(nodes[i].toString());
			if(i + 1 < nodes.length) {
				b.append(" ");
			}
		}
		b.append("}");
		return b.toString();
	}
}