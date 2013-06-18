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
package com.cubeia.firebase.server.syslog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cubeia.firebase.api.syslog.EventType;
import com.cubeia.firebase.api.syslog.SystemEvent;
import com.cubeia.firebase.api.util.Range;
import com.cubeia.firebase.server.syslog.BoundedEventBuffer;

import junit.framework.TestCase;

public class BoundedEventBufferTest extends TestCase {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd");
	
	public void testAllAndOverflow() {
		BoundedEventBuffer b = new BoundedEventBuffer(3);
		b.event(newError("kalle"));
		b.event(newError("adam"));
		b.event(newError("olla"));
		b.event(newError("bertil"));
		SystemEvent[] events = b.getEvents(null);
		super.assertEquals(3, events.length);
		super.assertEquals("adam", events[0].getMessage());
		super.assertEquals("olla", events[1].getMessage());
		super.assertEquals("bertil", events[2].getMessage());
	}
	
	public void testBounds() {
		BoundedEventBuffer b = new BoundedEventBuffer(10);
		b.event(newError("kalle"));
		b.event(newError("adam"));
		b.event(newError("olla"));
		b.event(newError("bertil"));
		b.event(newError("anna"));
		SystemEvent[] events = b.query(null, null, new Range<Integer>(1, 3));
		super.assertEquals(3, events.length);
		super.assertEquals("adam", events[0].getMessage());
		super.assertEquals("olla", events[1].getMessage());
		super.assertEquals("bertil", events[2].getMessage());
	}
	
	public void testBounds2() {
		BoundedEventBuffer b = new BoundedEventBuffer(10);
		b.event(newError("kalle"));
		b.event(newError("adam"));
		b.event(newError("olla"));
		b.event(newError("bertil"));
		b.event(newError("anna"));
		SystemEvent[] events = b.query(null, null, new Range<Integer>(1, null));
		super.assertEquals(4, events.length);
		super.assertEquals("kalle", events[0].getMessage());
		super.assertEquals("adam", events[1].getMessage());
		super.assertEquals("olla", events[2].getMessage());
		super.assertEquals("bertil", events[3].getMessage());
	}

	public void testDateBounds() throws Exception {
		BoundedEventBuffer b = new BoundedEventBuffer(10);
		b.event(newError("kalle", "2007/05/02"));
		b.event(newError("adam", "2007/05/03"));
		b.event(newError("olla", "2007/05/04"));
		b.event(newError("bertil", "2007/05/05"));
		b.event(newError("anna", "2007/05/06"));
		SystemEvent[] events = b.query(null, new Range<Date>(SDF.parse("2007/05/03"), SDF.parse("2007/05/05")), null);
		super.assertEquals(3, events.length);
		super.assertEquals("adam", events[0].getMessage());
		super.assertEquals("olla", events[1].getMessage());
		super.assertEquals("bertil", events[2].getMessage());
	}
	
	// --- PRIVATE METHODS --- //
	
	private SystemEvent newError(String msg) {
		return new SystemEvent(msg, null, EventType.ERROR);
	}
	
	private SystemEvent newError(String msg, String date) throws ParseException {
		return new SystemEvent(msg, SDF.parse(date), EventType.ERROR);
	}
}
