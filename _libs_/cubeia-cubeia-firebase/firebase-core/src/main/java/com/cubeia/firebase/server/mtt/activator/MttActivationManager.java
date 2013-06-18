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
package com.cubeia.firebase.server.mtt.activator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.game.activator.DefaultActivator;
import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.api.mtt.activator.ActivatorContext;
import com.cubeia.firebase.api.mtt.activator.MttActivator;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;
import com.cubeia.firebase.server.activation.ActivatorCommand;
import com.cubeia.firebase.server.activation.ActivatorConfig;
import com.cubeia.firebase.server.activation.ActivatorSender;
import com.cubeia.firebase.server.commands.CommandHandoffListener;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.mtt.MttFactoryImpl;
import com.cubeia.firebase.server.mtt.MttNodeContext;
import com.cubeia.firebase.server.routing.impl.InternalComponentRouter;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.server.routing.impl.ThreadedActivatorRouterImpl;
import com.cubeia.firebase.server.service.PublicServiceRegistry;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.util.GameObjectIdSysStateMapper;
import com.cubeia.firebase.server.util.SysStateIdMapperMemory;
import com.cubeia.firebase.server.util.ThreadPoolProperties;
import com.cubeia.firebase.service.activation.ActivationConfigManager;
import com.cubeia.firebase.service.activation.ActivationConfigSource;
import com.cubeia.firebase.service.activation.ActivationType;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;

public class MttActivationManager implements Initializable<MttNodeContext> {
	
	private static final Namespace ACTIVATOR_CONF_NS = new Namespace("activator.mtt");
	private static final Logger log = Logger.getLogger(MttActivationManager.class);
	
	/** Reference to the service registry */
	private ServiceRegistry services;
	
	/** Holds all activators */
	private Map<Integer, ActivationBean> activators = new ConcurrentHashMap<Integer, ActivationBean>();

	private Listener listener = new Listener();
	
	private ActivationConfigManager config;
	private GameObjectIdSysStateMapper idGenerator;

	private MttFactoryImpl factory;
	private MttNodeContext con;
	private JndiProvider jndiProvider;

	
	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/
	
	
	
	
	/*------------------------------------------------
	 
	 	LIFE CYCLE METHODS
	  
	 ------------------------------------------------*/
	
	public void init(MttNodeContext con) throws SystemException {
		this.con = con;
		services = con.getServices();
		jndiProvider = services.getServiceInstance(JndiProvider.class);
		SystemStateServiceContract sysState = services.getServiceInstance(SystemStateServiceContract.class);
		config = services.getServiceInstance(ActivationConfigManager.class);
		idGenerator = new GameObjectIdSysStateMapper(new SysStateIdMapperMemory(sysState, SystemStateConstants.MTT_IDGENERATION_FQN));
		factory = new MttFactoryImpl(services, idGenerator);
		initComm();
		populateActivators();
		initAndStartActivators();
	}

