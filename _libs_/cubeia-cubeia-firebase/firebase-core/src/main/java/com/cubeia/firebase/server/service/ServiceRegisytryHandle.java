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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.server.instance.ServerServiceConfig;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.routing.impl.ThreadedServiceRouterImpl;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.util.ContextClassLoaderInvocationHandler;
import com.cubeia.firebase.server.util.InternalComponentInvocationHandler;
import com.cubeia.firebase.server.util.InvocationHandlerAdapter;
import com.cubeia.firebase.server.util.JndiWrappingInvocationHandler;
import com.cubeia.firebase.util.InvocationFacade;

class ServiceRegisytryHandle {
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final DefaultServiceRegistry containingServiceRegistry;
	private final ServiceArchive archive;
	
	private StackTraceElement[] startupCapture;
	private boolean isStarted;
	private Class<? extends Contract>[] contractClasses;
	private ThreadedServiceRouterImpl router;
	
	private Contract contract; // may be a proxy
	private Service service; // may be a proxy
	
	ServiceRegisytryHandle(DefaultServiceRegistry defaultServiceRegistry, ServiceArchive arch) {
		containingServiceRegistry = defaultServiceRegistry;
		archive = arch;
	}
	
	public boolean hasAnnotation(Class<? extends Annotation> cl) {
		for (Class<? extends Contract> test : contractClasses) {
			if(test.isAnnotationPresent(cl)) {
				return true;
			}
		}
		return false;
	}
	
	public Class<? extends Contract>[] getContractClasses() {
		return contractClasses;
	}
	
