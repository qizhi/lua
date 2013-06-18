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
package com.cubeia.firebase.server.lobby.model;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;

/**
 * Data object for table information. Holds some lobby 
 * information for a Table.
 * 
 * @author Fredrik
 */
public class TableInfo {
	
	private String fqn;
	private int id = -1;
	private String name;
	private int capacity = 0;
	private int seated = 0;
	private int watching = 0;
	private int gameId = -1;
	
	private List<Parameter<?>> params = new ArrayList<Parameter<?>>();
	
	/**
	 * Empty constructor
	 * 
	 */
	public TableInfo(){}
	
	/**
	 * Construct a TableInfo for a given table.
	 * 
	 * @param table
	 */
	public TableInfo(FirebaseTable table) {
		InternalMetaData meta = table.getMetaData();
		TablePlayerSet plyrs = table.getPlayerSet();
		id = table.getId();
		name = meta.getName();
		capacity = plyrs.getSeatingMap().getNumberOfSeats();
		seated = plyrs.getSeatingMap().countSeatedPlayers();
		watching = table.getWatcherSet().getCountWatchers();
		gameId = meta.getGameId();
	}

	public String toString() {
		String info = "TableInfo - id["+id+"] name["+name+"] capacity["+capacity+"] seated["+seated+"] " +
				"Params:\n";
		for (Parameter<?> p : params) {
			info += "\t\t "+p.getKey()+" : "+p.getValue()+"\n";
		}
		
		return info;
	}
	
	
	
	public String getFqn() {
		return fqn;
	}

	public void setFqn(String fqn) {
		this.fqn = fqn;
	}

	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getSeated() {
		return seated;
	}
	
	public void setSeated(int seated) {
		this.seated = seated;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getWatching() {
		return watching;
	}

	public void setWatching(int watching) {
		this.watching = watching;
	}

	public List<Parameter<?>> getParams() {
		return params;
	}
	
	public void addParam(Parameter<?> param) {
		params.add(param);
	}
}
