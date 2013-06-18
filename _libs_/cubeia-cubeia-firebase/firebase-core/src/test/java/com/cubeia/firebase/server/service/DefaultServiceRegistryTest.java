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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mockito;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.UnmodifiableList;
import com.cubeia.firebase.server.service.jmx.RegistryStats;

public class DefaultServiceRegistryTest extends TestCase {

	private DefaultServiceRegistry reg;
	private DefaultServiceRegistryContext context; // mocked
	private DummyLoader archiveLoader;
	
	@Override
	protected void setUp() throws Exception {
		archiveLoader = new DummyLoader();
		reg = new DefaultServiceRegistry();
		reg.setArchiveLoader(archiveLoader);
		reg.setRegistryStats(mock(RegistryStats.class));
		reg.setRequireConfigDeps(false);
		context = Mockito.mock(DefaultServiceRegistryContext.class);
		reg.init(context);
	}
	
	public void testListPrivateSerices() throws Exception {
		List<ServiceInfo> list = reg.listServicesByContract(ClusterConfigProviderContract.class);
		assertEquals(2, list.size());
		assertEquals("config1", list.get(0).getPublicId());
		assertEquals("config2", list.get(1).getPublicId());
	}
	
	public void testListPublicSerices() throws Exception {
		List<ServiceInfo> list = reg.listPublicServices(ClusterConfigProviderContract.class);
		assertEquals(1, list.size());
		assertEquals("config1", list.get(0).getPublicId());
	}
	
	public void testListPrivateByAnnotation() throws Exception {
		List<ServiceInfo> list = reg.listServicesByAnnotation(Configurated.class);
		assertEquals(1, list.size());
		assertEquals("config3", list.get(0).getPublicId());
		// Only annotations on interfaces for now...
		// list = reg.listServicesByAnnotation(Entity.class);
		// assertEquals(0, list.size());
	}
	
	@Override
	protected void tearDown() throws Exception {
		reg.destroy();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private <T extends Contract, S extends Service> ServiceArchive createMockArchive(String id, Class<T> contract, S service, boolean isPublic) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ServiceArchive mock = mock(ServiceArchive.class);
		when(mock.getPublicId()).thenReturn(id);
		InternalServiceInfo info = mock(InternalServiceInfo.class);
		when(info.getPublicId()).thenReturn(id);
		when(info.getContractClasses()).thenReturn(new String[] { contract.getName() });
		when(mock.getServiceInfo()).thenReturn(info);
		when(info.getDependencies()).thenReturn(new UnmodifiableList<Dependency>() {
			
			private List<Dependency> list = Collections.emptyList();
			
			@Override
			public Iterator<Dependency> iterator() {
				return list.iterator();
			}
			
			@Override
			public int size() {
				return list.size();
			}
			
			@Override
			public Dependency get(int index) {
				return list.get(index);
			}
		});
		when(mock.getServiceClassLoader()).thenReturn(getClass().getClassLoader());
		when(mock.instantiateContracts()).thenReturn(new Class[] { contract });
		when(mock.instantiateService()).thenReturn(service);
		when(mock.isPublic()).thenReturn(isPublic);
		return mock;
	}
	
	// --- PRIVATE CLASSES --- //
	
	private class DummyLoader implements ArchiveLoader {
		
		private List<ServiceArchive> archives = new LinkedList<ServiceArchive>();
		
		public DummyLoader() throws Exception {
			// two cluster config services, one public and one private...
			archives.add(createMockArchive("config1", ClusterConfigProviderContract.class, mock(DummyClusterConfigService.class), true));
			archives.add(createMockArchive("config2", ClusterConfigProviderContract.class, mock(DummyClusterConfigService.class), false));
			// two service config services, with different annotations
			archives.add(createMockArchive("config3", DummyServerConfigService.class, new DummyServerConfigServiceImpl(), false));
		} 
		
		@Override
		public void destroy() { }
		
		@Override
		public ServiceArchive[] getServices() {
			return archives.toArray(new ServiceArchive[archives.size()]);
		}
		
		@Override
		public void init(ArchiveLoaderContext context) throws IOException, IllegalArchiveException { }
		
	}
}
