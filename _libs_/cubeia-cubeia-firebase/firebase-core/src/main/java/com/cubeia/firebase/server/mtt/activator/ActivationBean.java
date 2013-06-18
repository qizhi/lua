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
package com.cubeia.firebase.server.mtt.activator;

import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.api.mtt.activator.ActivatorContext;
import com.cubeia.firebase.api.mtt.activator.MttActivator;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;

/**
 * Simple DTO for activation data.
 *
 * @author Fredrik
 */
public class ActivationBean {
	
	private MttActivator activator;
	private TournamentRevision rev;
	private TournamentDefinition def;
	private final String name;
	
	private ActivatorContext context;
	
	public ActivationBean(String name, MttActivator activator, TournamentRevision rev, TournamentDefinition def) {
		this.name = name;
		this.activator = activator;
		this.rev = rev;
		this.def = def;
	}
	
	public ActivatorContext getContext() {
		return context;
	}
	
	public void setContext(ActivatorContext context) {
		this.context = context;
	}

	public String getName() {
		return name;
	}

	public MttActivator getActivator() {
		return activator;
	}


	public TournamentDefinition getDef() {
		return def;
	}


	public TournamentRevision getRev() {
		return rev;
	}
	
	public String toString() {
		return name+" "+rev;
	}

	public boolean isRoutable() {
		return (activator instanceof RoutableActivator);
	}
}
