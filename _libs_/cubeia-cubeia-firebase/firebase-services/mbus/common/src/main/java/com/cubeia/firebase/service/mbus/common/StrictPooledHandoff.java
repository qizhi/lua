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
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.util.FirebaseLockFactory;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This hand-off executor uses a single thread that sits between the dqueue channels
 * and the mbus receivers. The thread works with tasks executed in order, the "submit" 
 * is queued when a channel *may* be ready, the "retract" when a channel have been
 * executed, ie when a receiver acknowledges an event. Since the thread pool is single 
 * threaded only one "submit" or "retract" will be executed at any point, and strict 
 * ordering is kept. 
 * 
 * @author Lars J. Nilsson
 */
public class StrictPooledHandoff extends BasePooledHandoff<KeyedQueueObject<String>> {

	private boolean isOpen = false;
	/*
	 * Lock used to make sure we can't process an event and add/remove 
	 * a listener at the same time.
	 */
	private final ReadWriteLock eventLock = FirebaseLockFactory.createLock();
	
	/*
	 * A map of the current channels. This map is updated concurrently
	 * by the dqueue group.
	 */
	private final Map<Integer, StrictQueueReceiver<String>> internalQueues;
	
	public StrictPooledHandoff(EventType type, Map<Integer, StrictQueueReceiver<String>> internalQueues, boolean isHalted) {
		super(type.toString(), type);
		Arguments.notNull(type, "type");
		Arguments.notNull(internalQueues, "internalQueues");
		this.internalQueues = internalQueues;
		if(isHalted) {
			exec.halt();
		}
	}
	
	public boolean isValid(int channel) {
		return internalQueues.containsKey(channel);
	}
	
	@Override
	public void addEventListener(EventListener<ChannelEvent> list) {
		eventLock.writeLock().lock();
		try {
			listeners.add(list);
			if(listeners.size() == 1) {
				/*
				 * If this is the first listener will add all
				 * channels to make sure no event is missed.
				 */
				addAllChannels();
				isOpen = true;
			}
		} finally {
			eventLock.writeLock().unlock();
		}
	}

	@Override
	public void removeEventListener(EventListener<ChannelEvent> list) {
		eventLock.writeLock().lock();
		try {
			listeners.remove(list);
			if(listeners.size() == 0) {
				/*
				 * Last listener, we can stop de-queueing
				 */
				isOpen = false;
			}
		} finally {
			eventLock.writeLock().unlock();
		}
	}

	
	// --- ABSTRACT METHODS --- //
	
	protected Runnable createRetract(int queue) {
		return new Retract(queue);
	}
	
	@Override
	protected Runnable createSubmit(int queue, int attempt) {
		return new Submit(queue, attempt);
	}
	
	@Override
	protected void pushback(int queue, KeyedQueueObject<String> o) {
		StrictQueueReceiver<String> q = internalQueues.get(queue);
		if(q != null) {
			q.push(o);
		} else {
			log.error("Failed to push back object, this object will be dropped! " + o);
		}
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	/*
	 * When the first listener is added we double check
	 * all channels in order to make sure we're not missing any events
	 */
	private void addAllChannels() {
		int count = internalQueues.size();
		log.debug("Resubmitting " + count + " queues on listener addition");
		for (int id : internalQueues.keySet()) {
			submit(id);
		}
		int check = internalQueues.size();
		if(count != check) {
			log.warn("Queue added or removed during initial listener resubmition; was " + count + " but is " + check);
		}
	}
	
	
	// --- INNER CLASSES --- //
	
	private final class Submit extends SafeRunnable {
		
		private final int queue;
		private final int attempt;
		
		private Submit(int queue, int attempt) {
			this.queue = queue;
			this.attempt = attempt;
		}
		
		public void innerRun() {
			eventLock.readLock().lock();
			try {
				if(isOpen) {
					doRun();
				}
			} finally {
				eventLock.readLock().unlock();
			}
		}

		private void doRun() {
			if(!forwardSet.contains(queue)) {
				StrictQueueReceiver<String> q = internalQueues.get(queue);
				/*
				 * At this point the queue receiver may be null, this indicates that
				 * the queue has been removed since the submission was done. And this may
				 * indeed happen if a fail-over is performed.
				 */
				if(q != null) {
					selectOneFromQueue(q, queue);
				} else {
					log.debug("Submit ignored for queue " + queue + " due to missing receiver; type: " + type.toString());
				}
			}
		}

		private void selectOneFromQueue(StrictQueueReceiver<String> q, int id) {
			KeyedQueueObject<String> o = q.dequeue();
			if(o == null) {
				if(log.isTraceEnabled()) {
					log.trace("Submit cut-short as queue " + id + " is empty");
				}
				return; // SANITY CHECK
			} else {
				forwardSet.add(id);
				if(!doHandleEvent(q, id, o)) {
					/*
					 * If not handled by any listener we need
					 * to resubmit for later...
					 */
					forwardSet.remove(id);
					resubmit(queue, attempt, o);
				}
			}
		}

		private boolean doHandleEvent(StrictQueueReceiver<String> q, int id, KeyedQueueObject<String> o) {
			boolean handled = false;
			StrictPoolEvent ie = new StrictPoolEvent((Event<?>)o.getObject(), type, id, o.getKey(), q, StrictPooledHandoff.this);
			for (EventListener<ChannelEvent> el : listeners) {
				el.eventReceived(ie);
				handled = true;
			}
			return handled;
		}
	}

	private final class Retract extends SafeRunnable {
		
		private final int queue;

		private Retract(int queue) {
			this.queue = queue;
		}
		
		public void innerRun() {
			forwardSet.remove(queue);
			StrictQueueReceiver<String> q = internalQueues.get(queue);
			if(q != null && q.size() > 0) {
				StrictPooledHandoff.this.submit(queue);
			} else {
				if(q == null) {
					log.debug("Retraction for queue " + queue + " not submitting since the queue was not found; type: " + type.toString());
				}
			}
		}
	}
}
