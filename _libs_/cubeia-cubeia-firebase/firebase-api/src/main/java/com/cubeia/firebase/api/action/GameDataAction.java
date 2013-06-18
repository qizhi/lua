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
package com.cubeia.firebase.api.action;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * Action that contains game specific data.
 *  
 * @author Fredrik
 *
 */
public class GameDataAction extends AbstractPlayerAction {

	/** */
	private static final long serialVersionUID = 1L;

	/** If generated internally (i.e. scheduled) */
	private boolean internal = false;
	private final List<Attribute> attributes = new LinkedList<Attribute>();
	private byte[] data;
	
	public GameDataAction(int playerId, int tableId) {
		super(playerId, tableId);
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean traceDataAsHex) {
		String s = "Game Data Action : pid["+getPlayerId()+"] table["+getTableId()+"] Data (" + (data == null ? 0 : data.length) + " bytes)";
		if(traceDataAsHex) {
			String hex = toHex(data);
			s += " " + hex;
		}
		return s;
	}
	
	private String toHex(byte[] arr) {
		if(arr == null) return null; // SANITY CHECK
		StringBuilder b = new StringBuilder("{ ");
		int i = 0;
		for (byte tmp : arr) {
			i++;
			String s = Integer.toHexString(tmp);
			if(s.length() == 1) {
				s = "0" + s;
			}
			b.append(s);
			if(i % 8 == 0) {
				b.append(" ");
			}
		}
		return b.append(" }").toString();
	}
	
	/**
	 * Rewinds and returns a new bytebuffer
	 * @return
	 */
	public ByteBuffer getData() {
		return ByteBuffer.wrap(data);
	}

	/**
	 * Sets and rewinds the data bytebuffer.
	 * @param data
	 */
	public void setData(ByteBuffer src) {
		data = new byte[src.remaining()];
		src.get(data);
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	
}
