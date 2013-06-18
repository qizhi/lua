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

package com.cubeia.firebase.guice.tournament;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.action.mtt.*;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * A support class for tournament in Firebase with Guice. This implementation
 * differs a bit from the others in the Guice support, to implement a 
 * tournament you do not implement a proper Firebase interface but rather
 * the {@link TournamentHandler} interface. 
 * 
 * <p>For common methods a {@link TournamentAssist} will be automatically bound
 * in the injection context and ready to use. 
 * 
 * <p>For custom configuration, override the {@link #preInjectorCreation(List)}
 * method to add custom modules prior to the injection context creation. 
 * 
 * <p>This activator adds a {@link TournamentModule} to the injector 
 * configuration by default.
 * 
 * @author larsan
 *
 */
public abstract class GuiceTournament extends MTTSupport implements TournamentAssist {
	
	private Injector injector;

	public GuiceTournament() {
		createInjector();
	}
	
	
	// --- MTT SUPPORT --- //
	
	@Override
	public PlayerInterceptor getPlayerInterceptor(MTTStateSupport arg0) {
		return guice(TournamentHandler.class).getPlayerInterceptor(arg0);
	}

	@Override
	public PlayerListener getPlayerListener(MTTStateSupport arg0) {
		return guice(TournamentHandler.class).getPlayerListener(arg0);
	}

	@Override
	public void process(MttRoundReportAction arg0, MttInstance arg1) {
		guice(TournamentHandler.class).process(arg0, arg1);
	}

	@Override
	public void process(MttTablesCreatedAction arg0, MttInstance arg1) {
		guice(TournamentHandler.class).process(arg0, arg1);
	}

	@Override
	public void process(MttObjectAction arg0, MttInstance arg1) {
		guice(TournamentHandler.class).process(arg0, arg1);
	}

	@Override
	public void process(MttDataAction action, MttInstance instance) {
		guice(TournamentHandler.class).process(action, instance);
	}

	@Override
	public void process(MttSeatingFailedAction action, MttInstance instance) {
		guice(TournamentHandler.class).process(action, instance);
	}

	@Override
	public void tournamentCreated(MttInstance arg0) {
		guice(TournamentHandler.class).tournamentCreated(arg0);
	}

	@Override
	public void tournamentDestroyed(MttInstance arg0) {
		guice(TournamentHandler.class).tournamentDestroyed(arg0);
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
	public <T> T guice(Class<T> iface) {
		return injector.getInstance(iface);
	}
	
	
	// --- PRIVATE METHODS --- //

	private void createInjector() {
		List<Module> list = new ArrayList<Module>(5);
		addStandardModules(list);
		preInjectorCreation(list);
		injector = Guice.createInjector(list);
	}

	private void addStandardModules(List<Module> list) {
		// list.add(new FirebaseModule(context.getServices()));
		list.add(new TournamentModule(this));
		// list.add(new Jsr250Module());
		// list.add(new EventModule());
	}
}
