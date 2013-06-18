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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;

import com.cubeia.core.space.jboss.FqnUtil;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.lobby.LobbyPathUtil;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.ConcurrentHashSet;
import com.cubeia.firebase.api.util.SecondCounter;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.TableSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.LobbyQueryRequest;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.lobby.snapshot.generator.MttSnapshotGenerator;
import com.cubeia.firebase.server.lobby.snapshot.generator.SnapshotGenerator;
import com.cubeia.firebase.server.lobby.snapshot.generator.TableSnapshotGenerator;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.firebase.util.executor.JmxScheduler;

/**
 * The State Lobby uses the System State Service to get table
 * information.
 * 
 * The StateLobby holds a SnapshotGenerator that handles changes in the system state.
 * The SnapshotGenerator provides the StateLobby with sets of full packets for a node
 * or delta updates for a node. The packets within the generators should be cached so that
 * retrievals within a snapshot update-period are idempotent.
 * 
 * NOTE: This implementation is localized on the Gateway node and holds a direct reference
 * to the client session. This way we can optimize the lobby data sending.
 * 
 * @author Fredrik
 */
public class StateLobby implements Lobby, StateLobbyMBean, LobbyListener {

	static final StyxSerializer SERIALIZER = new StyxSerializer(null);
	private static final boolean USE_LIST_BROADCAST = System.getProperty("com.game.server.lobby.systemstate.listBroadcast", "true").equals("true");
	
	private Logger log = Logger.getLogger(this.getClass());
	
	/** Current service state */
	private State state = State.STOPPED;
	
	/** The service registry */
	private final ServiceRegistry registry;
	
	/** Lobby update period. Default = 2 seconds. */
	private long broadcastPeriod = 2000;
	
	/**
	 * Handles the lobby data structure.
	 * 
	 * @return
	 */
	private LobbyCacheHandler cacheHandler;
	
	/** A JMX-bound scheduler thread pool */
	private JmxScheduler scheduler = new JmxScheduler(1, "LobbyGenerator");
	
	/** A JMX-bound executor for executing change notifications */
	private JmxExecutor executor = new JmxExecutor(1, "LobbyUpdate");
	
	private ConcurrentMap<LobbyPathType, SnapshotGenerator> generators = new ConcurrentHashMap<LobbyPathType, SnapshotGenerator>();
	
	/**
	 * Holds all current subscriptions on this node.
	 */
	private ConcurrentMap<LobbyPath, Set<Client>> subscriptions = new ConcurrentHashMap<LobbyPath, Set<Client>>(); 

	/**
	 * Holds object specific subscribers
	 */
	private ObjectSubscriptions objectSubscriptions = new ObjectSubscriptions();
	
	/** Listeners for lobby changes */
	private Set<LobbyListener> listeners = new ConcurrentHashSet<LobbyListener>();

	/** The broadcast task. We keep a reference so we can stop and start it */
	private ScheduledFuture<?> bcastTask;
	
	private SecondCounter updateCounter = new SecondCounter();
	
	/**
	 * If this flag is set, then we will run broadcast tasks as usual,
	 * but we will not send out any data to the clients.
	 */ 
	private AtomicBoolean paused = new AtomicBoolean(false);
	
	/**
	 * Constructor.
	 * We will use the registry to lookup the system state.
	 * 
	 * @param registry
	 */
	public StateLobby(ServiceRegistry registry) {
		this.registry = registry;
		initJmx();
		initBroadcastInterval();
		// Create Generators
		SnapshotGenerator tableSnapshotGenerator = new TableSnapshotGenerator(this, LobbyPathType.TABLES);
		SnapshotGenerator mttSnapshotGenerator = new MttSnapshotGenerator(this, LobbyPathType.MTT);
		generators.put(LobbyPathType.TABLES, tableSnapshotGenerator);
		generators.put(LobbyPathType.MTT, mttSnapshotGenerator);
	}
	
	private void initBroadcastInterval() {
		if (registry != null) {
			ClusterConfigProviderContract contr = registry.getServiceInstance(ClusterConfigProviderContract.class);
			Namespace nameSpace = NodeRoles.getNodeNamespace(ClusterRole.CLIENT_NODE, "lobby");
			StateLobbyConfig conf = contr.getConfiguration(StateLobbyConfig.class, nameSpace);
			broadcastPeriod = conf.getLobbyBroadcastPeriod();
			log.info("Lobby broadcast period set to: " + broadcastPeriod);
		}
	}

