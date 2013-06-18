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
package com.cubeia.firebase.service.conn.local;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.instance.ServerConfig;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;

/**
 * This is an in-VM only implementation of the {@link ConnectionServiceContract}.
 * No clustering is done. This service uses a cached thread pool to make sure sending
 * and receiving are done on separate threads.
 * 
 * @author Lars J. Nilsson
 * @see ConnectionServiceContract
 */
public class LocalConnectionService implements Service, ConnectionServiceContract {

	private LocalConnection conn;
	private ServerConfig config;
	
	@Override
	public void destroy() { 
		if(conn != null) {
			conn.stop();
		}
	}

	@Override
	public void init(ServiceContext con) throws SystemException { 
		initConfig(con);
		initConn();
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }

	@Override
	public void closeConnection(ClusterConnection conn) { }

	@Override
	public ClusterConnection getSharedConnection() {
		return conn;
	}

	/*@Override
	public ClusterConnection openNewConnection() throws ClusterException {
		// TODO Auto-generated method stub
		return conn;
	}*/
	
	
	// --- PRIVATE METHODS --- //
	
	private void initConn() {
		SocketAddress b = config.getClusterBindAddress();
		if(b == null) b = LocalConstants.LOCAL_ADDRESS;
		SocketAddress c = config.getClusterMcastAddress();
		if(c == null) c = LocalConstants.LOCAL_ADDRESS;
		conn = new LocalConnection(b, c);
	}
	
	private void initConfig(ServiceContext con) throws SystemCoreException {
		ServerConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ServerConfigProviderContract.class);
		if(contr == null) throw new SystemCoreException("Failed service dependencies; Could not find server configuration service '" + Constants.SERVER_CONFIG_SERVICE_NS + "'.");
		config = contr.getConfiguration(ServerConfig.class, null);
	}
}
