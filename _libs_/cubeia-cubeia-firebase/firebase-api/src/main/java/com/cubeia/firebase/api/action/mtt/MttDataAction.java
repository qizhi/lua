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
package com.cubeia.firebase.api.action.mtt;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.visitor.GameActionVisitor;
import com.cubeia.firebase.api.action.visitor.MttActionVisitor;

/**
 * Action that contains tournament specific data.
 */
public class MttDataAction extends AbstractMttAction {

	/** */
	private static final long serialVersionUID = 1L;

	/** If generated internally (i.e. scheduled) */
	private boolean internal = false;
	private final List<Attribute> attributes = new LinkedList<Attribute>();
	private byte[] data;
    private int playerId;
	
	public MttDataAction(int mttId, int playerId) {
		super(mttId);
        this.playerId = playerId;
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void accept(MttActionVisitor visitor) {
	    visitor.visit(this);
	}
	
	public void accept(GameActionVisitor visitor) {
	    visitor.visit(this);
	}
	
	public String toString() {
		return "Mtt Data Action : mttId["+getMttId()+"] Data(bytes)["+data.length+"]";
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

	public int getPlayerId() {
        return playerId;
    }
	
	public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
