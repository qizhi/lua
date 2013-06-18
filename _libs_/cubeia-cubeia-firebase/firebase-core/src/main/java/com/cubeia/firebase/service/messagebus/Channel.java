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
package com.cubeia.firebase.service.messagebus;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;

/**
 * This class represents a message bus channel. The channel is usually acting
 * as a stand-in for an actual remote target. For example, in an ordinary table
 * game the channel usually represents a table. This is a immutable final bean
 * holding the channel name and id.
 * 
 * @author Larsan
 */
public final class Channel implements Serializable {

	private static final long serialVersionUID = -7611668432264270490L;
	
	private final int id;
	private final String name;
	
	/**
	 * @param id Channel id, may be negative
	 * @param name Channel readable name, may not be null
	 */
	public Channel(int id, String name) {
		Arguments.notNull(name, "name");
		this.name = name;
		this.id = id;
	}
	
	/**
	 * @return The channel id, may be negative
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The channel readable name, never null
	 */
	public String getName() {
		return name;
	}
	
	
	// --- PUBLIC METHODS --- //
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Channel)) return false;
		else return ((Channel)obj).id == id;
	}
	
	@Override
	public int hashCode() {
		return 7 ^ id;
	}
	
	@Override
	public String toString() {
		return "Channel " + id + " [" + name + "]";
	}
}