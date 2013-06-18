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
/**
 * 
 */
package com.cubeia.firebase.server.master;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.service.conn.CommandListener;

class MasterCommListener implements CommandListener {
	
	private final MasterCommandExec exec;
	private final String masterId;
	//private final SocketAddress local;
	
	MasterCommListener(MasterCommandExec e, String masterId) {
		//this.local = local;
		this.masterId = masterId;
		this.exec = e;
	}
	
	public Object commandReceived(CommandMessage c) {
		if(!masterId.equals(c.command.getSource())) {
			return exec.execute(c);
		} else {
			return null;
		}
	}
}