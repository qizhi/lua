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
package com.cubeia.firebase.io;

import com.cubeia.firebase.io.protocol.Attribute;
import com.cubeia.firebase.io.protocol.BadPacket;
import com.cubeia.firebase.io.protocol.CreateTableResponsePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelResponsePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.GoodPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelResponsePacket;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.MttPickedUpPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttSeatedPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyChannelChatPacket;
import com.cubeia.firebase.io.protocol.NotifyInvitedPacket;
import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.NotifyRegisteredPacket;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
import com.cubeia.firebase.io.protocol.PacketVisitor;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;
import com.cubeia.firebase.io.protocol.PingPacket;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.PlayerQueryResponsePacket;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
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
import com.cubeia.firebase.io.protocol.WatchResponsePacket;



/**
 * Extend this class if you want a handler that should 
 * process all server packets (from client to server).
 * 
 * This class ignores all server to client packets.
 * 
 * Created on 2006-sep-12
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public abstract class AbstractServerPacketHandler implements PacketVisitor {
	public void visit(PingPacket packet) { }
	public void visit(TableSnapshotPacket packet) {}
	public void visit(TableSnapshotListPacket packet) {}
	public void visit(TableUpdatePacket packet) {}
	public void visit(TableUpdateListPacket packet) {}
	public void visit(TableRemovedPacket packet) {}
	public void visit(Param packet) {}
	public void visit(Attribute packet) {}
	public void visit(ParamFilter packet) {}
	public void visit(GoodPacket packet) {}
	public void visit(BadPacket packet) {}
	public void visit(LoginResponsePacket packet) {}
	public void visit(PlayerInfoPacket packet) {}
	public void visit(NotifyChannelChatPacket packet) {}
	public void visit(NotifyJoinPacket packet) {}
	public void visit(NotifyLeavePacket packet) {}
	public void visit(JoinResponsePacket packet) {}
	public void visit(WatchResponsePacket packet) {}
	public void visit(FilteredJoinTableResponsePacket packet) {}
	public void visit(JoinChatChannelResponsePacket packet) {}
	public void visit(SystemMessagePacket packet) {}
	public void visit(NotifySeatedPacket packet) {}
	public void visit(NotifyWatchingPacket packet) {}
	public void visit(ForcedLogoutPacket packet) {}
	public void visit(FilteredJoinCancelResponsePacket packet) {}
	public void visit(FilteredJoinTableAvailablePacket packet) {}
	public void visit(PlayerQueryResponsePacket packet) {}
	public void visit(TableQueryResponsePacket packet) {}
	public void visit(SeatInfoPacket packet) {}
	public void visit(MttRegisterResponsePacket packet) {}
	public void visit(MttUnregisterResponsePacket packet) {}
	public void visit(TournamentSnapshotPacket packet) {}
	public void visit(TournamentSnapshotListPacket packet) {}
	public void visit(TournamentUpdatePacket packet) {}
	public void visit(TournamentUpdateListPacket packet) {}
	public void visit(TournamentRemovedPacket packet) {}
	public void visit(MttSeatedPacket packet) {}
	public void visit(MttPickedUpPacket packet) {}
	public void visit(NotifyInvitedPacket packet) {}
	public void visit(CreateTableResponsePacket packet) {}
	public void visit(NotifyRegisteredPacket packet) { }
}

