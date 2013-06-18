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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeCreated;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.annotation.ViewChanged;
import org.jboss.cache.notifications.event.NodeCreatedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent.ModificationType;
import org.jboss.cache.notifications.event.NodeRemovedEvent;
import org.jboss.cache.notifications.event.ViewChangedEvent;

import com.cubeia.core.space.jboss.FqnUtil;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.service.systemstate.util.Nodes;
import com.cubeia.firebase.util.executor.JmxExecutor;

@SuppressWarnings("unchecked")
@CacheListener
public class LobbyCacheListener {

	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** The executor for state changes */
	private JmxExecutor executor = new JmxExecutor(1, "LobbyCacheListener");
	
	/**
	 * The underlying tree cache.
	 * We need a direct reference since we need to make
	 * lookups of the nodes etc.
	 * 
	 */
	@SuppressWarnings("rawtypes")
	private final Cache cache;
	
	private final LobbyListener notifier;
	
	@SuppressWarnings("rawtypes")
	public LobbyCacheListener(Cache cache, LobbyListener notifier) {
		this.cache = cache;
		this.notifier = notifier;
		cache.addCacheListener(this);
	}
	
	public void destroy() {
		executor.stop();
	}
	
	
	
	
	
	
	/*------------------------------------------------- 
	  
 		CACHE LISTENER METHODS
 		
 		These methods are annotated so the cache
 		picks them up and notifies them.
 
 	 
 	 --------------------------------------------------*/
	
	/**
	 * Notify that a node has been created.
	 * Only notify the listener if it is a table node.
	 * 
	 * Here we actually do want to notify for pre-events since
	 * we want to setup stuff before data starts to roll in.
	 * 
	 * @param fqn
	 */
	@NodeCreated
	public void nodeCreated(NodeCreatedEvent event) {
		// FIXME: This listener method is broken. The fqn cannot be parsed since we do not
		//		  know if the node is a node or a table-node. The data is not set so we
		// 	  	  cant inspect the data either.
		
//		if (event.getFqn().toString().startsWith(SystemStateConstants.TABLE_ROOT_FQN)) {
//			NotifyCreated task = new NotifyCreated(event.getFqn());
//			executor.submit(task);
//		}
	}

	
	/**
	 * A node has been modified. We will execute after the change is performed. 
	 */
	@NodeModified
	public void nodeModified(NodeModifiedEvent event) {
		if (event.getFqn().toString().startsWith(SystemStateConstants.TABLE_ROOT_FQN) || event.getFqn().toString().startsWith(SystemStateConstants.TOURNAMENT_ROOT_FQN)) {
			// We don't care about premature changes
			if (!event.isPre()) {
				// We don't care about reporting timestamp attibute
				if (event.getData().size() == 1 && event.getData().containsKey(DefaultTableAttributes._LAST_MODIFIED.toString())) {
					return; // EARLY RETURN;
				}
				
				NotifyChanged task = new NotifyChanged(event.getFqn(), event.getData(), event.getModificationType().equals(ModificationType.REMOVE_DATA));
				// StateLobby will handle this async, so its ok to call the task synchronously
				// in order to save some CPU cycles. 
				task.run();
				//executor.submit(task);
			}
		}
	}

	/**
	 * The member view has changed.
	 * Tell the people!
	 */
	@ViewChanged
	public void viewChange(ViewChangedEvent event) {
		log.info("System State view changed: "+event.getNewView());
		log.info("System State view changed (details): "+event.getNewView().printDetails());
		
	}
	
	/**
	 * A node was removed.
	 * 
	 */
	@NodeRemoved
	public void nodeRemoved(NodeRemovedEvent event) {
		// We will place the lobby hooks on pre-conditions.
		if (event.isPre() && (event.getFqn().toString().startsWith(SystemStateConstants.TABLE_ROOT_FQN) || event.getFqn().toString().startsWith(SystemStateConstants.TOURNAMENT_ROOT_FQN))) {
			// It is a pre-reported remove for a node within the lobby.
			if (Nodes.isTableNode(event.getData()) || Nodes.isTournamentNode(event.getData())) {
				NotifyRemoved task = new NotifyRemoved(event.getFqn());
				executor.submit(task);
			} else {
				log.debug("A node was removed in the lobby system state ("+event.getFqn()+")");
				NotifyPathRemoved task = new NotifyPathRemoved(event.getFqn());
				executor.submit(task);
			}
		}
	}

	
	/*------------------------------------------------- 
	  
		PRIVATE METHODS
		
	 --------------------------------------------------*/
	
