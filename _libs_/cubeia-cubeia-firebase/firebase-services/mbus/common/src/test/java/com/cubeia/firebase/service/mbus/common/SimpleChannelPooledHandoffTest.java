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

import static com.cubeia.firebase.service.messagebus.EventType.CLIENT;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import junit.framework.TestCase;

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;

public class SimpleChannelPooledHandoffTest extends TestCase {

	private static final int TEST_CHANNEL = 0;
	
	private InternalChannel channel;
	private SelectingSimpleChannelAccess channelAccess;
	private SimpleChannelPooledHandoff handoff;


	@Override
	protected void setUp() throws Exception {
		channel = new InternalChannel();
		channelAccess = new SelectingSimpleChannelAccess("test", CLIENT, mock(MBeanServer.class), channel, (SelectorNotifier)null);
		handoff = new SimpleChannelPooledHandoff("test", CLIENT, channelAccess);
	}
	
	@Override
	protected void tearDown() throws Exception {
		handoff.destroy();
	}
	
	public void testResubmitOnStartup() throws Exception {
		ChannelEvent e = new ChannelEvent(mock(Event.class), CLIENT, TEST_CHANNEL, false);
		/*
		 * Push an event to the hand-off BEFORE we've added any listener (this represents a
		 * state when the containing node is starting up, ie is initialized but not yet started). This
		 * event should be re-scheduled for 5 seconds before being dropped. 
		 */
		channel.push(e);
		/*
		 * Ugly wait here, but we need to wait until the handoff "submit" has being run, and so far
		 * there's no good way of knowing that.
		 */
		Thread.sleep(100);
		/*
		 * Add a listener and sleep again for the resubmit to go through.
		 */
		Listener listener = new Listener();
		handoff.addEventListener(listener);
		Thread.sleep(600); // Re-submit is 500, + 100 for safety
		/*
		 * At this point we should have gotten the event above in the listener...
		 */
		assertEquals(1, listener.events.size());
		/*
		 * The event itself won't be the same as it's being wrapped to add a correct
		 * "acknowledge", so we'll compare the routed event instead.
		 */
		assertTrue(e.getRoutedEvent() == listener.events.get(0).getRoutedEvent());
	}
	
	
	// --- INNER CLASSES --- //
	
	private static class Listener implements EventListener<ChannelEvent> {

		private List<ChannelEvent> events = new ArrayList<ChannelEvent>();

		@Override
		public void eventReceived(ChannelEvent event) {
			events.add(event);
		}
	}
	
	private static class InternalChannel implements SimpleChannel {

		private SimpleChannelReceiver recevier;

		private void push(ChannelEvent event) {
			this.recevier.receive(event);
		}
		
		@Override
		public void setChannelreceiver(SimpleChannelReceiver recevier) {
			this.recevier = recevier;
		}

		@Override
		public String getLocalAddress() {
			return "localhost";
		}
	}
}
