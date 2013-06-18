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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;

import com.cubeia.core.space.jboss.FqnUtil;
import com.cubeia.firebase.api.lobby.AttributeMapper;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;
import com.cubeia.firebase.server.service.systemstate.util.Nodes;



/**
 * The Lobby Cache Handler is mapped to a Tree Cache implementation.
 * It wraps the system state cache handler in the sense that it
 * appends table and lobby specific tree-coordinates to the
 * addresses given.
 * 
 * 
 * 
 * @author Fredrik
 *
 */
@CacheListener
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LobbyCacheHandler {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** 
	 * The underlying cache.
	 * The Cache Handler should never manipulate this.
	 * The lobby should be strictly reading.
	 */
	private SystemStateCacheHandler cache;
	private LobbyCacheListener listener;
	
	private final LobbyPathRegister register;
	
	/**
	 * Start a cache handler for a given tree cache.
	 * 
	 * @param cache
	 */
	public LobbyCacheHandler(SystemStateCacheHandler cache, LobbyListener notifier) {
		this.cache = cache;
		Cache realCache = cache.getCache();
		listener = new LobbyCacheListener(realCache, notifier);
		realCache.addCacheListener(this);
		register = new LobbyPathRegister();
		registerInitial(cache.getSubNodes(SystemStateConstants.TABLE_ROOT_FQN, true));
		registerInitial(cache.getSubNodes(SystemStateConstants.TOURNAMENT_ROOT_FQN, true));
	}
	
	private void registerInitial(Map<Fqn<?>, Node> map) {
		for (Fqn<?> fqn : map.keySet()) {
			if(checkIfNodeIsLobbyDataNode(fqn)) {
				register.registerLeaf(fqn.toString());
			} else {
				register.registerPath(fqn.toString());
			}
		}
	}

	public void destroy() {
		listener.destroy();
	}


	/**
	 * See if an fqn address exists in the
	 * data model. The supplied FQN should not
	 * include the path including the root-node.
	 * 
	 * @param fqn
	 * @return
	 */
	public boolean exists(String fqn) {
		return cache.exists(fqn);
	}

	/**
     * Get the end nodes of Table attributes.
     * If the last node (i.e. the leave) is table/mtt id
     * it will removed from the returned data.
     * 
     * The _nodeType attribute will be used to determine 
     * if the node contains a lobby object or not.
     * 
     * 
     * Example:
     * 
     * Cache -
     *   /table/a/b/c/1  (table)
     *   /table/a/b/e/2
     *   /table/a/b/f/   (no tables)
     *   /table/a/g/h/3
     *   
     *   
     * Input FQN -
     *   /a/b
     *   
     * Returned FQN's -
     *  /a/b/c
     *  /a/b/e
     *  /a/b/f
     * 
     * 
     * 
     * 
     * @param fqn including typeroot (e.g. /table/123)
     * @return
     */
	public Collection<LobbyPath> getEndNodes(LobbyPath path) {
		return getEndNodes(path.getNameSpace());
	}


	/**
	 * Get the end nodes of Table attributes.
	 * If the last node (i.e. the leave) is table/mtt id
	 * it will removed from the returned data.
	 * 
	 * The _nodeType attribute will be used to determine 
	 * if the node contains a lobby object or not.
	 * 
	 * 
	 * Example:
	 * 
	 * Cache -
	 *   /table/a/b/c/1  (table)
	 *   /table/a/b/e/2
	 *   /table/a/b/f/   (no tables)
	 *   /table/a/g/h/3
	 *   
	 *   
	 * Input FQN -
	 *   /table/a/b
	 *   
	 * Returned FQN's -
	 *  /a/b/c
	 *  /a/b/e
	 *  /a/b/f
	 * 
	 * 
	 * 
	 * 
	 * @param fqn including typeroot (e.g. /table/123)
	 * @return
	 */
	public Collection<LobbyPath> getEndNodes(String fqn) {
		Set<String> endNodes = register.getEndNodes(fqn);
		List<LobbyPath> list = new ArrayList<LobbyPath>(endNodes.size());
		for (String path : endNodes) {
			LobbyPath tmp = FqnUtil.parseFqn(Fqn.fromString(path));
			if(tmp != null) {
				list.add(tmp);
			}
		}
		return list;
		
		/*Set<Fqn> endNodes = cache.getEndNodes(fqn).keySet();
		
		Map<String, LobbyPath> leaves = new HashMap<String, LobbyPath>();
		
		for (Fqn end: endNodes) {
		    boolean isTableNode = checkIfNodeIsLobbyDataNode(end);
		    
		    LobbyPath domain;
		    if (isTableNode) {
		        domain = FqnUtil.parseFullFqn(end);
		    } else {
		        domain = FqnUtil.parseFqn(end);
		    }
		    
			domain.setObjectId(-1); // Invalidate table id
			if (domain != null && !leaves.containsKey(domain.getRootLobbyPath())) {
				leaves.put(domain.getRootLobbyPath(), domain);
			}
		}
		return leaves.values();*/
	}
	

	

    /**
	 * Get all sub-nodes including the current node.
	 * Will ignore table and tournament lobby data nodes.
	 * 
	 * Example:
	 * 
	 * Cache -
	 *   /table/99/a/b/c/d
	 *   /table/99/a/b/e/f
	 *   /table/99/a/g/h/i
	 *   
	 * Input FQN -
	 *   /table/99/a/b
	 *   
	 * Returned FQN's -
	 *  /table/99/a/b
	 *  /table/99/a/b/c
	 *  /table/99/a/b/c/d
	 *  /table/99/a/b/e
	 *  /table/99/a/b/e/f
	 * 
	 * 
	 * @param fqn
	 * @return
	 */
	public Collection<LobbyPath> getSubNodes(String path) {
		Set<String> nodes = register.getNodes(path);
		List<LobbyPath> list = new ArrayList<LobbyPath>(nodes.size());
		for (String name : nodes) {
			LobbyPath tmp = FqnUtil.parseFqn(Fqn.fromString(name));
			if(tmp != null) {
				list.add(tmp);
			}
		}
		return list;
		
		/*path = formatFqn(path);
		Map<Fqn, Node> subNodes = cache.getSubNodes(path, true);
		Set<LobbyPath> subs = new HashSet<LobbyPath>();
		for (Fqn end: subNodes.keySet()) {
			boolean isTableNode = checkIfNodeIsLobbyDataNode(end);
			if (!isTableNode) {
				LobbyPath discriminator = FqnUtil.parseFqn(end);
				subs.add(discriminator);
			}
		}
		
		return subs;*/
	}
	
	public Collection<TableInfo> getTables(String fqn) {
		fqn = formatFqn(fqn);
		Map<Fqn<?>, Node> endNodes = cache.getEndNodes(fqn);
		Collection<TableInfo> snapshot = new ArrayList<TableInfo>(endNodes.size());
		for (Node node : endNodes.values()) {
			Map<Object, Object> data = node.getData();
			String path = LobbyCacheUtil.stripTableInfo(node.getFqn());
			TableInfo table = TableInfoBuilder.createTableInfo(path, data);
			snapshot.add(table);
		}
		return snapshot;
	}
	
	public Collection<TournamentInfo> getTournaments(String fqn) {
		fqn = formatFqn(fqn);
		Map<Fqn<?>, Node> endNodes = cache.getEndNodes(fqn);
		Collection<TournamentInfo> snapshot = new ArrayList<TournamentInfo>(endNodes.size());
		for (Node node : endNodes.values()) {
			Map<Object, Object> data = node.getData();
			String path = LobbyCacheUtil.stripTournamentRoot(node.getFqn());
			TournamentInfo info = TournamentInfoBuilder.createTableInfo(path, data);
			snapshot.add(info);
		}
		return snapshot;
	}

	public List<LobbyPath> getAreas(LobbyPathType type) {
		String root = getTypeRoot(type);
		List<LobbyPath> games = null; // new ArrayList<LobbyPath>();
		Node child = cache.getCache().getRoot().getChild(Fqn.fromString(root));
		if (child != null) {
			Set<Object> children = child.getChildrenNames();
			games = new ArrayList<LobbyPath>(children.size());
			for (Object game : children) {
				try {
					int gid = Integer.parseInt(String.valueOf(game));
					LobbyPath path = new LobbyPath(type, gid, "", -1);
					games.add(path);
				} catch (NumberFormatException e) {
					log.warn("Could not resolve game id for node: "+game);
				}
			}
		}
		if(games != null) {
			return games;
		} else {
			return Collections.emptyList();
		}
	}
	
	public void addPath(String path) {
		cache.getCache().put(Fqn.fromString(path), null);
		register.registerPath(path);
	}
	
	
	// --- CACHE LISTENER --- //

	@NodeModified
	public void nodeModified(NodeModifiedEvent event) { 
		String fqn = event.getFqn().toString(); 
		if (isTableOrMttFqn(fqn) && !event.isPre()) {
			// at this point we know that there an attribute at 
			// the node, in which case we can register it as a 
			// leaf, ie, it's parent path is legal
			register.registerLeaf(fqn);
		}
	}

	@NodeRemoved
	public void nodeRemoved(NodeRemovedEvent event) { 
		String fqn = event.getFqn().toString(); 
		if (isTableOrMttFqn(fqn) && event.isPre()) {
			// this is a removed table or a remove path
			if (Nodes.isTableNode(event.getData()) || Nodes.isTournamentNode(event.getData())) {
				// we don't care about this, until the parent is removed
			} else {
				// remove path, but not parent path (?)
				register.unregisterNode(fqn);
			}
		}
	}
	
	
	// --- PRIVATE METHODS ---- //
	
	private boolean isTableOrMttFqn(String fqn) {
		return (fqn.startsWith(SystemStateConstants.TABLE_ROOT_FQN) || fqn.startsWith(SystemStateConstants.TOURNAMENT_ROOT_FQN));
	}
	
	private String getTypeRoot(LobbyPathType type) {
		switch (type) {
			case MTT:
				return SystemStateConstants.TOURNAMENT_ROOT_FQN;
			default:
				return SystemStateConstants.TABLE_ROOT_FQN;
		}
	}
	
	/**
	 * Removes leading /
	 * Appends missing trailing /
	 * 
	 * @param fqn
	 * @return
	 */
	private String formatFqn(String fqn) {
		if (fqn.startsWith(Fqn.SEPARATOR)) {
			fqn = fqn.substring(1);
		}
		
		if (!fqn.endsWith(Fqn.SEPARATOR)) {
			fqn += Fqn.SEPARATOR;
		}
		
		return fqn;
	}
	
	private boolean checkIfNodeIsLobbyDataNode(Fqn end) {
	       boolean isDataNode = false;	
	       Object attribute = cache.getAttribute(end, AttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
	       if (attribute != null) {
	           if (attribute instanceof String) {
	            String sVal = (String) attribute;
	            if (sVal.equals(AttributeMapper.MTT_NODE_TYPE_ATTRIBUTE_VALUE) || sVal.equals(AttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE)) {
	                isDataNode = true;
	            }
	        }
	       }
	       return isDataNode;
	    }
}