	public ServiceArchive getArchive() {
		return archive;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	@Override
	public String toString() {
		return (archive == null ? super.toString() : archive.toString());
	}
	
	public void createSetRouter() {
		if(!isRoutable()) return; // SANITY CHECK
		RoutableService serv = (RoutableService)service;
		ServerServiceConfig conf = DefaultServiceRegistry.getServiceConf(containingServiceRegistry, archive.getPublicId());
		router = new ThreadedServiceRouterImpl(archive.getPublicId(), conf.getRouterPoolProperties(), containingServiceRegistry.sharedRouter);
		// set a proxy for the router to switch back to system class loader
		InvocationHandlerAdapter root = new InvocationHandlerAdapter(router);
		InvocationHandler handler = new ContextClassLoaderInvocationHandler(getSystemClassLoader(), root);
		serv.setRouter(
				(ServiceRouter) newProxy(
									new Class[] { ServiceRouter.class }, 
									handler, 
									ServiceRouter.class.getClassLoader()
								)
						);
	}
	
	public boolean useLegacyContextClassLoader() {
		return (archive != null ? archive.getServiceInfo().useLegacyContextClassLoader() : false);
	}

	public RoutableService getRoutable() {
		if(!isRoutable()) return null;
		else return new RoutableService() {
		
			private RoutableService real = (RoutableService)service;
			
			public void setRouter(ServiceRouter router) {
				real.setRouter(router);
			}
			
			public void onAction(ServiceAction action) {
				real.onAction(action);
			}
		};
	}

	public boolean isRoutable() {
		return (service instanceof RoutableService);
	}

	public void init() throws SystemException {
		initService();
	}
	
	public synchronized void start() {
		tryStart();
	}

	public synchronized void stop() {
		if(isStarted) {
			long time = System.currentTimeMillis();
			log.trace("Stop sequence for service '" + archive.getPublicId() + "' commencing");
			service.stop();
			isStarted = false;
			log.debug("Service '" + archive.getPublicId() + "' stopped [millis=" + (System.currentTimeMillis() - time) + "]");
		}
	}
	
	public void destroy() {
		long time = System.currentTimeMillis();
		log.trace("Destruction of service '" + archive.getPublicId() + "' commencing");
		service.destroy();
		log.debug("Service '" + archive.getPublicId() + "' destroyed [millis=" + (System.currentTimeMillis() - time) + "]");
		if(router != null) {
			router.stop();
			router = null;
		}
	}
	
	public synchronized Service getService() {
		tryStart();
		return service;
	}
	
	public synchronized Contract getContract() {
		tryStart();
		return contract;
	}
	
	public StackTraceElement[] getStartupCapture() {
		return startupCapture;
	}
	
	
	/// --- PRIVATE METHODS --- ///
	
	private void captureStart() {
		this.startupCapture = Thread.currentThread().getStackTrace();
	}
	
	private void tryStart() {
		if(!isStarted) {
			long time = System.currentTimeMillis();
			log.trace("Start sequence for service '" + archive.getPublicId() + "' commencing");
			service.start();
			isStarted = true;
			log.debug("Service '" + archive.getPublicId() + "' started [millis=" + (System.currentTimeMillis() - time) + "]");
			captureStart();
		}	
	}

	private void initService() throws SystemException {
		// FIXME: Illegal states?
		try {
			Object serv = archive.instantiateService();
			Class<?>[] contr = archive.instantiateContracts();
			checkAndAssignInterfaces(serv, contr);
			service.init(newServiceContext(archive));
		} catch (ClassNotFoundException e) {
			throw new SystemCoreException("Failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'", e);
		} catch (NoClassDefFoundError e) {
			throw new SystemCoreException("Failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'", e);
		} catch (InstantiationException e) {
			throw new SystemCoreException("Failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'", e);
		} catch (IllegalAccessException e) {
			throw new SystemCoreException("Failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'", e);
		}
	}

	private ServiceContext newServiceContext(ServiceArchive archive) {
		ServiceContext inner = containingServiceRegistry.con.newServiceContext(archive);
		// return a proxy, which in turn returns a proxy for the service registry
		InvocationHandler root = new InvocationHandlerAdapter(inner);
		return newProxy(new Class[] { ServiceContext.class }, new InternalComponentInvocationHandler(getSystemClassLoader(), root), ServiceContext.class.getClassLoader());
	}

	private ClassLoader getSystemClassLoader() {
		return getClass().getClassLoader();
	}

	@SuppressWarnings("unchecked")
	private void checkAndAssignInterfaces(Object service, Class<?>[] contracts) {
		/*
		 * If this is not a proxied service, make sure the implementation and 
		 * the contract classes are sane.
		 */
		if(!archive.getServiceInfo().isProxy()) {
			log.trace("Service '" + archive.getPublicId() + "' declared as a concrete implementation, checking interfaces.");
			checkClassRelations(service.getClass(), Service.class, "failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'; declared service " + service.getClass().getName() + " does not implement " + Service.class.getName());
			for (Class<?> contract : contracts) {
				checkClassRelations(contract, Contract.class, "failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'; declared contract " + contract.getName() + " does not implement " + Contract.class.getName());
				checkClassRelations(service.getClass(), contract, "failed to instantiate service '" + archive.getServiceInfo().getPublicId() + "'; declared service " + service.getClass().getName() + " does not implement " + contract.getName());
			}
		} else {
			log.debug("Service '" + archive.getPublicId() + "' declared as a proxy.");
			checkIsInvocationHandler(service);
		}
		/*
		 * Assign contract classes
		 */
		this.contractClasses = (Class<? extends Contract>[])contracts;
		/*
		 * If this is a proxy, the invocation handler adapter will forward
		 * calls to it instead of invoking.
		 */
		InvocationHandler rootHandler = new InvocationHandlerAdapter(service);
		/*
		 * Check legacy context status
		 */
		if(useLegacyContextClassLoader()) {
			log.warn("Service '" + archive.getPublicId() + "' instantiated with legacy thread context class loading! Context class loaders and JNDI will *not* be set correctly!");
		} else {
			log.debug("Service '" + archive.getPublicId() + "' proxied for correct thread context class loaders");
			rootHandler = new ContextClassLoaderInvocationHandler(archive.getServiceClassLoader(), rootHandler);
		}
		if(!(service instanceof JndiProvider)) {
			/*
			 * Wrap for JNDI (but exclude the provider itself), we're wrapping the real service to defer 
			 * access until everything is properly started...
			 */
			rootHandler = new JndiWrappingInvocationHandler(rootHandler, new JndiProvider() {
				
				private AtomicReference<JndiProvider> ref = new AtomicReference<JndiProvider>();
				
				@Override
				public <T extends Throwable> Object wrapInvocation(InvocationFacade<T> facade) throws T {
					JndiProvider provider = tryGetProvider();
					if(provider == null) {
						return facade.invoke();
					} else {
						return provider.wrapInvocation(facade);
					}
				}
	
				private JndiProvider tryGetProvider() {
					JndiProvider provider = ref.get();
					if(provider == null) {
						provider = containingServiceRegistry.getServiceInstance(JndiProvider.class);
						if(provider != null) {
							ref.set(provider);
						}
					}
					return provider;
				}
			});
		}
		/*
		 * Create proxy instances
		 */
		this.service = newProxy(getServiceInterfaces(archive.getPublicId(), service), rootHandler, null);
		this.contract = newProxy(contractClasses, rootHandler, null);
	}

	private Class<?>[] getServiceInterfaces(String publicId, Object service) {
		/*
		 * For now, there's only the Service interface and the 
		 * routable service to worry about...
		 */
		if(service instanceof RoutableService) {
			log.debug("Service '" + publicId + "' interfaces: Service, RoutableService");
			return new Class[] { Service.class, RoutableService.class };
		} else {
			log.debug("Service '" + publicId + "' interfaces: Service");
			return new Class[] { Service.class };
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T newProxy(Class<?>[] contracts, InvocationHandler handler, ClassLoader parent) {
		ClassLoader serviceLoader = (parent == null ? archive.getServiceClassLoader() : parent);
		return (T) Proxy.newProxyInstance(serviceLoader, contracts, handler);
	}
	
	private void checkIsInvocationHandler(Object service) {
		if(!(service instanceof InvocationHandler)) {
			throw new IllegalStateException("Service '" + archive.getPublicId() + "' declared as a proxy, but does not implement InvocationHandler.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void checkClassRelations(Class child, Class parent, String errMsg) {
		if(!parent.isAssignableFrom(child)) {
			throw new IllegalStateException(errMsg);
		}
	}
}