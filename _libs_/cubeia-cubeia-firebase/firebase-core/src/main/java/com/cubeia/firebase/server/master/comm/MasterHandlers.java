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

import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.mtt.comm.MttCreated;
import com.cubeia.firebase.mtt.comm.MttRemoved;
import com.cubeia.firebase.server.command.CommandHandlerContext;
import com.cubeia.firebase.server.command.CommandHandlerRegistry;
import com.cubeia.firebase.server.command.CommandNotRecognizedException;
import com.cubeia.firebase.server.commands.Handshake;
import com.cubeia.firebase.server.commands.Leave;
import com.cubeia.firebase.server.commands.Resume;
import com.cubeia.firebase.server.commands.Shun;
import com.cubeia.firebase.server.commands.TableCreated;
import com.cubeia.firebase.server.commands.TableRemoved;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.master.MasterNode;

public class MasterHandlers implements CommandHandlerRegistry<MasterNode>, Initializable<CommandHandlerContext<MasterNode>> {

	private CommandHandlerContext<MasterNode> con;
	private final Map<Class<?>, BaseHandler<? extends Command<?>>> registry;

	public MasterHandlers() {
		registry = new HashMap<Class<?>, BaseHandler<? extends Command<?>>>();
		populateRegistry();
	}
	
	public void init(CommandHandlerContext<MasterNode> con) throws SystemCoreException {
		Arguments.notNull(con, "context");
		this.con = con;
		initAll();
	}

	public void destroy() {
		this.con = null;
		for(BaseHandler<? extends Command<?>> h : registry.values()) {
			h.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Command<?>> BaseHandler<T> findHandler(T comm) throws CommandNotRecognizedException {
		Arguments.notNull(comm, "command");
		// Ugly cast, but we happend to know this is correct
		BaseHandler<T> h = (BaseHandler<T>)registry.get(comm.getClass());
		if(h == null) throw new CommandNotRecognizedException("command of class '" + comm.getClass().getName() + "' not recognized");
		else return h;
	}
	
	
	/// --- PRIVATE METHODS --- ///
	
	private void initAll() throws SystemCoreException {
		for(BaseHandler<? extends Command<?>> h : registry.values()) {
			h.init(con);
		}
	}

	private void populateRegistry() {
		registry.put(Handshake.class, new HandshakeHandler());
		registry.put(Leave.class, new LeaveHandler());
		registry.put(Shun.class, new ShunHandler());
		registry.put(Resume.class, new ResumeHandler());
		registry.put(TableCreated.class, new TableCreatedHandler());
		registry.put(TableRemoved.class, new TableRemovedHandler());
		registry.put(MttCreated.class, new MttCreatedHandler());
		registry.put(MttRemoved.class, new MttRemovedHandler());
	}
}
