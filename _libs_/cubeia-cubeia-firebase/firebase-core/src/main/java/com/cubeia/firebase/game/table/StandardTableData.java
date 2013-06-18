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
package com.cubeia.firebase.game.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.GameSeat;
import com.cubeia.firebase.api.game.table.Seat;

/**
 * A simple "bean like" shell for containing the table data 
 * for a given table. This object is not thread safe.
 * 
 * @author Lars J. Nilsson
 */
public abstract class StandardTableData implements Serializable, TableData {

	private static final long serialVersionUID = -767499962927947330L;

	protected byte[] stateData;

	protected InternalMetaData metaData;
    
	protected List<Seat<GenericPlayer>> seats;
	protected Map<Integer, GenericPlayer> players;
	protected Set<Integer> watchingPlayers;
	
	protected byte[] scheduledActions; // serialized map UUID -> scheduled action

    /**
     * Create a new table data. All collections and other member
     * variables will be null.
     */
    public StandardTableData() { }
    
    /**
     * @param metaData Table meta data, may be null
     * @param initCollections True if the collections should initialized, false otherwise
     * @param numSeats Number of seats to use when initializing collections
     */
    public StandardTableData(InternalMetaData metaData, boolean initCollections, int numSeats) { 
    	setMetaData(metaData);
    	if(initCollections) {
    		initCollections(numSeats);
    	}
    }
    
    
    // --- TABLE DATA --- //
    
	public InternalMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(InternalMetaData metaData) {
		this.metaData = metaData;
	}
	
	public Map<Integer, GenericPlayer> getPlayers() {
		return players;
	}
	
	public byte[] getScheduledActions() {
		return scheduledActions;
	}
	
	public List<Seat<GenericPlayer>> getSeats() {
		return seats;
	}

	public byte[] getStateData() {
		return stateData;
	}
	
	public void setStateData(byte[] stateData) {
		this.stateData = stateData;
	}
	
	public Set<Integer> getWatchingPlayers() {
		return watchingPlayers;
	}
	
	public void setPlayers(Map<Integer, GenericPlayer> players) {
		this.players = players;
	}
	
	public void setScheduledActions(byte[] scheduledActions) {
		this.scheduledActions = scheduledActions;
	}
	
	public void setSeats(List<Seat<GenericPlayer>> seats) {
		this.seats = seats;
	}
	
	public void setWatchingPlayers(Set<Integer> watchingPlayers) {
		this.watchingPlayers = watchingPlayers;
	}
	
	public int getId() {
		return metaData.getTableId();
	}
	
	
	// --- PROTECTED METHODS --- //
	
    /**
     * Initialize all collections. The number of seats is used
     * to populate the seating list, use null
     */
    protected void initCollections(int numSeats) {
    	players = new TreeMap<Integer, GenericPlayer>();
    	// scheduledActions = new TreeMap<UUID, ScheduledAction>();
    	watchingPlayers = new TreeSet<Integer>();
    	seats = new ArrayList<Seat<GenericPlayer>>(numSeats < 0 ? 10 : numSeats);
	    for (int i = 0; i < numSeats; ++i) {
	    	seats.add(new GameSeat<GenericPlayer>(i));
	    }		
    }
    
    /*
//	 * Externalizable implemenation. */
//	 * NOTE: This method will be used instead of regular java serialization.
//	 * We have implemented this for more efficient serializing, i.e. less data.
//	 */
//	@SuppressWarnings("unchecked")
//	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//		metaData = (InternalMetaData)in.readObject();
//		watchingPlayers = (Set<Integer>)in.readObject();
//		
//		// Players
//		players = new TreeMap<Integer, GenericPlayer>();
//		byte playerSize = in.readByte();
//		for (int i = 0; i < playerSize; i++) {
//			GenericPlayer player = (GenericPlayer)in.readObject();
//			players.put(player.getPlayerId(), player);
//		}
//		
//		// Scheduled actions
//		short actions = in.readShort();
//		scheduledActions = new byte[actions];
//		in.read(scheduledActions);
//		
//		// State Data
//		short dataSize = in.readShort();
//		stateData = new byte[dataSize];
//		in.read(stateData);
//		
//		// Seats
//		seats = (List<Seat<GenericPlayer>>)in.readObject();
//		
//	}

	/* Enable below to use Externalize instead of regular serialization */
//	/**
//	 * Externalizable implemenation.
//	 * NOTE: This method will be used instead of regular java serialization.
//	 * We have implemented this for more efficient serializing, i.e. less data.
//	 */
//	public void writeExternal(ObjectOutput out) throws IOException {
//		out.writeObject(metaData);
//		out.writeObject(watchingPlayers);
//		
//		// Players
//		out.write(players.size());
//		for (GenericPlayer player : players.values()) {
//			out.writeObject(player);
//		}
//		
//		// Scheduled actions
//		if (scheduledActions != null) {
//			out.writeShort(scheduledActions.length);
//			out.write(scheduledActions);
//		} else {
//			out.writeShort(0);
//		}
//		
//		// Scheduled actions
//		if (stateData != null) {
//			out.writeShort(stateData.length);
//			out.write(stateData);
//		} else {
//			out.writeShort(0);
//		}
//		
//		// Seats
//		out.writeObject(seats);
//		
//	}
}
