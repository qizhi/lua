/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.network.users.firebase.impl;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.users.client.UserServiceClientHTTP;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.plugin.playerlookup.PlayerLookupService;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.network.users.firebase.api.UserServiceConfig;
import com.cubeia.network.users.firebase.api.UserServiceContract;

/**
 * 
 * 
 * Why all the Classloader switching? 
 * Turns out that HttpClient uses Common Loggings, which tries to load classes from the
 * context classloader on the current thread. So we need to switch out the context classloader
 * for each call that will use the Client. 
 * 
 * @author Fredrik
 *
 */
public class UserServiceImpl implements Service, UserServiceContract, LoginLocator, PlayerLookupService  {

	private Logger log = Logger.getLogger(this.getClass());
	
	private UserServiceClient client;

	private String baseUrl;
	
	
	/**
	 * Called by Firebase when the service is loaded.
	 * We will check for the  System property 'com.cubeia.network.userservice.base-url', 
	 * if no system property found then we will check Firebase configuration
	 * Last plan is the default fallback URL 
	 */
	@Override
	public void init(ServiceContext con) throws SystemException {
		try {
			
			// Check System property
			baseUrl = System.getProperty("com.cubeia.network.userservice.base-url");
			
			if (baseUrl == null) {
				ClusterConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
				UserServiceConfig configuration = contr.getConfiguration(UserServiceConfig.class, new Namespace(UserServiceConfig.NAMESPACE));
				baseUrl = configuration.getBaseUrl();
			}
			log.info("Login and Player service will connect to user service at this URL: "+baseUrl);
			
			if (baseUrl == null || !baseUrl.startsWith("http:")) {
				log.warn("The user service URL does not contain a full URL pattern, e.g. 'http://userservice:8080/user-service'");
			}
			
		} catch (ConfigurationException e) {
			log.error("Failed to read user service configuration. Will fall back on default value", e);
		}
	}

	@Override
	public void start() {
		ClassLoader originalClassloader = switchClassloader();
		client = new UserServiceClientHTTP(baseUrl);
		restoreClassloader(originalClassloader);
	}

	@Override
	public void destroy() {}

	@Override
	public void stop() {}

	@Override
	public void init(ServiceRegistry serviceRegistry) {}

	/**
	 * Login Locator Service method
	 */
	@Override
	public LoginHandler locateLoginHandler(LoginRequestAction request) {
		return new UserServiceLoginHandler(client);
	}

	/**
	 * Player Lookup Service method
	 */
	@Override
	public PlayerQueryResponseAction handleRequest(PlayerQueryRequestAction request) {
		ClassLoader originalClassloader = UserServiceImpl.switchClassloader();
		try {
			log.debug("Accessing player query: "+request);
			PlayerQueryResponseAction response = new PlayerQueryResponseAction(request.getPlayerid());
			String username = client.getUsername(new Long(request.getPlayerid()));
			if (username == null) username = "N/A";
			response.setScreenname(username); 
			return response;
		} finally {
			UserServiceImpl.restoreClassloader(originalClassloader);
		}
	}
	

	protected static ClassLoader switchClassloader() {
		ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(UserServiceImpl.class.getClassLoader());
		return originalClassloader;
	}
	
	protected static void restoreClassloader(ClassLoader originalClassloader) {
		Thread.currentThread().setContextClassLoader(originalClassloader);
	}
	
	
	
	/* ---------------------------------------------
	 * 
	 * SERVICE CONTRACT METHODS
	 * 
	 * --------------------------------------------- */

	@Override
	public User getUserById(int userId) {
		ClassLoader originalClassloader = UserServiceImpl.switchClassloader();
		try {
			return client.getUserById(new Long(userId));
		} finally {
			UserServiceImpl.restoreClassloader(originalClassloader);
		}
		
	}

	@Override
	public void invalidateUserSession(int userId) {
		ClassLoader originalClassloader = UserServiceImpl.switchClassloader();
		try {
			client.invalidateSessionToken(new Long(userId));
		} finally {
			UserServiceImpl.restoreClassloader(originalClassloader);
		}
	}
	
	
	/* ---------------------------------------------
	 * 
	 * END OF SERVICE CONTRACT METHODS
	 * 
	 * --------------------------------------------- */

}