	public void destroy() {
		destroyComm();
		for (ActivationBean a : activators.values()) {
			final ActivationBean bean = a;
			jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
				@Override
				public Object invoke() throws RuntimeException {
					return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
						
						@Override
						public Object invoke() throws RuntimeException {
							bean.getActivator().stop();
							bean.getActivator().destroy();
							return null;
						}
					}, bean.getActivator().getClass().getClassLoader());
				}
			});
			DefContext con = (DefContext)a.getContext();
			con.destroy();
		}
		factory.destroy();
		listener.destroy();
		services = null;
	}

	
	
	
	/*------------------------------------------------
	 
	 	PRIVATE METHODS
	  
	 ------------------------------------------------*/
	
	/**
	 * Create activators for all deployed MTTs
	 */
	private void populateActivators() throws SystemException {
		DeploymentServiceContract service = services.getServiceInstance(DeploymentServiceContract.class);
		DeploymentManager deploymentManager = service.getDeploymentManager();
		Map<Integer, TournamentDeployment> all = deploymentManager.getAllTournamentDeployments();
		for (TournamentDeployment dep : all.values()) {
			TournamentRevision rev = dep.getLatestRevision();
			TournamentDefinition def = rev.getTournamentDefinition();
			String clazz = def.getActivator();
			
			// Create the MTT Activator and inject the mtt factory.
			final MttActivator a = checkCreate(rev, def, clazz);
			jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
				@Override
				public Object invoke() throws RuntimeException {
					return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
						
						@Override
						public Object invoke() throws RuntimeException {
							a.setMttFactory(factory);
							return null;
						}
					}, a.getClass().getClassLoader());
				}
			});
			
			String depName = dep.getName();
			activators.put(def.getId(), new ActivationBean(depName, a, rev, def));
		}
	}
	
	
	private MttActivator checkCreate(TournamentRevision rev, TournamentDefinition def, String clazz) throws SystemCoreException {
		if(clazz == null || clazz.length() == 0) clazz = DefaultActivator.class.getName();
		try {
			Class<?> cl = rev.getRevisionClassLoader().loadClass(clazz);
			if(!MttActivator.class.isAssignableFrom(cl)) throw new SystemCoreException("Failed to load mtt activator for game '" + def.getName() + "'; Class name in descriptor '" + clazz + "' not instance of MttActivator.");
			return (MttActivator)cl.newInstance();
		} catch (ClassNotFoundException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for mtt '" + def.getName() + "'; Class not found.", e);
		} catch (InstantiationException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for mtt '" + def.getName() + "'; Received message: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for mtt '" + def.getName() + "'; Received message: " + e.getMessage(), e);
		}
	}
	
	
	/**
	 * Call both init and start for all activators.
	 * Sets context classloader before calls and reverts the context classloader after.
	 *
	 */
	private void initAndStartActivators() {
		for (ActivationBean a : activators.values()) {
			try {
				final ActivationBean bean = a;
				final DefContext con = new DefContext(a, a.getDef().getId());
				a.setContext(con);
				con.init();
				jndiProvider.wrapInvocation(new InvocationFacade<SystemException>() {
					@Override
					public Object invoke() throws SystemException {
						return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<SystemException>() {
							
							@Override
							public Object invoke() throws SystemException {
								bean.getActivator().init(con);
								bean.getActivator().start();
								return null;
							}
						}, bean.getActivator().getClass().getClassLoader());
					}
				});
			} catch (Throwable th) {
				log.error("Could not initialize mtt activator: "+a, th);
			}
		}
	}
	
	private void handleCommand(ActivatorCommand com) {
		int id = com.getId();
		ActivationBean bean = activators.get(id);
		if(bean != null) {
			if(bean.isRoutable()) {
				((DefContext)bean.getContext()).dispatch(com);
			} else {
				log.error("Activation manager received an activator action for a tournament; But the tournament activator does not implement RoutableActivator; Tournament id: " + id);
			}
		} else {
			log.error("Activation manager recevied an activator action for a non existing tournament; Erronous id: " + id);
		}
	}
	
	private void initComm() {
		listener.attach(getClusterConnection());
	}
	
	private ClusterConnection getClusterConnection() {
		ConnectionServiceContract service = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		return service.getSharedConnection();
	}

	private void destroyComm() {
		listener.detach(getClusterConnection());
	}
	
	
	// --- INNER CLASSES --- //
	
	private class DefContext implements ActivatorContext {

		private final ActivationBean activator;
		private final ServiceRegistry publicRegistry;
		private final AtomicReference<ConfigSourceListener> list;
		private final int mttId;
		
		private ThreadedActivatorRouterImpl router;
		private InternalComponentRouter internalRouter;
		private ActivatorSender sender;
		
		private DefContext(ActivationBean activator, int mttId) {
			this.mttId = mttId;
			this.list = new AtomicReference<ConfigSourceListener>();
			publicRegistry = new PublicServiceRegistry(InternalComponentAccess.getRegistry());
			this.activator = activator;
			setupListener();
		}
		
		private void init() throws SystemException {
			seutpActivatorRouter();
			checkSetupActivatorSender();
		}
		
		private void destroy() {
			destroyActivatorRouter();
			destroyActivatorSender();
		}
		
		private void destroyActivatorSender() {
			if(sender != null) {
				sender.destroy();
			}
		}
		
		private void dispatch(ActivatorCommand com) {
			if(sender != null) {
				sender.dispatch(com);
			} else {
				log.error("Activator not instanceof RoutableActivator");
			}
		}

		private void checkSetupActivatorSender() {
			if(activator.getActivator() instanceof RoutableActivator) {
				sender = new ActivatorSender(activator.getDef().getId(), newRouterContext(), (RoutableActivator)activator.getActivator(), true, activator.getRev().getRevisionClassLoader());
			}
		}
		
		private void destroyActivatorRouter() {
			internalRouter.destroy();
			router.stop();
		}
		
		private void seutpActivatorRouter() throws SystemException {
			internalRouter = createInternalRouter();
			ThreadPoolProperties props = createRouterThreadProps();
			router = new ThreadedActivatorRouterImpl(activator.getDef().getId(), props, internalRouter, false);
		}
		
		private ThreadPoolProperties createRouterThreadProps() {
			ClusterConfigProviderContract prov = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
			ActivatorConfig conf = prov.getConfiguration(ActivatorConfig.class, ACTIVATOR_CONF_NS);
			return conf.getRouterPoolProperties();
		}

		private InternalComponentRouter createInternalRouter() throws SystemException {
			InternalComponentRouter rout = new InternalComponentRouter("mttActivatorRouter", "MTT Activator Router");
			rout.init(newRouterContext());
			return rout;
		}
		
		private RouterContext newRouterContext() {
			return new RouterContext() {
				
				public String getServerId() {
					return con.getServerId().id;
				}
			
				public MBusContract getMessageBus() {
					return con.getServices().getServiceInstance(MBusContract.class);
				}
				
				public ServiceRegistry getServices() {
					return con.getServices();
				}
			
				public MBeanServer getMBeanServer() {
					return con.getMBeanServer();
				}
			
			};
		}
		
		public int getMttId() {
			return mttId;
		}
		
		public ConfigSource getConfigSource() {
			return config.getConfigSourceFor(activator.getName(), ActivationType.TAR);
		}

		public ServiceRegistry getServices() {
			return publicRegistry;
		}

		public void setConfigSourceListener(ConfigSourceListener list) {
			this.list.set(list);
		}	
		
		public ActivatorRouter getActivatorRouter() {
			return router;
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private void setupListener() {
			config.addConfigSourceListener(new ConfigSourceListener() {
			
				public void sourceRemoved(ConfigSource src) {
					if(src.getName().equals(activator.getName()) && ((ActivationConfigSource)src).getType() == ActivationType.TAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceRemoved(src);
						}
					}
				}
			
				public void sourceModified(ConfigSource src) {
					if(src.getName().equals(activator.getName()) && ((ActivationConfigSource)src).getType() == ActivationType.TAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceModified(src);
						}
					}
				}
			
				public void sourceAdded(ConfigSource src) {
					if(src.getName().equals(activator.getName()) && ((ActivationConfigSource)src).getType() == ActivationType.TAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceAdded(src);
						}
					}
				}
			});
		}
	}
	
	private final class Listener {
		
		private final CommandHandoffListener handoff;
		
		private Listener() {
			handoff = new CommandHandoffListener(new CommandListener() {
			
				public Object commandReceived(CommandMessage c) {
					if(c.command instanceof ActivatorCommand) {
						ActivatorCommand com = (ActivatorCommand)c.command;
						if(!com.isForGame()) {
							handleCommand(com);
						}
					}
					return null;
				}
			});
		}

		private void destroy() {
			handoff.destroy();
		}
		
		private void attach(ClusterConnection con) {
			con.getCommandReceiver().addCommandListener(handoff);
		}
		
		private void detach(ClusterConnection con) {
			con.getCommandReceiver().removeCommandListener(handoff);
		}
	}
}
