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
package com.cubeia.firebase.util;

import com.cubeia.firebase.api.util.Arguments;

/**
 * Single object filter.
 * 
 * @author Larsan
 * @date 2007 maj 15
 */
public class TrivialFilter<T> implements Filter<T> {

	private final T object;

	/**
	 * @param object Obejct to accept, must not be null
	 */
	public TrivialFilter(T object) {
		Arguments.notNull(object, "object");
		this.object = object;
	}
	
	public boolean accept(T object) {
		return (this.object == object || this.object.equals(object));
	}
}
