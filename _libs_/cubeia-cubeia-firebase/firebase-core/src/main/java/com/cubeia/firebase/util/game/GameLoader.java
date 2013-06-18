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
package com.cubeia.firebase.util.game;

import java.lang.reflect.Constructor;

import com.cubeia.firebase.api.game.Game;

public class GameLoader {
	
	/** Logger */
	// @SuppressWarnings("unused")
	// private static Logger log = Logger.getLogger(GameLoader.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Game lookupGame(String clazz) { 
    	Game game = null;
    	try {
	        Class cl = Class.forName(clazz);
	        Constructor co = cl.getConstructor(new Class[]{});
	        if (co != null) {
	        	game = (Game)co.newInstance(new Object[]{});
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("You tried to create a Game for a bad class: '"+clazz+"' \n "+e.getMessage());
        }
		return game;
	}
}
