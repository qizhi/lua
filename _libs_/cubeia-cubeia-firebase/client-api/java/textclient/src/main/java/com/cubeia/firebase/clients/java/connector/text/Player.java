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
package com.cubeia.firebase.clients.java.connector.text;


/**
 * A Simple User holder
 */
public class Player {
	/** The name of the user assocaited with this ID */
	private String username;
	/** The ID provided from the server */
	private int id;
	
	/**
	 * Create a new user ID based on what the server sent us
	 * 
	 * @param username The name of the user (if we have it)
	 * @param serverID The ID sent form the server
	 */
	public Player(String username, int id) {
		this.username = username;
		this.id = id;
	}

	
	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setId(int id) {
		this.id = id;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
		return "["+username+" ("+id+")]";
	}
}
