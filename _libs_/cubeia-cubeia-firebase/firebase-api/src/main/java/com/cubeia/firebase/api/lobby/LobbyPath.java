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
package com.cubeia.firebase.api.lobby;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.api.util.Arguments;

/**
 * <p>A bean for holding lobby paths.</p>
 *
 * <p>A lobby path consists of 4 defined parts and combinations thereof.</p>
 * 
 * <p>Example for a table for gameid = 99 with tableid = 3 and resides
 * in the lobby tree at a/b/:</p>
 * 
 * <code>"/table/99/a/b/3"</code>
 *  
 * <p>The definitions for the string above will then be:</p>
 * 
 * <p>
 * <ul>
 *	 <li>type = "/table/"</li>
 *	 <li>area = "99/"</li>
 *	 <li>domain = "a/b/"</li>
 * 	 <li>objectId = "3"</li>
 * </ul>
 * </p>
 * 
 * <p>Combinations:</p>
 * 
 * <p>
 * <ul>
 *   <li>root = "/table/99"</li>
 *   <li>namespace = "/table/99/a/b/"</li>
 *   <li>rootLobbyPath = "/99/a/b/"</li>
 *	 <li>lobbyPath = "/a/b/3"</li>
 *   <li>systemPath  = "/table/99/a/b/3"</li>
 * </ul>
 * </p>
 *
 * <p>Preferred format of the domain is "name/", if it is formatted in any other
 * way it will be reformatted. Please see FqnUtil for formatting information.</p>

 * <p>NOTE: This class is externalizable, i.e. all read/write handling is managed within 
 * this class. If you add a new non-transient attribute you have to add it to read 
 * and writeExternal as well!</p>
 *
 * @author Fredrik
 */
public final class LobbyPath implements Externalizable {

	private static final long serialVersionUID = 1L;

	private LobbyPathType type = LobbyPathType.TABLES;
	
	protected int area;
	protected int objectId = -1;
	protected String domain = "";

	/**
	 * Empty constructor needed for serialization. NB: Do not use.
	 */
	public LobbyPath() {}
	
	/**
	 * Create a new lobby path from a parent, but with a new object id. The
	 * new path will have the same type, area and domain as the parent.
	 * 
	 * @param parent Path to take type, area and domain from
	 * @param objectId New object ID
	 */
	public LobbyPath(LobbyPath parent, int objectId) {
		this.type = parent.type;
		this.area = parent.area;
		this.domain = parent.domain;
		this.objectId = objectId;
	}
	
	/**
	 * Create an empty lobby path of the given type.
	 * @param type the path type
	 */
	public LobbyPath(LobbyPathType type) {
	    this.type = type;
	}
	
	/**
	 * <p>Create a lobby by only giving the area. Domain
	 * will default to '/'. Objectid to -1 and type to TABLE.</p>
	 * 
	 * <p>Area typically corresponds to a deployed game id or
	 * tournament logic id.</p>
	 * 
	 * @param area
	 */
	public LobbyPath(int area) {
		this.area = area;
	}
	
	/**
	 * <p>Create a lobby by giving the area and domain.
	 * Objectid will default to -1 and type to TABLE.</p>
	 * 
	 * <p>Area typically corresponds to a deployed game id or
	 * tournament logic id.</p>
	 * 
	 * <p>Domain is typically the lobby path within the area.</p>
	 * 
	 * @param area
	 * @param domain
	 */
	public LobbyPath(int area, String domain) {
		this.area = area;
		this.domain = formatMiddleFqn(domain);
	}
	
	/**
	 * <p>Create a lobby by giving the area and domain
	 * and object id. Type will default to TABLE.</p>
	 * 
	 * <p>Area typically corresponds to a deployed game id or
	 * tournament logic id.</p>
	 * 
	 * <p>Domain is typically the lobby path within the area.</p>
	 * 
	 * <p>ObjectId is the id of the end object. This is normally not 
	 * used for lobby handling purposes where the path/tree is 
	 * usually only relevant. This attribute will not be included
	 * if the LobbyPath is serialized.</p>
	 * 
	 * @param area
	 * @param domain
	 * @param objectId
	 */
	public LobbyPath(int area, String domain, int objectId) {
		this(area, domain);
		this.objectId = objectId;
	}
	
	/**
	 * <p>Include type as well as all other parameters.</p>
	 * 
	 * @param type
	 * @param area
	 * @param domain
	 * @param objectId
	 */
	public LobbyPath(LobbyPathType type, int area, String domain, int objectId) {
		this(area, domain);
		this.objectId = objectId;
		this.type = type;
	}
	
	/**
	 * @param type
	 * @param area
	 * @param domain
	 */
	public LobbyPath(LobbyPathType type, int area, String domain) {
		this(area, domain);
		this.type = type;
	}

	/**
	 * Get a human readable string representation of the lobby path.
	 */
	public String toString() {
		return "["+getTypeRoot()+"]["+getArea()+"]["+domain+"][" + objectId +"]";
	}
	
	
	/* ----------- PATH ELEMENTS ------------- */
	
	public LobbyPathType getType() {
		return type;
	}
	
