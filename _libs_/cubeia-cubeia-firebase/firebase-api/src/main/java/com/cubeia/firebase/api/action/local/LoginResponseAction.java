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

import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Server response for a request action.
 *
 * @author Fredrik
 */
public class LoginResponseAction implements LocalAction {
	
	public static int MAX_ERROR_MESSAGE_LENGTH = 128;
	
	/** Was the login accepted by the server? */
	private boolean accepted = false;
	
	/** Designated screenname */
	private String screenname;
	
	/** Resulting player id */
	private int playerid = -1;
	
	/** Standardized error codes from the server */
	private int errorCode = 0;
	
	/** 
	 * Special error message.
	 * For instance, if a third part supplier returns a failed
	 * login and a message, we might need to propagate the whole
	 * message to the client.
	 * 
	 * NOTE: Use with extreme caution! Sending string messages 
	 * to many clients is expensive.
	 * 
	 * The message will be truncated to a maximum length for
	 * safety.
	 */
	private String errorMessage = "";
	
	/** 
	 * Arbitrary credential data sent from the server.
	 * May be used with third party login services if needed.
	 */
	private byte[] data = new byte[0];
	
	/**
	 * Constructor. 
	 * 
	 * @param accepted
	 * @param playerid
	 */
	public LoginResponseAction(boolean accepted, String screenname, int playerid) {
		super();
		this.accepted = accepted;
		this.screenname = screenname;
		this.playerid = playerid;
	}
	
	/**
	 * Constructor. 
	 * 
	 * @param accepted
	 * @param playerid
	 */
	public LoginResponseAction(boolean accepted, int playerid) {
		super();
		this.accepted = accepted;
		this.playerid = playerid;
	}

	
	public String toString() {
		return "Login Response: pid["+playerid+"] screenname["+screenname+"] ok["+accepted+"]"; 
	}
	
	
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public int getPlayerid() {
		return playerid;
	}
	
	public String getScreenname() {
		return screenname;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}

	
}
