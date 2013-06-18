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
package com.cubeia.firebase.service.action;

import com.cubeia.firebase.api.action.ActionId;

/**
 * This is a final implementation of an {@link ActionId} used
 * for security in the {@link Generator} service.
 * 
 * @see Generator
 * @author Lars J. Nilsson
 */
public final class Id extends ActionId {

	private static final long serialVersionUID = 2169946630165175899L;

	/**
	 * Create a new ID.
	 * 
	 * @param serverId String string ID, must not be null
	 * @param sequence Long sequence, must not be null
	 */
	Id(String serverId, long sequence) {
		super(serverId, sequence);
	}
}
