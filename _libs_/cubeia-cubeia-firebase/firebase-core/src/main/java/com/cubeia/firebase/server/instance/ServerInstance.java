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
package com.cubeia.firebase.server.instance;

import static com.cubeia.firebase.api.server.ServerStatus.DESTROYING;
import static com.cubeia.firebase.api.server.ServerStatus.INITIALIZING;
import static com.cubeia.firebase.api.server.ServerStatus.STARTED;
import static com.cubeia.firebase.api.server.ServerStatus.STARTING;
import static com.cubeia.firebase.api.server.ServerStatus.STOPPING;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.server.NodeType;
import com.cubeia.firebase.api.server.ServerStatus;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.syslog.SystemLog;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.Halt;
import com.cubeia.firebase.server.commands.Resume;
import com.cubeia.firebase.server.deployment.DeploymentClassLoader;
import com.cubeia.firebase.server.deployment.ResourceManager;
import com.cubeia.firebase.server.game.GameNode;
import com.cubeia.firebase.server.gateway.GatewayNode;
import com.cubeia.firebase.server.jmx.LocalServer;
import com.cubeia.firebase.server.jmx.SystemMonitor;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.server.manager.ManagerNode;
import com.cubeia.firebase.server.master.MasterNode;
import com.cubeia.firebase.server.master.ServerId;
import com.cubeia.firebase.server.mtt.MttNode;
import com.cubeia.firebase.server.node.Node;
import com.cubeia.firebase.server.node.NodeContext;
import com.cubeia.firebase.server.service.DefaultServiceRegistry;
import com.cubeia.firebase.server.syslog.SystemLogImpl;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.util.threads.SafeRunnable;
import com.game.server.bootstrap.BootstrapNode;
import com.game.server.bootstrap.InitFolders;
import com.game.server.bootstrap.SharedClassLoader;
 
/**
 * This is the actual server platform implementation. It handles the 
 * server services and the node lifecycles. All nodes may throw runtime 
 * exceptions during {@link Startable#start()} to signal late errors, in 
 * which case the server will attempt to abort.
 * 
 * @author lars.j.nilsson
 */
 
/*
 * You can only ever have *one* master per server. Cannot add
 * master node when already running.
 */
public final class ServerInstance implements Startable, NodeListenerHandler {
	
	private static final int MAIN_THREAD_SLEEP = 200;

	
	/// --- INSTANCE MEMBERS --- ///
	
	private File configDir, workDir, gameDir;
	private InternalServiceRegistry reg;
	private SharedClassLoader sharedLoader;
	
    private File[] trustedSarLocations = new File[0];
	
	// private boolean isSingleton = false;
	private boolean haveMaster = false;
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	
	private final Map<String, NodeListener> nodeListeners = new ConcurrentHashMap<String, NodeListener>();
	
	// private int serverNodePort = -1;
	private final Listener listener = new Listener();
	
	private final SystemLog sysLog;
	private final List<NodeHandle> liveNodes;
	private final Logger log;

	private InitFolders folders;
	private boolean isSingleton;
	private DeploymentClassLoader depLoader;
	private String id;
	
	private AtomicReference<ServerStatus> status = new AtomicReference<ServerStatus>(INITIALIZING);

	private ResourceManager resources;

	// private final Object configLock = new Object();;
 
	/**
	 * Create a new server instance. This also instantiates the
	 * system logger, and sets up internal collections.
	 */
	public ServerInstance() {
		// init system log
		sysLog = new SystemLogImpl();
		SystemLogger.setInstance(sysLog);
		// live node list
		liveNodes = Collections.synchronizedList(new LinkedList<NodeHandle>());
		// install java util log bridge
		SLF4JBridgeHandler.install();
		// get log4j logger
		log = Logger.getLogger(getClass());
		// register JNDI 
		System.setProperty("java.naming.factory.initial", "com.cubeia.firebase.api.jndi.FirebaseContextFactory");
		System.setProperty("java.naming.factory.url.pkgs", "com.cubeia.firebase.api.jndi");
	} 
	
	
	// --- PUBLIC METHODS --- //
	
