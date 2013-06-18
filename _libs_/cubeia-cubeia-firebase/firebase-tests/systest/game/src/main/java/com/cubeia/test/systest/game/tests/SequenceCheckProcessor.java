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

import java.util.Random;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.SequenceCheckPacket;

public class SequenceCheckProcessor extends ProcessorBase {
	
	private static final Random RANDOM = new Random();
	private static final int MAX_SLEEP_TIME = 100;

	private static final long serialVersionUID = -3557311378178648825L;
	
	private static final Logger log = Logger.getLogger(SequenceCheckProcessor.class);

	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		SequenceCheckPacket packet = (SequenceCheckPacket)gamePacket;
		packet.base++;
		waitRandom();
		GameDataAction act = createDataAction(playerId, table.getId(), packet);
		log.debug("Returning sequence " + packet.seq + " to player " + playerId);
		table.getNotifier().notifyPlayer(playerId, act);
	}

	
	// --- PRIVATE METHODS --- //
	
	private void waitRandom() {
		try {
			Thread.sleep(RANDOM.nextInt(MAX_SLEEP_TIME));
		} catch(InterruptedException e) { }
	}
}
