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
package com.cubeia.firebase.service.clientreg.state;

import java.lang.management.ManagementFactory;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.PlayerStatusAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.action.UnWatchAction;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.plugin.clientlogout.ClientClosedReason;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.clientregistry.ClientSessionState;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.KickPlayerPacket;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.chat.ChatServiceContract;
import com.cubeia.firebase.service.clientreg.ClientReaper;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientStatusFields;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;
import com.cubeia.firebase.service.wlist.FilteredJoinServiceContract;
import com.cubeia.firebase.util.executor.JmxScheduler;

/**
 * Implementation of the Client Registry using system state as common
 * data area.
 *
 * We access the Lobby from the service registry only on demand. This is so we do not
 * start a lobby service without the explicit need for it.
 *
 * @author Fredrik
 */
public class StateClientRegistry implements ClientRegistry, StateClientRegistryMBean {

	private static final String _MTT_TABLE_PREFIX = "_mtt:";

	/** Where we store the client -> table mappings */
	public static String JOIN_REQUEST = "/request/";
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	private State state = State.STOPPED;
	
	private static final long KICK_WAIT = 2000;
	private static JmxScheduler forceCloseScheduler = null;

	/**
     * Map of local clients
     */
    private ConcurrentMap<Integer,Client> clients = new ConcurrentHashMap<Integer,Client>();

	private SystemStateServiceContract systemState;
	
	private Sender<Event<?>> sender;
	private StateClientCacheListener listener;
	private ServiceContext con;
	
	private ConcurrentMap<String, String> nodeIdMap = new ConcurrentHashMap<String, String>();

	private ServiceRegistry serviceRegistry;
	
	/**
	 * Listener for clients that are removed from the server and/or the system.
	 */
	private ClientLogoutReporter logoutListener;

