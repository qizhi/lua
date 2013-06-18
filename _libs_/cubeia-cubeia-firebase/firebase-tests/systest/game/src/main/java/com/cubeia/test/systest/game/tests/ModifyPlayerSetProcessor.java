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

import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.ModifyPlayersPacket;

public class ModifyPlayerSetProcessor extends ProcessorBase {
	
	private static final long serialVersionUID = -5712457021992292249L;
	
    private transient Table table;
	
	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		this.table = table;
        ModifyPlayersPacket packet = (ModifyPlayersPacket)gamePacket;
		if (packet.seatPlayer) {
//		    seatPlayer(packet.pid, packet.seat);
		} else {
		    unseatPlayer(packet.pid);
		}
	}
//    
//    private void seatPlayer(int pid, int seat) {
//        GenericPlayer player = new GenericPlayer(pid, "NoName");
//        table.getPlayerSet().seatPlayer(player, seat);
//        
//    }
    
    private void unseatPlayer(int pid) {
        table.getPlayerSet().unseatPlayer(pid);
    }

}
