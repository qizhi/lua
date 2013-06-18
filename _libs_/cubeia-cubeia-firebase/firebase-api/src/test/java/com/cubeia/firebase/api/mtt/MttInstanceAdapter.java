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
package com.cubeia.firebase.api.mtt;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.support.LobbyAttributeAccessorAdapter;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistryAdapter;

public class MttInstanceAdapter implements MttInstance {

	TournamentPlayerRegistry tournamentPlayerRegistry = new TournamentPlayerRegistryAdapter();
	
	LobbyAttributeAccessor lobbyAttributeAccessor = new LobbyAttributeAccessorAdapter();

	private MTTState state;

	private Scheduler<MttAction> scheduler; 
	
	public LobbyAttributeAccessor getLobbyAccessor() {
		return lobbyAttributeAccessor;
	}

	public Scheduler<MttAction> getScheduler() {
		return scheduler;
	}
	
	public void setScheduler(Scheduler<MttAction> scheduler) {
		this.scheduler = scheduler;
	}

	public ServiceRegistry getServiceRegistry() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MttNotifier getMttNotifier() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MttTableCreator getTableCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	public MTTState getState() {
		return state;
	}

	public TournamentPlayerRegistry getSystemPlayerRegistry() {
		return tournamentPlayerRegistry;
	}

	public LobbyAttributeAccessor getTableLobbyAccessor(int tableId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setState(MTTStateSupport state) {
		this.state = state;
	}

}
