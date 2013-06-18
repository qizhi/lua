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
package com.cubeia.firebase.server.lobby.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.TableRemovedPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentRemovedPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.lobby.systemstate.LobbyTransformer;
import com.cubeia.firebase.server.lobby.systemstate.TableInfoBuilder;
import com.cubeia.firebase.server.lobby.systemstate.TournamentInfoBuilder;
import com.cubeia.firebase.server.util.ParamUtils;
import com.cubeia.util.Lists;

/**
 * Keeps partial (delta) updates for a node in the lobby.
 * This class may contain both TableUpdates as well as TableInfos.
 *
 * @author Fredrik
 */
public class DeltaSnapshot extends AbstractSnapshot {

	private transient Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Map of tableid to table update packets.
	 * These packets are updated when a change is reported.
	 */
	protected ConcurrentMap<Integer, ProtocolObject> deltaPacketsMap = new ConcurrentHashMap<Integer, ProtocolObject>();

	public DeltaSnapshot(LobbyPath path) {
		super(path);
	}
	
	/**
	 * Get the full snapshot as protocol packets.
	 * 
	 * @return
	 */
	@Override
	public List<ProtocolObject> getLobbyData() {
		int size = deltaPacketsMap.values().size() + packets.values().size();
		List<ProtocolObject> allPackets = new ArrayList<ProtocolObject>(size); 
		allPackets.addAll(deltaPacketsMap.values());
		allPackets.addAll(packets.values());
		return allPackets;
	}
	
	public ProtocolObject getObjectData(int objectId) {
		ProtocolObject packet = packets.get(objectId);
		if (packet == null) {
			packet = deltaPacketsMap.get(objectId);
		}
		return packet;
	}	
	
	public void reportNew(int tableid, NodeChangeDTO change) {
		handleFullChange(tableid, change);
	}

	public void reportUpdate(int tableid, NodeChangeDTO change) {
		// Check if we are keeping a full packet
		if (packets.containsKey(tableid)) {
			handleFullChange(tableid, change);
		} else {
			handleDeltaChange(tableid, change);
		}
	}

	public void tableRemoved(LobbyPath path) {
		if (path.getType().equals(LobbyPathType.TABLES)) {
			TableRemovedPacket packet = new TableRemovedPacket();
			packet.tableid = path.getObjectId();
			// Place in map, thus overwriting any delta changes that are no longer of interest
			deltaPacketsMap.put(path.getObjectId(), packet);
			
		} else if (path.getType().equals(LobbyPathType.MTT)) {
			TournamentRemovedPacket packet = new TournamentRemovedPacket();
			packet.mttid = path.getObjectId();
			// Place in map, thus overwriting any delta changes that are no longer of interest
			deltaPacketsMap.put(path.getObjectId(), packet);
			
		} else {
			log.warn("Unknown lobby type encountered for object removed: "+path);
		}
	}
	
	/**
	 * We got update for a node that is not yet in the delta change list.
	 * We should add a full snapshot packet in the delta list.
	 * 
	 * @param objectId
	 * @param change
	 */
	private void handleFullChange(int objectId, NodeChangeDTO change) {
		// Just rebuild a full packet and reset it
		ProtocolObject packet = null;
		if (change.getPath().getType().equals(LobbyPathType.TABLES)) {
			TableInfo table = TableInfoBuilder.createTableInfo(change.getPath().getRootLobbyPath(), change.getAllData());
			packet = LobbyTransformer.transform(table);
			
		} else if (change.getPath().getType().equals(LobbyPathType.MTT)) {
			TournamentInfo mtt = TournamentInfoBuilder.createTableInfo(change.getPath().getRootLobbyPath(), change.getAllData());
			packet = LobbyTransformer.transform(mtt);
			
		} else {
			log.warn("Unknown lobby type encountered: "+change.getPath().getType());
		}
		
		if (packet != null) {
			packets.put(objectId, packet);
		} else {
			log.error("Could not create full lobby packet for deltasnapshot: "+objectId+" - "+change);
		}
	}

	/**
	 * We got an update that should truly be handled as a delta update.
	 * 
	 * @param tableid
	 * @param change
	 */
	private void handleDeltaChange(int objectId, NodeChangeDTO change) {
		Map<Object, Object> changed = change.getChanged();
		
		ProtocolObject packet = null;
		if (change.getPath().getType().equals(LobbyPathType.TABLES)) {
			packet = handleTableDeltaChange(objectId, change, changed);
			
		} else if (change.getPath().getType().equals(LobbyPathType.MTT)) {
			packet = handleTournamentDeltaChange(objectId, change, changed);
			
		} else {
			log.warn("Unknown lobby type encountered: "+change.getPath().getType());
		}
		
		deltaPacketsMap.put(objectId, packet);
	}

