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
package com.cubeia.firebase.server.service.systemstate.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheSPI;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.NodeSPI;
import org.jboss.cache.jmx.CacheJmxWrapper;

import com.cubeia.core.space.jboss.TreeCacheClusterConfig;
import com.cubeia.core.space.jboss.TreeCacheServerConfig;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.statistics.Level;
import com.cubeia.firebase.server.statistics.StatisticsLevel;
import com.cubeia.firebase.server.util.JGroupsConfig;
import com.cubeia.firebase.server.util.JGroupsConfigurator;
import com.cubeia.firebase.server.util.MCastgenWrapper;
import com.cubeia.firebase.util.ServiceMBean;
import com.cubeia.firebase.util.TreeCacheInfo;
import com.cubeia.firebase.util.executor.JmxExecutor;

/**
 * Holds a JBoss Cache as cache implementation for storing
 * and replicating system information.
 * 
 * Will use configuration-namespace of 'systemstate'
 * 
 * @author Fredrik
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SystemStateCacheHandler implements ServiceMBean {

	private static final Namespace SYSTEMSTATE_NAMESPACE = new Namespace("systemstate");
	private static final String DEFAULT_CACHE_CONF = "com/cubeia/firebase/server/service/systemstate/jbc/StateCacheService.xml";
	private static final AtomicInteger COUNT = new AtomicInteger();
	
	private JmxExecutor executor;
	
	private String configFile = DEFAULT_CACHE_CONF;
	
	/** The logger */
	private Logger log = Logger.getLogger(this.getClass());

	/** Current state */
	private State state = State.STOPPED;

    /** JMX bind name */
    private ObjectName monitorName;
    private ObjectName monitor2Name;
    
	/** The cache */
    private CacheSPI cache;
	private TreeCacheClusterConfig config;
	private TreeCacheServerConfig localConfig;
	private ServiceContext con;
	private boolean forceLocal; // Force local cache regardless of config

	/**
	 * Start the TreeCacheHandler with an alternate config.
	 * The configuration will not be configurated with the 
	 * cluster wide properties.
	 * 
	 */
	public SystemStateCacheHandler(String alternateConfig) {
		configFile = alternateConfig;
		initJmxNames();
	}
	
	/**
	 * Create a default TreeCacheHandler.
	 * 
	 * @param forceLocal Force the cache to be local regardless of configuration
	 * @param con
	 */
	public SystemStateCacheHandler(ServiceContext con, boolean forceLocal) {
		Arguments.notNull(con, "context");
		this.forceLocal = forceLocal;
		this.con = con;
		// Get the server local configuration
		ServerConfigProviderContract serv = con.getParentRegistry().getServiceInstance(ServerConfigProviderContract.class);
		localConfig = serv.getConfiguration(TreeCacheServerConfig.class, SYSTEMSTATE_NAMESPACE);
		// Get the cluster wide configuration
		ClusterConfigProviderContract clusterConfigService = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		config = clusterConfigService.getConfiguration(TreeCacheClusterConfig.class, SYSTEMSTATE_NAMESPACE);
		initJmxNames();
	}

	private void initJmxNames() {
		try{
            monitorName = new ObjectName("com.cubeia.firebase.cache:type=SystemStateCache");
            monitor2Name = new ObjectName("com.cubeia.firebase.cache:type=SystemStateCacheInfo");
        } catch (MalformedObjectNameException ex) {
            log.error("Error formatting JMX name: "+ex);
        }
	}

	/**
	 * Get the current state.
	 */
	public String getStateDescription() {
		return state.toString();
	}


	/**
	 * Start this service.
	 */
	public void start() {
		try{
			executor = new JmxExecutor(1, 12, "SystemStateUpdater{" + COUNT.incrementAndGet() + "}");
			cache = createCache("SystemState");
			log.info("SystemState repl Queue: "+cache.getConfiguration().isUseReplQueue());
			// Bind to JMX
			bindCacheToJMX();
			log.info("System State TreeCache service started.");
		}catch(Exception e){
			log.fatal("Could not start System State TreeCache service. "+e,e);
		}
	}


	/**
	 * Create and configure the TreeCache.
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
    private CacheSPI createCache(String name) throws Exception {
		log.debug("Create cache from config: "+configFile);
		File file = setupServerConfig();
		InputStream is = new FileInputStream(file);
		CacheSPI cache = (CacheSPI)new DefaultCacheFactory<Object, Object>().createCache(is, true);
		return cache;
	}


	/**
	 * Stop this service
	 */
	public void stop() {
		unbindCacheFromJMX();
		cache.stop();
		unbindCacheFromJMX();
		executor.stopNow();
	}
	
	/**
     * Bind the Tree-Cache to JMX
     */
	private void bindCacheToJMX() {
        try{
        	if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
	            MBeanServer mbs = getMBeanServer();
	            if(!mbs.isRegistered(monitorName)) {
	            	mbs.registerMBean(new CacheJmxWrapper(cache), monitorName);
	            }
        	}
        	
        	if (StatisticsLevel.getInstance().isEnabled(Level.DEPLOYMENT)) {
	            MBeanServer mbs = getMBeanServer();
	            if(!mbs.isRegistered(monitor2Name)) {
	            	mbs.registerMBean(new TreeCacheInfo(cache), monitor2Name);
	            }
        	}
        }catch(Exception ex) {
            log.error("Could not unbind the system state cache from the JMX Server", ex);
        }
    }
    
    private void unbindCacheFromJMX() {
        try{
        	MBeanServer mbs = getMBeanServer();
        	if (mbs.isRegistered(monitorName)) {
        		mbs.unregisterMBean(monitorName);
        	}
        	if (mbs.isRegistered(monitor2Name)) {
        		mbs.unregisterMBean(monitor2Name);
        	}
        }catch(Exception ex) {
            log.error("Could not bind the system state cache to the JMX Server", ex);
        }
    }
    
    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

	public Object getAttribute(String fqn, String attribute) throws CacheException {
		return cache.get(Fqn.fromString(fqn), attribute); // UNCHECKED
	}
	
	public Object getAttribute(Fqn fqn, String attribute) throws CacheException {
        return cache.get(fqn, attribute); // UNCHECKED
    }

	public void dumpInfo() {
		log.debug("System State Cache Details: \n"+cache.toString());
	}

	//-----------------------------------
	// 
	//  System State Logic
	// 
	//-----------------------------------


	/**
	 * Add a listener to the cache
	 */
	public void addListener(Object listener) {
		cache.addCacheListener(listener);
	}
	
	public void removeListener(Object listener) {
		cache.removeCacheListener(listener);
	}
	
	public boolean exists(String fqn) {
		return cache.getRoot().hasChild(Fqn.fromString(fqn));
	}
	
	public void updateAttribute(String fqn, String attribute, Object value) throws CacheException {
		updateAttribute(fqn, attribute, value, false);
	}

	public void updateAttribute(String fqn, String attribute, Object value, boolean doAsync) throws CacheException {
		if(!doAsync) cache.put(Fqn.fromString(fqn), attribute, value);
		else executor.submit(new Updater(fqn, attribute, value));
	}
	
	public void updateAttributes(String fqn, Map<String, Object> attributes) {
		updateAttributes(fqn, attributes, false);
	}
	
	public void updateAttributes(String fqn, Map<String, Object> attributes, boolean doAsync) {
		if(!doAsync) cache.put(Fqn.fromString(fqn), attributes);
		else executor.submit(new UpdaterMulti(fqn, attributes));
	}

	/*
	 * This is removed, the lobby gets very annoyed if we add paths without
	 * telling it, reason is that it cannot see the difference between ordinary nodes
	 * and table/mtt nodes in the cache without attributes set. If you start using
	 * this method, you may break the lobby /Larsan
	 */
	/*public void addNode(String fqn) {
		cache.put(Fqn.fromString(fqn), null);
	}*/

	/*
     * FIXME / DONE: See Cubeia Trac issue #402 and Jboss Cache JIRA-1337-
     * When JBX 2.2.0 is released and the immutable bug is fixed we can remove the 
     * transaction handling below.
     * 
     * @return A defensive map copy of the underlying data. 
     */
	public Map<Object,Object> getNodeData(String fqn) {
		/*UserTransaction tx = getJBossDummyTransaction();
        boolean doTrans = false;
	    try {
	    	doTrans = (tx.getStatus() == Status.STATUS_NO_TRANSACTION);
            if(doTrans) {
            	tx.begin();
            }*/
            
            HashMap<Object, Object> data = null;
            Node node = cache.getRoot().getChild(Fqn.fromString(fqn));
            if (node != null) {
                data = new HashMap<Object, Object>(node.getData());
            } else {
            	data = new HashMap<Object, Object>(0);
            }
            
            /*if(doTrans) {
            	tx.commit();
            }*/
            return data;
        
		/*} catch (Throwable th) {
            log.warn("Could not get systemstate data for node: "+fqn);
            if(doTrans) {
            	try { tx.rollback(); } catch(Throwable t) {
            		log.fatal("Could not roll back transaction, this is expected if a node just went down. Exception: "+t);
            	}
            }
            return new HashMap<Object, Object>();
        }*/
	}

	
	public Map<Fqn<?>, Node> getEndNodes(String address) {
		Map<Fqn<?>, Node> nodes = new HashMap<Fqn<?>, Node>();
		if (cache.getRoot().hasChild(Fqn.fromString(address))) {
			try {
//				NodeSPI baseNode = cache.getRoot().getChildDirect(Fqn.fromString(address));
			    Node baseNode = cache.getRoot().getChild(Fqn.fromString(address));
				// if (baseNode.getChildrenDirect().size() == 0) {
			    if (baseNode.getChildren().size() == 0) {
					nodes.put(baseNode.getFqn(), baseNode);
				} else { 
					findLeaves(nodes, baseNode);
				}
				
			} catch (CacheException e) {
				log.error("Could not get end nodes", e);
			}
		}
		
		return nodes;
	}

	
	/**
	 * Iterate recursively over the tree down to
	 * all end nodes and add them to the list.
	 * 
	 * @param nodes
	 * @param baseNode
	 */
	private void findLeaves(Map<Fqn<?>, Node> nodes, Node baseNode) {
		for (Object child : baseNode.getChildren()) {
		    Node node = (Node)child;
			if (node.getChildren().size() == 0) {
				// End node
				nodes.put(node.getFqn(), node);
			} else {
				findLeaves(nodes, node);
			}
		}
	}

	
	/**
	 * Get all sub-nodes including the current node.
	 * 
	 * Discards end node if includeLast = false
	 * 
	 * Example:
	 * 
	 * Cache -
	 *   /table/a/b/c/d
	 *   /table/a/b/e/f
	 *   /table/a/g/h/i
	 *   
	 * Input FQN -
	 *   /a/b
	 *   
	 * Returned FQN's -
	 *  /a/b
	 *  /a/b/c
	 *  /a/b/c/d
	 *  /a/b/e
	 *  /a/b/e/f
	 * 
	 * 
	 * @param fqn
	 * @param includeLast, true if end node(s) should be included
	 * @return
	 */
	public Map<Fqn<?>, Node> getSubNodes(String path, boolean includeLast) {
		Map<Fqn<?>, Node> nodes = new HashMap<Fqn<?>, Node>();
		if (cache.getRoot().hasChild(Fqn.fromString(path))) {
			try {
				NodeSPI baseNode = cache.getRoot().getChildDirect(Fqn.fromString(path));
				findAllChildren(nodes, baseNode, includeLast);
				
			} catch (CacheException e) {
				log.error("Could not get sub nodes", e);
			}
		}
		
		return nodes;
	}


    /**
     * Returns the underlying tree cache.
     * @return
     */
    public Cache getCache() {
        return cache;
    }

    public boolean hasNode(String fqn) throws CacheException {
        return cache.getRoot().hasChild(Fqn.fromString(fqn));
    }

    public void removeAttribute(String fqn, String attr) throws CacheException {
        Fqn fqn2 = Fqn.fromString(fqn);
        // If we call remove and node does not exist we get a WARN in the logs
        if (cache.exists(fqn2)) {
            cache.remove(fqn2, attr); // UNCHECKED !!
        }
    }

    public void removeNode(String fqn) throws CacheException {
        cache.removeNode(Fqn.fromString(fqn));
    }

    public Set<String> getChildren(String fqn) {
        Node node = cache.getNode(Fqn.fromString(fqn)); //cache.get(Fqn.fromString(fqn)); //
        if (node != null) {
            return node.getChildrenNames();
        }
        return Collections.EMPTY_SET;
    }
      
    
	/**
	 * Iterate recursively over the tree down to
	 * all nodes and add all of them to the list
	 * including the one sent in.
	 * 
	 * @param nodes
	 * @param baseNode
	 */
	private void findAllChildren(Map<Fqn<?>, Node> nodes, NodeSPI baseNode, boolean includeLast) {
		nodes.put(baseNode.getFqn(), baseNode);
		for (Object child : baseNode.getChildrenDirect()) {
			NodeSPI node = (NodeSPI)child;
			if (node.getChildrenDirect().size() == 0) {
				// End node
				if (includeLast) {
					nodes.put(node.getFqn(), node);
				}
			} else {
				findAllChildren(nodes, node, includeLast);
			}
		}
	}
	


	// --- PRIVATE METHODS --- //

	private File setupServerConfig() throws SystemCoreException {
			try {
				File file = getTempFile();
				InputStream in = getClass().getClassLoader().getResourceAsStream(configFile);
				if(in == null) throw new FileNotFoundException("did not find default tree cache config '" + configFile + "' in class path");
				BufferedReader read = new BufferedReader(new InputStreamReader(in));
				FileOutputStream out = new FileOutputStream(file);
				Writer write = new OutputStreamWriter(out, "UTF-8");
				String line = null;
				while((line = read.readLine()) != null) {
					line = doReplace(line);
					write.write(line);
				}
				read.close();
				write.flush();
				return file;
			} catch(Exception e) {
				// FIXME: Better message...
				throw new SystemCoreException("failed to setup configuration for server", e);
			}
	}

	private String doReplace(String line) throws IOException {
		if (config != null) {
			//SocketAddress ad = localConfig.getBindAddress();
			//line = line.replace("${bind-address}", ad.getHost().getHostAddress());
			//line = line.replace("${bind-port}", String.valueOf(ad.getPort()));
			//ad = config.getMcastAddress();
			//line = line.replace("${mcast-address}", ad.getHost().getHostAddress());
			//line = line.replace("${mcast-port}", String.valueOf(ad.getPort()));
			if(!forceLocal) {
				line = line.replace("${cache-mode}", config.getCacheMode());
			} else {
				/*
				 * This is where we force the cache to be local, this is done in 
				 * FCE to disable clustering completely. /LJN
				 */
				line = line.replace("${cache-mode}", "LOCAL");
			}
			line = line.replace("${isolation-level}", config.getIsolationLevel());
			line = line.replace("${UseReplQueue}", String.valueOf(config.getUseReplQueue()));
			line = line.replace("${ReplQueueInterval}", String.valueOf(config.getReplQueueInterval()));
			line = line.replace("${ReplQueueMaxElements}", String.valueOf(config.getReplQueueMaxElements()));
			//line = line.replace("${loopback}", String.valueOf(config.getLoopback()));
			
			if(line.indexOf("${jgroups-config}") != -1) {
				SocketAddress ad = localConfig.getBindAddress();
				JGroupsConfig realConf = getGeneratedMCastAddress(config);
				String tmp = JGroupsConfigurator.toXMLCharacters(realConf, ad, localConfig.getBindInterface());
				line = line.replace("${jgroups-config}", tmp);
			}
		}
		return line;
	}
	
    private JGroupsConfig getGeneratedMCastAddress(JGroupsConfig parent) {
    	String id = con.getPublicId();
    	ServiceRegistry registry = con.getParentRegistry();
		return new MCastgenWrapper(registry).checkWrap(parent, id);
	}

	private File getTempFile() throws IOException {
		File f = File.createTempFile("treeCache" + System.currentTimeMillis(), null, null);
		f.deleteOnExit();
		return f;
	}

	
	
	
	private class Updater implements Runnable {

	    private final String fqn;
	    private final String attribute;
        private final Object value;

        public Updater(String fqn, String attribute, Object value) {
            this.fqn = fqn;
            this.value = value;
            this.attribute = attribute;
	    }
	    
        public void run() {
            try {
                cache.put(Fqn.fromString(fqn), attribute, value);
            } catch (Throwable th) {
                log.error("Error updating systemstate", th);
            }
        }
	    
	}
	
	
	private class UpdaterMulti implements Runnable {

        private final String fqn;
        private final Map<String, Object> attributes;

        public UpdaterMulti(String fqn, Map<String, Object> attributes) {
            this.fqn = fqn;
            this.attributes = attributes;
        }
        
        public void run() {
            try {
                cache.put(Fqn.fromString(fqn), attributes);
            } catch (Throwable th) {
                log.error("Error updating systemstate", th);
            }
        }
        
    }
	
	
	   
    /*
     * Optimally we want to let JBC handle its own transactions but due to a bug (JIRA-1337 - http://jira.jboss.org/jira/browse/JBCACHE-1337)
     * we need to wrap all data acquiring within a tx.
     * 
     * Cubeia Trac issue: #402
     * 
     * @return
     */
    /*private UserTransaction getJBossDummyTransaction() {
        return new DummyUserTransaction(DummyTransactionManager.getInstance());
    }*/
}
