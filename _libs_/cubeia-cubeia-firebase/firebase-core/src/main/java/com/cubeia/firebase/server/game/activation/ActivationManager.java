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
package com.cubeia.firebase.server.game.activation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.DefaultActivator;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.game.table.comm.TableCreationRequest;
import com.cubeia.firebase.mtt.comm.MttCommand;
import com.cubeia.firebase.server.activation.ActivatorCommand;
import com.cubeia.firebase.server.activation.ActivatorConfig;
import com.cubeia.firebase.server.activation.ActivatorSender;
import com.cubeia.firebase.server.activation.ActivatorUtil;
import com.cubeia.firebase.server.commands.CommandHandoffListener;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.node.GameNodeContext;
import com.cubeia.firebase.server.routing.impl.InternalComponentRouter;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.server.routing.impl.ThreadedActivatorRouterImpl;
import com.cubeia.firebase.server.service.InternalServiceRegistry;
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
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;

/*
 * TODO: Messy class... Refactor
 */
public class ActivationManager implements Initializable<GameNodeContext> {
	
	private static enum ContextAction {
		START,
		STOP,
		DESTROY
	}
	
	// private static final Namespace ACTIVATOR_CONF_NS = new Namespace("activator.game");
	private transient Logger log = Logger.getLogger(getClass());
	
	private final GameObjectIdSysStateMapper sysMapper;
	private final Map<Integer, ActivationBean> map = new HashMap<Integer, ActivationBean>();
	private final SystemStateServiceContract state;
	private final ActivationConfigManager config;
	
	private GameNodeContext con;
	private TableFactory<FirebaseTable> tableFactory;
	
	private final Listener listener = new Listener();
	private final boolean haltOnInitError;

	private final JndiProvider jndiProvider;
	 
	public ActivationManager(boolean haltOnInitError) {
		this.haltOnInitError = haltOnInitError;
		InternalServiceRegistry registry = InternalComponentAccess.getRegistry();
		jndiProvider = registry.getServiceInstance(JndiProvider.class);
		config = registry.getServiceInstance(ActivationConfigManager.class);
		state = registry.getServiceInstance(SystemStateServiceContract.class);
		sysMapper = new GameObjectIdSysStateMapper(new SysStateIdMapperMemory(state, SystemStateConstants.TABLE_IDGENERATION_FQN));
		TableSpaceServiceContract gos = registry.getServiceInstance(TableSpaceServiceContract.class);
		tableFactory = gos.getTableFactory(FirebaseTable.class);
	}
	
	public void destroy() {
		destroyComm();
		stopActivators();
		destroyActivators();
		listener.destroy();
		map.clear();
	}

	public void init(GameNodeContext con) throws SystemException {
		this.con = con;
		populateActivators();
		initActivators();
		startActivators();
		initComm();
	}
	

	// --- PRIVATE METHODS --- //
	
	private void handleCommand(TableCreationRequest req) {
		int gameId = req.getGameId();
		ActivationBean bean = map.get(gameId);
		if(bean != null) {
			if(bean.isCreationAware()) {
				Sender<ClientEvent<?>> clientSender = con.getNodeRouter().getClientEventSender();
				try {
					createInvoker(bean).handleCommand(req, clientSender);
				} catch (ChannelNotFoundException e) {
					log.error("Failed to dispatch response for table creation request! Game id: " + gameId, e);
				}
			} else {
				log.error("Activation manager received a table creation request for a game; But the game activator does not support table creation requests; Game id: " + gameId);
			}
		} else {
			log.error("Activation manager recevied a table creation request for a non existing game; Erronous id: " + gameId);
		}
	}
	
	private void handleCommand(ActivatorCommand com) {
		int gameId = com.getId();
		final ActivationBean bean = map.get(gameId);
		if(bean != null) {
			if(bean.isRoutable()) {
				((DefContext)bean.context).dispatch(com);
			} else {
				log.error("Activation manager received an activator action for a game; But the game activator does not implement RoutableActivator; Game id: " + gameId);
			}
		} else {
			log.error("Activation manager recevied an activator action for a non existing game; Erronous id: " + gameId);
		}
	}
	
