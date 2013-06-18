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
package com.cubeia.firebase.service.mttplayerreg;

import java.util.Map;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class TournamentPlayerRegistryService implements TournamentPlayerRegistry, Service {
	
	private SystemStateServiceContract state;
	
	public TournamentPlayerRegistryService() { }
	
	/**
	 * Constructor for testing purposes only.
	 */
	TournamentPlayerRegistryService(SystemStateServiceContract state) {
		this.state = state;
	}

	public int[] getTournamentsForPlayer(int playerId) {
		String path = getPlayerFqn(playerId);
		return getAttributeArray(path);
	}

	public int[] getPlayersForTournament(int mttId) {
		String path = getMttFqn(mttId);
		return getAttributeArray(path);
	}
	
	public void register(int playerId, int mttId) {
		registerMtt(playerId, mttId);
		registerPlayer(playerId, mttId);
	}

	public void unregister(int playerId, int mttId) {
		unregisterMtt(playerId, mttId);
		unregisterPlayer(playerId, mttId);
	}

	public void unregisterAll(int mttId) {
		String path = getMttFqn(mttId);
		String tmpId = String.valueOf(mttId);
		Map<Object, Object> atts = state.getAttributes(path);
		for (Object key : atts.keySet()) {
			String playerPath = getPlayerFqn(Integer.parseInt(key.toString()));
			state.removeAttribute(playerPath, tmpId);
			/*
			 * Trac issue #396, we need to remove the entire node if this
			 * is the last MTT the player was registered for.
			 */
			Map<Object, Object> tmp = state.getAttributes(playerPath);
			if(tmp.size() == 0) {
				state.removeNode(playerPath);
			}
		}
		state.removeNode(path);
	}
	
	
	// --- SERVICE METHODS --- //

	public void destroy() { }

	public void init(ServiceContext con) throws SystemException {
		this.state = con.getParentRegistry().getServiceInstance(SystemStateServiceContract.class);
	}

	public void start() { }

	public void stop() { }
	
	
	// --- PRIVATE METHODS --- //
	
	private int[] getAttributeArray(String path) {
		Map<Object, Object> atts = state.getAttributes(path);
		if(atts == null) return new int[0];
		int[] arr = new int[atts.size()];
		int i = 0;
		for (Object key : atts.keySet()) {
			arr[i++] = Integer.parseInt(key.toString());
		}
		return arr;
	}
	
	private void unregisterPlayer(int playerId, int mttId) {
		String path = getPlayerFqn(playerId);
		String tmpId = String.valueOf(mttId);
		state.removeAttribute(path, tmpId);
	}

	private void unregisterMtt(int playerId, int mttId) {
		String path = getMttFqn(mttId);
		String tmpId = String.valueOf(playerId);
		state.removeAttribute(path, tmpId);
	}
	
	private void registerPlayer(int playerId, int mttId) {
		String path = getPlayerFqn(playerId);
		String tmpId = String.valueOf(mttId);
		state.setAttribute(path, tmpId, tmpId);
	}

	private void registerMtt(int playerId, int mttId) {
		String path = getMttFqn(mttId);
		String tmpId = String.valueOf(playerId);
		state.setAttribute(path, tmpId, tmpId);
	}
	
	private String getPlayerFqn(int playerId) {
		return SystemStateConstants.MTT_PLAYERREG_PLAYER_FQN + playerId + "/";
	}
	
	private String getMttFqn(int mttId) {
		return SystemStateConstants.MTT_PLAYERREG_MTT_FQN + mttId + "/";
	}
}
