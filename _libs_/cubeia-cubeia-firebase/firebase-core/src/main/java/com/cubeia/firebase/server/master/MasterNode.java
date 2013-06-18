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
package com.cubeia.firebase.server.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.Config;
import com.cubeia.firebase.server.commands.Halt;
import com.cubeia.firebase.server.commands.HaltMessage;
import com.cubeia.firebase.server.commands.Handshake;
import com.cubeia.firebase.server.commands.Leave;
import com.cubeia.firebase.server.commands.Promotion;
import com.cubeia.firebase.server.commands.Resume;
import com.cubeia.firebase.server.commands.ResumeMessage;
import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.server.node.Node;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.ClusterListener;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.conn.CommandResponse;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

public class MasterNode implements Node<MasterContext>, MasterNodeMBean {
	
	public static final int newHaltResumeId(SocketAddress a) {
		return a.hashCode();
	}
	
	public static final int newHaltResumeId(ClusterParticipant a) {
		return a.getServerId().hashCode();
	}
	
	public static enum HandshakeAction {
		SEND_CONFIG,
		SHUN_NODE,
		NAN
	}
	
	private static final long HALT_TIMEOUT = 30000;
	//private static final long RESUME_COMMAND_DELAY = 500;

	
	/// --- INSTANCE MEMBERS --- ///
	
	protected final Logger log = Logger.getLogger(getClass());

	private MasterCommandExec executor;
	private CommandListener listener;
	
	private MasterContext context;
	protected MasterConfig config;
	private ClusterNodeRegistry nodeRegistry;
	
	private final Lock layoutLock;
	private final MasterCommProxy commProxy;
	private final AtomicBoolean isInit;
	private final AtomicBoolean isSecondary;
	
	protected volatile State state;
	
	private final String id;

	private ScheduledThreadPoolExecutor sysThreads;

	public MasterNode(String id) {
		commProxy = new MasterCommProxy(id);
		isSecondary = new AtomicBoolean(true);
		layoutLock = new ReentrantLock();
		// mbusListener = new MBusTableListener(layout.get());
		isInit = new AtomicBoolean(false);
		this.id = id;
	}
	
	public void halt() { }
	
	public boolean isHalted() {
		return false;
	}
	
	public void resume() { }
	
	public String getId() {
		return id;
	}
	
	public void start() {
		state = State.STARTED;
	}
	
	public void stop() {
		state = State.STOPPED;
	}
	
	public String getStateDescription() {
		return state.toString();
	}
	
	public ClusterLayout getClusterLayout() {
		return nodeRegistry.getLayout();
	}

	public void destroy() {
		isInit.set(false);
		sendLeave();
		destroyJmx();
		stopListening();
		destroyThreadPools();
		commProxy.destroy();
	}

	public void init(MasterContext context) throws SystemException {
		this.context = context;
		setupConfig();
		commProxy.init(context);
		setupNodeRegistry(context);
		startListening();
		doHandshake();
		if(isPrimary()) {
			nodeRegistry.registerNodeUp(getMyNode());
			setupRequiredPartitions();
			broadcastConfig();
		}
		setupThreadPools();
		// setupTables();
		SystemLogger.info("Master node '" + getId() + "' initialized as [" + (isPrimary() ? "primary" : "secondary") + "] master.");
		isInit.set(true);
		initJmx();
	}
	
	public MasterContext getContext() {
		return context;
	}
	
	public ClusterNodeRegistry getNodeRegistry() {
		return nodeRegistry;
	}
	
	
	/// --- MBEAN --- ///
	
	public boolean isPrimary() {
		return !isSecondary.get();
	}
	
	public String getLocalAddress() {
		return getNodeAddress().toString();
	}
	
	public String getNodeId() {
		return getId();
	}
	
	
	/// --- PROTECTED METHODS --- ///
	
	protected CommandDispatcher getClusterDispatcher() {
		return commProxy.getClusterConnection().getCommandDispatcher();
	}
	
	protected CommandReceiver getClusterReceiver() {
		return commProxy.getClusterConnection().getCommandReceiver();
	}
	
	protected ClusterConnection getClusterConnection() {
		return commProxy.getClusterConnection();
	}
	
	protected boolean isPrimary(ClusterParticipant[] nodes) {
		return commProxy.isPrimaryMaster(nodes);
	}
	
