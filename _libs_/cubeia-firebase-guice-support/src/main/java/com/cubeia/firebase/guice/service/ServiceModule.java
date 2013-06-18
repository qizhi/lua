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

import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * This module configures the injection context for the Guice 
 * support for Firebase services. 
 * 
 * @author larsan
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiceModule extends AbstractModule {

	private final ServiceContext context;
	private final Configuration configuration;

	/**
	 * @param context Service context, must not be null
	 * @param configuration Configuration, may be null
	 */
	public ServiceModule(ServiceContext context, Configuration configuration) {
		this.context = context;
		this.configuration = configuration;
	}

	@Override
	protected void configure() {
		bind(ServiceContext.class).toInstance(context);
		if(configuration != null && configuration.getServiceContract() != null) {
			ContractsConfig con = configuration.getServiceContract();
			Class service = con.getService();
			checkServiceInheritence(service, Service.class);
			for (Class cl : con.getContracts()) {
				checkServiceInheritence(service, cl);
				bind(cl).to(service).in(Scopes.SINGLETON);
			}
		}
	}
	
	/*
	 * Check that the given service implements the interfaces it claims
	 * to do...
	 */
	private void checkServiceInheritence(Class service, Class iface) {
		if(!iface.isAssignableFrom(service)) {
			throw new IllegalArgumentException("Service class " + service.getName() + " does not implement configured interface " + iface.getName());
		}
	}
}
