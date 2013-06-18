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
package com.cubeia.test.systest.game.tests;

import java.security.MessageDigest;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.StateTestPacket;

public class StateTestProcessor extends ProcessorBase {
	
	private static final long serialVersionUID = -5712457021992292249L;

	private String data = "";
	
	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		StateTestPacket packet = (StateTestPacket)gamePacket;
		data += packet.payload;
		if(packet.fail) {
			throw new RuntimeException("State test failure trigged");
		}
		packet.checksum = md5sum();
		packet.payload = data;
		GameDataAction act = createDataAction(playerId, table.getId(), packet);
		table.getNotifier().notifyPlayer(playerId, act);
	}

	// --- PRIVATE METHODS --- //
	
	private byte[] md5sum() {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(data.getBytes("UTF-8"));
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("Failed to compute checksum", e);
			return null;
		}
	}
}
