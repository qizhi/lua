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
package com.cubeia.firebase.api.lobby;

public class LobbyPathUtil {
	

	/**
	 * Get the LobbyPath ancestor up to the Area root.
	 * 
	 * Example:
	 * /tables/99/a/b/(tableid) -> /tables/99/a -> /tables/99 -> /tables/99 
	 * 
	 * TODO: The implementation below can surely be optimized 
	 * 
	 * @param path
	 * @return
	 */
	public static LobbyPath getAncestor(LobbyPath path) {
		int area = path.getArea();
		
		String domain = path.getDomain();
		String[] split = domain.split("/");
		String[] trunc = new String[split.length-1]; 
			
		System.arraycopy(split, 0, trunc, 0, trunc.length);
		
		String dom = "/";
		for (String part : trunc) {
			dom += part+"/";
		}
		
		return new LobbyPath(path.getType(), area, dom, -1);
	
	}
}
