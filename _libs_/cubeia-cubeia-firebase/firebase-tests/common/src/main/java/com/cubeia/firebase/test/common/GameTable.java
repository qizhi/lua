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
package com.cubeia.firebase.test.common;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.protocol.JoinRequestPacket;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
import com.cubeia.firebase.io.protocol.UnwatchResponsePacket;
import com.cubeia.firebase.io.protocol.WatchRequestPacket;
import com.cubeia.firebase.io.protocol.WatchResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.JoinResponseStatus;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;
import com.cubeia.firebase.test.common.util.Parameters;

/**
 * This is a simple helper objects for tables in a Firebase test. If contains
 * a table id and helper methods for joining and watching with a client object.
 * 
 * @author Lars J. Nilsson
 */
public class GameTable {

	private final int tableId;

	/**
	 * @param creator Creator to use, must not be null
	 */
	public GameTable(TableCreator creator) {
		Arguments.notNull(creator, "creator");
		tableId = creator.create();
	}
	
	/**
	 * @param tableId Table id
	 */
	public GameTable(int tableId) {
		this.tableId = tableId;
	}

	/**
	 * Join a client to this table at a random seat. If the boolean parameter is true
	 * this method will automatically expect a join response and verify
	 * the the join succeeded.
	 * 
	 * @param client Client to join, must not be null
	 * @param expectResponse true to expect/verify a join response automatically
	 */
	public void join(Client client, boolean expectResponse) {
		join(client, -1, Parameters.emptyParams(), expectResponse);
	}
	
	/**
	 * Join a client to this table at a given seat. If the boolean parameter is true
	 * this method will automatically expect a join response and verify
	 * the the join succeeded.
	 * 
	 * @param client Client to join, must not be null
	 * @param seat Seat to join at, or -1 for a random seat
	 * @param expectResponse true to expect/verify a join response automatically
	 */
	public void join(Client client, int seat, boolean expectResponse) {
		join(client, seat, Parameters.emptyParams(), expectResponse);
	}
	
	/**
	 * Join a client to this table at a given seat with parameters. If the boolean parameter is true
	 * this method will automatically expect a join response and verify
	 * the the join succeeded.
	 * 
	 * @param client Client to join, must not be null
	 * @param seat Seat to join at, or -1 for a random seat
	 * @param params parameters to use, must not null
	 * @param expectResponse true to expect/verify a join response automatically
	 */
	public void join(Client client, int seat, List<Param> params, boolean expectResponse) {
		client.sendFirebasePacket(new JoinRequestPacket(tableId, (byte)seat, params));
		if(expectResponse) {
			JoinResponsePacket joinResponse = client.expectFirebasePacket(JoinResponsePacket.class);
			assertEquals(JoinResponseStatus.OK, joinResponse.status);
		}
	}
	
	/**
	 * Join a client to this table as a watcher. If the boolean parameter is true
	 * this method will automatically expect a watch response and verify
	 * the the watch succeeded.
	 * 
	 * @param client Client to join, must not be null
	 * @param expectResponse true to expect/verify a watch response automatically
	 */
	public void watch(Client client, boolean expectResponse) {
		client.sendFirebasePacket(new WatchRequestPacket(tableId));
		if(expectResponse) {
			WatchResponsePacket packet = client.expectFirebasePacket(WatchResponsePacket.class);
			assertEquals(WatchResponseStatus.OK, packet.status);
		}
	}
	
	/**
	 * @param client Client to remove, must not be null
	 * @param expectResponse true to expect/verify a watch response automatically
	 */
	public void unwatch(Client client, boolean expectResponse) {
		client.sendFirebasePacket(new UnwatchRequestPacket(tableId));
		if(expectResponse) {
			UnwatchResponsePacket packet = client.expectFirebasePacket(UnwatchResponsePacket.class);
			assertEquals(ResponseStatus.OK, packet.status);
		}
	}
	
	/**
	 * @param client Client to remove, must not be null
	 * @param expectResponse true to expect/verify a leave response automatically
	 */
	public void leave(Client client, boolean expectResponse) {
		client.sendFirebasePacket(new LeaveRequestPacket(tableId));
        if(expectResponse) {
        	LeaveResponsePacket packet = client.expectFirebasePacket(LeaveResponsePacket.class);
        	assertEquals(ResponseStatus.OK, packet.status);
        }
	}
	
	/**
	 * @return The table id
	 */
	public int getTableId() {
		return tableId;
	}
}