	/**
	 * Return the root element of the path
	 */
	public String getTypeRoot() {
		switch (type) {
			case MTT :
				return SystemStateConstants.TOURNAMENT_ROOT_FQN;
			/*case MTT_TABLES :
				return SystemStateConstants.TOURNAMENT_TABLE_ROOT_FQN;*/
			default:
				return SystemStateConstants.TABLE_ROOT_FQN;
		}
	}

	/**
	 * <p>Get the area of the lobby path. The area is usually
	 * what is considered the identifier of the deployed resource.</p> 
	 * 
	 * <p>See the class description for more information.</p>
	 * 
	 * for the 
	 * @return
	 */
	public int getArea() {
		return area;
	}
	
	/**
	 * <p>Get the domain of the lobby path. The area is usually
	 * what is considered the address in the lobby tree.</p> 
	 * 
	 * <p>See the class description for more information.</p>
	 * 
	 * for the 
	 * @return
	 */
	public String getDomain() {
		return domain;
	}

	public int getObjectId() {
		return objectId;
	}
	
	
	
	/* ----------- PATH COMBINATIONS ------------- */
	
	public String getNameSpace() {
		String s = getRoot();
		if(!s.endsWith("/")) {
			s += "/";
		}
		return s + domain;
	}
	
	public String getRootLobbyPath() {
		return area +"/" + domain;
	}
	
	public String getSystemPath() {
		return getNameSpace() + objectId;
	}
	
	public String getLobbyPath()  {
		return domain + objectId;
	}
	
	public String getRoot() {
		return getTypeRoot() + getArea();
	}
	
	/* ----------- SETTERS ------------- */
	
	/*public void setArea(int area) {
		this.area = area;
	}
	
	public void setDomain(String domain) {
		this.domain = formatMiddleFqn(domain);
	}
	
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	public void setType(LobbyPathType type) {
		this.type = type;
	}*/
	
	/* -------------- UTILITY METHODS ------------- */
	
	/**
	 * Removes leading /<br/>
	 * Appends missing trailing /
	 * 
	 * <p>E.g.
	 * 
	 * <pre>
	 *    "apa/" -> "apa/"
	 *    "/apa" -> "apa/"
	 *    "apa"  -> "apa/"
	 *    "/"    -> ""
	 * </pre>
	 */
	public static String formatMiddleFqn(String fqn) {	
		if (fqn.equals("/")) {
			fqn = "";
		}
		if (fqn.startsWith("/")) {
			fqn = fqn.substring(1);
		}
		if (!fqn.equals("") && !fqn.endsWith("/")) {
			fqn += "/";
		}
		return fqn;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + area;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final LobbyPath other = (LobbyPath) obj;
		if (area != other.area)
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	/**
	 * Externalizable implementation.
	 * NOTE: This method will be used instead of regular java serialization.
	 * We have implemented this for more efficient serializing, i.e. less data.
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		area = in.readInt();
		objectId = in.readInt();
		domain = in.readUTF();
		type = LobbyPathType.values()[in.readInt()];
	}

	/**
	 * Externalizable implementation.
	 * NOTE: This method will be used instead of regular java serialization.
	 * We have implemented this for more efficient serializing, i.e. less data.
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(area);
		out.writeInt(objectId);
		out.writeUTF(domain);
		out.writeInt(type.ordinal());
	}
	
	static String removeTableId(String fqn) {
		if (!fqn.endsWith("/")) {
			int i = fqn.lastIndexOf("/");
			fqn = fqn.substring(0, i);
		}
		return formatMiddleFqn(fqn);
	}
	
	/**
	 * This method attempts to parse a lobby path from an FQN. The
	 * path supplied must be complete. In particular it must end with an
	 * identifying integer id. If not, a message will be logged and null
	 * returned (trac issue #318). 
	 * 
	 * @param fqn Path to parse, must not be null
	 * @return A lobby path, or null on parse errors
	 */
	public LobbyPath parseFqn(String fqn) {
		try {
			// Check some constraints first
			Arguments.notNull(fqn, "fqn");
			/*boolean ok = true;
			
			ok &= fqn.startsWith(getTypeRoot());
			ok &= fqn.length() > (getTypeRoot().length() + 3); // root/gid/
			ok &= !fqn.endsWith("/");*/
			
			// Get path elements
			String rootLobbyPath = fqn.substring(getTypeRoot().length());
			
			// logic id, for games this is the game id, for tournaments the tournament logic id
			String logicId = rootLobbyPath.substring(0, rootLobbyPath.indexOf("/"));
			String lobbyPath = rootLobbyPath.substring(logicId.length()+1);
			
			String domain = "";
			
			// the instance id, for games this is the table id, for tournament the tournament instance id
			String instanceId = "";
			
			if (lobbyPath.contains("/")) {
				domain = removeTableId(lobbyPath);
				instanceId =  fqn.substring(fqn.lastIndexOf("/")+1);
			} else {
				instanceId = lobbyPath;
			}
			
			int gid = Integer.parseInt(logicId);
			int tid = Integer.parseInt(instanceId);
			
			LobbyPath path = new LobbyPath(type, gid, domain, tid);
			return path;
			
		} catch (Exception e) {
			Logger.getLogger(getClass()).debug("Could not parse fqn: "+fqn+"; Msg: " + e.getMessage());
			return null;
		}
	}


}