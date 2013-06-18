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
package com.cubeia.firebase.service.datasource.pub;

import java.util.List;

import javax.sql.DataSource;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.datasource.DatasourceListener;
import com.cubeia.firebase.api.service.datasource.DatasourceServiceContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.intern.InternalDataSourceProvider;

public class PublicDatasourceService implements DatasourceServiceContract, Service {

	private DatasourceManager manager;

	public boolean exists(String name) {
		Arguments.notNull(name, "name");
		return manager.exists(name);
	}
	
	@Override
	public void addDatasourceListener(DatasourceListener l) {
		manager.addDatasourceListener(l);
	}
	
	@Override
	public List<String> getDatasources() {
		return manager.getDatasources();
	}
	
	@Override
	public void removeDatasourceListener(DatasourceListener l) {
		manager.removeDatasourceListener(l);
	}

	public DataSource getDatasource(String name) {
		Arguments.notNull(name, "name");
		return manager.getDatasource(name);
	}

	public DataSource getSystemDatasource() {
		return manager.getSystemDatasource();
	}

	public void destroy() {
		manager = null;
	}

	public void init(ServiceContext con) throws SystemException {
		InternalDataSourceProvider serv = con.getParentRegistry().getServiceInstance(InternalDataSourceProvider.class);
		manager = serv.getDatasourceManager();
	}

	public void start() { }

	public void stop() { }

}