    private Sender<Event<?>> clientSender;

	
    /*
     * This method is synchronized together with the logoutClient method. We cannot allow for 
     * concurrent modifications of the client registry while we are adding a player.
     * 
     * This is only true per player so it would be possible to implement a per-id lock
     * strategy if a total synchronized block creates a definite bottleneck.
     *  
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#addClient(int, com.cubeia.firebase.server.gateway.client.Client)
	 */
	public synchronized void addClient(int id, Client client) {
		// Generate session Id
		client.setSessionId(UUID.randomUUID().toString());
		
		clients.put(id, client);
		
		// Add to system state
		// @TODO: It seems unnecessary to replicate the String format of the status fields here
		// we should look into allowing integers to represent fields in system state in order to save
		// internal bandwidth.
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.STATUS), ClientState.LOGGED_IN.ordinal());
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.SCREEN_NAME), client.getClientData().getScreenname());
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.TIMESTAMP), System.currentTimeMillis());
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.SESSION_ID), client.getSessionId());
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.NODE), client.getNodeInfo().getNodeId());
		String socketAddress = client.getNodeInfo().getMBusDetails().getSocketIdFor(EventType.CLIENT).toString();
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.MBUS_CHANNEL), socketAddress);
		systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.OPERATOR_ID), client.getClientData().getOperatorId());
	}
	
	
	/*
	 * This method is synchronized together with the addClient method. We cannot allow for 
	 * concurrent modifications of the client registry while we are logging out a player.
	 * 
	 * This is only true per player so it would be possible to implement a per-id lock
	 * strategy if a total synchronized block creates a definite bottleneck.
	 *  
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#logoutClient(int)
	 */
	public synchronized void logoutClient(Client client, boolean removeFromTables) {
		Client tmp = getClient(client.getId());
		if (tmp == null) {
			log.debug("Attempt to log out player [" + client + "] without login in first, ignoring. Possibly due to multiple logouts.");
		} else if (client.getSessionId().equals(tmp.getSessionId())) {
	        int playerId = client.getId();
    		if (removeFromTables) {
    			Lobby lobby = serviceRegistry.getServiceInstance(LobbyServiceContract.class).getLobby();
    			lobby.unsubscribeAll(getClient(playerId));
    			removeWatcherFromAllTables(playerId, true);
    			removeClientFromWaitingLists(playerId);
    	        removeClientFromChatChannels(playerId);
    			removeClient(playerId); 
    		} else {
    			// Mimicking lost connection (fugly)
    			closeClient(playerId); 
    			clientLostConnection(playerId);
    		}
    		logoutListener.clientClosed(playerId, ClientClosedReason.LOGOUT);
	    } else {
	        log.warn("Concurrent logout/login detected for client["+client+"]. I will not close active session");
	    }
	}
	
	
	public void removeClientFromChatChannels(int playerId) {
	    Client client = getClient(playerId);
        if (client != null) {
        	ChatServiceContract chat = serviceRegistry.getServiceInstance(ChatServiceContract.class);
            for (Integer channelId : client.getLocalData().getActiveChatChannels()) {
                chat.removePlayer(client, channelId);
            }
            client.getLocalData().getActiveChatChannels().clear();
        }
    }

    /* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#getClient(int)
	 */
	public Client getClient(int id) {
		return clients.get(id);
	}
	
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#getLocalClient()
	 */
	public Client[] getLocalClients() {
        return clients.values().toArray(new Client[clients.size()]);
    }
	
	/**
	 * Check if a player is logged in to the system.
	 * 
	 * @param playerId
	 * @return true if logged in, false otherwise
	 */
	public boolean isLoggedIn(int clientId) {
		return exists(clientId);
	}
	
	/**
	 * Does this client exist in the system at all. In effect we 
	 * will check if the client id is set in the system state. This
	 * does not guarantee anything regarding the state of the client
	 * or which node the client is connected to. 
	 * 
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#exists(int)
	 */
	public boolean exists(int id) {
		String fqn= SystemStateConstants.CLIENT_ROOT_FQN+id;
		return systemState.exists(fqn);
	}

	/**
	 * Return the client state for the given pid.
	 * Try to minimize system state lookup.
	 * 
	 * We need a mapping here from the internal ClientState to
	 * an external ClientSessionState.
	 * 
	 * @param pid
	 * @return
	 */
	public ClientSessionState getClientStatus(int pid) {
		if (clients.containsKey(pid)) {
			return ClientSessionState.CONNECTED;
		} 
		
		ClientSessionState state = ClientSessionState.NOT_CONNECTED;
		Integer index = (Integer)systemState.getAttribute(SystemStateConstants.CLIENT_ROOT_FQN+pid, ClientStatusFields.STATUS.name());
		if (index != null) {
		    ClientState sessionStatus = ClientState.values()[index];
            switch (sessionStatus) {
                case CONNECTED:
                    state = ClientSessionState.CONNECTED;
                    break;
                case LOGGED_IN:
                    state = ClientSessionState.CONNECTED;
                    break;
                case DISCONNECTED:
                    state = ClientSessionState.WAIT_REJOIN;
                    break;
            }
        }
        return state;
	}
	
	public int getClientStatusOrdinal(int pid) {
	    return getClientStatus(pid).ordinal();
	}
	
	public String getClientStatusString(int pid) {
        return getClientStatus(pid).name();
    }
	
	/**
	 * Returns true if the session is managed on a node local to 
	 * this server (might be more then one node per server instance).
	 * 
	 * Try to minimize system state lookup by first checking the
	 * local map of session objects. If not found there we check the 
	 * system state. 
	 * 
	 * A client can only be local and not in the clients map if
	 * the client was connected to a local node and then disconnected.
	 * In this case the session object is removed from the clients
	 * map but we still have the session info in system state in the
	 * event of a reconnect.
	 * 
	 * NOTE_1: The system state is asynchronous so the return value
	 * can not be guaranteed for the player id, only for the session
	 * id. 
	 * 
	 * NOTE_2: This method will be expensive for misses, so do not use
	 * this unless absolutely necessary.
	 * 
	 * @param pid
	 * @return
	 */
	public boolean isLocal(int pid) {
		if (clients.containsKey(pid)) {
			return true;
		} 
		Object nodeId = systemState.getAttribute(SystemStateConstants.CLIENT_ROOT_FQN+pid, ClientStatusFields.NODE.name());
		if (nodeId != null && nodeIdMap.containsKey(nodeId)) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#getNumberOfClients()
	 */
	public int getNumberOfClients() {
		return clients.size();
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.state.StateClientRegistryMBean#getAllLoggedIn()
	 */
	public int[] getAllLoggedIn() {
	    Set<String> children = systemState.getChildren(SystemStateConstants.CLIENT_ROOT_FQN);
	    int[] ids = new int[children.size()];
	    int i = 0;
	    for (String sId : children) {
    	    try {
    	        ids[i] = Integer.parseInt(sId);
    	        i++;
    	    } catch (NumberFormatException e) {
    	        log.warn("Could not parse client id: "+sId);
    	    }
	    }
	    return ids;
	}

	public int getNumberOfGlobalClients() {
		return systemState.getChildren(SystemStateConstants.CLIENT_ROOT_FQN).size();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#clientLostConnection(int)
	 */
	public boolean clientLostConnection(int id) {
		Client client = getClient(id);
		// check session ID, we don't want to remove the wrong instance of the client
		if (client != null ) {
			// Unregister client un all lobby subscriptions - REMOVED DUE TO Ticket #572 
		    // This is now handled directly in SessionHandler
			// Lobby lobby = serviceRegistry.getServiceInstance(LobbyServiceContract.class).getLobby();
			// lobby.unsubscribeAll(getClient(id));
			
			// Update state in replicated client data
			systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.STATUS), ClientState.DISCONNECTED.ordinal());
			systemState.setAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.TIMESTAMP), System.currentTimeMillis());
			reportStatusChangeToAllTables(id, PlayerStatus.WAITING_REJOIN);
			removeWatcherFromAllTables(id, false);
			removeClientFromWaitingLists(id);
            removeClientFromChatChannels(id);
			clients.remove(id);
			
			logoutListener.clientClosed(id, ClientClosedReason.WAIT_RECONNECT);
			
			return true;
		} else {
			return false;
		}
	}

    /**
     * Removes all waiting requests that a client was registered for.
     * 
     * @param id the id of the client
     */
	public void removeClientFromWaitingLists(int id) {
		FilteredJoinServiceContract waitingList = serviceRegistry.getServiceInstance(FilteredJoinServiceContract.class);
        Client client = getClient(id);
        if (client != null) {
            for (Long requestId : client.getLocalData().getJoinRequests().values()) {
                waitingList.cancelFilteredJoinAction(requestId);
            }
            client.getLocalData().getJoinRequests().clear();
        } else {
            log.warn("Cannot remove null client from waiting lists");
        }
    }

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#clientReaperTimeout(int)
	 */
	public void clientReaperTimeout(int id) {
	    removeClient(id);
	    logoutListener.clientClosed(id, ClientClosedReason.DISCONNECT_TIMEOUT);
	}

	

	public void destroy() {
		clients = null;
		systemState = null;
		checkDestroySender();
	}

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		listener = new StateClientCacheListener(this);
	}

    public void start() {
		try {
		    setForceCloseScheduler(new JmxScheduler(1, "StateClientRegistry-Force-Close"));
			serviceRegistry = con.getParentRegistry();
			systemState = serviceRegistry.getServiceInstance(SystemStateServiceContract.class);
			sender = new WrappingSender<Event<?>>(serviceRegistry.getServiceInstance(MBusContract.class).createSender(EventType.GAME, con.getPublicId()));
			clientSender = new WrappingSender<Event<?>>(serviceRegistry.getServiceInstance(MBusContract.class).createSender(EventType.CLIENT, con.getPublicId()));
			systemState.getCacheHandler().addListener(listener );
			state = State.STARTED;
			bindToJMX();
			logoutListener = new ClientLogoutReporter(serviceRegistry);
		} catch (MBusException e) {
			throw new RuntimeException("Could not create router for messagebus", e);
		}
	}

	public void stop() {
	    getForceCloseScheduler().shutdown();
	    setForceCloseScheduler(null);
		systemState.getCacheHandler().removeListener(listener);
		state = State.STOPPED;		
	}

	public boolean accepting() {
		return state.equals(State.STARTED);
	}

	
	/**
     * Register the service to the JMX server
     *
     */
    private void bindToJMX() {
    	try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.clients:type=ClientRegistry");
            mbs.registerMBean(this, monitorName);
    	} catch (Exception ex) {
    		log.warn("Could not bind Client Registry to JMX: "+ex);
    	}
    }

    public void addClientTable(int playerId, int tableId, int seat) {
    	addClientTable(playerId, tableId, seat, -1);
    }
    
	public void addClientTable(int playerId, int tableId, int seat, int mttId) {
		String fqn = getTableFqn(playerId);
		systemState.setAttribute(fqn, String.valueOf(tableId), seat);
		if(mttId != -1) {
			systemState.setAttribute(fqn, _MTT_TABLE_PREFIX + String.valueOf(tableId), mttId);
		}
	}
	
	public int getTableMttId(int playerId, int tableId) {
		String fqn = getTableFqn(playerId);
		Object o = systemState.getAttribute(fqn, _MTT_TABLE_PREFIX + String.valueOf(tableId));
		if(o == null) {
			return -1;
		} else {
			String tmp = String.valueOf(o);
			return Integer.parseInt(tmp);
		}
	}
	
	public void addWatchingTable(int playerId, int tableId) {
		String fqn = getWatcherFqn(playerId);
		systemState.setAttribute(fqn, String.valueOf(tableId), tableId);
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#removeClientTable(int, int)
	 */
	public void removeClientTable(int playerId, int tableId) {
		String fqn = getTableFqn(playerId);
		systemState.removeAttribute(fqn, _MTT_TABLE_PREFIX + String.valueOf(tableId));
		systemState.removeAttribute(fqn, String.valueOf(tableId));
	}

	
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#removeClientFromAllTables(int)
	 */
	public void removeClientFromAllTables(int playerId) {
		String fqn = getTableFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		for (Object table: tables.keySet()) {
			String tmp = String.valueOf(table);
			if(tmp.startsWith("_")) continue; // META DATA;
			int tableId = Integer.parseInt(tmp);
			GameEvent ev = createLeaveEvent(playerId, tableId);
			if (log.isDebugEnabled()) {
				log.debug("Unseating player["+playerId+"] from table["+tableId+"]");
			}
			try {
				sender.dispatch(ev);
				/*
				 * NB: Does not work. Local seated is not the correct
				 * number when running with multiple client nodes.
				 */
				// CGWMonitor.localSeatedPlayers.decrementAndGet();
			} catch (ChannelNotFoundException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not remove player from table. Table not found in router: "+ev.getTableId());
				}
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#removeWatchingTable(int, int)
	 */
	public void removeWatchingTable(int playerId, int tableId) {
		String fqn = getWatcherFqn(playerId);
		systemState.removeAttribute(fqn, String.valueOf(tableId));
	}

	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.ClientRegistry#removeWatcherFromAllTables(int)
	 */
	public void removeWatcherFromAllTables(int playerId, boolean clientGenerated) {
		String fqn = getWatcherFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		for (Object table: tables.keySet()) {
			int tableId = Integer.parseInt(String.valueOf(table));
			GameEvent ev = createUnwatchEvent(playerId, tableId, clientGenerated);
			try {
				sender.dispatch(ev);
			} catch (ChannelNotFoundException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not remove watcher from table. Table not found in router: "+ev.getTableId());
				}
			}
		}
	}

	public Map<Integer, Integer> getSeatedTables(int playerId) {
		String fqn = getTableFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Object table: tables.keySet()) {
			String tmp = String.valueOf(table);
			if(tmp.startsWith("_")) continue; // META DATA 
			int tableId = Integer.parseInt(tmp);
			result.put(tableId, (Integer)tables.get(table));
		}
		return result;
	}
	
	public Map<Integer, Integer> getSeatedTournamentTables(int playerId) {
		String fqn = getTableFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Object table: tables.keySet()) {
			String tmp = String.valueOf(table);
			if(tmp.startsWith("_")) continue; // META DATA 
			int tableId = Integer.parseInt(tmp);
			if(tables.containsKey(_MTT_TABLE_PREFIX + tableId)) {
				result.put(tableId, (Integer)tables.get(_MTT_TABLE_PREFIX + tableId));
			}
		}
		return result;
	}

	public List<Integer> getWatchingTables(int playerId) {
		String fqn = getWatcherFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		List<Integer> result = new ArrayList<Integer>(tables.size());
		for (Object table: tables.values()) {
			int tableId = Integer.parseInt(String.valueOf(table));
			result.add(tableId);
		}
		return result;
	}
	
	
	public Map<Object, Object> getPlayerData(int playerId) {
		String fqn = SystemStateConstants.CLIENT_ROOT_FQN+playerId;
		return systemState.getAttributes(fqn);
	}
	
	public String getScreenname(int playerId) {
		String fqn = SystemStateConstants.CLIENT_ROOT_FQN+playerId;
		return (String)systemState.getAttribute(fqn, String.valueOf(ClientStatusFields.SCREEN_NAME));
	}
	
	public int getOperatorId(int playerId) {
		String fqn = SystemStateConstants.CLIENT_ROOT_FQN+playerId;
		return (Integer)systemState.getAttribute(fqn, String.valueOf(ClientStatusFields.OPERATOR_ID));
	}
	
	public ClientReaper createReaper() {
		return new StateClientReaper(systemState, this);
	}
	
	/**
	 * Get session id from system state
	 * 
	 * @param playerId
	 * @return
	 */
	public String getSessionId(int playerId) {
		return systemState.getAttribute(SystemStateConstants.CLIENT_ROOT_FQN+playerId, String.valueOf(ClientStatusFields.SESSION_ID)).toString();
	}

	public com.cubeia.firebase.api.util.SocketAddress getClientMBusChannel(int id) {
		String tmp = (String)systemState.getAttribute(SystemStateConstants.CLIENT_ROOT_FQN+id, String.valueOf(ClientStatusFields.MBUS_CHANNEL));
		if(tmp != null) {
			try {
				return new com.cubeia.firebase.api.util.SocketAddress(tmp);
			} catch (UnknownHostException e) {
				log.error("Failed to create socket address", e);
				return null;
			}
		} else {
			return null;
		}
	}
	

	/**
	 * Create a system specific remove player action
	 */
	private GameEvent createLeaveEvent(int playerId, int tableId) {
		GameEvent ev = new GameEvent();
		ev.setAction(new RemovePlayerAction(playerId, tableId));
		ev.setPlayerId(playerId);
		ev.setTableId(tableId);
		return ev;
	}
	
	private GameEvent createUnwatchEvent(int playerId, int tableId, boolean clientGenerated) {
		GameEvent ev = new GameEvent();
		UnWatchAction unWatchAction = new UnWatchAction(playerId, tableId);
		unWatchAction.setByServer(!clientGenerated);
		ev.setAction(unWatchAction);
		ev.setPlayerId(playerId);
		ev.setTableId(tableId);
		return ev;
	}
	
	
	/**
	 * Send a PlayerStatusAction to all tables that the client is seated at.
	 * 
	 * @param playerId
	 * @param status
	 */
	private void reportStatusChangeToAllTables(int playerId, PlayerStatus status) {
		String fqn = getTableFqn(playerId);
		Map<Object, Object> tables = systemState.getAttributes(fqn);
		for (Object table: tables.keySet()) {
			String tmp = String.valueOf(table);
			if(tmp.startsWith("_")) continue; // META DATA
			int tableId = Integer.parseInt(tmp);
			GameEvent ev = createStatusChangedEvent(playerId, tableId, status);
			try {
				sender.dispatch(ev);
			} catch (ChannelNotFoundException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not update player status. Table not found in router: "+ev.getTableId());
				}
			}
		}
	}

	private GameEvent createStatusChangedEvent(int playerId, int tableId, PlayerStatus status) {
		GameEvent ev = new GameEvent();
		ev.setAction(new PlayerStatusAction(playerId, tableId, status));
		ev.setPlayerId(playerId);
		ev.setTableId(tableId);
		return ev;
	}
	
		
	private String getTableFqn(int playerId) {
		String fqn= SystemStateConstants.CLIENT_ROOT_FQN+playerId+SystemStateConstants.CLIENT_TABLES+"/";
		return fqn;
	}
	
	private String getWatcherFqn(int playerId) {
		String fqn= SystemStateConstants.CLIENT_ROOT_FQN+playerId+SystemStateConstants.CLIENT_WATCHING+"/";
		return fqn;
	}
	
	
	private void checkDestroySender() {
		if(sender != null) { 
			sender.destroy();
			sender = null;
		}
	}
	
	/**
	 * Kick out player operation from the network
	 * 
	 * @param playerId
	 */
	public void kickPlayer(int playerId) {

		ForcedLogoutPacket packet = new ForcedLogoutPacket();
		// TODO: change to non hardcoded values  
		packet.code = 2; 
		packet.message = "You have been kicked";

		// send packet to client if applicable
		Client client = getClient(playerId);
		if (client != null) {
			client.sendClientPacket(packet);
			
			// schedule kick client
			KickClient kickClient = new KickClient(this, playerId);
			getForceCloseScheduler().schedule(kickClient, KICK_WAIT, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Kick out player operation from the network
	 * 
	 * @param playerId
	 */
	public void clientLoggedInRemotely(int clientId) {
		// create a forced logout packet
		ForcedLogoutPacket packet = new ForcedLogoutPacket();
		// TODO: change to non hardcoded values  
		packet.code = 1; 
		packet.message = "You have logged in from another place";

		// send packet to client if applicable
		Client client = getClient(clientId);
		if (client != null) {
			client.sendClientPacket(packet);
			
			// schedule kick client
			KickClient kickClient = new KickClient(this, clientId);
			getForceCloseScheduler().schedule(kickClient, KICK_WAIT, TimeUnit.MILLISECONDS);
		}
	}
	
	 /**
	 * Task for kick client
	 * Will be executed after a delay of KICK_WAIT seconds 
	 *
	 * @author Peter
	 */
	private class KickClient implements Runnable {
		private final StateClientRegistry registry;
		private final int playerId;
		public KickClient(StateClientRegistry registry, int playerId) { this.registry = registry; this.playerId = playerId; }
		public void run() {
			// get client instance
			Client client = registry.getClient(playerId);
			// first, remove the client from all the tables and cleanup registry
			registry.logoutClient(client, true);
			// second, close connection if we have a valid client
			if ( client != null ) {
				client.close();
			}
			
			logoutListener.clientClosed(playerId, ClientClosedReason.KICKED);
		}
	}

	
    /* (non-Javadoc)
     * @see com.cubeia.firebase.service.clientreg.ClientRegistry#reportStatusChanged(int, int, com.cubeia.firebase.api.game.player.PlayerStatus)
     */
    public void reportTableStatusChanged(int tableId, int playerId, PlayerStatus status) {
        GameEvent ev = createStatusChangedEvent(playerId, tableId, status);
        try {
            sender.dispatch(ev);
        } catch (ChannelNotFoundException e) {
            log.error("Could not update player status. Table not found in router: "+ev.getTableId());
        }        
    }

    /**
     * Kick player from a specific table
     * 
     * @author peter
     * 
     * @param playerId
     * @param tableId
     */
	public void kickPlayerFromTable(int playerId, int tableId) {
		// Force player to leave table
		GameEvent ev = createLeaveEvent(playerId, tableId);
		try {
			sender.dispatch(ev);
			// CGWMonitor.localSeatedPlayers.decrementAndGet();
		} catch (ChannelNotFoundException e) {
			log.error("Could not remove player from table. Table not found in router: "+ev.getTableId());
		}

		// Send kick player packet to player
		KickPlayerPacket packet = new KickPlayerPacket();
		// TODO: change to non hardcoded values  
		packet.reasonCode = 0; 
		packet.tableid = tableId;

		// send packet to client if applicable
		Client client = getClient(playerId);
		if (client != null) {
			client.sendClientPacket(packet);
		}
	}

	public void addNodeId(String id) {
		nodeIdMap.put(id, id);
		log.info("Client Registry is now managing the following nodes (added): "+nodeIdMap.values());
	}

	public void removeNodeId(String id) {
		nodeIdMap.remove(id);
		log.info("Client Registry is now managing the following nodes (removed): "+nodeIdMap.values());
	}

	public SocketAddress getRemoteAddress(int clientId) {
		Client client = clients.get(clientId);
		if (client != null) {
			return client.getRemoteAddress();
		} else {
			return null;
		}
	}
	
	public String getRemoteAddressText(int clientId) {
		SocketAddress remoteAddress = getRemoteAddress(clientId);
		if (remoteAddress == null) {
			return null;
		} else {
			return remoteAddress.toString();
		}
	}
	
	public void sendSystemMessage(int type, int level, String message) {
		this.sendSystemMessage(type, level, null, message);
	}
	
	public void sendSystemMessage(int type, int level, int[] playerIds, String message) {
        MulticastClientEvent event = new MulticastClientEvent();
        SystemMessageAction action = new SystemMessageAction(type, level, playerIds, message);
        event.checkSetPlayersGlobal(playerIds);
        event.setAction(action);
        try {
            clientSender.dispatch(event);
        } catch (Exception e) {
            // We need a catch all here since this method will typically be executed
            // from a user generated remote call.
            log.error("Unexpected error when sending a system message: type["+type+"] level["+level+"] msg["+message+"]", e);
        }
    }
	
	/**
     * This is the hard and final removal of a client session.
     * 
     * Remove the client from local registry.
     * Remove client from all tables (set status to DISCONNECTED)
     * Remove client as watcher.
     * Remove session from system state.
     * @param id
     */
    private void removeClient(int id) {
        Client client = clients.remove(id);
        removeClientFromAllTables(id);
        // We need to run the unwatcher again due to asynch behaviour
        removeWatcherFromAllTables(id, false);
        systemState.removeNode(SystemStateConstants.CLIENT_ROOT_FQN+id);
        if (client != null) {
            client.close();
        }
    }
	
	private void closeClient(int clientId) {
		Client client = clients.get(clientId);
		if (client != null) {
			client.close();
		}
	}

    private static void setForceCloseScheduler(JmxScheduler forceCloseScheduler) {
        StateClientRegistry.forceCloseScheduler = forceCloseScheduler;
    }

    private static JmxScheduler getForceCloseScheduler() {
        return forceCloseScheduler;
    }

   

    
}