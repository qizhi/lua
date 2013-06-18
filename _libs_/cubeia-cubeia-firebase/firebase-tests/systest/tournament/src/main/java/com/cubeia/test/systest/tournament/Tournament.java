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
package com.cubeia.test.systest.tournament;

import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;

public class Tournament extends MTTSupport {

	@Override
	public PlayerInterceptor getPlayerInterceptor(MTTStateSupport state) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(state);
		try {
			return proc.getPlayerInterceptor(this, state);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public PlayerListener getPlayerListener(MTTStateSupport state) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(state);
		try {
			return proc.getPlayerListener(this, state);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void process(MttRoundReportAction action, MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.process(action, instance);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void process(MttTablesCreatedAction action, MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.process(action, instance);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void process(MttObjectAction action, MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.process(action, instance);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void tournamentCreated(MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.tournamentCreated(instance);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void tournamentDestroyed(MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.tournamentDestroyed(instance);
		} finally {
			proc.postMethod();
		}
	}

	@Override
	public void process(MttDataAction action, MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.process(action, instance);
		} finally {
			proc.postMethod();
		}
	}
	
	@Override
	public void process(MttSeatingFailedAction action, MttInstance instance) {
		checkContextClassLoader();
		TournamentTestProcessor proc = pre(instance.getState());
		try {
			proc.process(action, instance);
		} finally {
			proc.postMethod();
		}
	}
	
	// --- PRIVATE METHODS --- //
	
	private void checkContextClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null || !cl.equals(getClass().getClassLoader())) {
			throw new IllegalStateException("Found class loader: " + cl + "; Expected: " + getClass().getClassLoader());
		}
	}
	
	private TournamentTestProcessor pre(MTTState state) {
		TournamentTestProcessor proc = (TournamentTestProcessor) state.getState();
		proc.preMethod(this);
		return proc;
	}
}
