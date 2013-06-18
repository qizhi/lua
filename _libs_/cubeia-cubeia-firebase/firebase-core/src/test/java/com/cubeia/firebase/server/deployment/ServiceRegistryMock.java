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
package com.cubeia.firebase.server.deployment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.cubeia.firebase.api.service.datasource.DatasourceListener;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;
import com.cubeia.firebase.service.activation.ActivationConfigManager;
import com.cubeia.firebase.service.activation.ActivationConfigSource;
import com.cubeia.firebase.service.activation.ActivationType;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.TxType;
import com.cubeia.firebase.service.datasource.intern.InternalDataSourceProvider;
import com.cubeia.firebase.util.ServiceRegistryAdapter;

public class ServiceRegistryMock extends ServiceRegistryAdapter {

	public ServiceRegistryMock() {
		super.addContract(InternalDataSourceProvider.class, new DSProvicerMock());
		// super.addContract(PersistenceServiceContract.class, new PARServiceMock());
		super.addContract(SystemStateServiceContract.class, new SysStateService());
		super.addContract(ActivationConfigManager.class, new AConfManager());
	}
	
	
	// --- INNER CLASSES --- //
	
	private static class AConfManager implements ActivationConfigManager {

		public void addConfigSourceListener(ConfigSourceListener list) {
			// TODO Auto-generated method stub 
			
		}

		public ActivationConfigSource getConfigSourceFor(String gameName,
				ActivationType type) {
			// TODO Auto-generated method stub
			return null;
		}

		public void registerConfigSource(ConfigSource src, ActivationType type) {
			// TODO Auto-generated method stub
			
		}

		public void removeConfigSourceListener(ConfigSourceListener list) {
			// TODO Auto-generated method stub
			
		}

		public void unregisterConfigSource(String gameName, ActivationType type) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private static class SysStateService implements SystemStateServiceContract {

		public boolean exists(String fqn) {
			// TODO Auto-generated method stub
			return false;
		}

		public Object getAttribute(String fqn, String attribute) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void setAttribute(String fqn, String attribute, Object value,
				boolean doAsynch) {
			// TODO Auto-generated method stub
			
		}
		
		public void setAttributes(String fqn, Map<String, Object> attributes,
				boolean doAsynch) {
			// TODO Auto-generated method stub
			
		}

		public Map<Object, Object> getAttributes(String fqn) {
			// TODO Auto-generated method stub
			return null;
		}

		public SystemStateCacheHandler getCacheHandler() {
			// TODO Auto-generated method stub
			return null;
		}

		public Set<String> getChildren(String fqn) {
			// TODO Auto-generated method stub
			return null;
		}

		public Set<String> getEndNodes(String address) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasNode(String fqn) {
			// TODO Auto-generated method stub
			return false;
		}

		public void printAllData() {
			// TODO Auto-generated method stub
			
		}

		public void removeAttribute(String fqn, String attr) {
			// TODO Auto-generated method stub
			
		}

		public void removeNode(String fqn) {
			// TODO Auto-generated method stub
			
		}

		public void setAttribute(String fqn, String attribute, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void setAttributes(String fqn, Map<String, Object> attributes) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	/*private static class PARServiceMock implements PersistenceServiceContract {
		
		public PersistenceManager getPersistenceManager() {
			return new PersistenceManager() {
			
				public void stop() {
					// TODO Auto-generated method stub
			
				}
			
				public void start() {
					// TODO Auto-generated method stub
			
				}
			
				public void registerPersistenceUnits(URL persistenceUrl, URL jarUrl,
						ClassLoader revisionClassLoader)
						throws PersistenceDeploymentFailedException {
					// TODO Auto-generated method stub
			
				}
			
				public void redeployDatasource(String datasource)
						throws PersistenceDeploymentFailedException {
					// TODO Auto-generated method stub
			
				}
			
				public void redeploy(URL persistenceUrl, URL jarUrl)
						throws PersistenceDeploymentFailedException {
					// TODO Auto-generated method stub
			
				}
			
				public boolean isReady(String name) {
					// TODO Auto-generated method stub
					return false;
				}
			
				public EntityManager getEntityManager(String name) {
					// TODO Auto-generated method stub
					return null;
				}
			
				public EntityManager getEntityManager() {
					// TODO Auto-generated method stub
					return null;
				}
			
				public boolean exists(String name) {
					// TODO Auto-generated method stub
					return false;
				}
			
			};
		}
	}*/
	
	private static class DSProvicerMock implements InternalDataSourceProvider {

		public DatasourceManager getDatasourceManager() {
			return new DatasourceManager() {
			
				public void stop() {
					// TODO Auto-generated method stub
			
				}
				
				@Override
				public void addDatasourceListener(DatasourceListener l) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public List<String> getDatasources() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public void removeDatasourceListener(DatasourceListener l) {
					// TODO Auto-generated method stub
					
				}
			
				public void start() {
					// TODO Auto-generated method stub
			
				}
			
				public void redeploy(String name, DataSource source, TxType type) {
					// TODO Auto-generated method stub
			
				}
			
				public DataSource getSystemDatasource() {
					// TODO Auto-generated method stub
					return null;
				}
			
				public DataSource getDatasource(String name) {
					// TODO Auto-generated method stub
					return null;
				}
			
				public boolean exists(String name) {
					// TODO Auto-generated method stub
					return false;
				}
			
				public void addDatasource(String name, DataSource source, TxType type) {
					// TODO Auto-generated method stub
			
				}
				
				public TxType getDatasourceType(String name) {
					// TODO Auto-generated method stub
					return null;
				}
			
			};
		}
	}
}
