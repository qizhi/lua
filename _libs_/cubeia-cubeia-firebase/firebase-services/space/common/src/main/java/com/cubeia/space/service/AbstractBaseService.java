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
package com.cubeia.space.service;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.game.table.trans.TransactionalTableFactory;
import com.cubeia.firebase.mtt.state.MttStateFactory;
import com.cubeia.firebase.mtt.state.trans.DefaultMttStateFactory;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.space.handler.mtt.MTTHandler;
import com.cubeia.space.handler.table.TableHandler;

public abstract class AbstractBaseService implements Service, TableSpaceServiceContract {
	
	public static final String SPACE_NAMESPACE = "service.space";
	public static final String TABLE_SPACE_NAMESPACE = SPACE_NAMESPACE + ".tablespace";
	public static final String MTT_SPACE_NAMESPACE = SPACE_NAMESPACE + ".mttspace";
	
	public static final Class<?>[] TYPES = { TableHandler.class, MTTHandler.class };

	protected final Logger log = Logger.getLogger(getClass());
	
	protected MttStateFactory mttFactory;
	protected TableFactory<FirebaseTable> tableFactory;
	protected GameObjectSpace<FirebaseTable, GameAction> tableCache;
	protected GameObjectSpace<TransactionalMttState, MttAction> mttCache;
	
	protected TableSpaceServiceConfig config;
	protected ServiceContext con;
	
	@Override
	public final void init(ServiceContext con) throws SystemException {
		this.con = con;
		initConfig();
		setupFactories();
		initCaches();
	}

	@Override
	public Class<?>[] getAvailableTypes() {
		return TYPES;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Table> TableFactory<T> getTableFactory(Class<T> type) {
		Arguments.notNull(type, "type");
		if(!FirebaseTable.class.equals(type)) return null;
		return (TableFactory<T>) tableFactory;
	}
	
	@Override
	public MttStateFactory getMttFactory() {
		return mttFactory;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Identifiable, E extends Action> GameObjectSpace<T, E> getObjectSpace(Class<T> type, Class<E> actionType) {
		Arguments.notNull(type, "type");
		if(FirebaseTable.class.equals(type) && GameAction.class.equals(actionType)) {
			return (GameObjectSpace<T, E>) tableCache;
		} else if(TransactionalMttState.class.equals(type) && MttAction.class.equals(actionType)) {
			return (GameObjectSpace<T, E>) mttCache;
		} else {
			log.warn("You tried to access a space for the non-defined class: "+type+" and action: "+actionType);
			return null;
		}
	}

	/**
	 * This method is called in initialisation and subclasses should create both
	 * object cashes when it's called.
	 */
	protected abstract void initCaches() throws SystemException;
	
	protected CoreTransactionManager getTransactionManager() {
		return con.getParentRegistry().getServiceInstance(CoreTransactionManager.class);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupFactories() {
		mttFactory = new DefaultMttStateFactory(getTransactionManager(), config.getMttSpaceObjectWarnSize());
		tableFactory = new TransactionalTableFactory(getTransactionManager(), config.getTableSpaceObjectWarnSize());		
	}
	
	private void initConfig() throws SystemCoreException {
		ClusterConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		if(contr == null) throw new SystemCoreException("Failed service dependencies; Could not find server configuration service '" + Constants.SERVER_CONFIG_SERVICE_NS + "'.");
		config = contr.getConfiguration(TableSpaceServiceConfig.class, new Namespace(SPACE_NAMESPACE));
	}
}
