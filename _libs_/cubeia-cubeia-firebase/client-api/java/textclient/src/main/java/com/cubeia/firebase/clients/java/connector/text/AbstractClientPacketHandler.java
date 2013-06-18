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
package com.cubeia.firebase.clients.java.connector.text;

import com.cubeia.firebase.io.protocol.Attribute;
import com.cubeia.firebase.io.protocol.BadPacket;
import com.cubeia.firebase.io.protocol.ChannelChatPacket;
import com.cubeia.firebase.io.protocol.CreateTableRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.GoodPacket;
import com.cubeia.firebase.io.protocol.InvitePlayersRequestPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelRequestPacket;
import com.cubeia.firebase.io.protocol.JoinRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveChatChannelPacket;
import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
import com.cubeia.firebase.io.protocol.LobbyObjectSubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyObjectUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyQueryPacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.LogoutPacket;
import com.cubeia.firebase.io.protocol.MttRegisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterRequestPacket;
import com.cubeia.firebase.io.protocol.PacketVisitor;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.PlayerQueryRequestPacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.SystemInfoRequestPacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
import com.cubeia.firebase.io.protocol.WatchRequestPacket;



/**
 * Extend this class if you want a handler that should 
 * process all client packets (from server to client).
 * 
 * This class filters out (handles) all client to server packets.
 * 
 * Created on 2006-sep-12
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public abstract class AbstractClientPacketHandler implements PacketVisitor {
	public void visit(LoginRequestPacket packet) {}
	public void visit(LogoutPacket packet) {}
	public void visit(WatchRequestPacket packet) {}
	public void visit(UnwatchRequestPacket packet) {}
	public void visit(JoinChatChannelRequestPacket packet) {}
	public void visit(LeaveChatChannelPacket packet) {}
	public void visit(ChannelChatPacket packet) {}
	public void visit(JoinRequestPacket packet) {}
	public void visit(LeaveRequestPacket packet) {}
	public void visit(LobbyQueryPacket packet) {}
	public void visit(GoodPacket packet) {}
	public void visit(BadPacket packet) {}
	public void visit(PlayerInfoPacket packet) {}
	public void visit(TableChatPacket packet) {}
	public void visit(GameTransportPacket packet) {}
	public void visit(ParamFilter packet) {}
	public void visit(Param packet) {}
	public void visit(Attribute packet) {}
	public void visit(ProbeStamp packet) {}
	public void visit(FilteredJoinTableRequestPacket packet) {}
	public void visit(LobbySubscribePacket packet) {}
	public void visit(LobbyUnsubscribePacket packet) {}
	public void visit(FilteredJoinCancelRequestPacket packet) {}
	public void visit(TableQueryRequestPacket packet) {}
	public void visit(PlayerQueryRequestPacket packet) {}
	public void visit(MttRegisterRequestPacket packet) {}
	public void visit(MttUnregisterRequestPacket packet) {}
	public void visit(CreateTableRequestPacket packet) {}
	public void visit(InvitePlayersRequestPacket packet) {}
	public void visit(LobbyObjectSubscribePacket packet) {}
    public void visit(LobbyObjectUnsubscribePacket packet) {}
    public void visit(SystemInfoRequestPacket packet) {}
}

