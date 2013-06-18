/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.common.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class JmxUtil {

    private final MBeanServer mbs;
	
	public JmxUtil() { 
		this(ManagementFactory.getPlatformMBeanServer());
	}
	
	public JmxUtil(MBeanServer server) {
		this.mbs = server;
	}

	public void mountBean(String name, Object bean) {
		try {
            ObjectName monitorName = new ObjectName(name);
            if(!mbs.isRegistered(monitorName)) {
                mbs.registerMBean(bean, monitorName);           	
            }
        } catch (Exception e) {
            Logger.getLogger(JmxUtil.class).error("failed to bind poker activator to JMX", e);
        }
	}

	public void unmountBean(String name) {
		try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName(name);
            if (mbs.isRegistered(monitorName)) {
                mbs.unregisterMBean(monitorName);
            }
        } catch (Exception e) {
        	Logger.getLogger(JmxUtil.class).error("failed to unbind poker activator to JMX", e);
        }
	}
}