	private void handleCommand(MttCommand<?> comm) {
		int gameId = comm.getGameId();
		ActivationBean bean = map.get(gameId);
		if(bean != null) {
			if(bean.isMttAware()) {
				try {
					createInvoker(bean).handleCommand(comm);
				} catch (ChannelNotFoundException e) {
					log.error("Failed to dispatch response for table creation request! Game id: " + gameId + "; MTT id: " + comm.getMttId(), e);
				}
			} else {
				log.error("Activation manager received an MTT command for a game; But the game activator does not support MTTs; Game id: " + gameId);
			}
		} else {
			log.error("Activation manager recevied an MTT command for a non existing game; Erronous id: " + gameId);
		}
	}

	private CommandAwareInvokator createInvoker(ActivationBean bean) {
		return new CommandAwareInvokator((DefaultTableFactory)bean.context.getTableFactory(), this.tableFactory, con.getNodeRouter().getMttSender());
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
	
	private void stopActivators() {
		for (ActivationBean a : map.values()) {
			((DefContext)a.context).stop();
		}
	}

	private void destroyActivators() {
		for (ActivationBean a : map.values()) {
			((DefContext)a.context).destroy();
		}
	}
	
	private void initActivators() throws SystemException {
		for (ActivationBean a : map.values()) {
			DefContext context = new DefContext(a);
			try {
				context.init();
			} catch (SystemException e) {
				GameDefinition def = a.def;
				String msg = "Failed initiate actiavtor for game " + def.getName() + " (" + def.getId() + ")";
				log.fatal(msg, e);
				if(haltOnInitError) {
					throw new SystemCoreException(msg, e);
				}
			}
		}
	}

	private void startActivators() {
		for (ActivationBean a : map.values()) {
			((DefContext)a.context).start();
		}		
	}

	private void populateActivators() throws SystemException {
		ServiceRegistry services = con.getServices();
		DeploymentServiceContract service = services.getServiceInstance(DeploymentServiceContract.class);
		DeploymentManager deploymentManager = service.getDeploymentManager();
		Map<String, GameDeployment> all = deploymentManager.getAllGameDeployments();
		for (GameDeployment dep : all.values()) {
			GameRevision rev = dep.getLatestRevision();
			GameDefinition def = rev.getGameDefinition();
			String clazz = def.getActivator();
			GameActivator a = checkCreate(rev, def, clazz);
			String depName = dep.getName();
			map.put(def.getId(), new ActivationBean(depName, a, def, rev));
		}
	}

	private GameActivator checkCreate(GameRevision rev, GameDefinition def, String clazz) throws SystemCoreException {
		if(clazz == null || clazz.length() == 0) clazz = DefaultActivator.class.getName();
		try {
			Class<?> cl = rev.getRevisionClassLoader().loadClass(clazz);
			if(!GameActivator.class.isAssignableFrom(cl)) throw new SystemCoreException("Failed to load game activator for game '" + def.getName() + "'; Class name in descriptor '" + clazz + "' not instance of GameActivator.");
			return (GameActivator)cl.newInstance();
		} catch (ClassNotFoundException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for game '" + def.getName() + "'; Class not found.", e);
		} catch (InstantiationException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for game '" + def.getName() + "'; Received message: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new SystemCoreException("Failed to load class activator '" + clazz + "' for game '" + def.getName() + "'; Received message: " + e.getMessage(), e);
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private final class Listener {
		
		private final CommandHandoffListener handoff;
		
		private Listener() {
			handoff = new CommandHandoffListener(new CommandListener() {
			
				public Object commandReceived(CommandMessage c) {
					if(c.command instanceof MttCommand<?>) {
						MttCommand<?> com = (MttCommand<?>)c.command;
						if(com.isRequest()) {
							handleCommand(com);
						}
					} else if(c.command instanceof TableCreationRequest) {
						TableCreationRequest req = (TableCreationRequest)c.command;
						handleCommand(req);
					} else if(c.command instanceof ActivatorCommand) {
						ActivatorCommand com = (ActivatorCommand)c.command;
						if(com.isForGame()) {
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
	
	private class DefContext implements ActivatorContext {

		private final ActivationBean activator;
		private final ServiceRegistry publicRegistry;
		private final DefaultTableFactory fact;
		
		private ThreadedActivatorRouterImpl router;
		
		private ActivatorSender sender;
		private final AtomicReference<ConfigSourceListener> list;
		private InternalComponentRouter internalRouter;
		
		private DefContext(ActivationBean activator) {
			this.list = new AtomicReference<ConfigSourceListener>();
			publicRegistry = new PublicServiceRegistry(InternalComponentAccess.getRegistry());
			ClientRegistryServiceContract clientReg = InternalComponentAccess.getRegistry().getServiceInstance(ClientRegistryServiceContract.class);
			fact = new DefaultTableFactory(activator, sysMapper, state, clientReg, tableFactory, getClusterConnection());
			this.activator = activator;
			setupListener();
		}
		
		public int getGameId() {
			return activator.def.getId();
		}
		
		public void stop() {
			wrappedAction(ContextAction.STOP);
		}
		
		private void start() {
			wrappedAction(ContextAction.START);
		}
		
		private void destroy() {
			wrappedAction(ContextAction.DESTROY);
			destroyActivatorRouter();
			destroyActivatorSender();
		}

		private void destroyActivatorSender() {
			if(sender != null) {
				sender.destroy();
			}
		}

		private void init() throws SystemException {
			seutpActivatorRouter();
			checkSetupActivatorSender();
			activator.context = this;
			jndiProvider.wrapInvocation(new InvocationFacade<SystemException>() {
				@Override
				public Object invoke() throws SystemException {
					return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<SystemException>() {
						
						@Override
						public Object invoke() throws SystemException {
							activator.act.init(DefContext.this);
							return null;
						}
					}, activator.act.getClass().getClassLoader());
				}
			});
		}
		
		private void wrappedAction(final ContextAction action) {
			jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
				@Override
				public Object invoke() {
					return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
						
						@Override
						public Object invoke() {
							switch(action) {
								case START : {
									activator.act.start();
									break;
								}
								case STOP : {
									activator.act.stop();
									break;
								}
								case DESTROY : {
									activator.act.destroy();
									break;
								}
							}
							return null;
						}
					}, activator.act.getClass().getClassLoader());
				}
			});
		}
		
		private void dispatch(ActivatorCommand com) {
			if(sender != null) {
				sender.dispatch(com);
			} else {
				log.error("Activator not instanceof RoutableActivator");
			}
		}

		private void checkSetupActivatorSender() {
			if(activator.act instanceof RoutableActivator) {
				sender = new ActivatorSender(activator.def.getId(), newRouterContext(), (RoutableActivator)activator.act, true, activator.rev.getRevisionClassLoader());
			}
		}

		private void destroyActivatorRouter() {
			internalRouter.destroy();
			router.stop();
		}
		
		private void seutpActivatorRouter() throws SystemException {
			internalRouter = createInternalRouter();
			ThreadPoolProperties props = createRouterThreadProps();
			router = new ThreadedActivatorRouterImpl(activator.def.getId(), props, internalRouter, true);
		}

		private ThreadPoolProperties createRouterThreadProps() {
			ActivatorConfig conf = ActivatorUtil.createConfig(con.getServices(), false);
			return conf.getRouterPoolProperties();
		}

		private InternalComponentRouter createInternalRouter() throws SystemException {
			InternalComponentRouter rout = new InternalComponentRouter("gameActivatorRouter", "Game Activator Router");
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
		
		public ActivatorRouter getActivatorRouter() {
			return router;
		}

		public ConfigSource getConfigSource() {
			return config.getConfigSourceFor(activator.deploymentName, ActivationType.GAR);
		}

		public ServiceRegistry getServices() {
			return publicRegistry;
		}

		public DefaultTableFactory getTableFactory() {
			return fact;
		}

		public void setConfigSourceListener(ConfigSourceListener list) {
			this.list.set(list);
		}	
		
		// --- PRIVATE METHODS --- //
		
		private void setupListener() {
			config.addConfigSourceListener(new ConfigSourceListener() {
			
				public void sourceRemoved(ConfigSource src) {
					if(src.getName().equals(activator.deploymentName) && ((ActivationConfigSource)src).getType() == ActivationType.GAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceRemoved(src);
						}
					}
				}
			
				public void sourceModified(ConfigSource src) {
					if(src.getName().equals(activator.deploymentName) && ((ActivationConfigSource)src).getType() == ActivationType.GAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceModified(src);
						}
					}
				}
			
				public void sourceAdded(ConfigSource src) {
					if(src.getName().equals(activator.deploymentName) && ((ActivationConfigSource)src).getType() == ActivationType.GAR) {
						ConfigSourceListener listener = list.get();
						if(listener != null) {
							listener.sourceAdded(src);
						}
					}
				}
			});
		}
	}
}