	@SuppressWarnings({ "rawtypes" })
	private Map<Object,Object> getNodeData(Fqn fqn) {
		Map<Object,Object> data = new HashMap<Object, Object>();
		try {
			Node node = cache.getNode(fqn); //cache.get(fqn); //
			if (node != null) {
				// Create a defensive copy
				data = new HashMap<Object, Object>(node.getData());
			}
		} catch (Exception ex) {
			log.error("Error on get node data for FQN: "+fqn, ex);
		}
		return data;
	}
	
	
	
	
	/*------------------------------------------------- 
	  
	 	INNER CLASSES FOR ASYNCH HAND OFF
	 
	 --------------------------------------------------*/
	
	/**
	 * Task for notifying listeners.
	 * This *must* be done in a separate thread since executing 
	 * cache.getRoot().getChild(fqn) can lead to an infininte loop.
	 *
	 * @author Fredrik
	 */
	@SuppressWarnings({ "rawtypes" })
	private class NotifyChanged implements Runnable {
		
		private final Fqn fqn;
		private final Map<Object, Object> data;
		private final boolean isRemoval;

		public NotifyChanged(Fqn fqn, Map<Object, Object> data, boolean isRemoval) {
			this.fqn = fqn;
			this.data = data;
			this.isRemoval = isRemoval;
		}
		
		public void run() {
			try {
				LobbyPath path;
				// Since non-data nodes should not be modified by design we parse the FQN as a full FQN. 
				path = FqnUtil.parseFullFqn(fqn);
				Map<Object, Object> nodeData = getNodeData(fqn);
				NodeChangeDTO change = new NodeChangeDTO(path, data, isRemoval, nodeData);
				notifier.nodeAttributeChanged(change);
			} catch (Throwable th) {
				log.warn("Could not handle lobby change. Probable reason: We have received a change notification for a non-data node.", th);
			}
		}
	}
	
	/*
	 * Task for notifying listeners.
	 * This *must* be done in a separate thread since executing 
	 * cache.getRoot().getChild(fqn) can lead to an infinite loop.
	 *
	 * @author Fredrik
	 */
//	private class NotifyCreated implements Runnable {
//		private final Fqn fqn;
//		public NotifyCreated(Fqn fqn) { this.fqn = fqn; }
//		public void run() {
//			boolean dataNode = checkIfNodeIsLobbyDataNode(fqn);
//			if (!dataNode) {
//				LobbyPath path = FqnUtil.parseFqn(fqn);
//				notifier.nodeCreated(path);
//			}
//		}
//	}
	
	/**
	 * Task for notifying listeners.
	 * This *must* be done in a separate thread since executing 
	 * cache.getRoot().getChild(fqn) can lead to an infininte loop.
	 *
	 * @author Fredrik
	 */
	@SuppressWarnings({ "rawtypes" })
	private class NotifyRemoved implements Runnable {
		private final Fqn fqn;
		public NotifyRemoved(Fqn fqn) { this.fqn = fqn; }
		public void run() {
			try {
				LobbyPath path = FqnUtil.parseFullFqn(fqn);
				notifier.tableRemoved(path);
			} catch (Exception e) {
				log.error("Failed to remove object from lobby. FQN: "+fqn, e);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	private class NotifyPathRemoved implements Runnable {
		private final Fqn fqn;
		public NotifyPathRemoved(Fqn fqn) { this.fqn = fqn; }
		public void run() {
			try {
				notifier.nodeRemoved(fqn.toString());
			} catch (Exception e) {
				log.error("Failed to remove path from lobby. FQN: "+fqn, e);
			}
		}
	}
	
//	private boolean checkIfNodeIsLobbyDataNode(Fqn end) {
//       boolean isDataNode = false;
//       Object attribute = cache.get(end, AttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
//       if (attribute != null) {
//           if (attribute instanceof String) {
//            String sVal = (String) attribute;
//            if (sVal.equals(AttributeMapper.MTT_NODE_TYPE_ATTRIBUTE_VALUE) || sVal.equals(AttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE)) {
//                isDataNode = true;
//            }
//        }
//       }
//       return isDataNode;
//    }

}
