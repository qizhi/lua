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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.util.executor.JmxScheduler;

public abstract class BasePooledHandoff<T> implements HandoffExecutor {
	
	private static final int MAX_RESUBMIT_ATTEMPTS = 10;
	private static final long RESUBMIT_DELAY = 500;
	
	protected final SelectorSet forwardSet = new UnsafeSelectorSet();
	protected final List<EventListener<ChannelEvent>> listeners = new LinkedList<EventListener<ChannelEvent>>();
	
	protected final JmxScheduler exec;
	protected final EventType type;

	protected final Logger log = Logger.getLogger(getClass());

	protected BasePooledHandoff(String id, EventType type) {
		Arguments.notNull(type, "type");
		exec = new JmxScheduler(1, "JChannel Handoff Executor Pool Thread { " + id + " }");
		this.type = type;
	}
	
	protected abstract Runnable createSubmit(int queue, int attempt);
	
	protected abstract Runnable createRetract(int queue);
	
	protected abstract void pushback(int queue, T e);
	
	public void addEventListener(EventListener<ChannelEvent> list) {
		listeners.add(list);
	}
	
	public void removeEventListener(EventListener<ChannelEvent> list) {
		listeners.remove(list);
	}
	
	public int getCountListeners() {
		return listeners.size();
	}
	
	public void halt() {
		exec.halt();
	}
	
	public boolean isHalted() {
		return exec.isHalted();
	}
	
	public void resume() {
		exec.resume();
	}
	
	public void destroy() { 
		exec.stop();
	}
	
	
	// --- PACKAGE METHODS --- //
	
	public SelectorSet getForwardSet() {
		return forwardSet;
	}
	
	/**
	 * This method is called from the dqueue when an object is added
	 * and is available to read.
	 */
	// TODO Public?!
	public void submit(int queue) {
		exec.submit(createSubmit(queue, 0));
	}
	
	void resubmit(int queue, int attempt, T e) {
		if(attempt < MAX_RESUBMIT_ATTEMPTS) {
			pushback(queue, e); // PUT BACK! We're going to try again in a while...
			log.warn("Re-submitting queue " + queue + " as we're lacking listeners; attempt = " + attempt + "; delay = " + RESUBMIT_DELAY);
			exec.schedule(createSubmit(queue, attempt + 1), RESUBMIT_DELAY, MILLISECONDS);
		} else {
			log.error("Max re-submittal attempts " + MAX_RESUBMIT_ATTEMPTS + " reached! This event will be dropped: " + e);
		}
	}
	
	
	/**
	 * This method is called when an event is acknowledged by the event
	 * daemon and the queue is available for more execution.
	 */
	// TODO Public?!
	public void retract(int queue) {
		exec.submit(createRetract(queue));
	}
}
