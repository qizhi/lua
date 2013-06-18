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
package com.cubeia.firebase.service.mbus.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.service.mbus.common.StrictPooledHandoff;
import com.cubeia.firebase.service.mbus.common.StrictQueueReceiver;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.util.MBusListenerAdapter;

public class PartitionedQueues implements Haltable, PartitionedQueuesMBean {
	
	public static final AtomicInteger ID_GENERATOR = new AtomicInteger();

	private Map<Integer, StrictQueueReceiver<String>> mttReceivers;
	private StrictPooledHandoff mttHandoff;
	
	private Map<Integer, StrictQueueReceiver<String>> gameReceivers;
	private StrictPooledHandoff gameHandoff;
	
	private AtomicReference<OrphanEventListener<ChannelEvent>> gameOrphanListener;
	private AtomicReference<OrphanEventListener<ChannelEvent>> mttOrphanListener;

	private InternalMapping mapping;
	private MBeanServer mbs;
	
	public PartitionedQueues(InternalMapping mapping) {
		this.mapping = mapping;
		setupMembers();
		initListening();
	}
	
	public void init(MBeanServer serv) {
		this.mbs = serv;
		initJmx();
	}

	public void destroy() {
		gameHandoff.destroy();
		mttHandoff.destroy();
		destroyJmx();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	public void setGameOrphanListener(OrphanEventListener listener) {
		this.gameOrphanListener.set(listener);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setMttOrphanListener(OrphanEventListener listener) {
		this.mttOrphanListener.set(listener);
	}
	
	public OrphanEventListener<ChannelEvent> getGameOrphanListener() {
		return this.gameOrphanListener.get();
	}
	
	public OrphanEventListener<ChannelEvent> getMttOrphanListener() {
		return this.mttOrphanListener.get();
	}
	
	public EventSink getSinkForType(EventType type) {
		if(type == EventType.GAME) {
			return new GameQueue(this);
		} else if(type == EventType.MTT) {
			return new MttQueue(this);
		} else {
			throw new IllegalStateException("Type '" + type + "' is not allowed here...");
		}
	}
	
	
	// --- MBEAN --- //
	
	@Override
	public int getTotalGameQueueSize() {
		int i = 0;
		for (StrictQueueReceiver<String> r : gameReceivers.values()) {
			i += r.size();
		}
		return i;
	}
	
	@Override
	public int getTotalMttQueueSize() {
		int i = 0;
		for (StrictQueueReceiver<String> r : mttReceivers.values()) {
			i += r.size();
		}
		return i;
	}
	
	
	
	// --- HALTABLE --- //
	
	@Override
	public void halt() {
		gameHandoff.halt();
		mttHandoff.halt();
	}
	
	@Override
	public boolean isHalted() {
		return gameHandoff.isHalted();
	}
	
	@Override
	public void resume() {
		mttHandoff.resume();
		gameHandoff.resume();
	}
	
	
	// --- PACKAGE METHODS --- //
	
	Map<Integer, StrictQueueReceiver<String>> getGameReceivers() {
		return gameReceivers;
	}
	
	StrictPooledHandoff getGameHandoff() {
		return gameHandoff;
	}
	
	Map<Integer, StrictQueueReceiver<String>> getMttReceivers() {
		return mttReceivers;
	}
	
	StrictPooledHandoff getMttHandoff() {
		return mttHandoff;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	
	private void initJmx() {
		try {
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.mbus.local:type=PartitionedQueues");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.mbus.local:type=PartitionedQueues");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("failed to stop mbean", e);
		}
	}
	
	private void setupMembers() {
		gameOrphanListener = new AtomicReference<OrphanEventListener<ChannelEvent>>();
		mttOrphanListener = new AtomicReference<OrphanEventListener<ChannelEvent>>();
		gameReceivers = new ConcurrentHashMap<Integer, StrictQueueReceiver<String>>();
		gameHandoff = new StrictPooledHandoff(EventType.GAME, gameReceivers, false);
		mttReceivers = new ConcurrentHashMap<Integer, StrictQueueReceiver<String>>();
		mttHandoff = new StrictPooledHandoff(EventType.MTT, mttReceivers, false);
	}
	
	private void initListening() {
		mapping.addMBusListener(new MBusListenerAdapter() {
			
			@Override
			public void channelAdded(Partition part, Channel[] channels) {
				EventType type = part.getType();
				if(!isValidType(type)) return; // EARLY RETURN
				StrictPooledHandoff handoff = selectHandoff(type);
				Map<Integer, StrictQueueReceiver<String>> map = selectReceivers(type);
				for (Channel ch : channels) {
					map.put(ch.getId(), new QueueReceiverWrap());
					handoff.submit(ch.getId());
				}
			}

			@Override
			public void channelRemoved(Partition part, Channel[] channels) {
				EventType type = part.getType();
				if(!isValidType(type)) return; // EARLY RETURN
				Map<Integer, StrictQueueReceiver<String>> map = selectReceivers(type);
				for (Channel ch : channels) {
					map.remove(ch.getId());
				}
			}
			
			
			// --- PRIVATE METHODS --- //
			
			private boolean isValidType(EventType type) {
				return type == EventType.GAME || type == EventType.MTT;
			}
			
			private Map<Integer, StrictQueueReceiver<String>> selectReceivers(EventType type) {
				if(type == EventType.GAME) {
					return gameReceivers;
				} else {
					return mttReceivers;
				}
			}
			
			private StrictPooledHandoff selectHandoff(EventType type) {
				if(type == EventType.GAME) {
					return gameHandoff;
				} else {
					return mttHandoff;
				}
			}
		});
	}
}