	public String getStateDescription() {
		return state.toString();
	}

	
	// -------------- SERVICE METHODS  ------------------
	
	/**
	 * Start the service and add the listener.
	 * Fetches the cache handler from the System State service.
	 */
	public void start() {
		state = State.STARTED;
		SystemStateCacheHandler cache = getCache();
		cacheHandler = new LobbyCacheHandler(cache, this);
		for (SnapshotGenerator gen : generators.values()) {
			gen.start();
		}
				
		scheduleBroadcast();
	}

	/**
	 * Schedules lobby broadcast with fixed delay.
	 */
	private void scheduleBroadcast() {
		Runnable bcast = null;
		if(USE_LIST_BROADCAST) {
			bcast = new StateLobbyListBroadcast(paused, generators, subscriptions, objectSubscriptions);
		} else {
			bcast = new StateLobbySingleBroadcast(paused, generators, subscriptions, objectSubscriptions);
		}
		bcastTask = scheduler.scheduleWithFixedDelay(bcast, broadcastPeriod, broadcastPeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * Needed for mocking reasons
	 * 
	 * @return SystemStateCacheHandler
	 */
	protected SystemStateCacheHandler getCache() {
		SystemStateCacheHandler cache = getSystemState().getCacheHandler();
		return cache;
	}

	public void stop() {
		bcastTask.cancel(false);
		scheduler.stop();
		for (SnapshotGenerator gen : generators.values()) {
			gen.stop();
		}
		
		cacheHandler.destroy();
		state = State.STOPPED;
		executor.stop();
	}
	
	// ----------- END OF SERVICE METHODS  ---------------
	
	

	// -------------- LOBBY LISTENER METHODS ------------------
    
	public void nodeCreated(LobbyPath path) {
		TableCreatedTask task = new TableCreatedTask(path);
		executor.submit(task);
	}

	public void nodeAttributeChanged(NodeChangeDTO change) {
		TableChangedTask task = new TableChangedTask(change);
		executor.submit(task);
	}

	public void tableRemoved(LobbyPath path) {
		TableRemovedTask task = new TableRemovedTask(path);
		executor.submit(task);
	}
	
	public void nodeRemoved(String path) {
		PathRemovedTask task = new PathRemovedTask(path);
		executor.submit(task);
	}

	
	// ---------- END OF LOBBY LISTENER METHODS  ---------------
	
	public void addPath(String path) {
		cacheHandler.addPath(path);
		/*
		 * Force generation of a full snapshot. This is done
		 * in order prevent snapshots from being created later, when 
		 * tables are actually picked up... Well, I have no
		 * idea what that actually means, but it does seem
		 * to work, at least for now /Larsan
		 */
		LobbyPath lp = FqnUtil.parseFqn(Fqn.fromString(path));
		SnapshotGenerator gen = generators.get(lp.getType());
		gen.initSnapshot(lp);
	}
	
	public void addLobbyListener(LobbyListener listener) {
		listeners.add(listener);
	}

	public void removeLobbyListener(LobbyListener listener) {
		listeners.remove(listener);
	}
	
	public long getBroadcastPeriod() {
		return broadcastPeriod;
	}

	public void setBroadcastPeriod(long period) {
		broadcastPeriod = period;
		
		if (bcastTask != null) {
			bcastTask.cancel(false);
			scheduleBroadcast();
		}
	}
	
	/**
	 * Acquire the system state.
	 * The method is protected to it can be overridden by 
	 * mock objects.
	 * @return
	 */
	protected SystemStateServiceContract getSystemState() {
		return registry.getServiceInstance(SystemStateServiceContract.class);
	}
	
	/**
	 * One shot query
	 */
	public List<ProtocolObject> getLobbyQuery(LobbyQueryRequest request) {
		SnapshotGenerator generator = getSnapshotGenerator(request.getType());
		List<ProtocolObject> packets = generator.getFullSnapshot(request.getPath());
		return packets;
	}
	
	
	public Collection<TableInfo> getTableInfos(LobbyPath path) {
		//Collection<TableInfo> tables = null; //new ArrayList<TableInfo>();
		String fqn = path.getNameSpace();
		if (cacheHandler.exists(fqn)) {
			return cacheHandler.getTables(fqn);			
		} else {
			return Collections.emptySet();
		}
		// return tables;
	}
	
	public Collection<TournamentInfo> getMttInfos(LobbyPath path) {
		//Collection<TournamentInfo> tournaments = new ArrayList<TournamentInfo>();
		String fqn = path.getNameSpace();
		if (cacheHandler.exists(fqn)) {
			return cacheHandler.getTournaments(fqn);			
		} else {
			return Collections.emptySet();
		}
		//return tournaments;
	}

	/**
	 * Subscription request.
	 * We get all leaves in the lobby tree and add the client as a subscriber to
	 * all of those.
	 * 
	 * 
	 * @param request
	 * @param client, reference to the Client for direct communication
	 */
	public void subscribe(LobbySubscriptionRequest request, Client client) {
		LobbyPath path = request.getPath();
	
		Collection<ProtocolObject> fullSnapshot = getSnapshotGenerator(request.getType()).getFullSnapshot(path);
		List<ProtocolObject> listSnapshot = toSnapshotLists(fullSnapshot);
		client.sendClientPackets(listSnapshot);
		
		// Get all nodes that we should register the player on
		Collection<LobbyPath> subNodes = cacheHandler.getSubNodes(path.getNameSpace());
		addSubscriberToNodes(client, subNodes);
	}
	
	private List<ProtocolObject> toSnapshotLists(Collection<ProtocolObject> fullSnapshot) {
		List<TableSnapshotPacket> tables = new LinkedList<TableSnapshotPacket>();
		List<TournamentSnapshotPacket> mtts = new LinkedList<TournamentSnapshotPacket>();
		
		for (ProtocolObject o : fullSnapshot) {
			if(o instanceof TableSnapshotPacket) {
				tables.add((TableSnapshotPacket)o);
			} else if(o instanceof TournamentSnapshotPacket) {
				mtts.add((TournamentSnapshotPacket)o);
			} else {
				log.error("Unknown packet type: " + o.getClass().getName());
			}
		}
		
		List<ProtocolObject> ret = new ArrayList<ProtocolObject>(2);
		if (tables.size() > 0) {
		    ret.add(new TableSnapshotListPacket(tables));
		}
		if (mtts.size() > 0) {
		    ret.add(new TournamentSnapshotListPacket(mtts));
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.game.server.lobby.Lobby#unsubscribeAll(com.cubeia.firebase.server.gateway.client.Client)
	 */
	public void unsubscribeAll(Client client) {
		if (client == null) {
			log.debug("Client was null when trying to unsubscribe all. Doing nothing.");
			return;
		}
		
		for (Set<Client> subscribers : subscriptions.values()) {
			subscribers.remove(client);
		}
		objectSubscriptions.removeAllSubscriptionsForClient(client);
	}

	/* (non-Javadoc)
	 * @see com.game.server.lobby.Lobby#unsubscribe(com.game.server.lobby.LobbyUnsubscriptionRequest, com.cubeia.firebase.server.gateway.client.Client)
	 */
	public void unsubscribe(LobbyUnsubscriptionRequest request, Client client) {
		Collection<LobbyPath> subNodes = cacheHandler.getSubNodes(request.getPath().getNameSpace());
		
		for (LobbyPath key : subNodes) {
			Set<Client> subscribers = getSubscribers(key);
			subscribers.remove(client);
		}
	}

	public void subscribeToLobbyObject(LobbySubscriptionRequest request, Client client) {
		log.debug("Add lobby object subscription: "+request);
		// Get applicable full snapshot
		Collection<ProtocolObject> fullSnapshot = getSnapshotGenerator(request.getType()).getFullSnapshot(request.getPath());
		ProtocolObject snapshotPacket = null;
		
		if (request.getType() == LobbyPathType.TABLES) {
			snapshotPacket = findTableSnapshotPacket(request.getPath().getObjectId(), fullSnapshot);
		} else 	if (request.getType() == LobbyPathType.MTT) {
			snapshotPacket = findTournamentSnapshotPacket(request.getPath().getObjectId(), fullSnapshot);
		}
		
		if (snapshotPacket != null) {
			objectSubscriptions.addSubscription(request, client);
			client.sendClientPacket(snapshotPacket);
		}
	}

	public void unsubscribeToLobbyObject(LobbyUnsubscriptionRequest request, Client client) {
		log.debug("Remove lobby object subscription: "+request);
		objectSubscriptions.removeSubscription(request, client);
	}
	
	public void addSubscribersToNewFQN(LobbyPath path) {
		// Check if we are missing snapshot for the path
		if (subscriptions.get(path) == null) {
			List<LobbyPath> newLobbyPaths = new LinkedList<LobbyPath>();
			LobbyPath base = new LobbyPath(path.getType(), path.getArea(), path.getDomain(), -1);
			newLobbyPaths.add(base);
			
			// Either we have no subscribers for this branch or we have a newly created FQN.
			// We will traverse upwards until we find an FQN that exists in subscription map or we hit the root
			LobbyPath ancestor = LobbyPathUtil.getAncestor(path);
			while (!ancestor.getDomain().equals("") && subscriptions.get(ancestor) == null) {
				newLobbyPaths.add(ancestor);
				ancestor = LobbyPathUtil.getAncestor(ancestor);
			}
			
			// FIXME: Correct?
			if(subscriptions.get(ancestor) != null) {
				addSubscribersToNodes(subscriptions.get(ancestor), newLobbyPaths);
				for (Client client : subscriptions.get(ancestor)) {
					List<ProtocolObject> fullSnapshot = getSnapshotGenerator(path.getType()).getFullSnapshot(path);
					client.sendClientPackets(fullSnapshot);
				}
			}
		}
		
	}
	
	public List<LobbyPath> getAllLobbyLeaves(LobbyPathType type) {
		List<LobbyPath> leaves = new ArrayList<LobbyPath>();
		List<LobbyPath> games = cacheHandler.getAreas(type);
		for (LobbyPath path : games) {
			leaves.addAll(cacheHandler.getEndNodes(path));
		}
		return leaves;
	}
	
	public Collection<LobbyPath> getLeaves(LobbyPath path) {
		Collection<LobbyPath> leaves = cacheHandler.getEndNodes(path);
		return leaves;
	}
	
	public ProtocolObject getSnapshot(LobbyPathType type, int objectId) {
		return getSnapshotGenerator(type).getSingleSnapshot(getPath(type, objectId), objectId);
	}	
	
	private LobbyPath getPath(LobbyPathType type, int objectId) {
		return getSnapshotGenerator(type).getPath(objectId);
	}

	/**
	 * Add this client as a subscriber to all provided nodes.
	 * 
	 * @param client
	 * @param subNodes
	 */
	private void addSubscriberToNodes(Client client, Collection<LobbyPath> subNodes) {
		for (LobbyPath key : subNodes) {
			Set<Client> subscribers = getSubscribers(key);
			subscribers.add(client);
			subscriptions.put(key, subscribers);
		}
	}
	
	private void addSubscribersToNodes(Set<Client> subscribers, Collection<LobbyPath> subNodes) {
		for (LobbyPath key : subNodes) {
			subscriptions.put(key, subscribers);
		}
	}
	
	/**
	 * Get snapshot generator depending on lobby type
	 * 
	 * @param type
	 * @return
	 */
	protected SnapshotGenerator getSnapshotGenerator(LobbyPathType type) {
		return generators.get(type);
	}
	
	/**
	 * Get or Create subscriber list for a path key 
	 * 
	 * @param key
	 * @return
	 */
	public Set<Client> getSubscribers(LobbyPath path) {
		Set<Client> clients = subscriptions.get(path);
	    if (clients == null) {
	    	ConcurrentHashSet<Client> newSet = new ConcurrentHashSet<Client>();
	    	clients = subscriptions.putIfAbsent(path, newSet);
	        if (clients == null) {
	        	clients = newSet;
	        }
	    }
	    return clients;
	}
	
	@Override
	public int countSubscribersForPath(String type, int area, String domain) {
		Set<Client> clients = subscriptions.get(new LobbyPath(LobbyPathType.valueOf(type), area, domain, -1));
	    return (clients == null ? 0 : clients.size());
	}
	
	 /**
     * Add MBean info to JMX.
     * Will be called from the constructor.
     *
     */
    private void initJmx() {
        try {
            MBeanServer mbs = getMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.lobby:type=SysLobby");
            if (!mbs.isRegistered(monitorName)) {
            	mbs.registerMBean(this, monitorName);
            }
        } catch(Exception e) {
            log.error("failed to start JMX for State Lobby", e);
        }
    }
    
    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
    
    /**
     * Iterate all packets and find the matching table snapshot packet.
     * 
     * @param tableId
     * @param fullSnapshot
     * @return
     */
    private ProtocolObject findTableSnapshotPacket(int tableId, Collection<ProtocolObject> fullSnapshot) {
		ProtocolObject snapshotPacket = null;
		for (ProtocolObject protocol : fullSnapshot) {
			if (protocol instanceof TableSnapshotPacket) {
				TableSnapshotPacket packet = (TableSnapshotPacket) protocol;
				if (packet.tableid == tableId) {
					snapshotPacket = packet;
					break;
				}
			}
		}
		return snapshotPacket;
	}
	
    /**
     * Iterate all packets and find the matching tournament snapshot packet.
     * 
     * @param tableId
     * @param fullSnapshot
     * @return
     */
	private ProtocolObject findTournamentSnapshotPacket(int mttId, Collection<ProtocolObject> fullSnapshot) {
		ProtocolObject snapshotPacket = null;
		for (ProtocolObject protocol : fullSnapshot) {
			if (protocol instanceof TournamentSnapshotPacket) {
				TournamentSnapshotPacket packet = (TournamentSnapshotPacket) protocol;
				if (packet.mttid == mttId) {
					snapshotPacket = packet;
					break;
				}
			}
		}
		return snapshotPacket;
	}

    /**
	 * Task for executing a table removed notification.
	 * We will notify all users.
	 * 
	 * @author Fredrik
	 */
	private class TableRemovedTask implements Runnable {
		private final LobbyPath path;
		public TableRemovedTask(LobbyPath path){ this.path = path; }
		
		public void run() {
			try {
				// Code below reports to both MTT and Table generators = no good!
				for (LobbyListener listener : listeners) {
					listener.tableRemoved(path);
				}
				
				// Get the correct generator
				LobbyListener listener = generators.get(path.getType());
				listener.tableRemoved(path);
				
				// Make sure object subscriptions can clean up
				objectSubscriptions.removeObject(path, path.getObjectId());
				
			} catch (Exception e) {
				log.error("Failed to remove object from listener. Path: "+path, e);
			}
		}
	}
	
	private class PathRemovedTask implements Runnable {
		private final String path;
		public PathRemovedTask(String path){ this.path = path; }
		
		public void run() {
			try {
				// Code below reports to both MTT and Table generators = no good!
				for (LobbyListener listener : listeners) {
					listener.nodeRemoved(path);
				}
				// Check un-subscriptions
				subscriptions.remove(path);
				Collection<LobbyPath> subNodes = cacheHandler.getSubNodes(path);
				for (LobbyPath key : subNodes) {
					subscriptions.remove(key);
				}
			} catch (Exception e) {
				log.error("Failed to remove lobby path from listener. Path: "+path, e);
			}
		}
	}
	
	/**
	 * Task for executing a table changed notification
	 * 
	 * @author Fredrik
	 */
	private class TableChangedTask implements Runnable {
		private final NodeChangeDTO change;
		public TableChangedTask(NodeChangeDTO change){ this.change = change; }
		
		public void run() {
			try {
				updateCounter.register();
				
				for (LobbyListener listener : listeners) {
					listener.nodeAttributeChanged(change);
				}
				
				// Get the correct generator
				LobbyListener listener = generators.get(change.getPath().getType());
				listener.nodeAttributeChanged(change);
				
				// If a new FQN we might need to include subscribers
				addSubscribersToNewFQN(change.getPath());
			} catch (Throwable e) {
				log.error("Failed to handle lobby node change", e);
			}
		}
	}
	
	/**
	 * Task for executing a table created notification
	 * 
	 * @author Fredrik
	 */
	private class TableCreatedTask implements Runnable {
		private final LobbyPath path;
		public TableCreatedTask(LobbyPath path){ this.path = path; }
		
		public void run() {
			for (LobbyListener listener : listeners) {
				listener.nodeCreated(path);
			}
		}
	}
	
	
	/* --------------------------------------------------
	 * 
	 *   JMX METHODS
	 * 
	 * -------------------------------------------------- */
	
	/**
	 * Write all subscriptions to the log file.
	 * This can be very extensive and should not be used frequently.
	 * 
	 */
	public void dumpSubscriptionInfoToLog() {
		log.info(printSubscriptionInfo());
	}
	
	public String printSubscriptionInfo() {
		String info = "Current Lobby Subscriptions: \n";
		for (LobbyPath path : subscriptions.keySet()) {
			Set<Client> clients = new HashSet<Client>(subscriptions.get(path));
			info += "\n"+path+" ("+clients.size()+")\n";
			for (Client c : clients) {
				info += "\t"+c+"\n";
			}
    	}
		return info;
	}
	
	public String printPaths() {
		SnapshotGenerator tableGenerator = generators.get(LobbyPathType.TABLES);
		SnapshotGenerator tournamentGenerator = generators.get(LobbyPathType.MTT);
		
		String info = "TABLE PATHS:\n";
		ConcurrentMap<LobbyPath, FullSnapshot> tableSnapshots = tableGenerator.getFullSnapshots();
		for (LobbyPath path : tableSnapshots.keySet()) {
			info += path+"\n";
		}
		
		info += "\nTOURNAMENT PATHS\n";
		ConcurrentMap<LobbyPath, FullSnapshot> tournamentSnapshots = tournamentGenerator.getFullSnapshots();
		for (LobbyPath path : tournamentSnapshots.keySet()) {
			info += path+"\n";
		}
		
		return info;
	}
	
	public String printTableData(int tableId) {
		ProtocolObject snapshot = getSnapshot(LobbyPathType.TABLES, tableId);
		return String.valueOf(snapshot);
	}
	
	public String printTournamentData(int mttId) {
		ProtocolObject snapshot = getSnapshot(LobbyPathType.MTT, mttId);
		return String.valueOf(snapshot);
	}
	
	public int getTableUpdatesPerSecond() {
		return updateCounter.current();
	}

	public boolean isPaused() {
        return paused.get();
    }

    public void pause() {
        paused.set(true);
    }

    public void unpause() {
        paused.set(false);
    }
    
    public int getSnapshotNodeCount() {
    	int count = 0;
    	for (SnapshotGenerator generator : generators.values()) {
			ConcurrentMap<LobbyPath, FullSnapshot> snapshots = generator.getFullSnapshots();
			count += snapshots.size();
		}
    	return count;
    }
    
    public int getDeltaSnapshotNodeCount() {
    	int count = 0;
    	for (SnapshotGenerator generator : generators.values()) {
			ConcurrentMap<LobbyPath, DeltaSnapshot> snapshots = generator.getDeltaSnapshots();
			count += snapshots.size();
		}
    	return count;
    }
    
    public String printPacketCounts() {
    	ConcurrentMap<LobbyPath, Integer> fullPacketCount = new ConcurrentHashMap<LobbyPath, Integer>();
    	ConcurrentMap<LobbyPath, Integer> deltaPacketCount = new ConcurrentHashMap<LobbyPath, Integer>();
    	
    	for (SnapshotGenerator generator : generators.values()) {
			ConcurrentMap<LobbyPath, FullSnapshot> fullSnapshots = generator.getFullSnapshots();
			ConcurrentMap<LobbyPath, DeltaSnapshot> deltaSnapshots = generator.getDeltaSnapshots();
			
			for (LobbyPath path : fullSnapshots.keySet()) {
				FullSnapshot fullSnapshot = fullSnapshots.get(path);
				fullPacketCount.put(path, fullSnapshot.getLobbyData().size());
			}
			
			for (LobbyPath path : deltaSnapshots.keySet()) {
				DeltaSnapshot deltaSnapshot = deltaSnapshots.get(path);
				deltaPacketCount.put(path, deltaSnapshot.getLobbyData().size());
			}
		}
    	
    	String info = "Lobby Snapshots Per Chached Lobby Path\n";
    	info += "-----------------------\n";
    	info += "Full Packet Path Count:  "+fullPacketCount.size()+"\n";
    	info += "Delta Packet Path Count: "+deltaPacketCount.size()+"\n";
    	info += "-----------------------\n";
    	info += "FULL PACKET COUNT PER PATH\n";
    	for (LobbyPath path : fullPacketCount.keySet()) {
    		info += StringUtils.rightPad(path.toString(), 60)+"\t : "+fullPacketCount.get(path)+"\n";
    	}
    	info += "-----------------------\n";
    	info += "DELTA PACKET COUNT PER PATH\n";
    	for (LobbyPath path : deltaPacketCount.keySet()) {
    		info += StringUtils.rightPad(path.toString(), 60)+"\t : "+deltaPacketCount.get(path)+"\n";;
    	}
    	
    	return info;
    }
    
    public int getSubscriptionCount() {
    	int count = 0;
    	for (LobbyPath path : subscriptions.keySet()) {
    		count += subscriptions.get(path).size();
    	}
    	return count;
    }
    
}
