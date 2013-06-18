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

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This receiver does not support orphan events listening.
 */
public final class SimpleChannelPooledHandoff extends BasePooledHandoff<ChannelEvent> implements Receiver<ChannelEvent> {

	private final SelectingSimpleChannelAccess fetcher;
	
	public SimpleChannelPooledHandoff(String id, EventType type, SelectingSimpleChannelAccess fetcher) {
		super(id, type);
		Arguments.notNull(fetcher, "fetcher");
		this.fetcher = fetcher;
		setNotifier();
	}
	
	public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list) { }

	public void destroy() { 
		fetcher.destroy();
		super.destroy();
	}
	
	
	// --- ABSTRACT METHODS --- //
	
	protected Runnable createRetract(int queue) {
		return new Retract(queue);
	}
	
	protected Runnable createSubmit(int queue, int attempt) {
		return new Submit(queue, attempt);
	}
	
	protected void pushback(int queue, ChannelEvent e) {
		fetcher.push(e);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setNotifier() {
		fetcher.setSelectorNotifier(new SelectorNotifier() {
			public void objectAdded(int id) {
				submit(id);
			}
		});
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
			if(!forwardSet.contains(queue)) {
				ChannelEvent e = fetcher.poll(queue);
				if(e != null) {
					forwardSet.add(queue);
					if(!doHandleEvent(queue, e)) {
						/*
						 * If not handled by any listener we need
						 * to resubmit for later...
						 */
						forwardSet.remove(queue);
						resubmit(queue, attempt, e);
					} 
				}
			}
		}

		private boolean doHandleEvent(int id, ChannelEvent e) {
			boolean handled = false;
			SimpleChannelPoolEvent ie = new SimpleChannelPoolEvent((Event<?>)e.getRoutedEvent(), type, id, SimpleChannelPooledHandoff.this);
			for (EventListener<ChannelEvent> list : listeners) {
				list.eventReceived(ie);
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
			if(fetcher.getChannelSize(queue) > 0) {
				submit(queue);
			}
		}
	}
}