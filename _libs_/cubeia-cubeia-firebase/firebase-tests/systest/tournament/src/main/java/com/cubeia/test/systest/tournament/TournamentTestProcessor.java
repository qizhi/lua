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

import java.io.Serializable;

import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;

/*
 * This interface works much like the game processor. It steals its methods
 * from the MTTSupport, and will be inserted in the tournament state and used
 * for testing.
 * 
 * Method preMethod(MTTSupport) will be called before a test. The support
 * object should not be saved. postMethod() will be called after an invocation.
 */
public interface TournamentTestProcessor extends Serializable {
	
	public void preMethod(MTTSupport support);

	public void postMethod();
	
	
	// --- MTT METHODS --- //
	
	public PlayerInterceptor getPlayerInterceptor(MTTSupport mtt, MTTStateSupport state);

	public PlayerListener getPlayerListener(MTTSupport mtt, MTTStateSupport state);
	
	public void process(MttDataAction action, MttInstance instance);

	public void process(MttRoundReportAction action, MttInstance mttInstance);

	public void process(MttTablesCreatedAction action, MttInstance instance);

	public void process(MttObjectAction action, MttInstance instance);
	
	public void process(MttSeatingFailedAction action, MttInstance instance);

	public void tournamentCreated(MttInstance mttInstance);

	public void tournamentDestroyed(MttInstance mttInstance);

}
