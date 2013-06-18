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
package com.cubeia.firebase.service.wlist;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinStatus;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.game.lobby.TableAttributeMapper;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.wlist.linear.LinearWaitingList;
import com.cubeia.firebase.service.wlist.linear.SimpleFilteredRequest;
import com.cubeia.firebase.service.wlist.lobby.WaitListLobbyListener;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

/*
 * TODO: 
 * 
 *  1) Question, the check for empty seats bypasses the waiting list,
 * does that open the issue of un-fair behavior?
 */
public class FilteredJoinServiceImpl implements Service, FilteredJoinServiceContract, FilteredJoinServiceImplMBean {

	private static final boolean DO_RETURN_HEAD_CHECK = System.getProperty("com.cubeia.firebase.service.wlist.returnHeadCheck", "true").equals("true");

	private transient Logger log = Logger.getLogger(this.getClass());
	
	private ServiceRegistry registry;

	/** The waiting list */
	private WaitingList waitingList = new LinearWaitingList();
	
	/** Listens to changes in the lobby and notifies this */
	private WaitListLobbyListener lobbyListener;
	
	private State state = State.STOPPED;

	/** Sender to the game layer */
	private Sender<GameEvent> sender;
	
	/** Map of requests. Used for cancellation and returns for failed seatings. */
	private ConcurrentMap<Long, FilteredRequest> requests = new ConcurrentHashMap<Long, FilteredRequest>();
	
	/** Locally unique IDs for requests */
	private AtomicLong idGenerator = new AtomicLong(0);

	
	public void destroy() {
		registry = null;
	}

	public void init(ServiceContext con) throws SystemException {
		registry = con.getParentRegistry();
	}

	public void start() {
		registerAsLobbyChangeListener(getLobby());
		initJMX();
		state = State.STARTED;
	}

	public void stop() {
		unregisterAsLobbyChangeListener(getLobby());
		destroyJMX();
		state = State.STOPPED;
	}
	
