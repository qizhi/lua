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
package com.cubeia.firebase.server.service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.UnmodifiableList;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.instance.ServerServiceConfig;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.routing.impl.InternalComponentRouter;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.server.service.jmx.RegistryStats;
import com.cubeia.firebase.server.service.jmx.ServiceBean;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.util.FirebaseLockFactory;
import com.cubeia.util.Lists;
import com.game.server.bootstrap.SharedClassLoader;

/**
 * @author lars.nilsson
 */

/*
 * TODO: HACK WARNING: In order to set a service router on services that needs it, we need
 * the mbus. In other words: a routable service implicitly depends on the mbus which is in itself
 * a service. The init methods ends with:
 * 
 *    initSharedRouter();
 *	  checkRoutables();
 *
 * The first of the above calls attempt to retrieve the message bus in order to create a sender
 * for the routable. Ugly stuff.
 */
public class DefaultServiceRegistry implements InternalServiceRegistry, Initializable<DefaultServiceRegistryContext>, Startable {
	
	public static Namespace getServerServiceNamespace(String publicId) {
		return new Namespace("service." + publicId);
	}
	
	public static ServerServiceConfig getServiceConf(ServiceRegistry reg, String publicId) {
		ServerConfigProviderContract serv = reg.getServiceInstance(ServerConfigProviderContract.class);
		return serv.getConfiguration(ServerServiceConfig.class, getServerServiceNamespace(publicId));
	}
	
	
	// --- INSTANCE MEMBERS --- //

	DefaultServiceRegistryContext con;

	/*
	 * Trac #562: Using fair locks + #581
	 */
	private ReadWriteLock lock = FirebaseLockFactory.createLock();
	private Map<Class<? extends Contract>, List<ServiceRegisytryHandle>> clServices = new HashMap<Class<? extends Contract>, List<ServiceRegisytryHandle>>(5);
	private Map<String, ServiceRegisytryHandle> services = new HashMap<String, ServiceRegisytryHandle>();
	
	private RegistryStats stats = new RegistryStats();
	private Logger log = Logger.getLogger(getClass());
	private List<ServiceRegisytryHandle> startupOrder; // LIST IN DEPENDENCY ORDER, USE FOR START/STOP/DESTROY
	InternalComponentRouter sharedRouter; // sender for service->client events
	
	private ArchiveLoader archiveLoader = new ArchiveDirectorLoader();
	private boolean requireConfigDeps = true;
	
	public void init(DefaultServiceRegistryContext con) throws SystemException {
		Arguments.notNull(con, "context");
		this.con = con;
		doInit();
		initJmx();
	}
	
