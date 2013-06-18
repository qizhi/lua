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
package com.cubeia.firebase.api.action.local;

import java.net.InetSocketAddress;

import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Data wrapper for sending data to a local handler service.
 *
 * @author Fredrik
 */
public class LocalServiceAction implements LocalAction {

	private int sequence = -1;
	
	private byte[] data;
	
	private InetSocketAddress remoteAddress;
	
	public LocalServiceAction() { }
	
	public LocalServiceAction(int sequence) {
		this.sequence = sequence;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}
	
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

}
