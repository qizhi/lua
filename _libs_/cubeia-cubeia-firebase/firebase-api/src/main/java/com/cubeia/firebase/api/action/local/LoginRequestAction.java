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
 * Wraps a login request from a client.
 * 
 * @author Fredrik
 *
 */
public class LoginRequestAction implements LocalAction {
	
	/** User / screeenname etc */
	private String user;
	
	/** Password */
	private String password;
	
	/** Operator id */
	private int operatorid = -1;
	
	/**
	 * Arbitrary credential data.
	 * May be used with third part login services.
	 */
	private byte[] data;
	
	/** Remote IP Address as reported by the socket server */
	private InetSocketAddress remoteAddress;
	
	
	/**
	 * Create a Login Request.
	 * 
	 * @param user
	 * @param password
	 */
	public LoginRequestAction(String user, String password, int operatorid) {
		super();
		this.user = user;
		this.password = password;
		this.operatorid = operatorid;
	}

	
	public String toString() {
		return "user["+user+"] pwd["+password.length()+"] opid["+operatorid+"] ip["+remoteAddress+"]";
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUser() {
		return user;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getOperatorid() {
		return operatorid;
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
