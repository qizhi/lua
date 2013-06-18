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
package com.cubeia.firebase.service.datasource.intern;

import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.DatasourceManagerImpl;


/**
 * Service wrapper for the Datasource Manager.
 * 
 * @author Fredrik
 *
 */
public class InternalDatasourceService implements Service, InternalDataSourceProvider {
	
	// --- INSTANCE MEMBERS --- //

	private DatasourceManager man;
	
	public void destroy() {
		man = null;
	}

	public void init(ServiceContext con) throws IllegalStateException {
		man = new DatasourceManagerImpl();
	}
	
	public void start() {
		man.start();
	}
	
	public DatasourceManager getDatasourceManager() {
		return man;
	}
	
	public void stop() {
		man.stop();
	}
}