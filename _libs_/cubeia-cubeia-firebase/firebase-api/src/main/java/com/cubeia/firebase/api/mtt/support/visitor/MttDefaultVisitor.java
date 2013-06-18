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
package com.cubeia.firebase.api.mtt.support.visitor;

import java.util.Set;

import com.cubeia.firebase.api.action.MttRegisterResponseAction;
import com.cubeia.firebase.api.action.MttUnregisterResponseAction;
import com.cubeia.firebase.api.action.mtt.MttCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttDestroyedAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRegisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesRemovedAction;
import com.cubeia.firebase.api.action.mtt.MttUnregisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.ScheduledMttAction;
import com.cubeia.firebase.api.action.visitor.DefaultMttActionVisitor;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributeMapper;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.jmx.MttStats;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;

/**
 * <p>Default logic for handling MTT Actions in a tournament.</p>
 * 
 * <p>Will handle register, unregister and such actions but will
 * propagate actions that needs to be handled by the mtt logic,
 * e.g. MttRoundReportAction.</p>
 *
 * <p>This class requires the use of MTTSupport and MTTStateSupport.</p>
 * 
 * @author Fredrik
 */
public class MttDefaultVisitor extends DefaultMttActionVisitor {
	
	private final MTTStateSupport state;
	private final MTTSupport handler;
	private final MttInstance instance;
    // private final MttNotifier mttNotifier;
    
    /** Used when updating statistics. */
	private int originalRegPlayersCount;
	
	
	
	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/
	
	/**
	 * Create a default visitor from a MTT Support and MTT State Support.
	 * @param mttNotifier 
	 * @deprecated 
	 */
	@Deprecated
	public MttDefaultVisitor(MTTSupport handler, MttInstance instance, MttNotifier mttNotifier) {
		this(handler, instance);
		/*this.handler = handler;
		this.instance = instance;
        this.mttNotifier = mttNotifier;
		this.state = (MTTStateSupport)instance.getState();
		this.originalRegPlayersCount = state.getRegisteredPlayersCount();*/
	}
	
	public MttDefaultVisitor(MTTSupport handler, MttInstance instance) {
		this.handler = handler;
		this.instance = instance;
        // this.mttNotifier = mttNotifier;
		this.state = (MTTStateSupport)instance.getState();
		this.originalRegPlayersCount = state.getRegisteredPlayersCount();
	}
	
	
	
	/*------------------------------------------------
	 
		MTT ACTION VISITOR METHODS

	 ------------------------------------------------*/
	/**
	 * <p>Register interceptors and listeners to the player registry in the 
	 * tournament registry and then call the register(player) method.</p>
	 * 
	 * <p>The player registry will use the interceptor to determine if the 
	 * player is allowed or denied to join the tournament.</p>
	 * 
	 * @param action
	 */
	@Override
	public void visit(MttRegisterPlayerAction action) {
		MttPlayer player = new MttPlayer(action.getPlayerId(), action.getScreenname());
		MttRegistrationRequest request = new MttRegistrationRequest(player, action.getParameters());
		MttRegisterResponse response = state.getPlayerRegistry().register(instance, request);
		
		// Send a response
		MttRegisterResponseAction responseAction = new MttRegisterResponseAction(player.getPlayerId(), action.getMttId(), response);
		instance.getMttNotifier().notifyPlayer(player.getPlayerId(), responseAction);
	}

	private void updatePlayerCount(int originalRegPlayersCount) {
		// Only update if changed
		if (instance.getState().getRegisteredPlayersCount() != originalRegPlayersCount) {
			instance.getLobbyAccessor().setIntAttribute(DefaultMttAttributes.REGISTERED.name(), instance.getState().getRegisteredPlayersCount());
		}
	}

	/**
	 * <p>Register interceptors and listeners to the player registry in the 
	 * tournament registry and then call the unregister(pid) method.</p>
	 * 
	 * <p>The player registry will use the interceptor to determine if the 
	 * player is allowed or denied to leave the tournament.</p>
	 * 
	 * @param action
	 */
	@Override
	public void visit(MttUnregisterPlayerAction action) {
		MttRegisterResponse response = state.getPlayerRegistry().unregister(instance, action.getPlayerId());

		// Send a response
		MttUnregisterResponseAction responseAction = new MttUnregisterResponseAction(action.getPlayerId(), action.getMttId(), response);
		instance.getMttNotifier().notifyPlayer(action.getPlayerId(), responseAction);		
	}

