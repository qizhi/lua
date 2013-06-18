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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.command.CommandHandler;
import com.cubeia.firebase.server.command.CommandHandlerContext;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.master.MasterNode;

public abstract class BaseHandler<T extends Command<?>> implements CommandHandler<T, MasterNode> {

	protected final Logger log = Logger.getLogger(getClass());
	protected CommandHandlerContext<MasterNode> con;

	public void destroy() {
		con = null;
	}

	public void init(CommandHandlerContext<MasterNode> con) throws SystemCoreException {
		Arguments.notNull(con, "context");
		this.con = con;
	}
}
