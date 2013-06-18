/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.service;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This is a game with built in support for Guice injected processing. 
 * Implementors should override the {@link #getConfigurationHelp()} help method
 * to configure the injector context.
 * 
 * <p>As Firebase does not support direct proxying implementation must push contract
 * calls to a guice instance manually. Eg:
 * 
 * <pre>
 * public class MyService extends GuiceService, MyContract {
 * 
 *   @Override
 *   public void myContractMethod() {
 *     guice(MyContract.class).myContractMethod();
 *   }
 *   
 * [...]
 * </pre>
 * 
 * This class will bind the contract implementation as a singleton.
 * 
 * <p>For custom configuration, override the {@link #preInjectorCreation(List)}
 * method to add custom modules prior to the injection context creation.
 * 
 * <p>This activator adds a {@link FirebaseModule} and a {@link ServiceModule}
 * to the injector configuration by default.
 * 
 * @author larsan
 * @deprecated Use {@link GuiceServiceHandler} instead
 */
@Deprecated
public abstract class GuiceService implements Service {

	private Injector injector;
	protected ServiceContext context;

	@Override
	public void destroy() { }

	@Override
	public void init(ServiceContext context) throws SystemException {
		this.context = context;
		createInjector();
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }
	
	
	
	// --- GUICE ACCESS --- //

	/**
	 * Return a Guice-created instance of the interface. This
	 * method returns...
	 * 
	 * <pre>
	 *    Injector.getInstance(iface)
	 * </pre>
	 * 
	 * @param iface Interface to create, must not be null
	 * @return A Guice instance of the interface
	 */
	protected <T> T guice(Class<T> contract) {
		return injector.getInstance(contract);
	}
	
	
	
	// --- CONFIG METHDOS --- //
	
	/**
	 * @return The configuration to use, must not return null
	 */
	public abstract Configuration getConfigurationHelp();
	
	/**
	 * Override this method to add you own modules to the 
	 * injection context.
	 * 
	 * @param modules List of modules for the context, never null
	 */
	protected void preInjectorCreation(List<Module> modules) { }
	
	
	// --- PRIVATE METHODS --- //
	
	private void createInjector() {
		List<Module> list = new ArrayList<Module>(5);
		addStandardModules(list);
		preInjectorCreation(list);
		injector = Guice.createInjector(list);
	}

	private void addStandardModules(List<Module> list) {
		list.add(new FirebaseModule(context.getParentRegistry()));
		list.add(new ServiceModule(context, getConfigurationHelp()));
	}
}
