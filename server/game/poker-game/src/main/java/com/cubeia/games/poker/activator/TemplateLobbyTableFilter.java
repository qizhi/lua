/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.activator;

import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_TEMPLATE;

import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.common.AttributeValue.Type;
import com.cubeia.firebase.api.game.lobby.LobbyTableFilter;
import com.cubeia.games.poker.entity.TableConfigTemplate;

public class TemplateLobbyTableFilter implements LobbyTableFilter {

	private final TableConfigTemplate template;

	public TemplateLobbyTableFilter(TableConfigTemplate template) {
		this.template = template;
	}
	
	@Override
	public boolean accept(Map<String, AttributeValue> map) {
		return map.containsKey(TABLE_TEMPLATE.name()) && matches(map.get(TABLE_TEMPLATE.name()));
	}

	// --- PRIVATE METHODS --- //
	
	private boolean matches(AttributeValue val) {
		return val.getType() == Type.INT && val.getIntValue() == template.getId();
	}
}
