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
package com.cubeia.firebase.api.common;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.ProbeAction;

/**
 * Small game action utility class.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 26
 */
public class ActionUtils {

	private ActionUtils() { }
	
	
	/**
	 * Check if the supplied action is a probe action, and if it is, add a 
	 * timestamp with the supplied class as checkpoint.
	 * 
	 * @param action Action to check, may be null
	 * @param checkpoint Class check point, must not be null
	 */
	public static void checkAddTimestamping(GameAction action, Class<?> checkpoint) {
		if(action instanceof ProbeAction) {
			((ProbeAction)action).addTimestamp(checkpoint);
		}
	}
}
