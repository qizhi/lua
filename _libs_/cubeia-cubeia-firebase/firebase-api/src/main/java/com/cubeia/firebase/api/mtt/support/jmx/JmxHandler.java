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
package com.cubeia.firebase.api.mtt.support.jmx;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.mtt.support.MTTStateSupport;

public class JmxHandler {
	
	private final Logger log = Logger.getLogger(getClass());
	private ConcurrentMap<Integer, MttStats> statBeans = new ConcurrentHashMap<Integer, MttStats>();

	public JmxHandler() { }

	public ConcurrentMap<Integer, MttStats> getStatBeans() {
		return statBeans;
	}
	
	public void removeStatsBean(int id) {
		if (statBeans.containsKey(id)) {
			MttStats stats = statBeans.remove(id);
			removeFromJmx(id, stats);
		}
	}
	
	public MttStats getStatsBean(MTTStateSupport state, int id) {
		MttStats stats = statBeans.get(id);
		if(stats == null) {
			return createStatsBean(state);
		} else {
			return stats;
		}
	}			
	
	private MttStats createStatsBean(MTTStateSupport state) {
		MttStats stats = null;
		if (!statBeans.containsKey(state.getId())) {
			stats = new MttStats(state.getMttLogicId(), state.getId(), state.getName());
			statBeans.put(state.getId(), stats);
			addToJmx(state.getId(), stats);
		}
		return statBeans.get(state.getId());
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private void removeFromJmx(int id, MttStats stats) {
		String name = getJmxName(id, stats);
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName monitorName = new ObjectName(name);
			if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to unregister MttStats bean ["+stats.getMttInstanceId()+"]", e);
		}
	}

	private String getJmxName(int id, MttStats stats) {
		return "com.cubeia.firebase.mtt:type=" + ObjectName.quote(stats.getName() + " [" + stats.getMttInstanceId() + "]");
	}
	
	private void addToJmx(int id, MttStats stats) {
		String name = getJmxName(id, stats);
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName monitorName = new ObjectName(name);
	        mbs.registerMBean(stats, monitorName);
		} catch(Exception e) {
			log.error("failed to register MttStats bean ["+stats.getMttInstanceId()+"] to name '" + name + "'", e);
		}
	}
	
}
