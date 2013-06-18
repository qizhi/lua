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
package com.cubeia.firebase.server.util;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.commands.Config;
import com.cubeia.firebase.server.conf.ConfigDeltaListener;

public class CommandUtils {

	private CommandUtils() { }

	public static void forward(Config comm, ConfigDeltaListener listener) {
		Arguments.notNull(comm, "command");
		if(listener == null) return; // SANITY CHECK
		else if(isAddOrInit(comm)) listener.added(comm.getAttachment());
		else if(isRem(comm)) listener.removed(comm.getAttachment());
		else listener.modified(comm.getAttachment());
	}
	
	/// --- PRIVATE METHODS --- ///

	private static boolean isRem(Config con) {
		return (con.getType().equals(Config.Type.DELTA_REM));
	}

	private static boolean isAddOrInit(Config con) {
		return (con.getType().equals(Config.Type.DELTA_INIT) || con.getType().equals(Config.Type.DELTA_ADD));
	}
}
