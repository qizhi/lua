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
package com.cubeia.firebase.service.activation;

/**
 * Enumeration for marking config source types. Currently two types
 * are recognized, game archives and tournament archives.
 * 
 * @see ActivationConfigManager
 * @see ActivationConfigSource
 * @author Lars J. Nilsson
 */
public enum ActivationType {

	/**
	 * Game archive config type.
	 */
	GAR,
	
	/**
	 * Tournament archive config type.
	 */
	TAR
	
}
