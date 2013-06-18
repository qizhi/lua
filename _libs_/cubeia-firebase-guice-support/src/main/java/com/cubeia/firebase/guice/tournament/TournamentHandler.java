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

import com.cubeia.firebase.api.action.mtt.*;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;

/**
 * This is the interface which should be implemented to
 * support tournaments. Please refer to the MTTSupport class
 * for method definitions.
 * 
 * @author larsan
 */
public interface TournamentHandler {
	
	public PlayerInterceptor getPlayerInterceptor(MTTStateSupport arg0);
	
	public PlayerListener getPlayerListener(MTTStateSupport arg0);
	
	public void process(MttRoundReportAction arg0, MttInstance arg1);

	public void process(MttTablesCreatedAction arg0, MttInstance arg1);
	
	public void process(MttObjectAction arg0, MttInstance arg1);

	public void process(MttDataAction action, MttInstance instance);

	public void process(MttSeatingFailedAction action, MttInstance instance);
	
	public void tournamentCreated(MttInstance arg0);
	
	public void tournamentDestroyed(MttInstance arg0);
	
}