	/**
	 * Propagate the action to the parent MTT Support
	 * together with the state.
	 * 
	 * @param action
	 */
	@Override
	public void visit(MttRoundReportAction action) {
		handler.process(action, instance);
	}

	@Override
	public void visit(ScheduledMttAction action) {
//		handler.process(action, instance);
        action.getScheduledAction().accept(this);
	}
	
	@Override
	public void visit(MttDataAction action) {
	    handler.process(action, instance);
	}
	
	@Override
	public void visit(MttCreatedAction action) {
		MttStats stats = handler.getJmxHandler().getStatsBean(state, state.getId());
		stats.setCapacity(instance.getLobbyAccessor().getIntAttribute(DefaultMttAttributes.CAPACITY.toString()));
	    handler.tournamentCreated(instance);
	    DefaultMttAttributeMapper.setReady(instance.getLobbyAccessor(), true);
	}
	
	@Override
	public void visit(MttDestroyedAction action) {
		handler.getJmxHandler().removeStatsBean(state.getId());
		handler.tournamentDestroyed(instance);
	}
	
	/**
	 * <p>1. Add the tables to the state.</p>
	 * <p>2. Trigger tables created in the state</p>
	 * <p>3. Update statistics</p>
	 * <p>4. Start seating on the parent MTT Support</p>
	 */
	@Override
	public void visit(MttTablesCreatedAction action) {
		Set<Integer> tables = state.getTables();
		for (Integer tableId : action.getTables()) {
			tables.add(tableId);
		}
		state.tablesCreated();
		updateTableCountStats();
		handler.process(action, instance);
	}
	
	/**
	 * 
	 * @param action
	 */
	@Override
	public void visit(MttTablesRemovedAction action) {
	    state.getTables().removeAll(action.getTables());
	    updateTableCountStats();
	}
	
	@Override
    public void visit(MttObjectAction action) {
        handler.process(action, instance);
    }
	
	@Override
	public void visit(MttSeatingFailedAction action) {
	    handler.process(action, instance);
	}
	
	/*------------------------------------------------
	 
		PRIVATE METHODS

	 ------------------------------------------------*/
	
	public void registerListeners() {
		PlayerInterceptor interceptor = handler.getPlayerInterceptor(state);
		PlayerListener listener = handler.getPlayerListener(state);
		if (interceptor != null) state.getPlayerRegistry().registerInterceptor(interceptor);
		if (listener != null) state.getPlayerRegistry().registerListener(listener);
	}
	
	
	public void clearListeners() {
		state.getPlayerRegistry().clearInterceptors();
		state.getPlayerRegistry().clearListeners();
	}
	
	public void updateStats() {
		updatePlayerStats();
		updatePlayerCount(originalRegPlayersCount);
	}
	
	/**
	 * Does only work if we are guaranteed reference integrity of the listener & interceptor.
	 *
	 */
	@SuppressWarnings("unused")
	private void unregisterListeners() {
		PlayerInterceptor interceptor = handler.getPlayerInterceptor(state);
		PlayerListener listener = handler.getPlayerListener(state);
		if (interceptor != null) state.getPlayerRegistry().unregisterInterceptor(interceptor);
		if (listener != null) state.getPlayerRegistry().unregisterListener(listener);
	}

	
	private void updatePlayerStats() {
		MttStats stats = handler.getJmxHandler().getStatsBean(state, state.getId());
		//if(stats != null) {
			stats.setRemainingPlayers(state.getRemainingPlayerCount());
			stats.setRegisteredPlayers(state.getRegisteredPlayersCount());
		//}
	}
	
	private void updateTableCountStats() {
		MttStats stats = handler.getJmxHandler().getStatsBean(state, state.getId());
		//if(stats != null) {
			stats.setTableCount(state.getTables().size());
		//}
	}
}