	/**
	 * Handle delta changes on a table node.
	 * 
	 * @param tableid
	 * @param change
	 * @param changed
	 * @return
	 */
	private TableUpdatePacket handleTableDeltaChange(int tableid, NodeChangeDTO change, Map<Object, Object> changed) {
		// log.debug("Lobby DeltaChange: "+tableid+" : "+change);
		// The packet we should update
		TableUpdatePacket packet = null;
		// Check if we have a defined table update packet
		ProtocolObject deltaProtocol = deltaPacketsMap.get(tableid);
		if (deltaProtocol instanceof TableUpdatePacket) {
			packet = (TableUpdatePacket) deltaProtocol;
		}
		
		if (packet == null) {
			packet = new TableUpdatePacket();
			packet.tableid = tableid;
		}
		
		List<Param> oldParameters = packet.params;
		if (oldParameters == null) {
			oldParameters = Collections.emptyList();
		}
		String[] oldRemoved = packet.removedParams;
		
		if(change.isRemoval()) {
			Set<String> removed = toRemovedKeySet(change.getChanged());
			List<Param> params = new ArrayList<Param>(oldParameters);
			// Remove any cached parameters
			mergeRemovedParameters(params, removed);
			// Merge list of removed parameters
			mergeRemovedNames(oldRemoved, removed);
			// Store
			packet.removedParams = Lists.toArray(removed, String.class);
			packet.params = params;	
		} else {
			Set<String> removed = toRemovedKeySet(oldRemoved);
			// Always update seated if possible
			if (change.getAllData().containsKey(DefaultTableAttributes._SEATED.toString())) {
				packet.seated = ((Integer)change.getAllData().get(DefaultTableAttributes._SEATED.toString())).shortValue();
			}
			List<Param> params = ParamUtils.getParameterList(changed);
			// Delete new parameters from removal list
			mergeCheckRemoveParameters(params, removed);
			// Merge parameters to one list
			mergeParameters(oldParameters, params);
			// Store
			packet.removedParams = Lists.toArray(removed, String.class);
			packet.params = params;	
		}
		return packet;
	}
	


	/**
	 * Handle delta changes on a table node.
	 * 
	 * @param tableid
	 * @param change
	 * @param changed
	 * @return
	 */
	private TournamentUpdatePacket handleTournamentDeltaChange(int mttid, NodeChangeDTO change, Map<Object, Object> changed) {
		// The packet we should update
		TournamentUpdatePacket packet = null;
		// Check if we have a defined table update packet
		ProtocolObject deltaProtocol = deltaPacketsMap.get(mttid);
		if (deltaProtocol instanceof TournamentUpdatePacket) {
			packet = (TournamentUpdatePacket) deltaProtocol;
			
		}
		
		if (packet == null) { // WTF, this really shouldn't happen
			packet = new TournamentUpdatePacket();
			packet.mttid = mttid;
		}

		List<Param> oldParameters = packet.params;
		if (oldParameters == null) {
			oldParameters = Collections.emptyList();
		}
		String[] oldRemoved = packet.removedParams;
		
		if(change.isRemoval()) {
			Set<String> removed = toRemovedKeySet(change.getChanged());
			List<Param> params = new ArrayList<Param>(oldParameters);
			// Remove any cached parameters
			mergeRemovedParameters(params, removed);
			// Merge list of removed parameters
			mergeRemovedNames(oldRemoved, removed);
			// Store
			packet.removedParams = Lists.toArray(removed, String.class);
			packet.params = params;	
		} else {
			Set<String> removed = toRemovedKeySet(oldRemoved);
			List<Param> params = ParamUtils.getParameterList(changed);
			// Delete new parameters from removal list
			mergeCheckRemoveParameters(params, removed);
			// Merge parameters to one list
			mergeParameters(oldParameters, params);
			// Store
			packet.removedParams = Lists.toArray(removed, String.class);
			packet.params = params;	
			
		}
		return packet;
	}
	
	/*
	 * Make sure the removal set does not conatain any of the parameters
	 */
	private void mergeCheckRemoveParameters(List<Param> params, Set<String> removed) {
		for (Param p : params) {
			removed.remove(p.key);
		}
	}

	/*
	 * To set of strings
	 */
	private Set<String> toRemovedKeySet(String[] oldRemoved) {
		Set<String> set = new TreeSet<String>();
		for (String o : oldRemoved) {
			set.add(o);
		}
		return set;
	}

	/*
	 * Make sure all string in the array also exists in the set
	 */
	private void mergeRemovedNames(String[] oldRemoved, Set<String> removed) {
		for (String s : oldRemoved) {
			removed.add(s);
		}
	}

	/*
	 * Given a list of parameters, if the parameter name exists in the removed
	 * set, also remove it from the list
	 */
	private void mergeRemovedParameters(List<Param> params, Set<String> removed) {
		for (Iterator<Param> it = params.iterator(); it.hasNext(); ) {
			Param p = it.next();
			if(removed.contains(p.key)) {
				it.remove();
			}
		}
	}

	/*
	 * To set of keys as string
	 */
	private Set<String> toRemovedKeySet(Map<Object, Object> changed) {
		Set<String> set = new TreeSet<String>();
		for (Object o : changed.keySet()) {
			set.add(o.toString());
		}
		return set;
	}

	protected void mergeParameters(List<Param> oldParameters, List<Param> params) {
		// Merge the parameters, new params have precedence
		 for (Param p : oldParameters) {
			 // ugh... protocol object does not have any nice equal function
			 boolean found = false;
			 for (Param changedParam : params) {
				 if (p.key.equals(changedParam.key)) {
					 found = true;
					 break;
				 }
			 }
			 
			 if (!found) {
				 params.add(p);
			 }
		 }
	}

	
}
