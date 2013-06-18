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
package com.cubeia.firebase.server.commands;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.util.Arguments;


/*
 * Send by the master when as a response on the client
 * handshake with all config properties. Then on delta 
 * changes to all nodes.
 */

public class Config extends Command<ConfigProperty[]> {

	private static final long serialVersionUID = 2623388299078546190L;

	public static enum Type { DELTA_ADD, DELTA_REM, DELTA_MOD, DELTA_INIT }

	
	/// --- INSTANCE MEMBERS --- ///
	
	private final Type type;
	
	public Config(Type type) {
		super(Types.CONFIG.ordinal());
		Arguments.notNull(type, "type");
		this.type = type;
	}
	
	public Config(Type type, ConfigProperty[] props) {
		this(type);
		setAttachment(props);
	}

	public Type getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Config)) return false;
		return super.equals(obj) && ((Config)obj).type == type;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() ^ type.hashCode();
	}
}