	/**
	 * Start the server. This method propagates to all nodes. Prior to this method 
	 * {@link #init(File, SharedClassLoader, BootstrapNode[])} must have 
	 * been successfully called.
	 */
    public synchronized void start() {
    	if(isRunning.get()) return; // SANITY CHECK
    	else {
    		status.set(STARTING);
    		isRunning.set(true);
    		startThread();
    		if(startNodes()) {
    			status.set(STARTED);
	    		startStatisticsPrinter();
	    		sysinfo("System [" + Version.VM_NAME + " " + Version.VM_VERSION + "; " + Version.OS_NAME + " " + Version.OS_VERSION + " (" + Version.OS_ARCH + ")]");
	    		sysinfo("Server started [" + new Date() + "]");
	    		sysinfo("Firebase Server Platform [version: " + Version.FULL_VERSION + "]");
    		} else {
    			syserror("Unexpected Error! Aborting startup sequence. Attempting to stop server.");
    			doStop(false);
    			doDestroy(false);
    			/*
    			 * At this point, there will be stupidly implemented modules
    			 * like the mina server left which thinks that the word "gracefull"
    			 * shutdown has something to do with the pub. Idiots. /LJN
    			 */
    			sysexit(1, 1000);
    		}
    	}
    }


	/**
     * @return An array of "live" nodes, never null 
     */
	public synchronized Node<?>[] getLiveNodes() {
    	int c = 0;
    	Node<?>[] arr = new Node<?>[liveNodes.size()];
    	for (NodeHandle h : liveNodes) {
			arr[c++] = h.node;
		}
    	return arr;
    }
    
	
	/**
	 * @return True if the server is started, false otherwise
	 */
    public boolean isRunning() {
		return isRunning.get();
	}
    
    
    /**
     * Stop the server. This call will propagate to all nodes
     * but does not stop the service registry.
     */
	public synchronized void stop() {
    	if(!isRunning.get()) return; // SANITY CHECK
    	else {
    		doStop(true);
    	}
    }

	
	/**
	 * Initiate the server instance. This method checks preconditions, initiates
	 * and starts the service registry and initiates, but does not start, all requested nodes.
	 * 
	 * @param id Server id, must not be null
     * @param folders Init folders for the server, must not be null
     * @param loader Shared class loader to use as immediate parent loader
     * @param nodes nodes to start, if not null node type one of "game", "manager", "master", "client" or "singleton"
	 * @throws SystemCoreException 
     */
    public synchronized void init(String id, InitFolders folders, SharedClassLoader loader, BootstrapNode[] nodes) throws SystemException {
    	Arguments.notNull(folders, "folders");
    	Arguments.notNull(loader, "shared class loader");
    	Arguments.notNull(id, "id");
    	// Arguments.notNull(nodes, "bootstrap nodes");
    	
    	//((NamedClassLoader)getClass().getClassLoader()).__tc_setClassLoaderName("firebase cl");
    	//ClassProcessorHelper.registerGlobalLoader((NamedClassLoader)getClass().getClassLoader());
    	
    	sysinfo("Firebase Server Platform initializing; Server id: " + id);
    	status.set(INITIALIZING);
    	initJmx();
    	this.id = id;
    	this.folders = folders;
		this.sharedLoader = loader;
    	checkInitDirs(folders);
    	setupTrustedSarLocations();
    	createDeploymentClassLoader();
    	createDeploymentResources();
    	checkSingletonStatus(nodes);
    	initServices();
    	startListening();
    	initNodes(nodes);
    }


	/**
     * Destroy the server which should have been {@link #stop() stopped} before
     * this method is called. This destroys the nodes and stops and destroys the
     * service registry.
     */
	public synchronized void destroy() {
		doDestroy(true);
	}
	
	
	/**
	 * This method will attempt to remove a node from the server. If the
	 * node is started it will first be stopped.
	 *  
	 * @param id Old node id, must not be null
	 */
	
	public synchronized void removeNode(String id) {
		Arguments.notNull(id, "id");
		NodeHandle node = findNode(id, true);
		if(node != null) {
			if(node.node instanceof MasterNode) {
				haveMaster = false;
			}
			node.node.stop();
			node.node.destroy();
			((Initializable<?>)node.context).destroy();
		}
	}
	

