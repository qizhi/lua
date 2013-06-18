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
package com.cubeia.firebase.server.master;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandExecutor;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.server.command.CommandHandlerContext;
import com.cubeia.firebase.server.command.CommandNotRecognizedException;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.server.master.comm.MasterHandlers;
import com.cubeia.firebase.server.node.NodeContextAdapter;
import com.cubeia.firebase.service.conn.ClusterConnection;

class MasterCommandExec implements CommandExecutor {
	
	private final MasterHandlers registry;
	private final CommandHandlerContext<MasterNode> context;
		
	private final Logger log = Logger.getLogger(getClass());
	
	private final MasterNode parent;
	
	MasterCommandExec(MasterNode parent) throws SystemCoreException {
		this.parent = parent;
		context = new CommandContext();
		registry = new MasterHandlers();
		// slight hack... shouldn't cast /LJN
		// LocalSystemMasterProxy tmp = (LocalSystemMasterProxy)con.getMasterProxy();
		// registry.setConfigDeltaListener(commProxy.getConfigDeltaListener());
		registry.init(context);
	}
	
	public void destroy() {
		registry.destroy();
	}
	
	public Object execute(CommandMessage com) {
		try {
			return registry.findHandler(com.command).handle(com.sender, com.command);
		} catch(CommandNotRecognizedException e) {
			if(log.isTraceEnabled()) {
				log.trace("Unrecognized command", e);
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to execute command", e);
			return null;
		} 
	}
	
	private class CommandContext extends NodeContextAdapter implements CommandHandlerContext<MasterNode> {
		
		private CommandContext() {
			super(parent.getContext());
		}
		
		public MasterNode getNode() {
			return parent;
		}
		
		public ClusterConnection getCluster() {
			return parent.getClusterConnection();
		}
		
		public void resume(long haltId, ClusterLayout layout) {
			if(parent.isPrimary() && layout != null) {
				log.warn("primary master received layout update (resume command); are there several masters in the cluster? or is loopback set to true?");
			} else {
				if(layout != null) {
					parent.getNodeRegistry().registerAll(layout.getClusterNodes(), true);
				}
			}
		}
		
		public void halt(long haltId, String msg) {
			if(parent.isPrimary()) {
				log.warn("master received halt command; are there several masters in the cluster? or is loopback set to true?");
			}
		}
	}
}