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

package com.cubeia.network.example.rps;

import com.cubeia.firebase.clients.java.connector.text.AbstractClientPacketHandler;
import com.cubeia.firebase.io.protocol.CreateTableResponsePacket;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.Enums.JoinResponseStatus;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelResponsePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.GameVersionPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelResponsePacket;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.KickPlayerPacket;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.MttPickedUpPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttSeatedPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyChannelChatPacket;
import com.cubeia.firebase.io.protocol.NotifyInvitedPacket;
import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.NotifyRegisteredPacket;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
import com.cubeia.firebase.io.protocol.PingPacket;
import com.cubeia.firebase.io.protocol.PlayerQueryResponsePacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.SystemInfoResponsePacket;
import com.cubeia.firebase.io.protocol.SystemMessagePacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
import com.cubeia.firebase.io.protocol.TableRemovedPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdateListPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentRemovedPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdateListPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.io.protocol.UnwatchResponsePacket;
import com.cubeia.firebase.io.protocol.VersionPacket;
import com.cubeia.firebase.io.protocol.WatchResponsePacket;

public class CommandHandler extends AbstractClientPacketHandler {

	private final Client client;

	public CommandHandler(Client client) {
		this.client = client;
	}

	@Override
	public void visit(GameTransportPacket packet) {
	    String cmd = new String(packet.gamedata);
	    System.out.println("Message from ["+packet.pid+"] : "+cmd);
	}

	@Override
	public void visit(VersionPacket arg0) {}

	@Override
	public void visit(GameVersionPacket arg0) {}

	@Override
	public void visit(SystemMessagePacket arg0) {}

	@Override
	public void visit(PingPacket arg0) {}

	@Override
	public void visit(LoginResponsePacket lrp) {}

	@Override
	public void visit(ForcedLogoutPacket arg0) {}

	@Override
	public void visit(SeatInfoPacket arg0) {}

	@Override
	public void visit(PlayerQueryResponsePacket arg0) {}

	@Override
	public void visit(SystemInfoResponsePacket arg0) {}

	@Override
	public void visit(JoinResponsePacket jrp) {
	    if (jrp.status == JoinResponseStatus.OK) {
	    	client.setTableId(jrp.tableid);
	    	System.err.println("joined table " + jrp.tableid);
	    } else {
	    	System.err.println("failed to join table: " + jrp.code);
	    }
	}

	@Override
	public void visit(WatchResponsePacket arg0) {}

	@Override
	public void visit(UnwatchResponsePacket arg0) {}

	@Override
	public void visit(LeaveResponsePacket lrp) {
		if (lrp.status == ResponseStatus.OK) {
			client.setTableId(null);
		}
		
	}

	@Override
	public void visit(TableQueryResponsePacket arg0) {}

	@Override
	public void visit(CreateTableResponsePacket arg0) {}

	@Override
	public void visit(NotifyInvitedPacket arg0) {}

	@Override
	public void visit(NotifyJoinPacket arg0) {}

	@Override
	public void visit(NotifyLeavePacket arg0) {}

	@Override
	public void visit(NotifyRegisteredPacket arg0) {}

	@Override
	public void visit(NotifyWatchingPacket arg0) {}

	@Override
	public void visit(KickPlayerPacket arg0) {}

	@Override
	public void visit(ServiceTransportPacket arg0) {}

	@Override
	public void visit(LocalServiceTransportPacket arg0) {}

	@Override
	public void visit(MttTransportPacket arg0) {}

	@Override
	public void visit(EncryptedTransportPacket arg0) {}

	@Override
	public void visit(JoinChatChannelResponsePacket arg0) {}

	@Override
	public void visit(NotifyChannelChatPacket arg0) {}

	@Override
	public void visit(TableSnapshotPacket arg0) {}

	@Override
	public void visit(TableUpdatePacket arg0) {}

	@Override
	public void visit(TableRemovedPacket arg0) {}

	@Override
	public void visit(TournamentSnapshotPacket arg0) {}

	@Override
	public void visit(TournamentUpdatePacket arg0) {}

	@Override
	public void visit(TournamentRemovedPacket arg0) {}

	@Override
	public void visit(TableSnapshotListPacket arg0) {}

	@Override
	public void visit(TableUpdateListPacket arg0) {}

	@Override
	public void visit(TournamentSnapshotListPacket arg0) {}

	@Override
	public void visit(TournamentUpdateListPacket arg0) {}

	@Override
	public void visit(FilteredJoinTableResponsePacket arg0) {}

	@Override
	public void visit(FilteredJoinCancelResponsePacket arg0) {}

	@Override
	public void visit(FilteredJoinTableAvailablePacket arg0) {}

	@Override
	public void visit(ProbePacket arg0) {}

	@Override
	public void visit(MttRegisterResponsePacket arg0) {}

	@Override
	public void visit(MttUnregisterResponsePacket arg0) {}

	@Override
	public void visit(MttSeatedPacket arg0) {}

	@Override
	public void visit(MttPickedUpPacket arg0) {}

	@Override
	public void visit(NotifySeatedPacket arg0) {}
	
}
