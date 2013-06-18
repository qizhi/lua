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
 * The Table Interceptor enables an easy hook for allowing/disallowing
 * players to join/leave the table. This interface is accessed by Firebase
 * by one of two methods: 1) The game implements TableInterceptorProvider; or 
 * 2) the game implements TableInterceptor directly. If the game implements both, the 
 * provider has precedence.
 * 
 * <p>Any method may return null, and this will be interpreted as an "assent", ie. an
 * {@link InterceptionResponse} with "allowed" as true and response code as -1.
 *
 * @author Fredrik
 */
public interface TableInterceptor {
	
	/**
	 * <p>This method will be called if the default seating rules
	 * deem that the player should be allowed to be seated.</p>
	 * 
	 * <p>This implementation then serves as a final decision.</p>
	 * 
	 * <p>Below are the implications of the returned allowed value 
	 * (InterceptionResponse.isAllowed):</p>
	 * 
	 * <p><b>True</b></br>
	 * If <code>true</code> is returned, then the player will be added to this table.
	 * A subsequent call will then be made to all {@link TableListener}s for 
	 * 'PlayerJoined'. The number of players in the lobby will be incremented 
	 * by one since one player is added. <i>This is the default behavior if 
	 * no TableInterceptor is specified.</i> The client will receive a join 
	 * response message with status ok and the supplied response code.
	 * </p>
	 * 
	 * <p><b>False</b></br>
	 * If <code>false</code> is returned, then the player will not be added to the table.
	 * Since the player was denied, no TableListener or PlayerStatusChange will 
	 * be triggered. The number of  players in the lobby will not be changed. 
	 * The client will receive a join response message with status denied and
	 * the supplied response code.
	 * </p>
	 * 
	 * <p>Calls to the TableListener method will be called on by the
	 * same executing thread that calls this method.</p>
	 * 
	 * <p>The parameter list can be used to supply additional information 
	 * needed by table or passwords for private tables.</p>
	 * 
	 * @param table
	 * @param request, Value Object for the request from the client
	 * @return {@link InterceptionResponse} with the necessary parameters, or null for assent
	 */
	public InterceptionResponse allowJoin(Table table, SeatRequest request);
	
	/**
	 * <p>This method will be called if the default seating rules
	 * deem that the player should be allowed to reserve a seat.</p>
	 * 
	 * <p>This implementation then serves as a final decision.</p>
	 * 
	 * <p>Below are the implications of the returned allowed value 
	 * (InterceptionResponse.isAllowed):</p>
	 * 
	 * <p><b>True</b></br>
	 * If <code>true</code> is returned, then the player will be added to this table with status
	 * set to RESERVATION. The number of players in the lobby will be incremented 
	 * by one since one player is added. <i>This is the default behavior if 
	 * no TableInterceptor is specified.</i> The client will receive a notify reserved
	 * message with status ok and the supplied response code. The client may then
	 * take the reserved seat by sending a join request, which will be handled as 
	 * a normal join.
	 * </p>
	 * 
	 * <p><b>False</b></br>
	 * If <code>false</code> is returned, then the player will not be added to the table.
	 * The number of  players in the lobby will not be changed. 
	 * </p>
	 * 
	 * <p>The call to the {@link TableListener} method will be made on the
	 * same executing thread that calls this method.</p>
	 * 
	 * @param table
	 * @param request, Value Object for the request from the client
	 * @return {@link InterceptionResponse} with the necessary parameters, or null for assent
	 */
	public InterceptionResponse allowReservation(Table table, SeatRequest request);
	
	/**
	 *  <p>This method will be called if the default seating rules
	 * deem that the player should be allowed to leave the table.
	 * <i>(Since the default seating rules currently always allow players
	 * to leave, then this method will be called for every player
	 * that signals a LeaveAction.)</i></p>
	 * 
	 * <p>Below are the implications of the returned value
	 * (InterceptionResponse.isAllowed):</p>
	 * 
	 * <p><b>True</b></br>
	 * If 'true' is returned, then the player will removed from the table.
	 * A subsequent call will then be made to all TableListeners for 
	 * 'PlayerLeft'. The number of players in the lobby will be decremented 
	 * by one since one player is removed. <i>This is the default behavior if 
	 * no TableInterceptor is specified.</i>
	 * </p>
	 * 
	 * <p><b>False</b></br>
	 * If 'false' is returned, then the player will not be removed from the table.
	 * Instead the status of the player will be changed to LEAVING. A subsequent call
	 * will be made to all TableListeners for 'PlayerStatusChanged'. The number of 
	 * players in the lobby will not be decremented. The client will receive a leave 
	 * response message with status denied and the supplied response code.
	 * </p>
	 * 
	 * <p>Calls to the TableListener method will be called on by the
	 * same executing thread that calls this method.</p>
	 * 
	 * @param table
	 * @param playerId
	 * @return {@link InterceptionResponse} with the necessary parameters, or null for assent
	 */
	public InterceptionResponse allowLeave(Table table, int playerId);
	
}
