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
package com.game.server.service.login;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.server.login.LoginConfig;
import com.cubeia.firebase.server.login.LoginManager;
import com.cubeia.firebase.server.login.LoginManagerConfiguration;
import com.cubeia.firebase.server.login.LoginManagerImpl;
import com.cubeia.firebase.server.service.login.LoginServiceContract;

public class LoginService implements Service, LoginServiceContract {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	private static final String CONFIG_NS = "service.loginmanager";
	
	private LoginManager manager;

	private ServiceContext con;
	
	public void destroy() {
		manager = null;
	}

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
	}
	
    public void start() {
		LoginLocator locator = checkInitLocator();
		PostLoginProcessor proc = checkInitProcessor();
		if (locator != null) {
			manager = new LoginManagerImpl(locator, proc, getLoginManagerConfiguration());
			manager.start();
		} else {
			log.fatal("no Login Locator defined");
		}
	}
    
	private LoginManagerConfiguration getLoginManagerConfiguration() {
		ClusterConfigProviderContract provider = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		return provider.getConfiguration(LoginManagerConfiguration.class, new Namespace(CONFIG_NS));	
	}
    

	/**
	 * Get the login manager
	 */
	public LoginManager getLoginManager() {
		return manager;
	}
	
	public void stop() {
		manager.stop();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private PostLoginProcessor checkInitProcessor() {
		ServiceRegistry reg = con.getParentRegistry();
		return reg.getServiceInstance(PostLoginProcessor.class);
	}
	
	private LoginLocator checkInitLocator() {
		LoginLocator locator = checkLocatorService();
		if(locator == null) locator = checkInitConfiguredLocator();
		return locator;
	}
	
	private LoginLocator checkLocatorService() {
		ServiceRegistry reg = con.getParentRegistry();
		return reg.getServiceInstance(LoginLocator.class);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LoginLocator checkInitConfiguredLocator() {
		try {
			// FIXME The locator lookup should be done in init() so we can
			// report failure and halt startup
			ClusterConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
			LoginConfig clusterConfiguration = contr.getConfiguration(LoginConfig.class, null);
			String locatorClass = clusterConfiguration.getLocator();
			log.info("Defined Login Locator: "+locatorClass);
			log.warn("Definition of Login Locators via configuration will be removed in Firebase 1.9! Please mount the locator as a service instead.");
			Class cl = Class.forName(locatorClass);
			Class[] params = new Class[]{};
			java.lang.reflect.Constructor co = cl.getConstructor(params);
			LoginLocator loc = (LoginLocator) co.newInstance((Object[]) params);
			if(loc != null) loc.init(con.getParentRegistry());
			return loc;
		} catch (Exception e) {
			log.fatal("Could not instantiate a Login Locator: " + e, e);
			return null;
		}
	}
}