	/**
	 * We need a game router to send join actions
	 */
	public void setGameRouter(Sender<GameEvent> sender) {
		this.sender = sender;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.wlist.FilteredJoinService#addFilteredJoinAction(com.cubeia.firebase.api.action.local.FilteredJoinAction)
	 */
	public void addFilteredJoinAction(FilteredJoinAction action, LocalActionHandler loopback) {
		if (state != State.STARTED) {
			log.warn("Filtered Join Service not started yet. Request: "+action);
		}
		
		// Map the action to a request
		LobbyPath path = new LobbyPath(action.getGameId(), action.getAddress());
		FilteredRequest request = new SimpleFilteredRequest(action.getPlayerId(), path, action.getParameters());
		// Set client loopback here... NOTE: This must be removed if we ever distribute the requests
		request.setLoopback(loopback);
		
		// Add to cache
		long id = idGenerator.incrementAndGet();
		request.setId(id);
		requests.put(id, request);
		
		/*
		 * Here we should see if we can send a join request right away.
		 * But we haven't implemented that yet so we place in line instead.
		 */
		int directMatch = checkDirectMatch(request);
		boolean seating = directMatch != -1;
		
		// Send response, the status is depending on direct match found or not
		sendResponse(request, seating, action.getSeq(), loopback);
		
		if (seating) {
			sendJoin(request, directMatch, action.getSeq());
		} else {
			waitingList.addJoinRequest(request);
		}
	}

	
	/**
	 * Send a reserve seat request to the table.
	 * If the reservation failed, it will be picked up by the client event handler and
	 * the waiting list 
	 * @param seq 
	 * 
	 */
	public void sendJoin(FilteredRequest request, int tableId, int seq) {
		// Create action
		ReserveSeatRequestAction reserve = new ReserveSeatRequestAction(request.getPlayerId(), tableId, -1);
		reserve.setWaitingList(true);
		reserve.setWaitingListId(request.getId());
		reserve.setWaitingListSequence(seq);
		
		// Create message bus event
		GameEvent event = new GameEvent();
		event.setAction(reserve);
		event.setPlayerId(request.getPlayerId());
		event.setTableId(tableId);
		
		try {
			sender.dispatch(event);
		} catch (ChannelNotFoundException e) {
			log.error("No channel defined for the request: "+request, e);
		}
	}

	/**
	 * Cancel a request and return with a response.
	 * Status will be set as:
	 * 
	 * OK: Request was removed
	 * DENIED: Request was not found
	 * FAILED: Error occured
	 */
	public void cancelFilteredJoinAction(FilteredJoinCancelAction action, LocalActionHandler loopback) {
        Arguments.notNull(loopback, "loopback");
		FilteredJoinCancelResponseAction response = new FilteredJoinCancelResponseAction(action.getRequestId());
		response.setStatus(Status.DENIED.ordinal());
		
		try {
			FilteredRequest request = requests.remove(action.getRequestId());
			if (request != null) {
				waitingList.removeRequest(request);
				response.setStatus(Status.OK.ordinal());
			}
		} catch (Exception e) {
			response.setStatus(Status.FAILED.ordinal());
			log.warn("Removing a filtered join request reported failure: "+e, e);
		} finally {
			loopback.handleAction(response);
		}
	}
    
    /* (non-Javadoc)
     * @see com.cubeia.firebase.service.wlist.FilteredJoinService#cancelFilteredJoinAction(java.lang.Long)
     */
    public boolean cancelFilteredJoinAction(Long requestId) {
        boolean removed = false;
        FilteredRequest request = requests.remove(requestId);
        if (request != null) {
            waitingList.removeRequest(request);
            removed = true;
        }
        return removed;
    }
    
    
    public void consumeFilteredJoinRequest(long requestId) {
    	log.debug("Consume filtered join request; "+requestId+" -> "+requests.get(requestId));
		requests.remove(requestId);
	}
    
    
    public void returnFilteredJoinRequest(long requestId) {
    	boolean atHead = waitingList.returnRequest(requests.get(requestId));
    	if(atHead && DO_RETURN_HEAD_CHECK) {
    		FilteredRequest req = requests.get(requestId);
    		if(req == null) return; // EARLY RETURN; SANITY CHECK !!

    		log.debug("Returned request " + requestId + " was at the HEAD of the waiting list, will re-check placement; Trac issue [ #248 ]");
    		
    		int directMatch = checkDirectMatch(req);
    		
    		if (directMatch != -1) {
    			log.debug("Returned request " + requestId + " did match table " + directMatch + " on at-HEAD re-check; Sending join");
    			sendJoin(req, directMatch, -1);
    		}
    	}
	}
	
	/**
	 * MBean JMX interface method for getting the size of the cached requests.
	 */
	public long getRequests() {
		return requests.size();
	}
	
	protected Lobby getLobby() {
		return registry.getServiceInstance(LobbyServiceContract.class).getLobby();
	}
	
	private void registerAsLobbyChangeListener(Lobby lobby) {
		Arguments.notNull(lobby, "Lobby");
		ClientRegistry clientRegistry = registry.getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
        lobbyListener = new WaitListLobbyListener(this, waitingList, clientRegistry);
		lobby.addLobbyListener(lobbyListener);
	}
	
	private void unregisterAsLobbyChangeListener(Lobby lobby) {
		Arguments.notNull(lobby, "Lobby");
		lobby.removeLobbyListener(lobbyListener);
		lobbyListener = null;
	}

	private int checkDirectMatch(FilteredRequest request) {
		SystemStateServiceContract state = getSystemState();
		/*
		 * Find table end nodes for the given request, iterate linearly, sort
		 * matches by number of seated players, if match is on 1 empty seat: return
		 * eagerly. /LJN
		 */
		LobbyPath rootPath = request.getFqn();
		List<TmpTable> relevantNodes = getTableNodes(rootPath, state);
		return searchDirectMatch(relevantNodes, state, request);
	}

	private int searchDirectMatch(List<TmpTable> relevantNodes, SystemStateServiceContract state, FilteredRequest request) {
		LinkedList<TmpTable> matches = new LinkedList<TmpTable>();
		for (TmpTable table : relevantNodes) {
			if(request.match(table.attributes)) {
				int free = table.getCapacity() - table.getSeated();
				/*if(free == 1) return table.tableId; // SHORTCUT, KNOWN BEST MATCH == ONE EMPTY SEAT
				else */if(free > 0) { // AVAILABLE SEATS
					matches.add(table);
				}
			}
		}
		
		removeSeatedTables(matches, request.getPlayerId());

		if (matches.size() == 0) {
		    return -1;
		} else {
			Collections.sort(matches, new TmpTableComparator());
			return matches.getLast().tableId;
		}
	}

	/**
	 * Removes all tables where the player is already seated from
	 * the list of tables.
	 * @param matches
	 */
	private void removeSeatedTables(List<TmpTable> matches, int playerId) {
	    ClientRegistryServiceContract clientRegistryService = registry.getServiceInstance(ClientRegistryServiceContract.class);
	    Map<Integer, Integer> seatedTables = clientRegistryService.getClientRegistry().getSeatedTables(playerId);
	    
	    ListIterator<TmpTable> listIterator = matches.listIterator();
	    while (listIterator.hasNext()) {
	        TmpTable match = listIterator.next();
	        if (seatedTables.containsKey(match.tableId)) {
	            log.debug("Removing seated table: " + match.tableId);
                listIterator.remove();
            }
	    }
    }

    private List<TmpTable> getTableNodes(LobbyPath root, SystemStateServiceContract state) {
		Set<String> set = state.getEndNodes(root.getNameSpace());
		LinkedList<TmpTable> tabs = new LinkedList<TmpTable>();
		for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
			String path = it.next();
			Object test = state.getAttribute(path, TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
			if(test != null && test.equals(TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE)) {
				Map<?, ?> attributes = state.getAttributes(path);
				int id = parseTableId(attributes);
				if(id != -1) {
					tabs.add(new TmpTable(id, attributes));
				}
			}
		}
		return tabs;
	}

	private int parseTableId(Map<?, ?> attributes) {
		Object tmp = attributes.get(DefaultTableAttributes._ID.toString());
		return safeParse(tmp);
	}
	
	private int safeParse(Object tmp) {
		if(tmp instanceof Integer) return ((Integer)tmp).intValue();
		try {
			return (tmp == null ? -1 : Integer.parseInt(tmp.toString()));
		} catch(NumberFormatException e) {
			log.error(e);
			return -1;
		}
	}

	private SystemStateServiceContract getSystemState() {
		return registry.getServiceInstance(SystemStateServiceContract.class);
	}
	
	

	
	/**
	 * Send a response message to the client synchonously.
	 * The synchronousity is needed to guarantee ordering, the client should
	 * recieve a response before a seating/waiting list notification.
	 * @param seq 
	 * 
	 * @pararm request, the request
	 * @param loopback, handler for the calling client
	 */
	private void sendResponse(FilteredRequest request, boolean seating, int seq, LocalActionHandler loopback) {
		FilteredJoinResponseAction action = new FilteredJoinResponseAction(request.getPlayerId(), request.getFqn().getArea());
		action.setRequestId(request.getId());
		action.setSeq(seq);
		action.setAddress(request.getFqn().getDomain());
		action.setParameters(request.getParameters().values());
		if (seating) {
			action.setStatus(FilteredJoinStatus.SEATING.ordinal());
		} else {
			action.setStatus(FilteredJoinStatus.WAIT_LIST.ordinal());
		}
		
		loopback.handleAction(action);
	}
	
	
	/**
     * Register the class to the JMX server
     *
     */
    private void initJMX() {
    	try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.waitlist:type=FilteredJoinService");
            mbs.registerMBean(this, monitorName);
            
    	} catch (Exception ex) {
    		log.warn("Could not bind Node Waiting List to JMX: "+ex);
    	}
    }
    