	protected SocketAddress getNodeAddress() {
		return commProxy.getNodeAddress();
	}
	
	protected boolean sendAutomaticHandshake() {
		return false;
	}
	

	/// --- PRIVATE METHODS --- ///
	
	private void initJmx() {
		try {
			MBeanServer mbs = context.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=MasterNode");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = context.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=MasterNode");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void sendLeave() {
		Leave l = new Leave();
    	l.setAttachment(getMyNode());
    	ClusterConnection conn = getClusterConnection();
    	// Listener l = startHandshakeListening(conn);
		try {
			conn.getCommandDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, l);
			// conn.getCommandDispatcher().dispatch(l);
		} catch (ClusterException e) {
			log.error("Failed to send leave", e);
		} /*finally {
			stopHandshakeListening(conn, l);
		}*/	
	}
	
	private MBusContract getMBus() {
		return context.getServices().getServiceInstance(MBusContract.class);
	}

	private void setupConfig() throws SystemException {
		ClusterConfigProviderContract contr = context.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		config = contr.getConfiguration(MasterConfig.class, NodeRoles.getNodeNamespace(ClusterRole.MASTER_NODE, getId()));
	}
	
	private boolean isRelevantNode(ClusterParticipant node) {
		return node.getRole().isNodeRole() && !node.getRole().equals(ClusterRole.SERVER_NODE);
	}
    