	/**
     * This method can be used to add a node to the server. If called before the server
     * is started the node will not be started until the server is. If called when 
     * the server is already running the node will be automatically initiated and started.
     * 
     * <p><b>NB:</b> There can only be one master node per server. Also if the server is already
     * started and have nodes running you cannot add a master node.
     * 
     * @param id New node id, must not be null
     * @param nodeType Node type to start immediately, must be null or one of "game", "master", "client", "mtt"
	 * @throws SystemException If the node type cannot be legally added
     */
    public synchronized void addNode(String id, ClusterRole node) throws SystemException {
    	Arguments.notNull(id, "id");
    	Node<?> add = null;
    	if(node == ClusterRole.CLIENT_NODE) add = new GatewayNode(id);
    	if(node == ClusterRole.GAME_NODE) add = new GameNode(id);
    	if(node == ClusterRole.MANAGER_NODE) add = new ManagerNode(id);
    	if(node == ClusterRole.MTT_NODE) add = new MttNode(id);
    	if(node == ClusterRole.MASTER_NODE) {
    		// FIXME: Fix error messages ...
    		if(haveMaster) throw new SystemCoreException("can only have one master node");
    		if(isRunning.get() && liveNodes.size() > 0) throw new SystemCoreException("can not add master when already started with running nodes");
    		add = new MasterNode(id);
    		haveMaster = true;
    	}
    	NodeHandle handle = new NodeHandle(add);
    	liveNodes.add(handle);
    	if(isRunning.get()) {
    		initNode(handle, true);
    		add.start();
    	}
    }
    
    public String getServerStatus() {
		return String.valueOf(status.get());
	}
    
    /**
     * This method should be called before the server is initiated in order to
     * add a location from which to read "trusted" service archives.
     * 
     * @param dir Directory to add, must not be null
     */
	public void addTrustedSarLocation(File dir) {
		Arguments.notNull(dir, "dir");
		File[] next = new File[trustedSarLocations.length + 1];
		System.arraycopy(trustedSarLocations, 0, next, 0, trustedSarLocations.length);
		next[next.length - 1] = dir;
		trustedSarLocations = next;
	}

	
	// --- NODE LISTENER HANDLER --- //
	
	public void addNodeListener(String id, NodeListener listener) {
		nodeListeners.put(id, listener);
	}
	
	public void removeNodeListener(String id) {
		nodeListeners.remove(id);
	}
	
	
    
    // --- PACKAGE METHDOS --- //

	/**
	 * @return The active root service registry, null before initiated
	 */
	ServiceRegistry getServiceRegistry() {
		return reg;
	}
	
	
	/**
	 * @return The configured game deployment directory, never null
	 */
	File getGameDirectory() {
		return gameDir;
	}
	
	/**
	 * @return The configured server library directory, never null
	 */
	File getLibDirectory() {
		return this.folders.lib;
	}
	
	String getServerStringId() {
		return id;
	}
	