	/**
	 * @return A list of all started services, and only started 
	 */
	public List<String> getAvailableServiceIds() {
		lock.readLock().lock();
		try {
			List<String> list = new ArrayList<String>();
			for (String id : services.keySet()) {
				ServiceRegisytryHandle h = services.get(id);
				if(h.isStarted()) {
					list.add(id);
				}
			}
			return list;
		} finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public <T extends Contract> boolean isPublic(Class<T> contract, String publicId) {
		Arguments.notNull(contract, "class");
		lock.readLock().lock();
	    try {
	    	List<ServiceRegisytryHandle> handles = clServices.get(contract);
	    	if(handles == null) return false;
	    	else {
	    		for (ServiceRegisytryHandle handle : handles) {
	    			InternalServiceInfo info = handle.getArchive().getServiceInfo();
					if(publicId == null) return handle.getArchive().isPublic(); // EARLY RETURN
					else if(publicId.equals(info.getPublicId())) {
						return handle.getArchive().isPublic(); // EARLY RETURN
					}
				}
	    		return false;
	    	}
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public RoutableService getRoutableService(String id) {
		Arguments.notNull(id, "id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(id);
	    	if(handle != null) return handle.getRoutable();
	    	else return null;
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public List<String> getServiceIdsForContract(String contractClass) {
		Arguments.notNull(contractClass, "contractClass");
		lock.readLock().lock();
	    try {
	    	List<String> list = new LinkedList<String>();
	    	for (Class<?> cl : clServices.keySet()) {
	    		if(cl.getName().equals(contractClass)) {
	    			List<ServiceRegisytryHandle> handles = clServices.get(cl);
	    			for(ServiceRegisytryHandle h : handles) {
	    				list.add(h.getArchive().getPublicId());
	    			}
	    			break;
	    		}
	    	}
	    	if(list.size() > 0) return Collections.unmodifiableList(list);
	    	else return null;
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public boolean isPublic(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
			return (handle == null ? false : handle.getArchive().isPublic());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public StackTraceElement[] getStartupCapture(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
			return (handle == null ? null : handle.getStartupCapture());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public boolean isStarted(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
			return (handle == null ? false : handle.isStarted());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public void destroy() {
		destroyJmx();
		archiveLoader.destroy();
		destroyServices();
		destroyRouter();
	}
	
	public <T extends Contract> ServiceInfo getServiceInfo(Class<T> contract, String publicId) {
		Arguments.notNull(contract, "class");
		lock.readLock().lock();
	    try {
	    	List<ServiceRegisytryHandle> handles = clServices.get(contract);
	    	if(handles == null) return null;
	    	else {
	    		for (ServiceRegisytryHandle handle : handles) {
	    			InternalServiceInfo info = handle.getArchive().getServiceInfo();
					if(publicId == null) return info; // EARLY RETURN
					else if(publicId.equals(info.getPublicId())) return info; // EARLY RETURN
				}
	    		return null;
	    	}
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public <T extends Contract> T getServiceInstance(Class<T> contract) {
		return getServiceInstance(contract, (String)null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId) {
		Arguments.notNull(contract, "class");
		lock.readLock().lock();
	    try {
	    	List<ServiceRegisytryHandle> handles = clServices.get(contract);
	    	if(handles == null) return null;
	    	else {
	    		for (ServiceRegisytryHandle handle : handles) {
	    			InternalServiceInfo info = handle.getArchive().getServiceInfo();
					if(publicId == null) return (T)handle.getContract(); // EARLY RETURN
					else if(publicId.equals(info.getPublicId())) return (T)handle.getContract(); // EARLY RETURN
				}
	    		return null;
	    	}
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation) {
		return doListServicesByAnnotation(annotation, true);
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listPublicServicesByAnnotation(Class<T> annotation) {
		return doListServicesByAnnotation(annotation, false);
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract) {
		return doListServicesByContract(contract, true);
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listPublicServices(Class<T> cont) {
		return doListServicesByContract(cont, false);
	}
	
	public ServiceInfo getServiceInfo(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
			return (handle == null ? null : handle.getArchive().getServiceInfo());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}

	public Contract getServiceInstance(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
	    	return (handle == null ? null : handle.getContract());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	public void start() {
		lock.writeLock().lock();
		try {
			for (ServiceRegisytryHandle h : startupOrder) {
				if(h.getArchive().getServiceInfo().isAutoStart()) {
					h.start();
				}
			}
		} finally {
	    	lock.writeLock().unlock();
	    }
	}

	public void stop() {
		lock.writeLock().lock();
		try {
			// Iterate in reverse init order
	    	for (int i = 1; startupOrder != null && i <= startupOrder.size(); i++) {
				ServiceRegisytryHandle h = startupOrder.get(startupOrder.size() - i);
				try {
					h.stop();
				} catch(Throwable th) {
					log.error("Failed to stop service", th);
				}
			}
		} finally {
	    	lock.writeLock().unlock();
	    }
	}

	// --- PROTECTED METHODS --- //
	
	protected Service getInternalService(String publicId) {
		Arguments.notNull(publicId, "public id");
		lock.readLock().lock();
	    try {
	    	ServiceRegisytryHandle handle = services.get(publicId);
	    	return (handle == null ? null : handle.getService());
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	/*
	 * For testing purposes...
	 */
	protected void setArchiveLoader(ArchiveLoader loader) {
		Arguments.notNull(loader, "archive loader");
		this.archiveLoader = loader;
	}
	
	/*
	 * For testing purposes...
	 */
	protected void setRegistryStats(RegistryStats stats) {
		Arguments.notNull(stats, "registry stats");
		this.stats = stats;
	}
	
	/*
	 * For testing purposes...
	 */
	protected void setRequireConfigDeps(boolean requireConfigDeps) {
		this.requireConfigDeps = requireConfigDeps;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private <T extends Contract> List<ServiceInfo> doListServicesByContract(Class<T> contract, boolean includePrivate) {
		Arguments.notNull(contract, "class");
		lock.readLock().lock();
	    try {
	    	List<ServiceRegisytryHandle> handles = clServices.get(contract);
	    	if(handles == null) return Collections.emptyList();
	    	else {
	    		List<ServiceInfo> list = new ArrayList<ServiceInfo>(handles.size());
	    		for (ServiceRegisytryHandle h : handles) {
	    			if(h.getArchive().isPublic() || includePrivate) {
	    				list.add(h.getArchive().getServiceInfo());
	    			}
	    		}
	    		return list;
	    	}
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	private <T extends Annotation> List<ServiceInfo> doListServicesByAnnotation(Class<T> annotation, boolean includePrivate) {
		Arguments.notNull(annotation, "annotation");
		lock.readLock().lock();
	    try {
	    	List<ServiceInfo> list = new ArrayList<ServiceInfo>();
	    	for (ServiceRegisytryHandle h : services.values()) {
	    		if(h.hasAnnotation(annotation)) {
	    			if(h.getArchive().isPublic() || includePrivate) {
	    				list.add(h.getArchive().getServiceInfo());
	    			}
	    		}
	    	}
	    	return list;
	    } finally {
	    	lock.readLock().unlock();
	    }
	}
	
	private void destroyRouter() {
		if(sharedRouter != null) {
			sharedRouter.destroy();
			sharedRouter = null;
		}
	}

	private void destroyServices() {
		if(startupOrder == null) return; // SANITY CHECK
		lock.writeLock().lock();
		try {
			// Iterate in reverse init order
	    	for (int i = 1; i <= startupOrder.size(); i++) {
				ServiceRegisytryHandle h = startupOrder.get(startupOrder.size() - i);
				stats.unRegisterService(h.getArchive().getPublicId());
				try {
					h.destroy();
				} catch(Throwable th) {
					log.error("Failed to destroy service", th);
				}
			}
		} finally {
	    	lock.writeLock().unlock();
	    }
	}
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=ServiceRegistry");
	        mbs.registerMBean(stats, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=ServiceRegistry");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to stop mbean", e);
		}
	}
	
	private void doInit() throws SystemException {
		try {
			initArchiveLoader();
			ServiceArchive[] servs = archiveLoader.getServices();
			log.trace("Service registry checks for ID clashes");
			verifyUniqueIds(servs);
			log.trace("Service registry resolves service dependencies");
			startupOrder = new ArrayList<ServiceRegisytryHandle>();
			servs = resolveDependencies(servs);
			initServices(servs);
			initSharedRouter();
			checkRoutables();
		} catch (IOException e) {
			log.error(e, e);
			// FIXME: Better messages please
			throw new SystemCoreException("failed to load archive", e);
		} catch (IllegalArchiveException e) {
			log.error(e);
			// FIXME: Better messages please
			throw new SystemCoreException("failed to load archive", e);
		} 
	}

	private void initArchiveLoader() throws IOException, IllegalArchiveException {
		archiveLoader.init(new ArchiveLoaderContext() {
			
			@Override
			public File getWorkDirectory() {
				return con.getWorkDirectory();
			}
			
			@Override
			public SharedClassLoader getSharedSpace() {
				return con.getSharedSpace();
			}
			
			@Override
			public SystemLocations getLocations() {
				return getArchiveLocations();
			}
			
			@Override
			public ClassLoader getDeploymentClassLoader() {
				return con.getDeploymentClassLoader();
			}
		});
	}

	private void verifyUniqueIds(ServiceArchive[] servs) throws SystemException {
		Map<String, String> checker = new HashMap<String, String>(servs.length);
		for (ServiceArchive a : servs) {
			String pudId = a.getPublicId();
			if(checker.containsKey(pudId)) {
				String other = checker.get(pudId);
				throw new SystemCoreException("Public ID clash. Id '" + pudId + "' is declared by both '" + other + "' and '" + a.getName() + "'");
			} else {
				checker.put(pudId, a.getName());
			}
		}
	}

	private void initSharedRouter() throws SystemException {
		sharedRouter = new InternalComponentRouter("sharedServiceRouter", "Shared Service Router");
		sharedRouter.init(newRouterContext());
	}

	private RouterContext newRouterContext() {
		return new RouterContext() {
			
			public String getServerId() {
				return con.getServerId();
			}
		
			public MBusContract getMessageBus() {
				return getServiceInstance(MBusContract.class);
			}
			
			public ServiceRegistry getServices() {
				return DefaultServiceRegistry.this;
			}
		
			public MBeanServer getMBeanServer() {
				return con.getMBeanServer();
			}
		
		};
	}

	private void checkRoutables() {
		for (ServiceRegisytryHandle h : startupOrder) {
			if(h.isRoutable()) {
				h.createSetRouter();
			}
		}
	}

	private ServiceArchive[] resolveDependencies(ServiceArchive[] servs) throws SystemException {
		DepSetOrder order = new DepSetOrder();
		Mapping mapping = toArchiveMap(servs);
		/*
		 * Here's a hack: manually request the cluster config first, this
		 * will force the following order:
		 * 
		 *  1) server config
		 *  2) cluster connection
		 *  3) cluster config
		 */
		if(requireConfigDeps ) {
			resolveConfigDeps(order, mapping);
		}
		for (ServiceArchive archive : servs) {
			// Ignore already added services
			if(!order.resolved(archive)) {
				resolveDeps(archive, order, mapping);
			}
		}
		return order.order.toArray(new ServiceArchive[order.order.size()]);
	}

	private void resolveDeps(ServiceArchive archive, DepSetOrder order, Mapping mapping) throws SystemException {
		if(order.resolved(archive)) return; // SANITY CHECK
		String pubId = archive.getPublicId();
		checkCircularDeps(order, pubId); 
		order.current.push(pubId);
		UnmodifiableList<Dependency> deps = archive.getServiceInfo().getDependencies();
		for (Dependency dep : deps) {
			// Ignore already added services
			if(!order.resolved(dep)) {
				ServiceArchive next = null;
				if(!dep.isContract) {
					next = mapping.idMapping.get(dep.data);
				} else {
					next = mapping.contractMapping.get(dep.data);
				}
				if(next == null) {
					throw new SystemCoreException("Missing required service dependency; Service with id '" + pubId + "' depends on " + (dep.isContract ? "contract" : "id") + " '" + dep.data + "' which cannot be found.");
				}
				checkIllegalDependency(archive, next);
				resolveDeps(next, order, mapping); 
			}
		}
		order.idList.add(pubId);
		order.order.add(archive);
		order.current.pop();
	}

	private void checkIllegalDependency(ServiceArchive dependent, ServiceArchive dependency) throws SystemException {
		if(dependent.isTrusted()) {
			/*
			 * An trusted service may only depend on other trusted services.
			 */
			if(!dependency.isTrusted()) {
				String msg = "Unusual service dependency; Trusted service with id '" + dependent.getPublicId() + "' depends on '" + dependency.getPublicId() + "' which is an isolated service. This may cause problems as isolated services are initialized and started after trusted services.";
				SystemLogger.info(msg);
				log.warn(msg);
			}
		} else {
			/*
			 * Isolated service may depend on 1) other isolated services; or 2) a trusted
			 * service if that service is exported.
			 */
			if(dependency.isTrusted() && !dependency.isPublic()) {
				throw new SystemCoreException("Illegal service dependency; Isolated service with id '" + dependent.getPublicId() + "' depends on '" + dependency.getPublicId() + "' which is an trusted non-exporting service which cannot be reached by the dependent.");
			}
		}
	}

	private void resolveConfigDeps(DepSetOrder order, Mapping mapping) throws SystemException {
		ServiceArchive arch = mapping.idMapping.get(Constants.CLUSTER_CONFIG_SERVICE_NS);
		if(arch == null) throw new SystemCoreException("Could not find required configuration service '" + Constants.SERVER_CONFIG_SERVICE_NS + "'.");
		resolveDeps(arch, order, mapping);
	}

	private void checkCircularDeps(DepSetOrder order, String pubId) throws SystemCoreException {
		if(order.current.contains(pubId)) {
			// Oooops, cyclic dependency
			String parent = order.current.peek();
			throw new SystemCoreException("Circular service dependency detected; Services '" + parent + "' and '" + pubId + "' depends on each other.");
		}
	}

	private Mapping toArchiveMap(ServiceArchive[] servs) {
		Mapping map = new Mapping();
		for (ServiceArchive archive : servs) {
			InternalServiceInfo info = archive.getServiceInfo();
			String[] contractClasses = info.getContractClasses();
			for (String contract : contractClasses) {
				map.contractMapping.put(contract, archive);
			}
			map.idMapping.put(archive.getPublicId(), archive);
		}
		return map;
	}

	private SystemLocations getArchiveLocations() {
		return new SystemLocations() {
		
			public File[] trustedLocation() {
				return con.getTrustedSarLocations();
			}
		
			public File[] isolatedLocations() {
				return con.getIsolatedSarLocations();
			}
			
			public File[] deployedLocations() {
				return con.getDeployedSarLocations();
			}
		};
	}
	
	private void initServices(ServiceArchive[] archives) throws SystemException {
		initTrustedServices(archives);
		initIsolatedServices(archives);
	}
	
	private void initTrustedServices(ServiceArchive[] archives) throws SystemException {
		initSelectedServices(selectServices(archives, true));
	}
	
	private void initIsolatedServices(ServiceArchive[] archives) throws SystemException {
		initSelectedServices(selectServices(archives, false));
	}

	private ServiceArchive[] selectServices(ServiceArchive[] archives, boolean trusted) {
		List<ServiceArchive> tmp = new LinkedList<ServiceArchive>();
		for (ServiceArchive a : archives) {
			if(a.isTrusted() == trusted) {
				tmp.add(a);
			}
		}
		return Lists.toArray(tmp, ServiceArchive.class);
	}

	private void initSelectedServices(ServiceArchive[] archives) throws SystemException {
		for (ServiceArchive arch : archives) {
			long time = System.currentTimeMillis();
			log.trace("Init sequence on service '" + arch.getPublicId() + "' commencing");
			ServiceRegisytryHandle handle = new ServiceRegisytryHandle(this, arch);
			handle.init();
			boolean isIsolated = !handle.getArchive().isTrusted();
			InternalServiceInfo info = handle.getArchive().getServiceInfo();
			stats.registerService(new ServiceBean(info, isIsolated, this));
			services.put(handle.getArchive().getServiceInfo().getPublicId(), handle);
			Class<? extends Contract>[] classes = handle.getContractClasses();
			for (Class<? extends Contract> contract : classes) {
				List<ServiceRegisytryHandle> list = clServices.get(contract);
				if(list == null) {
					list = new LinkedList<ServiceRegisytryHandle>();
					clServices.put(contract, list);
				}
				list.add(handle);
			}
			startupOrder.add(handle);
			log.debug("Service '" + arch.getPublicId() + "' initiated [millis=" + (System.currentTimeMillis() - time) + "]");
			if(handle.getArchive().isDeployed()) {
				SystemLogger.info("Deployed: Service " + handle.getArchive().getName() + "; Id: " + handle.getArchive().getPublicId());
			}
		}
	}


	/// --- INNER CLASSES --- ///
	
	private static class Mapping {
		
		private final Map<String, ServiceArchive> idMapping = new HashMap<String, ServiceArchive>();
		private final Map<String, ServiceArchive> contractMapping = new HashMap<String, ServiceArchive>();
		
	}
	
	private static class DepSetOrder {
		
		// final order
		private final List<ServiceArchive> order = new LinkedList<ServiceArchive>();
		
		// already in order list, ie already resolved
		private final Set<String> idList = new TreeSet<String>();
		
		// stack for recursive cyclic dependency checks
		private final Stack<String> current = new Stack<String>();

		public boolean resolved(Dependency dep) {
			return idList.contains(dep.data);
		}

		public boolean resolved(ServiceArchive archive) {
			String id = archive.getPublicId();
			return idList.contains(id);
		}
	}
}
