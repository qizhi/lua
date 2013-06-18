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

import org.testng.Assert;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.protocol.MttRegisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttUnregisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.TournamentRegisterResponseStatus;
import com.cubeia.firebase.test.common.util.Parameters;

/**
 * A simple helper object to use for tournaments. If contains
 * a tournament id and helper methods for registering.
 * 
 * @author Lars J. Nilsson
 */
public class GameTournament {

	private final int tournamentId;
	
	/**
	 * @param cr Creator to use, must not be null
	 */
	public GameTournament(TournamentCreator cr) {
		Arguments.notNull(cr, "creator");
		tournamentId = cr.create();
	}
	
	/**
	 * @param tournamentId Tournament id
	 */
	public GameTournament(int tournamentId) {
		this.tournamentId = tournamentId;
	}
	
	/**
	 * @return The tournament id
	 */
	public int getTournamentId() {
		return tournamentId;
	}

	/**
	 * Register a client to this tournament. If the boolean parameter is true
	 * this method will automatically expect a response and verify
	 * the the registration succeeded.
	 * 
	 * @param client Client to register, must not be null
	 * @param expectResponse true to expect/verify a response automatically
	 */
	public void register(Client client, boolean expectResponse) {
		client.sendFirebasePacket(new MttRegisterRequestPacket(tournamentId, Parameters.emptyParams()));
		if(expectResponse) {
			MttRegisterResponsePacket packet = client.expectFirebasePacket(MttRegisterResponsePacket.class);
			Assert.assertEquals(packet.status, TournamentRegisterResponseStatus.OK);
		}
	}
	
	/**
	 * @param client Client to unregister, must not be null
	 * @param expectResponse true to expect/verify a response automatically
	 */
	public void unregister(Client client, boolean expectResponse) {
		client.sendFirebasePacket(new MttUnregisterRequestPacket(tournamentId));
		if(expectResponse) {
			MttUnregisterResponsePacket packet = client.expectFirebasePacket(MttUnregisterResponsePacket.class);
			Assert.assertEquals(packet.status, TournamentRegisterResponseStatus.OK);
		}
	}
}
