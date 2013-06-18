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
package com.cubeia.firebase.api.game.table;

/**
 * <p>Container/Value Object for {@link TableInterceptor} return values.</p>
 *
 * <p>The response contains the following attributes:</br></br>
 * 
 * <b>allowed</b></br>
 * Used to flag if the player is allowed to join the table or not.</br></br>
 * 
 * <b>response code</b></br>
 * Game specific return code. For instance, the interceptor might perform an account
 * lookup to make sure the player has enough funds to join this table. If the funds
 * are low you will deny the seat request (allowed = false), but you also want to
 * tell client the reason why he/she was denied. This is what the response code is for.
 * </p>
 * 
 * <p>We have opted not to include a String-based message to force implementation into
 * using response codes instead of localized messaging. This has two advantages,</br>
 * 1. Codes are not localized to a specific language.</br>
 * 2. Strings are verbose and consume more bandwidth (both internally and externally).</br></br>
 * </p>
 * 
 * @author Fredrik
 */
public class InterceptionResponse {
	
	private boolean allowed = false;
	private int responseCode = -1;
	
	/**
	 * 
	 * 
	 * <p>Below are the implications of the allowed value:</p>
	 * 
	 * <p><b>True</b></br>
	 * If 'true' is returned, then the player will be added to this table.
	 * A subsequent call will then be made to all TableListeners for 
	 * 'PlayerJoined'. The number of players in the lobby will be incremented 
	 * by one since one player is added. <i>This is the default behaviour if 
	 * no TableInterceptor is specified.</i> The client will receive a join 
	 * response message with status ok and the supplied response code.
	 * </p>
	 * 
	 * <p><b>False</b></br>
	 * If 'false' is returned, then the player will not be added to the table.
	 * Since the player was denied, no TableListener or PlayerStatusChange will 
	 * be triggered. The number of  players in the lobby will not be changed. 
	 * The client will receive a join response message with status denied and
	 * the supplied response code.
	 * </p>
	 * 
	 * @param allowed
	 * @param responseCode, will be included in the response packet to the client.
	 */
	public InterceptionResponse(boolean allowed, int responseCode) {
		super();
		this.allowed = allowed;
		this.responseCode = responseCode;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public int getResponseCode() {
		return responseCode;
	}
	
	public String toString() {
		return "Interception Response - allowed["+allowed+"] code["+responseCode+"]";
	}
	
}
