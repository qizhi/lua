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

import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributeMapper;
import com.cubeia.firebase.api.mtt.lobby.MttAttributeMapper;
import com.cubeia.firebase.api.util.Arguments;

public class DefaultCreationParticipant implements CreationParticipant {
	
	private final MttAttributeMapper mapper;

	public DefaultCreationParticipant() {
		this(new DefaultMttAttributeMapper());
	}
	
	/**
	 * @param mapper Mapper to use, must not be null
	 */
	public DefaultCreationParticipant(MttAttributeMapper mapper) {
		Arguments.notNull(mapper, "mapper");
		this.mapper = mapper;
	}

	public LobbyPath getLobbyPathForTournament(MTTState mtt) {
		return new LobbyPath(mtt.getMttLogicId(), "", mtt.getId());		
	}

	public void tournamentCreated(MTTState mtt, LobbyAttributeAccessor acc) {
		Map<String, AttributeValue> atts = mapper.toMap(mtt);
		for (String key : atts.keySet()) {
			AttributeValue val = atts.get(key);
			acc.setAttribute(key, val);
		}
	}
}
