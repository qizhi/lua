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

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;

/**
 * This support interface contains common methods for tournaments. Please
 * refer to the MTTSuppport class for method definitions.
 * 
 * @author larsan
 */
public interface TournamentAssist {

	public void setLobbyAttribute(MttInstance instance, Attribute attribute);
	
	public void removeLobbyAttribute(MttInstance instance, String name);
	
	public void createTables(MTTStateSupport state, int numberOfTables, String tableBaseName, Object attachment);

	public void closeTables(MTTStateSupport state, Collection<Integer> tables);
	
	public void seatPlayers(MTTStateSupport state, Collection<SeatingContainer> seating);
	
	public void unseatPlayers(MTTStateSupport state, int tableId, Collection<Integer> players, UnseatPlayersMttAction.Reason reason);

	public void movePlayer(MTTStateSupport state, int playerId, int toTableId, int toSeatId, UnseatPlayersMttAction.Reason reason, Serializable playerData);
	
	public void sendRoundStartActionToTables(MTTStateSupport state, Collection<Integer> tables);
	
	public void sendRoundStopActionToTables(MTTStateSupport state, Collection<Integer> tables);
	
	public int getTableIdByPlayerId(MTTStateSupport state, int playerId);
	
	public Set<Integer> getRemainingPlayers(MTTStateSupport state);
	
}
