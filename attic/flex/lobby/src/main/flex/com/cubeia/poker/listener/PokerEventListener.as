/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.listener
{
	import com.cubeia.firebase.events.PacketEvent;
	import com.cubeia.firebase.io.protocol.SeatInfoPacket;
	import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
	import com.cubeia.poker.event.PokerEventDispatcher;
	import com.cubeia.poker.event.TableEvent;
	import com.cubeia.util.players.PlayerRegistry;
	
	/**
	 * Listener for local and global Poker Events
	 */
	public class PokerEventListener
	{
		public function PokerEventListener()
		{
			// PokerLobby.firebaseClient.addEventListener(PacketEvent.PACKET_RECEIVED, onFirebasePacket);
		}

		
		public function onFirebasePacket(event:PacketEvent):void {
			if (event.getObject().classId() == TableQueryResponsePacket.CLASSID) {
				handleTableQueryResponse(event);
			}
		}
		
		// Not used ATM
		private function handleTableQueryResponse(event:PacketEvent):void {
			var packet:TableQueryResponsePacket = event.getObject() as TableQueryResponsePacket;
			for each (var seat:SeatInfoPacket in packet.seats) {
				PlayerRegistry.instance.addPlayer(seat.player);
			}
		}
	}
}