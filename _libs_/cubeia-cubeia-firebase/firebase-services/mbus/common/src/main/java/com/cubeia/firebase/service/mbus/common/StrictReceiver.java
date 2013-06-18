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
package com.cubeia.firebase.service.mbus.common;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Receiver;

/**
 * A wrapper object for the strict client event handoff.
 * 
 * <p>This receiver does not support orphan events listening.
 * 
 * @author Lars J. Nilsson
 */
public final class StrictReceiver implements Receiver<ChannelEvent>, StrictReceiverMBean {

	// private final JChannelReceiverHandoffExecutor exec;
	private final String ownerId;
	private final MBeanServer mbs;
	
	private final SimpleChannelPooledHandoff exec;
	
	/**
	 * @param ownerId Owner id, must not be null
	 * @param queue Handoff queue, must not be null
	 * @param exec Handoff executor, must not be null
	 */
	public StrictReceiver(String ownerId, SimpleChannelPooledHandoff exec, MBeanServer mbs) {
		Arguments.notNull(exec, "exec");
		Arguments.notNull(ownerId, "ownerId");
		Arguments.notNull(mbs, "mbs");
		this.mbs = mbs;
		this.ownerId = ownerId;
		this.exec = exec;
		// this.exec = exec;
		initJmx();
	}
	
	public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list) { }
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void destroy() { 
		exec.destroy();
		destroyJmx();
	}

	/*public boolean enabled(Feature feat) {
		return false; 
	}*/

	public String getForwardSetInfo() {
		return exec.getForwardSet().toString();
	}

	@Override
	public int getForwardSetSize() {
		return exec.getForwardSet().size();
	}
	
	public void addEventListener(EventListener<ChannelEvent> list) {
		exec.addEventListener(list);
	}
	
	public void removeEventListener(EventListener<ChannelEvent> list) {
		exec.removeEventListener(list);
	}
	
	public int getCountListeners() {
		return exec.getCountListeners();
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private void initJmx() {
		try {
			ObjectName name = new ObjectName("com.cubeia.firebase.mbus.dqueue:type=StrictRecevier,id=" + ObjectName.quote(ownerId));
	        mbs.registerMBean(this, name);
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
    }
	
	private void destroyJmx() {
		try {
			ObjectName name = new ObjectName("com.cubeia.firebase.mbus.dqueue:type=StrictRecevier,id=" + ObjectName.quote(ownerId));
	        if(mbs.isRegistered(name)) {
	        	mbs.unregisterMBean(name);
	        }
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
	}
}