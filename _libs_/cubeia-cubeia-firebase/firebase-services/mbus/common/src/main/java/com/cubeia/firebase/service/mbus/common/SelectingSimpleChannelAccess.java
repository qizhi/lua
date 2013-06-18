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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.util.FirebaseLockFactory;
import com.cubeia.util.threads.NamedThreadFactory;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This fetcher receives event of a given type and orders them into queues 
 * by channel id. A separate thread runs in the background and "garbage collects"
 * empty queues.
 * 
 * @author Lars J. Nilsson
 */
public class SelectingSimpleChannelAccess implements SelectingSimpleChannelAccessMBean {
	
	private final static long GC_INTERVAL = 60;

	private final SimpleChannel channel;
	private final String id, interceptName;
	private final MBeanServer mbs;
	private final EventType type;

	private final ReadWriteLock masterLock = FirebaseLockFactory.createLock();
	private final Map<Integer, BlockingDeque<ChannelEvent>> queues = new ConcurrentHashMap<Integer, BlockingDeque<ChannelEvent>>();
	private final SelectorSet gcSuspected = new SafeSelectorSet();
	private SelectorNotifier notifier;
	
	private ScheduledExecutorService gcExecutor;

	private SimpleChannelReceiver receiver;

	
	/**
	 * @param id Fetcher id, must not be null
	 * @param type Fetcher event type, only events of this type will be recevied, must not be null
	 * @param mbs Bean server to use, must not be null
	 * @param channel Underlying channel to use, must not be null
	 * @param notifier Selector notifier, must not be null (if nor set later)
	 */
	public SelectingSimpleChannelAccess(String id, EventType type, MBeanServer mbs, SimpleChannel channel, SelectorNotifier notifier) {
		Arguments.notNull(id, "id");
		Arguments.notNull(mbs, "mbs");
		Arguments.notNull(channel, "channel");
		Arguments.notNull(type, "type");
		// Arguments.notNull(notifier, "notifier");
		this.notifier = notifier;
		this.type = type;
		this.id = id;
		this.mbs = mbs;
		this.channel = channel;
		this.interceptName = getClass().getName() + ":" + id;
		initGcThread();
		initReceiver();
		initJmx();
	}
	
	/**
	 * @param id Fetcher id, must not be null
	 * @param type Fetcher event type, only events of this type will be recevied, must not be null
	 * @param mbs Bean server to use, must not be null
	 * @param channel Underlying channel to use, must not be null
	 * @param selectorSet Selector set, must not be null
	 */
	public SelectingSimpleChannelAccess(String id, EventType type, MBeanServer mbs, SimpleChannel channel, SelectorSet set) {
		this(id, type, mbs, channel, new SelectorSetNotifier(set));
	}

	// Use with care !
	public void setSelectorNotifier(SelectorNotifier not) {
		notifier = not;
	}
	
	public String getInterceptName() {
		return interceptName;
	}
	
	public void destroy() { 
		//jchannel.shutdown();
		channel.setChannelreceiver(null);
		destroyJmx();
	}
	
	public String getLocalAddress() {
		return channel.getLocalAddress();
	}
	
