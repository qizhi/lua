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
package com.cubeia.firebase.api.server;

import com.cubeia.firebase.api.util.Arguments;

/**
 * Node type. The type is one of "mtt", "game", "master" or "client",
 * and can be converted using the {@link #parse(String)} method.
 * 
 * @author Larsan
 */
public enum NodeType {
	
	GAME,
	MTT,
	MASTER,
	MANAGER,
	CLIENT;
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
	
	/**
	 * Parse a string to an enum object. This method
	 * throws an illegal argument exception if the string
	 * is null or not one of "client", "master", "game" or
	 * "mtt".
	 * 
	 * @param s String to parse, must be one of "client", "master", "game" or "mtt"
	 * @return The corresponding node type, never null
	 * @throws IllegalArgumentException If the string is not known or null
	 */
	public static NodeType parse(String s) {
		Arguments.notNull(s, "string");
		if(s.equalsIgnoreCase(GAME.toString())) {
			return GAME;
		} else if(s.equalsIgnoreCase(MTT.toString())) {
			return MTT;
		} else if(s.equalsIgnoreCase(CLIENT.toString())) {
			return CLIENT;
		} else if(s.equalsIgnoreCase(MASTER.toString())) {
			return MASTER;
		} else if(s.equalsIgnoreCase(MANAGER.toString())) {
			return MANAGER;
		} else {
			throw new IllegalArgumentException("String '" + s + "' not recognized; Excepted 'client', 'master', 'game' or 'mtt'");
		}
	}
}