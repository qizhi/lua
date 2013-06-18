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

import java.io.Serializable;

/**
 * This class is used by the probe actions in order to save a 
 * checkpoint in the probe pathway. It stores a timestamp and
 * a class name checkpoint.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 26
 */
public final class Stamp implements Serializable {
	
	private static final long serialVersionUID = -8938410269194149198L;
	
	/**
	 * Class checkpoint name, usually full class name, may be null.
	 */
	public final String clazz;
	
	/**
	 * Millisecond timestamp.
	 */
	public final long timestamp;

	/**
	 * @param cl Class checkpoint, must not be null
	 * @param l Millisecond timestamp
	 */
	public Stamp(Class<?> cl, long l) {
		com.cubeia.firebase.api.util.Arguments.notNull(cl, "class");
		clazz = cl.getName();
		timestamp = l;
	}

	
	/**
	 * @param cl Class checkpoint, may be null
	 * @param l Millisecond timestamp
	 */
	public Stamp(String cl, long timestamp) {
		this.clazz = cl;
		this.timestamp = timestamp;
	}
}
