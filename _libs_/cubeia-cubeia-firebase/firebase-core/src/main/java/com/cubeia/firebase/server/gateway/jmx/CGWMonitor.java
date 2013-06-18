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
package com.cubeia.firebase.server.gateway.jmx;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.SecondCounter;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.statistics.Level;
import com.cubeia.firebase.server.statistics.StatisticsLevel;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;

public class CGWMonitor implements CGWMonitorMBean {
	
	private static SecondCounter gamePacketCounter = new SecondCounter();
	private final ClientNodeContext con;
	
	// public static AtomicLong localSeatedPlayers = new AtomicLong(0);
	private SystemStateServiceContract systemState;
	private String nodeId;
	
	public CGWMonitor(ClientNodeContext con) {
		this.con = con;
		nodeId = con.getNodeRouter().getId();
		systemState = con.getServices().getServiceInstance(SystemStateServiceContract.class);
	}

	public static void registerGamePacket() {
		if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
			gamePacketCounter.register();
		}
	}
	
	
	public int getAverageGamePacketsPerSecond() {
		if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
			return gamePacketCounter.current();
		} else {
			return -1;
		}
	}
	
	public long getLocalSeatedClients() {
		try {
		 	return LocallySeatedCalculator.countSeated(nodeId, systemState);
		} catch(Throwable th) {
			Logger.getLogger(getClass()).error("Failed to calculate seated players", th);
			return -1;
		}
	}
	
	public int getLocalClients() {
		ClientRegistryServiceContract registry = con.getServices().getServiceInstance(ClientRegistryServiceContract.class);
		if (registry != null) {
			return registry.getClientRegistry().getNumberOfClients();
		} else {
			return -1;
		}
	}
	
	public int getGlobalClients() {
		ClientRegistryServiceContract registry = con.getServices().getServiceInstance(ClientRegistryServiceContract.class);
		if (registry != null) {
			return registry.getClientRegistry().getNumberOfGlobalClients();
		} else {
			return -1;
		}
	}
}