    private void doHandshake() throws SystemCoreException {
    	ClusterConnection conn = getClusterConnection();
    	if(conn.countMembers() > 1) {
	    	Handshake h = new Handshake();
	    	h.setAttachment(getMyNode());
	    	h.setSource(id);
			try {
				CommandResponse[] rsps = getClusterDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, h);
				checkHandshakeResponse(rsps);
				// checkWaitForConfig();
			} catch (ClusterException e) {
				log.error("Failed to send handshake", e);
			}
    	} else {
    		isSecondary.set(false);
    	}
	}
    
	private void checkHandshakeResponse(CommandResponse[] rsps) throws SystemCoreException {
		for (CommandResponse rsp : rsps) {
			Object[] arr = (Object[])rsp.getAnswer();
			if(rsp.isReceived() && arr != null && arr.length > 0) {
				for (Object s : arr) {
					if(s instanceof Boolean) {
						if(((Boolean)s).booleanValue()) {
							isSecondary.set(true);
							return; // SUCESSFULL ENTRANCE TO CLUSTER
						} else {
							throw new SystemCoreException("Master node denied entrance to cluster!");
						}
					}
				}
			}
		}
		/*
		 * At this point: No master has managed to respond to the handshake, which 
		 * means we're probably the master.
		 */
		isSecondary.set(false);
	}

	private void sendPromotion(ClusterParticipant node) {
		if(!isPrimary()) return; // SANITY CHECK
		if(node == null) return; // SANITY CHECK
		Promotion p = new Promotion(node);
		p.setSource(id);
		try {
			log.info("Promoting node; Id: " + node.getId() + "; Role: " + node.getRole());
			getClusterDispatcher().send(Constants.NODE_LIFETIME_COMMAND_CHANNEL, p, node.getServerId().address);
		} catch (ClusterException e) {
			String msg = "Failed to send 'promotion' command over cluster channel; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
    
	private void sendHalt(int id) {
		if(!isPrimary()) return; // SANITY CHECK
		log.debug("Sending halt for id: " + id);
		Halt h = new Halt(new HaltMessage(id, "Cluster topology change"));
		h.setSource(this.id);
		try {
			sendWarnOnTimeout(Constants.NODE_LIFETIME_COMMAND_CHANNEL, h, HALT_TIMEOUT);
		} catch (ClusterException e) {
			String msg = "Failed to send 'halt' command over cluster channel; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}

	private void sendWarnOnTimeout(String channel, Command<?> com, long timeout) throws ClusterException {
		CommandResponse[] send = getClusterDispatcher().send(channel, com, timeout);
		for (CommandResponse r : send) {
			if(!r.isReceived()) {
				log.warn("Timeout occured while sending to '" + r.getReceiver() + "'; Timeout: " + timeout + "; Command: " + com);
			}
		}
	}
	
	static boolean doPartitionChangesFor(ClusterRole role) {
		return ClusterRole.GAME_NODE.equals(role) || ClusterRole.MTT_NODE.equals(role) || ClusterRole.CLIENT_NODE.equals(role);
	}
	
	static EventType getTypeFor(ClusterRole role) {
		if(role == ClusterRole.GAME_NODE) {
			return EventType.GAME;
		} else if(role == ClusterRole.MTT_NODE) {
			return EventType.MTT;
		} else {
			return null;
		}
	}
	
	static boolean doPartitionChangesFor(List<ClusterParticipant> nodes) {
		for (ClusterParticipant p : nodes) {
			ClusterRole r = p.getRole();
			if(doPartitionChangesFor(r)) {
				return true; // EARLY RETURN !!!
			}
		}
		return false;
	}
	
	private void setupNodeRegistry(MasterContext context) throws SystemException {
		nodeRegistry = new ClusterNodeRegistry(context.getMBeanServer());
	}
	
	private ClusterParticipant getMyNode() {
		return new ClusterParticipant(new NodeId(context.getServerId(), id), ClusterRole.MASTER_NODE, getMBus().getMBusDetails(), isPrimary());
	}

	private void stopListening() {
		// stopMBusListening();
		getClusterReceiver().removeCommandListener(listener);
		getClusterReceiver().removeCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, listener);
		getClusterReceiver().removeCommandListener(Constants.MTT_CREATION_COMMAND_CHANNEL, listener);
		getClusterReceiver().removeCommandListener(Constants.NODE_LIFETIME_COMMAND_CHANNEL, listener);
		// getClusterReceiver().removeCommandListener(null, listener);
		executor.destroy();
	}
	
	private void startListening() throws SystemCoreException {
		// startStateListening();
		startCommandListening();
		// startMBusListening();
	}

	private void startCommandListening() throws SystemCoreException {
		executor = new MasterCommandExec(this);
		listener = new MasterCommListener(executor, id);
		getClusterReceiver().addCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, listener);
		getClusterReceiver().addCommandListener(Constants.MTT_CREATION_COMMAND_CHANNEL, listener);
		getClusterReceiver().addCommandListener(Constants.NODE_LIFETIME_COMMAND_CHANNEL, listener);
		getClusterReceiver().addCommandListener(listener);
		ClusterListener list = new ChannelListener();
		getClusterConnection().addClusterListener(list);
	}
	
	private void broadcastConfig() {
		broadcastConfig(null);
	}
	
	private void broadcastConfig(SocketAddress target) {
		Config con = new Config(Config.Type.DELTA_INIT);
		con.setAttachment(cloneServerConfigProperties());
		con.setSource(id);
		try {
			if(target == null) {
				getClusterDispatcher().dispatch(con);
			} else {
				getClusterDispatcher().dispatch(con, target);
			}
		} catch (ClusterException e) {
			log.fatal("Failed to broadcast initial cluster config", e);
		}
	}
	
	private void waitBroadcastConfig(final SocketAddress target) {
		scheduleRunnableSingleExecution(new SafeRunnable() {
			public void innerRun() {
				broadcastConfig(target);
			}
		}, config.getConfigCommandDelay());
	}
	
	private ConfigProperty[] cloneServerConfigProperties() {
		ServerConfigProviderContract contr = context.getServices().getServiceInstance(ServerConfigProviderContract.class);
		return contr.getAllProperties();
	}
	
	private void waitSendResume(final long haltId) {
		scheduleRunnableSingleExecution(new SafeRunnable() {
			public void innerRun() {
				sendResume(haltId);
			}
		}, config.getResumeCommandDelay());
	}
	
	private void waitSendPromotion(final ClusterParticipant node) {
		//scheduleRunnableSingleExecution(new Runnable() {
		//	public void run() {
				sendPromotion(node);
		//	}
		//}, RESUME_COMMAND_DELAY);
	}

	private void sendResume(long haltId) {
		if(!isPrimary()) return; // SANITY CHECK
		log.debug("Sending resume for id: " + haltId);
		Resume r = new Resume(new ResumeMessage(haltId, getClusterLayout()));
		r.setSource(this.id);
		try {
			getClusterDispatcher().dispatch(r);
		} catch (ClusterException e) {
			log.fatal("Failed to send resume command to cluster", e);
		}
	}

	private void setupThreadPools() {
		int size = config.getSchedulerPoolInitSize();
		sysThreads = new JmxScheduler(size, "MasterNode");
	}
	
	private void destroyThreadPools() {
		if(sysThreads != null) {
			sysThreads.shutdownNow();
		}
	}
	
	private void scheduleRunnableSingleExecution(Runnable run, long delay) {
		if(sysThreads == null) return; // SANITY CHECK
		else {
			sysThreads.schedule(run, delay, TimeUnit.MILLISECONDS);
		}
	}
	
	private void redistributeForNodeUp(ClusterParticipant node) {
		EventType type = null;
		ClusterRole role = node.getRole();
		if(role.equals(ClusterRole.GAME_NODE)) type = EventType.GAME;
		else if(role.equals(ClusterRole.CLIENT_NODE)) type = EventType.CLIENT;
		else type = EventType.MTT;
		try {
			log.debug("Redistributing for added node: " + node);
			getMBus().getRedistributor().addPartition(type, node.getId(), node.getMBusDetails().getSocketIdFor(type), node.getServerId().id);
			//PartitionMap map = getMBus().getCurrentPartitionMap();
			//redistributeData(type, map);
		} catch (MBusException e) {
			String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
	
	/*private void redistributeData(EventType type, PartitionMap map) {
		List<Partition> parts = map.getAllPartitions(type);
		Map<String, List<Integer>> groups = new HashMap<String, List<Integer>>();
		for (Partition p : parts) {
			String serverId = findServerId(p);
			if(serverId == null) {
				log.warn("Found no server id for partition: " + p);
				continue;
			}
			List<Integer> list = getPartitionIds(p, map);
			if(groups.containsKey(serverId)) {
				groups.get(serverId).addAll(list);
			} else {
				groups.put(serverId, list);
			}
		}
		StateGroup[] arr = toGroupArr(groups);
		getServer().partition(type, arr);
	}
	
	private StateGroup[] toGroupArr(Map<String, List<Integer>> groups) {
		int i = 0;
		StateGroup[] arr = new StateGroup[groups.size()];
		for (String id : groups.keySet()) {
			List<Integer> list = groups.get(id);
			int[] ids = toIntArray(list);
			arr[i++] = new StateGroup(id, ids);
		}
		return arr;
	}

	private List<Integer> getPartitionIds(Partition p, PartitionMap map) {
		Channel[] channels = map.getChannelsForPartition(p);
		List<Integer> list = new ArrayList<Integer>(channels.length);
		for (Channel ch : channels) {
			list.add(ch.getId());
		}
		return list;
	}

	private Server getServer() {
		return context.getServices().getServiceInstance(Server.class);
	}*/

	private void redistributeForNodeDown(ClusterParticipant node) {
		try {
			log.debug("Redistributing for dropped node: " + node);
			getMBus().getRedistributor().dropPartition(node.getId());
			/*EventType type = getTypeFor(node.getRole());
			if(type != null) {
				PartitionMap map = getMBus().getCurrentPartitionMap();
				redistributeData(type, map);
			}*/
		} catch (MBusException e) {
			String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
	
	private void objectRemoved(EventType type, int id) {
		layoutLock.lock();
		try {
			Partition p = getMBus().getCurrentPartitionMap().getPartitionForChannel(type, id);
			if(p == null) log.warn("Received removal event for non-existing partition; id: " + id);
			else {
				// p.removeTable(id);
				/*if(type == EventType.GAME) {
					removeTableData(p, new int[] { id });
				} else {
					// removeMttData(p, new int[] { id });
				}*/
				dropChannel(id, p);
			}
		} catch (MBusException e) {
			String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		} finally {
			layoutLock.unlock();
		}
	}
	
	private void setupRequiredPartitions() throws MBusException {
		if(isPrimary()) {
			getMBus().getRedistributor().addPartition(EventType.CHAT, Constants.CHAT_TOPIC_NAME, null, null);
			getMBus().getRedistributor().addPartition(EventType.CLIENT, Constants.GAME_TOPIC_NAME, null, null);
		}	
	}
	
	public void tableRemoved(int[] tableIds, int mttId) {
		for (int id : tableIds) {
			objectRemoved(EventType.GAME, id);
		}
	}

	public void tableAdded(TableCommandData[] attachment, int mttId) {
		int[] ids = toIdArray(attachment);
		//Map<Integer, Object> data = toDataMap(attachment);
		objectsAdded(EventType.GAME, ids); //, data);
	}

	public void mttAdded(int mttId) {
		objectAdded(EventType.MTT, mttId);
	}
	
	public void mttRemoved(int mttId) {
		objectRemoved(EventType.MTT, mttId);
	}
	
	private int[] toIdArray(TableCommandData[] tables) {
		int[] arr = new int[tables.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = tables[i].getId();
		}
		return arr;
	}

	private void dropChannel(final int id, final Partition p) throws MBusException {
		if(isPrimary()) {
			/*scheduleRunnableSingleExecution(new Runnable() {
				public void run() {
					try {*/
						getMBus().getRedistributor().dropChannel(p.getId(), id);
					/*} catch (MBusException e) {
						String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
						SystemLogger.error(msg);
						log.fatal(msg, e);
					}
				}
			}, REDISTRIBUTION_DELAY);*/
		}
	}
	
	private void addChannels(final int[] ids, final Partition p) throws MBusException {
		if(isPrimary()) {
			/*scheduleRunnableSingleExecution(new Runnable() {
				public void run() {
					try {*/
						getMBus().getRedistributor().addChannels(p.getId(), ids);
					/*} catch (MBusException e) {
						String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
						SystemLogger.error(msg);
						log.fatal(msg, e);
					}
				}
			}, REDISTRIBUTION_DELAY);*/
		}
	}

	private void objectAdded(EventType type, int id) {
		layoutLock.lock();
		try {
			Partition p = findSmallestPartition(type);
			if(p == null) log.warn("Received addition event without any pre-existing partitions; id: " + id);
			else {
				// p.addTable(id);
				// tableMapping.put(id, p);
				addChannels(new int[] { id }, p);
			}
		} catch (MBusException e) {
			String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		} finally {
			layoutLock.unlock();
		}
	}
	
	private void objectsAdded(EventType type, int[] ids) { //, Map<Integer, Object> data) {
		layoutLock.lock();
		try {
			Map<Partition, List<Integer>> redist = calculateNewDistribution(type, ids);
			if(redist == null) {
				log.warn("Received addition event without any pre-existing partitions; Multiple ids");
				return;
			}
			for (Partition p : redist.keySet()) {
				List<Integer> list = redist.get(p);
				int[] newIds = toIntArray(list);
				/*if(type == EventType.GAME) {
					addTableData(p, newIds, data);
				} else {
					// addMttData(p, newIds, data);
				}*/
				addChannels(newIds, p);
			}
		} catch (MBusException e) {
			String msg = "MBus failed to redistribute cluster; Received message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		} finally {
			layoutLock.unlock();
		}
	}
	
	/*private void removeTableData(Partition p, int[] ids) {
		String serverId = findServerId(p);
		if(serverId == null) {
			log.warn("Found no server id for partition: " + p);
			return;
		}
		//Map<String, String> meta = Collections.singletonMap(Constants.SERVER_ID_KEY, serverId);
		//GameObjectSpace<FirebaseTable, GameAction> space = getTableSpace();
		getServer().remove(serverId, p.getType(), ids);
	}
	
	private void addTableData(Partition p, int[] newIds, Map<Integer, Object> data) {
		String serverId = findServerId(p);
		if(serverId == null) {
			log.warn("Found no server id for partition: " + p);
			return;
		}
		//Map<String, String> meta = Collections.singletonMap(Constants.SERVER_ID_KEY, serverId);
		//GameObjectSpace<FirebaseTable, GameAction> space = getTableSpace();
		getServer().add(serverId, p.getType(), toEntries(newIds, data));
	}

	private Entry[] toEntries(int[] newIds, Map<Integer, Object> data) {
		int i = 0;
		Entry[] arr = new Entry[newIds.length];
		for (int id : newIds) {
			arr[i++] = new Entry(id, data.get(id));
		}
		return arr;
	}*/
	
	/*private GameObjectSpace<FirebaseTable, GameAction> getTableSpace() {
		TableSpaceServiceContract con = context.getServices().getServiceInstance(TableSpaceServiceContract.class);
		return con.getObjectSpace(FirebaseTable.class, GameAction.class);
	}*/

	/*private String findServerId(Partition p) {
		return nodeRegistry.findServerId(p);
	}

	private Map<Integer, Object> toDataMap(TableCommandData[] attachment) {
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		for (TableCommandData d : attachment) {
			map.put(d.getId(), d.getData());
		}
		return map;
	}*/

	private int[] toIntArray(List<Integer> list) {
		int[] arr = new int[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i).intValue();
		}
		return arr;
	}

	private Map<Partition, List<Integer>> calculateNewDistribution(EventType type, int[] ids) {
		Map<Partition, Integer> sizes = getPartitionsSizes(type);
		if(sizes.size() > 0) {
			return MasterNodeUtil.calculateNewDistribution(sizes, ids);
		} else {
			return null;
		}
	}

	private Map<Partition, Integer> getPartitionsSizes(EventType type) {
		Map<Partition, Integer> map = new HashMap<Partition, Integer>();
		PartitionMap pmap = getMBus().getCurrentPartitionMap();
		for (Partition p : pmap.getAllPartitions()) {
			if(p.getType().equals(type)) {
				map.put(p, pmap.countChannelsForPartition(p));
			}
		}
		return map;
	}

	// layout lock must be held
	private Partition findSmallestPartition(EventType type) {
		Partition small = null;
		int smallest = -1;
		PartitionMap pmap = getMBus().getCurrentPartitionMap();
		for (Partition p : pmap.getAllPartitions()) {
			if(p.getType().equals(type)) {
				int count = pmap.countChannelsForPartition(p);
				if(small == null || smallest > count) {
					smallest = count;
					small = p;
				}
			}
		}
		return small;
	}
	
	private void performNodeDown(List<ClusterParticipant> nodes) {
		for (ClusterParticipant p : nodes) {
			performNodeDown(p);
		}
	}
	
	private void performNodeDown(ClusterParticipant p) {
		checRedistForNodeDown(p);
		// if the old node was a coordinator, send out new promotion
		if(p != null && p.isCoordinator()) {
			ClusterParticipant coord = nodeRegistry.getCoordinator(p.getRole());
			if(coord != null) {
				log.info("Lost member was coordinator for role " + p.getRole() + "; Coordinator status passed to node at address: " + coord.getNodeId());
			} else {
				SystemLogger.warning("Cluster lost member " + p.getNodeId() + "' which was coordinator for role " + p.getRole() + "; Coordinator lost! Cluster is now in an incomplete state!");
				log.warn("Lost member was coordinator for role " + p.getRole() + "; Coordinator lost, cluster is in incomplete state!");
			}
			sendPromotion(coord);
		}
	}

	private void checRedistForNodeDown(ClusterParticipant p) {
		if(doPartitionChangesFor(p.getRole())) {
			redistributeForNodeDown(p);
		}
	}
	
	/*private void waitPostHalt() {
		try {
			Thread.sleep(config.getResumeCommandDelay());
		} catch(InterruptedException e) { }
	}*/
	
	// return true if I'm the next coordinator
	private boolean isFirstSecondary() {
		return commProxy.isPrimaryMaster(nodeRegistry.getAllNodes());
	}

	// return true if any of the participants is the primary master
	private boolean checkPrimaryMaster(List<ClusterParticipant> nodes) {
		for (ClusterParticipant p : nodes) {
			if(p.isCoordinator() && p.getRole().equals(ClusterRole.MASTER_NODE)) {
				return true; // EARLY RETURN
			}
		}
		return false;
	}
	
	// return true if the participant is the primary master
	private boolean checkPrimaryMaster(ClusterParticipant node) {
		return (node != null && node.isCoordinator() && node.getRole().equals(ClusterRole.MASTER_NODE));
	}
	
	// print warning of we've got duplicate ids
	private void checkWarnDuplicateId(ClusterParticipant emerged) {
		for (ClusterParticipant p : nodeRegistry.getAllNodes()) {
			if(p.getId().equals(emerged.getId())) {
				log.warn("Duplicate ID '" + p.getId() + " found for nodes " + p.getNodeId() + " and " + emerged.getNodeId());
				SystemLogger.warning("Duplicate ID '" + p.getId() + " found for nodes " + p.getNodeId() + " and " + emerged.getNodeId());
			}
		}
	}

	
	// --- HANDSHAKE / LEAVE --- //

	public boolean handshakeReceived(ClusterParticipant node) {
		if(!isInit.get()) {
			log.debug("Recieved handshake before init was completed");
			return false;
		} else {
			layoutLock.lock();
			try {
				checkWarnDuplicateId(node);
				nodeRegistry.registerNodeUp(node);
				if(isPrimary()) {	
					/* 
					 * 1) Halt (verify ?)
					 * 3) Re-calculate cluster layout
					 * 4) Close and re-populate queues
					 * 5) Send resume command
					 */
					if(isRelevantNode(node)) {
						// all nodes except server nodes
						log.info("Received handshake for role " + node.getRole() + " from id: " + node.getNodeId() + "; Halting");
						int id = node.getNodeId().hashCode();
						// send halt
						sendHalt(id);
						// re-do layout
						boolean change = doPartitionChangesFor(node.getRole());
						if(change) {
							redistributeForNodeUp(node);
						}
						// if this is a new coordinator, send promotion
						if(nodeRegistry.isCoordinator(node)) {
							waitSendPromotion(node);
						}
						// log message and resume (after wait)
						SystemLogger.info("Master node processed handshake for role " + node.getRole() + " from id: " + node.getNodeId());
						log.info("Resuming after processing role " + node.getRole() + " from: " + node.getNodeId() + "; Halt id: " + id);
						waitSendResume(id);
					} else {
						log.info("Received handshake for role " + node.getRole() + " from id: " + node.getNodeId());
					}
					if(ClusterRole.MASTER_NODE.equals(node.getRole())) {
						waitBroadcastConfig(node.getServerId().address);
					}
				}
			} finally {
				layoutLock.unlock();
			}
			return true;
		}
	}

	public boolean leaveReceived(ClusterParticipant node) {
		if(!isInit.get()) {
			log.debug("Recieved leave outside init state");
			return false;
		} else {
			layoutLock.lock();
			try {
				boolean isNextPrimary = checkPrimaryMaster(node) && isFirstSecondary();
				// boolean change = doPartitionChangesFor(node.getRole());
				// String roleString = (p == null ? "n/a" : p.getRole().toString());
				log.info("Master node received node down for node: " + node.getId() + " (coordinator: " + node.isCoordinator() + "); This master is primary: " + isPrimary() + "; This master is next primary: " + isFirstSecondary());	
				/*
				 * Check if 1) the node down was the primary, and 2)
				 * if I'm first in line for primacy
				 */
				log.info("Before node down: " + nodeRegistry);
				/*
				 * Clone the node to keep coordinator status
				 */
				ClusterParticipant clone = new ClusterParticipant(node);
				clone.setIsCoordinator(nodeRegistry.isCoordinator(node));
				/*
				 * Change the register (this will mutate the "node" object).
				 */
				nodeRegistry.registerNodeDown(node.getNodeId());		
				if(isPrimary() || isNextPrimary) {
					int id = newHaltResumeId(clone);
					sendHalt(id);
					
					// HACK TEST
					// waitPostHalt();
					
					isSecondary.set(false);
					performNodeDown(clone);
					/// resume
					if(isNextPrimary) {
						SystemLogger.warning("Master node received node down from primary master '" + clone.getId() + "; This node is now the primary master!");
					} /*else {
						SystemLogger.warning("Master node received node down for node: " + node.getId());
					}*/
					SystemLogger.info("Master node processed node down for: " + clone.getId());
					log.info("Resuming after processing node down for: " + clone.getId() + "; Halt id: " + id);
					waitSendResume(id);
				} 
				log.debug("After node down: " + nodeRegistry);
			} finally {
				layoutLock.unlock();
			}
			return true;
		}
	}
	
	
	// --- INNER CLASSES --- //
	
	/*@CacheListener
	public class StateListener {
		
		//FIXME: This is a hack class, figure out a way to do this correctly!!!!

		@SuppressWarnings("unchecked")
        private final Map<Fqn, Integer> tableNodes = new ConcurrentHashMap<Fqn, Integer>();
		
		@SuppressWarnings("unchecked")
        private final Map<Fqn, Integer> mttNodes = new ConcurrentHashMap<Fqn, Integer>();
		
		@NodeModified
		public void nodeModified(NodeModifiedEvent event) {
			if(!event.isPre() && event.getModificationType() != ModificationType.REMOVE_DATA) {
				Integer id = (Integer)event.getData().get(DefaultTableAttributes._ID.toString());
				if(id != null && !tables.contains(id) && isTableTypeFqnRoot(event)) {
					tableNodes.put(event.getFqn(), id);
					objectAdded(EventType.GAME, id);
					tables.add(id);
				} else if(id != null && !mtts.contains(id) && event.getFqn().toString().startsWith(SystemStateConstants.TOURNAMENT_ROOT_FQN)) {
					mttNodes.put(event.getFqn(), id);
					objectAdded(EventType.MTT, id);
					mtts.add(id);
				}
			}
		}

		private boolean isTableTypeFqnRoot(NodeEvent event) {
			return event.getFqn().toString().startsWith(SystemStateConstants.TABLE_ROOT_FQN) || event.getFqn().toString().startsWith(SystemStateConstants.TOURNAMENT_TABLE_ROOT_FQN);
		}

		@NodeRemoved
		public void nodeRemoved(NodeRemovedEvent event) { 
			Integer i = tableNodes.remove(event.getFqn());
			if(i != null) {
				objectRemoved(EventType.GAME, i);
				tables.remove(i);
			} else {
				i = mttNodes.remove(event.getFqn());
				if(i != null) {
					objectRemoved(EventType.MTT, i);
					mtts.remove(i);
				}
			}
		}
	}*/
	
	private class ChannelListener implements ClusterListener {
		
		public void memberUp(SocketAddress node) { 
			// Wait for handshake...
		}
	
		public void memberDown(final SocketAddress member) {
			scheduleRunnableSingleExecution(new SafeRunnable() {
			
				public void innerRun() {
					doMemberDown(member);
				}
			}, 0);
		}
		
		private void doMemberDown(SocketAddress node) { 
			if(!isInit.get()) {
				log.warn("Received node down event before initiation was finished");
			} else {
				layoutLock.lock();
				try {
					/*
					 * 1) Send halt
					 * 2) Optionally: Wait for node restart
					 * 3) Re-calculate cluster layout
					 * 4) Close and re-populate queues
					 * 5) Send resume command
					 */
					List<ClusterParticipant> nodes = nodeRegistry.getNodesForServer(node, true);
					boolean isNextPrimary = checkPrimaryMaster(nodes) && isFirstSecondary();
					// boolean change = doPartitionChangesFor(nodes);
					// String roleString = (p == null ? "n/a" : p.getRole().toString());
					if(nodes.size() == 0) {
						log.info("Master node received server down event from address: " + node.toInetSocketAddress() + "; No cluster nodes included in downed server, ignoring.");
						if(isPrimary() || isNextPrimary) {
							sendResume(newHaltResumeId(node));
						}
					} else {
						log.info("Master node received member down event from address: " + node.toInetSocketAddress() + "; This master is primary: " + isPrimary() + "; This master is next primary: " + isFirstSecondary());	
					
						/*
						 * Check if 1) the node down was the primary, and 2)
						 * if I'm first in line for primacy
						 */
						log.info("Before server down: " + nodeRegistry);
						nodeRegistry.registerServerDown(node);		
						if(isPrimary() || isNextPrimary) {
							int id = newHaltResumeId(node);
							sendHalt(id);
							
							// HACK TEST
							// waitPostHalt();
							
							isSecondary.set(false);
							performNodeDown(nodes);
							/// resume
							if(isNextPrimary) {
								SystemLogger.warning("Master node received member down from primary master at address '" + node.toInetSocketAddress() + "; This node is now the primary master!");
							} /*else {
								SystemLogger.warning("Master node received member down event from address: " + node.toInetSocketAddress());
							}*/
							SystemLogger.info("Master node processed member down from address: " + node.toInetSocketAddress());
							log.info("Resuming after processing server down from address: " + node.toInetSocketAddress() + "; Halt id: " + id);
							
							waitSendResume(id);
						} 
						log.debug("After server down: " + nodeRegistry);
					}
				} finally {
					layoutLock.unlock();
				}
			}
		}
	}
}
