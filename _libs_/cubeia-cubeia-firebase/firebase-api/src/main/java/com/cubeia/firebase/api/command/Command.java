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
package com.cubeia.firebase.api.command;

import java.io.Serializable;

/**
 * This class represents an internal command in the firebase cluster, or
 * else were. A command is serializable, it has an integer type and can 
 * optionally carry an attachment. Two commands are considered equal based on
 * type only.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public abstract class Command<T> implements Serializable {  
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 271789392134277259L;
	private T attachment;
	private final int type;
	private String source;
	
	/**
	 * @param type Command type
	 */
	protected Command(int type) {
		this.type = type;
	}
		
	/**
	 * @return Get command attachment, or null
	 */
	public T getAttachment() {
		return attachment;
	}

	/**
	 * @param attachment Set command attachment, may be null
	 */
	public void setAttachment(T attachment) {
		this.attachment = attachment;
	}
	
	/**
	 * @param source Source id, may be null
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * @return Source id, may be null
	 */
	public String getSource() {
		return source;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public String toString() {
		return type + "; source: " + source + "; att: " + attachment;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Command<?>)) return false;
		else return ((Command<?>)obj).type == type;
	}
	
	@Override
	public int hashCode() {
		return type;
	}
}
