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
package com.cubeia.firebase.server.commands;

import java.io.Serializable;

import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.service.messagebus.Partition;

/**
 * This object is used both for creation requests and for responses. In 
 * requests the table id will typically be set to -1 and in responses the
 * attributes are usually set to null.
 * 
 * <p>If a response comes back with id set to -1 it usually indicates the table 
 * could not be created for some reason. Check the logs.
 * 
 * <p>The attachment may be game specific for creation requests. In such case
 * the game class loader must be fetched before the attachment is retrieved.
 * 
 * @author lars.j.nilsson
 */
public final class TableCommandData implements Serializable {

	private static final long serialVersionUID = 8519546130566490443L;
	
	private int id;
	private Attribute[] attributes;
	private LobbyPath lobbyPath;
	private Partition p;
	
	private final String name;
	private final int seats;
	private final int mttId;
	private final TableData data;
	
	public TableCommandData(int id, int mttId, int seats, String name, LobbyPath lobbyPath, Attribute[] atts, Partition p, TableData data) {
		this.mttId = mttId;
		this.seats = seats;
		this.lobbyPath = lobbyPath;
		this.attributes = atts;
		this.name = name;
		this.id = id;
		this.p = p;
		this.data = data;
	}
	
	public TableData getData() {
		return data;
	}
	
	public int getMttId() {
		return mttId;
	}
	
	public String getName() {
		return name;
	}

	public int getSeats() {
		return seats;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return The table partition, only known on destruction, null if not known
	 */
	public Partition getPartition() {
		return p;
	}
	
	public void setPartition(Partition p) {
		this.p = p;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}
	
	public LobbyPath getLobbyPath() {
		return lobbyPath;
	}
	
	public void setLobbyPath(LobbyPath path) {
		this.lobbyPath = path;
	}
}
