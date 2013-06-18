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
package com.cubeia.firebase.server.master.comm;

import java.lang.reflect.InvocationTargetException;

import com.cubeia.firebase.api.command.CommandException;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.commands.Leave;
import com.cubeia.firebase.server.master.ClusterParticipant;
import com.cubeia.firebase.server.master.MasterNode;

public class LeaveHandler extends BaseHandler<Leave> {

	public Object handle(SocketAddress sender, Leave com) throws InvocationTargetException, CommandException {
		ClusterParticipant node = com.getAttachment();
		MasterNode master = con.getNode();
		boolean answer = master.leaveReceived(node);
		/*if(act.equals(HandshakeAction.SEND_CONFIG)) {
			sendConfig(sender);
		} else if(act.equals(HandshakeAction.SHUN_NODE)) {
			sendShun(sender);
		}*/
		return new Boolean(answer);
	}

	
	/// --- PRIVATE METHODS --- ///
	
	/*private void sendConfig(SocketAddress sender) throws CommandException {
		Config con = new Config(Config.Type.DELTA_INIT);
		con.setAttachment(cloneProperties());
		try {
			ClusterConnection conn = super.con.getCluster();
			conn.getCommandDispatcher().dispatch(con, sender);
		} catch (ClusterException e) {
			throw new CommandException(e);
		}
	}
	
	private void sendShun(SocketAddress sender) throws CommandException {
		try {
			ClusterConnection conn = super.con.getCluster();
			conn.getCommandDispatcher().dispatch(new Shun("handshake denied by master"), sender);
		} catch (ClusterException e) {
			throw new CommandException(e);
		}
	}
	
	private ConfigProperty[] cloneProperties() {
		ServerConfigProviderContract contr = con.getServices().getServiceInstance(ServerConfigProviderContract.class);
		return contr.getAllProperties();
	}*/
}
