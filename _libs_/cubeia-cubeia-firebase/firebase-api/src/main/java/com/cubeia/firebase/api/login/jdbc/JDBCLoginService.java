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
package com.cubeia.firebase.api.login.jdbc;

import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.datasource.DatasourceServiceContract;

/**
 * This is a simple service implementation looking for user names and 
 * passwords from a database table. Configuration for this service is done
 * in the cluster configuration file. The following properties
 * apply:
 * 
 * <pre>
 * 		service.login.data-source - deployed data source name
 * 		service.login.table-name - database table to use
 * 		service.login.id-column - user id column name
 * 		service.login.user-column - user name column name
 * 		service.login.password-column - user password column name
 * </pre>
 * 
 * All properties needs to be supplied. The data source must be sucessfully
 * deployed. The service should depend on {@link ClusterConfigProviderContract} and
 * {@link DatasourceServiceContract}. A service xml is included in the same package as
 * this class. Errors will appear in the log when running if the data source 
 * cannot be found.
 * 
 * @author Larsan
 * @date 2007 maj 24
 */
public class JDBCLoginService implements LoginLocator, Service {
	
	private final Logger log = Logger.getLogger(getClass());
	
	private ServiceContext con;
	private AtomicReference<JDBCLoginHandler> handler;
	
	public LoginResponseAction handle(LoginRequestAction request) {
		JDBCLoginHandler handler = getHandler();
		if(handler != null) return handler.handle(request);
		else {
			LoginResponseAction act = new LoginResponseAction(false, -1);
			act.setErrorMessage("Failed to access login handler");
			return act;
		}
	}
	
	public LoginHandler locateLoginHandler(LoginRequestAction request) {
		return new LoginHandler() {
			public LoginResponseAction handle(LoginRequestAction request) {
				return JDBCLoginService.this.handle(request);
			}
		};
	}

	public void destroy() {
		handler.set(null);
		con = null;
	}

	public void init(ServiceRegistry serviceRegistry) { }

	public void init(ServiceContext con) throws SystemException {
		handler = new AtomicReference<JDBCLoginHandler>();
		this.con = con;
	}

	public void start() { }

	public void stop() { }

	
	// --- PRIVATE METHODS --- //
	
	private JDBCLoginHandler getHandler() {
		JDBCLoginHandler tmp = handler.get();
		if(tmp == null) tmp = createHandler();
		return tmp;
	}

	private JDBCLoginHandler createHandler() {
		JDBCLoginConfig config = getConfig();
		if(config == null) return null;
		DataSource source = getDataSource(config);
		if(source == null) return null;
		JDBCLoginHandler hand = new JDBCLoginHandler(source, config);
		handler.set(hand);
		return hand;
	}

	private DataSource getDataSource(JDBCLoginConfig config) {
		ServiceRegistry reg = con.getParentRegistry();
		DatasourceServiceContract serv = reg.getServiceInstance(DatasourceServiceContract.class);
		DataSource source = serv.getDatasource(config.getDataSource());
		if(source == null) log.fatal("Login handler could not find any data source with the configured name '" + config.getDataSource() + "'");
		return source;
	}

	private JDBCLoginConfig getConfig() {
		ServiceRegistry reg = con.getParentRegistry();
		ClusterConfigProviderContract conf = reg.getServiceInstance(ClusterConfigProviderContract.class);
		return conf.getConfiguration(JDBCLoginConfig.class, null);
	}
}
