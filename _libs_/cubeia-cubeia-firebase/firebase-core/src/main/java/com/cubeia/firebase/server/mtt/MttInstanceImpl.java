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
package com.cubeia.firebase.server.mtt;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.mtt.MttNotifierImpl;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.game.activation.DefaultTableFactory;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyAttributeAccessor;
import com.cubeia.firebase.server.mtt.tables.MttTableCreatorImpl;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

/**
 * DTO implementation of a tournament instance.
 * Holds state and other needed classes, e.g. scheduler.
 *
 * @author Fredrik
 */
public class MttInstanceImpl implements MttInstance {

	private Scheduler<MttAction> scheduler;
	private TransactionalMttState state;
	private ServiceRegistry serviceRegistry;
	
	private DefaultLobbyAttributeAccessor accessor;
	private SystemStateServiceContract sysState;
	
	private MttNotifierImpl notifier;
	private MttTableCreatorImpl creator;
	
	public MttInstanceImpl(TransactionalMttState state, SystemStateServiceContract sysState) {
		super();
		this.state = state;
		this.sysState = sysState;
		LobbyPath path = getState().getLobbyPath();
		accessor = new DefaultLobbyAttributeAccessor(sysState, path);
	}
	
	public LobbyAttributeAccessor getTableLobbyAccessor(int tableId) {
		int gameId = state.getMttState().getGameId();
		int mttId = state.getMttState().getId();
		LobbyPath path = DefaultTableFactory.getLobbyPathForMttTable(gameId, mttId, tableId);
		DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(sysState, path);
		return (acc.exists() ? acc : null);
	}

	public Scheduler<MttAction> getScheduler() {
		return scheduler;
	}
	
	public void setScheduler(Scheduler<MttAction> scheduler) {
		this.scheduler = scheduler;
	}

	public void setState(TransactionalMttState state) {
		this.state = state;
	}

	public MTTState getState() {
		return state.getMttState();
	}

	public int getId() {
		return state.getId();
	}
	
	public TournamentPlayerRegistry getSystemPlayerRegistry() {
		return serviceRegistry.getServiceInstance(TournamentPlayerRegistry.class);
	}
	
	public TransactionalMttState getTransactionalState() {
		return state;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public LobbyAttributeAccessor getLobbyAccessor() {
		return accessor;
	}
	
	public MttNotifier getMttNotifier() {
		return notifier;
	}
	
	public MttTableCreator getTableCreator() {
		return creator;
	}
	
	public void setMttCreator(MttTableCreatorImpl creator) {
		this.creator = creator;
	}
	
	public void setMttNotifier(MttNotifierImpl notifier) {
		this.notifier = notifier;
	}
}
