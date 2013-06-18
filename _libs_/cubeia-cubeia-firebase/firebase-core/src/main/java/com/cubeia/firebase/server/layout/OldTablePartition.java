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
package com.cubeia.firebase.server.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.EventType;

/**
 * A table partition maps to a mbus partition by id. It contains 
 * cluster information about a game node matching the partition.
 * 
 * @author lars.j.nilsson
 * @date 2007 apr 20
 */
public class OldTablePartition implements Serializable, Cloneable {

	private static final long serialVersionUID = 8568209906635188190L;
	
	
	/// --- INSTANCE MEMBERS --- ///
	
	private Set<Integer> tables;
	private final String id;


	private final EventType type;

	public OldTablePartition(String id, EventType type, int[] tables) {
		//Arguments.notNull(owner, "owner");
		Arguments.notNull(id, "id");
		Arguments.notNull(tables, "tables");
		this.type = type;
		this.id = id;
		//this.owner = owner;
		//this.tables = tables;
		this.tables = new HashSet<Integer>(tables.length);
		for (int tmp : tables) {
			this.tables.add(tmp);
		}
	}
	
	@Override
	public synchronized Object clone() {
		return new OldTablePartition(id, type, getTables());
	}
	
	public EventType getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	/*public SocketAddress getOwner() {
		return owner;
	}*/
	
	public synchronized int countTables() {
		return tables.size();
	}
	
	public synchronized int[] getTables() {
		int count = 0;
		int[] arr = new int[tables.size()];
		for (int id : tables) {
			arr[count++] = id;
		}
		return arr;
	}
	
	public synchronized void addTable(int id) {
		tables.add(id);
	}
	
	public synchronized void addTables(int[] arr) {
		for (int id : arr) {
			tables.add(id);
		}
	}
	
	public void removeTables(int[] arr) {
		for (int id : arr) {
			tables.remove(id);
		}
	}
	
	public synchronized void removeTable(int id) {
		tables.remove(id);
	}
	
	public synchronized String toString() {
		StringBuilder b = new StringBuilder();
		b.append(id).append(" { ");
		for (Iterator<Integer> it = tables.iterator(); it.hasNext(); ) {
			Integer i = it.next();
			b.append(i);
			if(it.hasNext()) {
				b.append(", ");
			}
		}
		b.append("}");
		return b.toString();
	}
}
