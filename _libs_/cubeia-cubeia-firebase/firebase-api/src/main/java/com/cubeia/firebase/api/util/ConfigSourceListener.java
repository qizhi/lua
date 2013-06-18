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
package com.cubeia.firebase.api.util;

/**
 * A listener for configuration sources. 
 * 
 * @author lars.j.nilsson
 */
public interface ConfigSourceListener {

	/**
	 * @param src Added source, never null
	 */
	public void sourceAdded(ConfigSource src);
	
	
	/**
	 * @param src Modified source, never null
	 */
	public void sourceModified(ConfigSource src);
	
	
	/**
	 * @param src Removed source, never null
	 */
	public void sourceRemoved(ConfigSource src);
	
}