	public ChannelEvent poll(int channel) {
		masterLock.readLock().lock();
		try {
			return doUnsafePoll(channel);
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	public void push(ChannelEvent e) {
		masterLock.writeLock().lock();
		try {
			BlockingDeque<ChannelEvent> queue = safeGetCreateQueue(e.getChannel());
			queue.addLast(e);
		} finally {
			masterLock.writeLock().unlock();
		}
	}
	
	public int size(int channel) {
		masterLock.readLock().lock();
		try {
			BlockingQueue<ChannelEvent> queue = queues.get(channel);
			return (queue == null ? 0 : queue.size());
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	
	// --- MBEAN METHODS --- //
	
	public int getAggregatedSize() {
		masterLock.readLock().lock();
		try {
			int i = 0;
			for (BlockingQueue<ChannelEvent> q : queues.values()) {
				i += q.size();
			}
			return i;
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	public int getChannelSize(int channel) {
		return size(channel);
	}
	
	public int getCountKnownChannels() {
		masterLock.readLock().lock();
		try {
			return queues.size();
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	public int[] getKnownChannels() {
		masterLock.readLock().lock();
		try {
			int c = 0;
			int[] arr = new int[queues.size()];
			for (int id : queues.keySet()) {
				arr[c++] = id;
			}
			return arr;
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	public int getLargestChannelSize() {
		masterLock.readLock().lock();
		try {
			int i = -1;
			for (BlockingQueue<ChannelEvent> q : queues.values()) {
				if(q.size() > i) {
					i = q.size();
				}
			}
			return i;
		} finally {
			masterLock.readLock().unlock();
		}
	}
	
	public int getCountSuspectedChannels() {
		return gcSuspected.size();
	}
	

	// --- PRIVATE METHODS --- //
	
	// lock elsewhere
	private ChannelEvent doUnsafePoll(int channel) {
		BlockingQueue<ChannelEvent> queue = queues.get(channel);
		if(queue != null) {
			ChannelEvent ev = queue.poll();
			if(queue.size() == 0) {
				gcSuspected.add(channel);
			}
			return ev;
		} else {
			return null;
		}
	}
	
	private void initGcThread() {
		gcExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SelectingJChannelFetcher-GC"));
		gcExecutor.scheduleWithFixedDelay(new SafeRunnable() {
		
			public void innerRun() {
				checkGc();
			}
		}, GC_INTERVAL, GC_INTERVAL, TimeUnit.SECONDS);
	}

	private void checkGc() {
		masterLock.writeLock().lock();
		try {
			Set<Integer> set = gcSuspected.get(true);
			for (int channel : set) {
				BlockingQueue<ChannelEvent> queue = queues.get(channel);
				if(queue.size() == 0) {
					queues.remove(channel);
				}
			}
		} finally {
			masterLock.writeLock().unlock();
		}
	}
	
	protected void doHandoff(ChannelEvent ce) {
		int channel = ce.getChannel();
		// StatsInterceptor.checkStamping(ce.getRoutedEvent(), interceptName);
		if(!checkReadLockHandoff(ce, channel)) {
			doWriteLockHandoff(ce, channel);
		}
	}

	private void doWriteLockHandoff(ChannelEvent ce, int channel) {
		masterLock.writeLock().lock();
		try {
			registerChannel(channel);
			BlockingQueue<ChannelEvent> queue = safeGetCreateQueue(channel);
			queue.add(ce);
		} finally {
			masterLock.writeLock().unlock();
		}
	}

	// needs write lock else where
	private BlockingDeque<ChannelEvent> safeGetCreateQueue(int channel) {
		BlockingDeque<ChannelEvent> queue = queues.get(channel);
		if(queue == null) {
			queue = new LinkedBlockingDeque<ChannelEvent>();
			queues.put(channel, queue);
		}
		return queue;
	}

	private void registerChannel(int channel) {
		gcSuspected.remove(channel);
		notifier.objectAdded(channel);
	}

	private boolean checkReadLockHandoff(ChannelEvent ce, int channel) {
		masterLock.readLock().lock();
		try {
			BlockingQueue<ChannelEvent> queue = queues.get(channel);
			if(queue != null) {
				queue.add(ce);
				registerChannel(channel);
				return true;
			} else {
				return false;
			}
		} finally {
			masterLock.readLock().unlock();
		}
	}

	private void initReceiver() {
		this.receiver = new SimpleChannelReceiver() {
		
			public void receive(ChannelEvent ce) {
				if(ce == null) return; // SANITY CHECK
				try {
					// Message e = (Message)o;
					// registerStats(e);
					// ChannelEvent ce = (ChannelEvent)ser.deserialize(e.getBuffer());
					if(ce.getType().equals(type)) {
						doHandoff(ce);
					}
				} catch(Exception ex) {
					Logger.getLogger(SelectingSimpleChannelAccess.class).error(ex);
				}
			}
		};
		channel.setChannelreceiver(receiver);
	}
	
	/*private void registerStats(Message e) {
		this.jchannel.incrementMessageStats(e.getLength());
	}*/

	private void initJmx() {
		try {
			ObjectName name = new ObjectName("com.cubeia.firebase.mbus.dqueue:type=SelectingJChannelAccess,id=" + id);
	        mbs.registerMBean(this, name);
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
    }
	
	private void destroyJmx() {
		try {
			ObjectName name = new ObjectName("com.cubeia.firebase.mbus.dqueue:type=SelectingJChannelAccess,id=" + id);
	        if(mbs.isRegistered(name)) {
	        	mbs.unregisterMBean(name);
	        }
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
	}
}
