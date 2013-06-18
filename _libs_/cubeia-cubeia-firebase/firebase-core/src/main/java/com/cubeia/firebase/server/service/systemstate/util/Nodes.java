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
package com.cubeia.firebase.server.service.systemstate.util;

import java.util.Map;

import com.cubeia.firebase.api.game.lobby.TableAttributeMapper;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class Nodes {

	private Nodes() { }
	
	public static final boolean isTableNode(String path, SystemStateServiceContract state) {
		Object test = state.getAttribute(path, TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
		return (test == null ? false : test.equals(TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE));
	}
	
	public static final boolean isTableNode(Map<Object, Object> attributes) {
		Object type = attributes.get(TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
		return (type == null ? false : type.equals(TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE));
	}
	
	public static final boolean isTournamentNode(Map<Object, Object> attributes) {
		Object type = attributes.get(TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
		return (type == null ? false : type.equals(TableAttributeMapper.MTT_NODE_TYPE_ATTRIBUTE_VALUE));
	}
}
