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
package com.cubeia.firebase.api.mtt.activator;

import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;

/**
 * This is a participant interface for mtt creation. This interface
 * can be supplied by activators to define lobby attributes and initial mtt
 * data.
 */
public interface CreationParticipant { 
	
	/**
	 * A table will always reside in the lobby as a descendant of the
	 * path "/mtt/&lt;mttId&gt;/". This method is used to determine exactly 
	 * where under the fixed prefix the table should be located. 
	 * 
	 * @param table The mtt to create a path for, never null
	 * @return The LobbyPath object for the table. Never null
	 */
	public LobbyPath getLobbyPathForTournament(MTTState mtt);
	
	public void tournamentCreated(MTTState mtt, LobbyAttributeAccessor acc);

}
