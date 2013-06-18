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

package com.cubeia.firebase.guice.game;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * An activator with built in Guice support. Implementors
 * should override the {@link #getActivatorClass()} method to 
 * configure the injection context.
 * 
 * <p>For custom configuration, override the {@link #preInjectorCreation(List)}
 * method to add custom modules prior to the injection context creation. 
 * 
 * <p>This activator adds a {@link FirebaseModule} and a {@link ActivatorModule}
 * to the injector configuration by default.
 * 
 * @author larsan
 */
public abstract class GuiceActivator implements GameActivator {
	
	private Injector injector;
	protected ActivatorContext context;

	@Override
	public void init(ActivatorContext context) throws SystemException {
		this.context = context;
		createInjector();
		guice(GameActivator.class).init(context);
	}
	
	@Override
	public void start() {
		guice(GameActivator.class).start();	
	}
	
	@Override
	public void stop() {
		guice(GameActivator.class).stop();
	}
	
	@Override
	public void destroy() {
		guice(GameActivator.class).destroy();
	}
	
	// --- CONFIG METHDOS --- //
	
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
	protected <T> T guice(Class<? extends T> iface) {
		return injector.getInstance(iface);
	}

	/**
	 * Override this method to add you own modules to the 
	 * injection context.
	 * 
	 * @param modules List of modules for the context, never null
	 */
	protected void preInjectorCreation(List<Module> modules) { }
	
	/**
	 * @return The activator class, must not return null
	 */
	protected abstract Class<? extends GameActivator> getActivatorClass();

	
	// --- PACKAGE METHODS --- //
	
	ActivatorContext getContext() {
		return context;
	}
	
	
	// --- PRIVATE METHODS --- //

	private void createInjector() {
		List<Module> list = new ArrayList<Module>(5);
		addStandardModules(list);
		preInjectorCreation(list);
		injector = Guice.createInjector(list);
	}

	private void addStandardModules(List<Module> list) {
		list.add(new FirebaseModule(context.getServices()));
		list.add(new ActivatorModule(this));
		// list.add(new Jsr250Module());
		// list.add(new EventModule());
	}
}