	/**
	 * @return The server id, or null before initiated
	 */
	ServerId getServerId() {
		if(reg == null) return null;
		else {
			ConnectionServiceContract service = reg.getServiceInstance(ConnectionServiceContract.class);
			if(service != null) {
				SocketAddress address = service.getSharedConnection().getLocalAddress();
				return new ServerId(id, address);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * @return The current mbean server, never null
	 */
	MBeanServer getMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}
	
	
	/*
	 * Calculate the cluster bind port base from a component namespace and id.
	 * 
	 * @param ns Node namespace, must not be null
	 * @param id Node is, must not be null
	 * @return The cluster bind port base, or -1
	 */
	/*int getClusterBindPortBase(Namespace ns, String id) {
		Arguments.notNull(ns, "ns");
		Arguments.notNull(id, "id");
		try {
			Namespace ns1 = new Namespace(ns.toString() + "." + id);
			ServerConfig c = getServerConfiguration(ns1);
			return c.getClusterBindPortBase();
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException("failed to setup comm port base", e);
		}
	}*/
	
	
	/**
	 * This method should be used with care as it will attempt to shutdown (stop/destroy)
	 * the server. It can be used for remote (JMX and cluster command) control.
	 * 
	 * @param msg Shutdown message, may, but should not, be null
	 * @param emergency True for unplanned fatal errors, false for recevied commands
	 */
	void shutdown(String msg, boolean emergency) {
		msg = "Shutdown received from node contexts: " + (msg == null ? "<no message>" : msg);
		if(!emergency) log.warn(msg);
		else log.fatal(msg);
		destroy();
	}
	
	
	/**
	 * @param ns Namespace to root the configuration in, may be null
	 * @return The server config, root at given namespace, null before service init
	 */
	ServerConfig getServerConfiguration(Namespace ns) { 
		ServerConfigProviderContract contr = reg.getServiceInstance(ServerConfigProviderContract.class);
		if(contr != null) return contr.getConfiguration(ServerConfig.class, ns);
		else return null;
	}
	
	
	/**
	 * @return The current config directory, null before init
	 */
	File getConfigDirectory() {
		return configDir;
	}
	
	
	/**
	 * @return The configured loig directory, null before init
	 */
	File getLogDirectory() {
		Namespace ns1 = new Namespace("server");
		ServerConfig c = getServerConfiguration(ns1);
		if(c == null) {
			return null;
		} else {
			return c.getLogDirectory();
		}
	}
	

    /**
     * @return The current work directory, null before init
     */
	File getWorkDirectory() {
		return workDir;
	}

	
	/**
	 * @return The shared class loader, null before init
	 */
	SharedClassLoader getSharedClassLoader() {
		return sharedLoader;
	}


	/**
	 * @return All "trusted" sar locations, never null
	 */
	File[] getTrustedSarLocations() {
		return trustedSarLocations;
	}
	
	
	/**
	 * @return True if the service reside on a singleton server, false otherwise
	 */
	boolean isSingleton() {
		return isSingleton;
	}
	
	
	/**
	 * @return The deployment class loader
	 */
	ClassLoader getDeploymentClassLoader() {
		return depLoader;
	}

	
	
    /// --- PRIVATE METHODS --- ///
	
	private void createDeploymentResources() throws SystemException {
		File file = getGameDirectory();
		resources = new ResourceManager(file, depLoader);
		InternalComponentAccess.setDeploymentResources(resources);
		resources.start();
	}
	
	private void notifyNodeInit(NodeHandle handle, boolean isPre) {
		NodeType type = getType(handle);
		String id = handle.node.getId();
		NodeInfo info = new NodeInfo(type, id);
		for (NodeListener l : nodeListeners.values()) {
			l.nodeInit(info, isPre);
		}
		if(!isPre) {
			handle.isInit = true;
		}
	}
	
	private NodeType getType(NodeHandle handle) {
		if(handle.node instanceof GameNode) {
			return NodeType.GAME;
		} else if(handle.node instanceof MttNode) {
			return NodeType.MTT;
		} else if(handle.node instanceof GatewayNode) {
			return NodeType.CLIENT;
		} else if(handle.node instanceof MasterNode) {
			return NodeType.MASTER;
		} else if(handle.node instanceof ManagerNode) {
			return NodeType.MANAGER;
		} else {
			return null;
		}
	}

	private void notifyNodeStart(NodeHandle handle, boolean isPre) {
		NodeType type = getType(handle);
		String id = handle.node.getId();
		NodeInfo info = new NodeInfo(type, id);
		for (NodeListener l : nodeListeners.values()) {
			l.nodeStart(info, isPre);
		}
	}
	
	private void notifyNodeStop(NodeHandle handle, boolean isPre) {
		NodeType type = getType(handle);
		String id = handle.node.getId();
		NodeInfo info = new NodeInfo(type, id);
		for (NodeListener l : nodeListeners.values()) {
			l.nodeStop(info, isPre);
		}
	}
	
	private void notifyNodeDestroy(NodeHandle handle, boolean isPre) {
		NodeType type = getType(handle);
		String id = handle.node.getId();
		NodeInfo info = new NodeInfo(type, id);
		for (NodeListener l : nodeListeners.values()) {
			l.nodeDestroy(info, isPre);
		}
		if(!isPre) {
			handle.isInit = false;
		}
	}
	
	
	/*
	 * This method when aborting startup.
	 */
	private void sysexit(final int code, final long delay) {
		Thread th = new Thread(new Runnable() {
		
			public void run() {
				try {
					Thread.sleep(delay);
					System.exit(code);
				} catch (InterruptedException e) { }
			}
		});
		th.setName("System Exit Call Thread");
		th.setDaemon(true);
		th.start();
	}
	
	private void doDestroy(boolean isNormal) {
		if(isRunning.get()) {
			stop();
		}
		status.set(DESTROYING);
		destroyNodes(isNormal);
		stopListening();
		stopAndDestroyRegistry();
		resources.stop();
		liveNodes.clear();
	}
	
	private void startListening() {
		ConnectionServiceContract conn = reg.getServiceInstance(ConnectionServiceContract.class);
		conn.getSharedConnection().getCommandReceiver().addCommandListener(Constants.NODE_LIFETIME_COMMAND_CHANNEL, listener);
		conn.getSharedConnection().getCommandReceiver().addCommandListener(listener);
	}
	
	public void stopListening()  {
		ConnectionServiceContract conn = reg.getServiceInstance(ConnectionServiceContract.class);
		if(conn != null && conn.getSharedConnection() != null) {
			conn.getSharedConnection().getCommandReceiver().removeCommandListener(Constants.NODE_LIFETIME_COMMAND_CHANNEL, listener);
			conn.getSharedConnection().getCommandReceiver().removeCommandListener(listener);
		}
	}
	
	private void doStop(boolean isNormal) {
		status.set(STOPPING);
		isRunning.set(false);
		stopNodes(isNormal);
		stopStatisticsPrinter();
		sysinfo("Server stopped [" + new Date() + "]");
	}
    
	private void checkInitDirs(InitFolders folders) throws SystemCoreException {
		checkInitGameDir(folders.game);
		checkInitConfigDir(folders.config);
	    checkInitWorkDir(folders);
    }

	private void checkSingletonStatus(BootstrapNode[] nodes) throws SystemException {
		if(nodes == null) return;
		for (BootstrapNode node : nodes) {
			if(node.type != null && node.type.equals("singleton")) {
				isSingleton = true;
				break;
			}
		}
	}
	
	private void initNodes(BootstrapNode[] nodes) throws SystemException {
		liveNodes.clear();
    	// setupCommPortBase();
    	// registerJmsFactories();
    	if(nodes != null) {
	    	for (BootstrapNode node : nodes) {
	    		setupNodes(node.type, node.id);
			}
    	}
    	initNodes();
	}

	private void initServices() throws SystemException {
		reg = new InternalServiceRegistry();
    	InternalComponentAccess.setRegistry(reg);
    	initAndStartRegistry();
    	checkAutoStartServices();    	
	}
    
    private void sysinfo(String msg) {
		SystemLogger.info(msg);
	}
    
    private void syserror(String msg) {
		SystemLogger.error(msg);
	}
    
    private void checkInitWorkDir(InitFolders folders) throws SystemCoreException {
		// File home = new File(Constants.FIREBASE_HOME);
		workDir = folders.work; // new File(home, WORK_DIR);
		if(!workDir.exists() && !workDir.mkdirs()) {
			throw new SystemCoreException("Server failed to create work directory '" + workDir + "'");
		}
	}
	
	private void setupTrustedSarLocations() {
		addTrustedSarLocation(new File(folders.lib, "internal/"));
	}
    
	private void checkInitGameDir(File gameDir) throws SystemCoreException {
		if(!gameDir.isDirectory() || !gameDir.exists()) throw new SystemCoreException("Missing game deployment directory: " + gameDir);
    	if(isRunning.get()) throw new SystemCoreException("Server instance already running");
    	this.gameDir = gameDir;
	}
	
	private void checkInitConfigDir(File configDir) throws SystemCoreException {
		if(!configDir.isDirectory() || !configDir.exists()) throw new SystemCoreException("Missing config directory: " + configDir);
    	if(isRunning.get()) throw new SystemCoreException("Server instance already running");
    	this.configDir = configDir;
	}
    
    // synch live nodes elsewhere
	private NodeHandle findNode(String id, boolean remove) {
		for (Iterator<NodeHandle> it = liveNodes.iterator(); it.hasNext();) {
			NodeHandle h = it.next();
			if(id.equals(h.node.getId())) {
				if(remove) it.remove();
				return h; // EARLY RETURN
			}
		}
		return null;
	}
	
	private void stopAndDestroyRegistry() {
		reg.stop();
		reg.destroy();
	}
    
	// start server "sleeper" thread
    private void startThread() {
		new Thread(new SafeRunnable() {
			public void innerRun() {
				while(isRunning()) {
					try {
						Thread.sleep(MAIN_THREAD_SLEEP);
					} catch(InterruptedException e) { }
				}
			}
		}, "Server instance main thread").start();
	}

	private void initAndStartRegistry() throws SystemException {
		reg.init(new DefaultRegistryContext(this, this));
		reg.start();
	}
	
	private void checkAutoStartServices() throws SystemCoreException {
		StringList ids = getAutoStartServiceIds();
		if(ids != null) {
			for(String id : ids) {
				Service s = reg.doGetInternalService(id);
				if(s == null) {
					String msg = "Could not find service '" + id + "' during services auto start; Please check configuration.";
					log.warn(msg);
					sysinfo(msg);
				}
			}
		}
	}
	
	private StringList getAutoStartServiceIds() {
		Namespace ns1 = new Namespace("server");
		ServerConfig c = getServerConfiguration(ns1);
		return c.getAutostartServices();
	}
	
	private void initJmx() {
		try {
			MBeanServer mbs = getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=ServerInstance");
	        mbs.registerMBean(new LocalServer(this), monitorName);
            if(isRunningOnLinux()) {
	            monitorName = new ObjectName("com.cubeia.firebase:type=SystemMonitor");
	            mbs.registerMBean(SystemMonitor.getInstance(), monitorName);
            }
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void initNodes() throws SystemException {
		if(haveMaster) initLocalMaster(null);
		for(NodeHandle n : liveNodes) {
			long time = System.currentTimeMillis();
			log.debug("Init sequence for node '" + n.node.getId() + "' started");
			initNode(n, false);
			time = System.currentTimeMillis() - time;
			log.debug("Init sequence for node '" + n.node.getId() + "' finished [millis=" + time + "]");
		}
	}

	
	private void initNode(NodeHandle n, boolean acceptMaster) throws SystemException {
		notifyNodeInit(n, true);
		if(n.node instanceof GameNode) initGameNode((GameNode)n.node, n);
		else if(n.node instanceof GatewayNode) initClientNode((GatewayNode)n.node, n);
		else if(n.node instanceof ManagerNode) initManagerNode((ManagerNode)n.node, n);
		else if(n.node instanceof MttNode) initMttNode((MttNode)n.node, n);
		else if(acceptMaster && n.node instanceof MasterNode) initLocalMaster(n);
		notifyNodeInit(n, false);
	}

	private void initManagerNode(ManagerNode node, NodeHandle hand) throws SystemException {
		ManagerNodeContextImpl impl = new ManagerNodeContextImpl(ClusterRole.MANAGER_NODE, node, this);
		hand.context = impl;
		impl.init(new IntContext());
		node.init(impl);
	}

	private void initClientNode(GatewayNode node, NodeHandle hand) throws SystemException {
		ClientNodeContextImpl impl = new ClientNodeContextImpl(ClusterRole.CLIENT_NODE, node, this);
		hand.context = impl;
		impl.init(new IntContext());
		node.init(impl);
	}
	
	private void initGameNode(GameNode node, NodeHandle hand) throws SystemException {
		GameNodeContextImpl impl = new GameNodeContextImpl(ClusterRole.GAME_NODE, node, this);
		hand.context = impl;
		impl.init(new IntContext());
		node.init(impl);
	}
	
	private void initMttNode(MttNode node, NodeHandle hand) throws SystemException {
		MttNodeContextImpl impl = new MttNodeContextImpl(ClusterRole.MTT_NODE, node, this);
		hand.context = impl;
		impl.init(new IntContext());
		node.init(impl);
	}

	private void initLocalMaster(NodeHandle h) throws SystemException {
		NodeHandle hand = (h == null ? findMasterNode() : h);
		MasterNode node = (MasterNode)hand.node; 
		MasterContextImpl con = new MasterContextImpl(node, this);
		hand.context = con;
		con.init(new IntContext());
		node.init(con);
	}

	private NodeHandle findMasterNode() {
		for(NodeHandle n : liveNodes) {
			if(n.node instanceof MasterNode) return n;
		}
		return null;
	}
	
    private void createDeploymentClassLoader() {
    	File libDir = new File(getGameDirectory(), "lib/");
    	if(!libDir.exists()) {
    		libDir = null;
    	}
    	if(!Constants.IN_ECLIPSE) depLoader = new DeploymentClassLoader(libDir, sharedLoader);
		else depLoader = new DeploymentClassLoader(libDir, getClass().getClassLoader());
	}

	private void setupNodes(String nodeType, String nodeId) throws SystemException {
		ClusterRole role = parseNodeType(nodeType);
    	if(role != null) addNode(nodeId, role);
    	else if(nodeType != null && nodeType.equals("singleton")) {
    		isSingleton = true;
    		addNode(getIdForRole(ClusterRole.MASTER_NODE), ClusterRole.MASTER_NODE);
    		// addNode(getIdForRole(ClusterRole.MANAGER_NODE), ClusterRole.MANAGER_NODE);
    		addNode(getIdForRole(ClusterRole.GAME_NODE), ClusterRole.GAME_NODE);
    		addNode(getIdForRole(ClusterRole.CLIENT_NODE), ClusterRole.CLIENT_NODE);
    		addNode(getIdForRole(ClusterRole.MTT_NODE), ClusterRole.MTT_NODE);
    	}
	}
	
    // Just for checking during startup
    public boolean isRunningOnLinux() {
    	return("Linux".equalsIgnoreCase(System.getProperty("os.name")));
    }
		
    private String getIdForRole(ClusterRole role) {
		if(ClusterRole.CLIENT_NODE == role) return "cli1";
		if(ClusterRole.MANAGER_NODE == role) return "man1";
		if(ClusterRole.GAME_NODE == role) return "gam1";
		if(ClusterRole.MASTER_NODE == role) return "mas1";
		if(ClusterRole.MTT_NODE == role) return "mtt1";
		return null;
	}

    private ClusterRole parseNodeType(String nodeType) {
		return ClusterRole.parse(nodeType);
	}
    
    // return false on failure
    private boolean startNodes() {
    	boolean success = true;
		for (NodeHandle node : liveNodes) {
			notifyNodeStart(node, true);
			long time = System.currentTimeMillis();
			log.debug("Start sequence for node '" + node.node.getId() + "' started");
			try {
				node.node.start();
				time = System.currentTimeMillis() - time;
				log.debug("Start sequence for node '" + node.node.getId() + "' finished [millis=" + time + "]");
			} catch(Throwable th) {
				/*
				 * Oops, failed to start a node, scream for help, abort startup 
				 * sequence and return failed (trac issue #92) /LJN
				 */
				String msg = "Start sequence for node '" + node.node.getId() + "' failed! Recevied message: " + th.getMessage();
				SystemLogger.error(msg);
				log.fatal(msg, th);
				success = false;
				break;
			}
			notifyNodeStart(node, false);
		}
		return success;
	}

    private void stopNodes(boolean normal) {
		for (int i = 1; i <= liveNodes.size(); i++) {
			// Stop in reverse order
			NodeHandle handle = liveNodes.get(liveNodes.size() - i);
			notifyNodeStop(handle, true);
			long time = System.currentTimeMillis();
			log.debug("Stop sequence for node '" + handle.node.getId() + "' started");
			try {
				handle.node.stop();
				time = System.currentTimeMillis() - time;
				log.debug("Stop sequence for node '" + handle.node.getId() + "' finished [millis=" + time + "]");
			} catch(Throwable th) {
				/*
				 * Only print this error if this is a normal shutdown, if it is an emergency
				 * we don't want anything "shadowing" a previous exception. /LJN
				 */
				if(normal) {
					log.error("Failed to Stop node '" + handle.node.getId() + "'.", th);
				} 
			}
			notifyNodeStop(handle, false);
		}
	}
    
    private void destroyNodes(boolean normal) {
    	NodeHandle master = null;
    	for (int i = 1; i <= liveNodes.size(); i++) {
			// Destroy in reverse order
			NodeHandle handle = liveNodes.get(liveNodes.size() - i);
			// Destroy master last
			if(handle.node instanceof MasterNode) master = handle;
			else {
				destroyHandle(handle, normal);
			}
		}
    	if(master != null) {
    		destroyHandle(master, normal);
    	}
	}


	private void destroyHandle(NodeHandle handle, boolean normal) {
		notifyNodeDestroy(handle, true);
		long time = System.currentTimeMillis();
		log.debug("Destroy sequence for node '" + handle.node.getId() + "' started");
		try {
			handle.destroy();
			time = System.currentTimeMillis() - time;
			log.debug("Destroy sequence for node '" + handle.node.getId() + "' finished [millis=" + time + "]");
		} catch(Throwable th) {
			/*
			 * Only print this error if this is a normal shutdown, if it is an emergency
			 * we don't want anything "shadowing" a previous exception. /LJN
			 */
			if(normal) {
				log.error("Failed to Stop node '" + handle.node.getId() + "'.", th);
			} 
		}
		notifyNodeDestroy(handle, false);
	}

    private void startStatisticsPrinter() {
    	HitCounter.getInstance().start();
    }
    
    private void stopStatisticsPrinter() {
    	HitCounter.getInstance().stop();
    }
    
    /// --- PRIVATE CLASSES --- ///
    
    private static final class InternalServiceRegistry extends DefaultServiceRegistry {
    	
    	private Service doGetInternalService(String publicId) {
    		return super.getInternalService(publicId);
    	}
    }
    
    private class Listener implements CommandListener {
    	
    	private final Set<Long> haltStack = new HashSet<Long>();
    	
    	public Object commandReceived(CommandMessage c) {
    		try {
	    		if(c.command instanceof Halt) {
	    			doHalt((Halt)c.command);
	    		} else if(c.command instanceof Resume) {
	    			doResume((Resume)c.command);
	    		}
	    		return null;
    		} catch(Throwable th) {
    			log.error("Failed to process halt/resume", th);
    			return null;
    		}
    	}

		private synchronized void doResume(Resume com) {
			long id = com.getAttachment().getId();
			haltStack.remove(id);
			if(haltStack.size() == 0) {
				log.info("Resuming; Id: " + id);
				resumeMBus(com.getAttachment().getLayout());
				resumeServices();
				resumeNodes();
				log.debug("Resumed; Id: " + id);
			} else {
				log.debug("Resume; Stack not empty, ignoring id: " + id);
			}
		}

		private synchronized void doHalt(Halt com) {
			long id = com.getAttachment().getId();
			haltStack.add(id);
			if(haltStack.size() == 1) {
				log.info("Halting; Id: " + id);
				haltMBus();
				haltServices();
				haltNodes();
				log.debug("Halted; Id; " + id);
			} else {
				log.debug("Halt; Stack not empty, ignoring id: " + id);
			}
		}
		
		private void resumeMBus(ClusterLayout layout) {
			log.debug("Resuming message bus: " + layout);
			reg.getServiceInstance(MBusContract.class).resume(layout);
		}
		
		private void haltMBus() {
			log.debug("Halting message bus");
			reg.getServiceInstance(MBusContract.class).halt();
		}

		private void haltServices() {
			for (String id : reg.getAvailableServiceIds()) {
				Contract serv = reg.getServiceInstance(id);
				if(serv instanceof Haltable) {
					log.debug("Halting service: " + id);
					((Haltable)serv).halt();
				}
			}
		}
		
		private void resumeServices() {
			for (String id : reg.getAvailableServiceIds()) {
				Contract serv = reg.getServiceInstance(id);
				if(serv instanceof Haltable) {
					log.debug("Resuming service: " + id);
					((Haltable)serv).resume();
				}
			}
		}

		private void resumeNodes() {
			for (NodeHandle node : liveNodes) {
				if(node.isInit) {
					log.debug("Resuming node '" + node.node.getId() + "'");
					node.node.resume();
				}
			}	
		}

		private void haltNodes() {
			for (NodeHandle node : liveNodes) {
				if(node.isInit) {
					log.debug("Halting node '" + node.node.getId() + "'");
					node.node.halt();
				}
			}
		}
    }
    
    private final class IntContext implements NodeContext {
    	
    	public MBeanServer getMBeanServer() {
    		return ServerInstance.this.getMBeanServer();
    	}
    	
    	public ServerId getServerId() {
    		return ServerInstance.this.getServerId();
    	}
    	
    	public ServiceRegistry getServices() {
    		return ServerInstance.this.getServiceRegistry();
    	}
    	
    	public void shutdown(String msg, boolean emergency) {
    		ServerInstance.this.shutdown(msg, emergency);
    	}
    	
    	public ClassLoader getDeploymentClassLoader() {
    		return ServerInstance.this.getDeploymentClassLoader();
    	}
    }
    
    private static final class NodeHandle {
    	
    	public volatile boolean isInit;
		private final Node<?> node;
		private NodeContext context;
		
		private NodeHandle(Node<?> node) {
			this.node = node;
		}

		public void destroy() {
			node.destroy();
			if(context instanceof Initializable<?>) {
				((Initializable<?>)context).destroy();
			}
		}
    }
}