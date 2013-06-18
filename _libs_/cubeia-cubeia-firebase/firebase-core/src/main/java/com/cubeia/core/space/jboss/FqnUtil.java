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
package com.cubeia.core.space.jboss;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;

public class FqnUtil {
	
	private static transient Logger log = Logger.getLogger(FqnUtil.class);
	
	/**
	 * Parse a full FQN excluding trailing table id.
	 * 
	 * @param fqn
	 * @return
	 */
	public static LobbyPath parseFullFqn(Fqn<String> fqn) {
		try {
			Fqn<String> domain = fqn.getParent();
			Fqn<String> current = domain;
			LobbyPathType type = LobbyPathType.TABLES;
			
			int gid = -1;
			int tableId = Integer.parseInt(fqn.getLastElementAsString());
			String loc = "";
			
			while (!current.getParent().isRoot()) {
				Fqn<String> parent = current.getParent();
				
				if (parent.getLastElementAsString().equals(SystemStateConstants.TABLE_ROOT_NODE)) {
					gid = Integer.parseInt(current.getLastElementAsString());
					
				} else if (parent.getLastElementAsString().equals(SystemStateConstants.TOURNAMENT_ROOT_NODE)) {
					gid = Integer.parseInt(current.getLastElementAsString());
					type = LobbyPathType.MTT;
					
				} else {
					loc = current.getLastElementAsString()+"/"+loc;
				}
				
				current = current.getParent();
			}
			
			
			return new LobbyPath(type, gid, loc, tableId);
			
		} catch (Exception e) {
			log.warn("Could not parse full fqn: "+fqn+". Ex: "+e, e);
		}
		// This is a failure return
		return null;
	}
	
	/**
	 * Parse a full FQN not including table id
	 * 
	 * @param fqn
	 * @return
	 */
	public static LobbyPath parseFqn(Fqn<String> fqn) {
		try {
			Fqn<String> current = fqn;
			LobbyPathType type = LobbyPathType.TABLES;
			
			int gid = -1;
			String loc = "";
			
			while (!current.getParent().isRoot()) {
				Fqn<String> parent = current.getParent();
				
				if (parent.getLastElementAsString().equals(SystemStateConstants.TABLE_ROOT_NODE)) {
					gid = Integer.parseInt(current.getLastElementAsString());
					
				} else if (parent.getLastElementAsString().equals(SystemStateConstants.TOURNAMENT_ROOT_NODE)) {
					gid = Integer.parseInt(current.getLastElementAsString());
					type = LobbyPathType.MTT;
					
				} else {
					loc = current.getLastElementAsString()+"/"+loc;
				}
				
				current = current.getParent();
			}
			
			
			return new LobbyPath(type, gid, loc);
			
		} catch (Exception e) {
			log.warn("Could not parse fqn: "+fqn+". Ex: "+e, e);
		}
		// This is a failure return
		return null;
	}
	
	
	/**
	 * Removes leading /
	 * Appends missing trailing /
	 * 
	 * E.g.
	 * 
	 * "apa/" -> "apa/"
	 * "/apa" -> "apa/"
	 * "apa"  -> "apa/"
	 * "/"    -> ""
	 * 
	 * @param fqn
	 * @return
	 */
	public static String formatMiddleFqn(String fqn) {
		
		if (fqn.equals("/")) {
			fqn = "";
		}
		
		if (fqn.startsWith(Fqn.SEPARATOR)) {
			fqn = fqn.substring(1);
		}
		
		if (!fqn.equals("") && !fqn.endsWith(Fqn.SEPARATOR)) {
			fqn += Fqn.SEPARATOR;
		}
		
		return fqn;
	}
}