    /**
     * Register the class to the JMX server
     *
     */
    private void destroyJMX() {
    	try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.waitlist:type=FilteredJoinService");
            mbs.unregisterMBean(monitorName);
    	} catch (Exception ex) {
    		log.warn("Could not bind Node Waiting List to JMX: "+ex);
    	}
    }
	
    
    
	// --- PRIVATE CLASSES --- //
	
	private class TmpTableComparator implements Comparator<TmpTable> {
		public int compare(TmpTable o, TmpTable p) {
			int a = o.getSeated();
			int b = p.getSeated();
			if(a == b) return 0;
			else if(a < b) return -1;
			else return 1;
		}
	}
	
	private class TmpTable {

		private final int tableId;
		private final Map<?, ?> attributes;
		
		private int seated = -1;
		private int capacity = -1;
		
		public TmpTable(int id, Map<?, ?> attributes) {
			this.attributes = attributes;
			this.tableId = id;
		}
		
		public int getCapacity() {
			if(capacity == -1) readCapacity();
			return capacity;
		}
		
		public int getSeated() {
			if(seated == -1) readSeated();
			return seated;
		}

		private void readSeated() {
			Object tmp = attributes.get(DefaultTableAttributes._SEATED.toString());
			seated = safeParse(tmp);
		}
		
		private void readCapacity() {
			Object tmp = attributes.get(DefaultTableAttributes._CAPACITY.toString());
			capacity = safeParse(tmp);
		}
	}


	
}
