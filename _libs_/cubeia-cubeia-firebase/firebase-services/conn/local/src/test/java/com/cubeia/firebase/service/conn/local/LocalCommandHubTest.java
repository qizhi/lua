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
package com.cubeia.firebase.service.conn.local;

import junit.framework.TestCase;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandResponse;

public class LocalCommandHubTest extends TestCase {

	private LocalCommandHub hub;

	@Override
	protected void setUp() throws Exception {
		hub = new LocalCommandHub(LocalConstants.LOCAL_ADDRESS);
	}
	
	@Override
	protected void tearDown() throws Exception {
		hub.stop();
	}
	
	public void testAsynch() throws Exception {
		Retreiver l = new Retreiver();
		hub.addCommandListener(l);
		StringCommand c = new StringCommand("kalle");
		hub.dispatch(c);
		Thread.sleep(100);
		assertNotNull(l.c);
		assertEquals(l.c.command, c);
	}
	
	public void testSync() throws Exception {
		Reverser l = new Reverser();
		hub.addCommandListener("666", l);
		String s = "kalle";
		StringCommand c = new StringCommand("kalle");
		CommandResponse[] resp = hub.send("666", c);
		assertEquals(1, resp.length);
		s = new StringBuffer(s).reverse().toString();
		assertEquals(resp[0].getAnswer()[0], s);
	}
	
	
	
	// --- INNER CLASSES --- //
	
	private static class Reverser implements CommandListener {
		
		@Override
		public Object commandReceived(CommandMessage c) {
			StringCommand s = (StringCommand) c.command;
			return new StringBuffer(s.getAttachment()).reverse().toString();
		}
	}
	
	private static class Retreiver implements CommandListener {
		
		private CommandMessage c;

		@Override
		public Object commandReceived(CommandMessage c) {
			this.c = c;
			return null;
		}
	}
	
	private static class StringCommand extends Command<String> {
		
		private static final long serialVersionUID = 6105494332406831855L;

		private StringCommand(String msg) {
			super(666);
			setAttachment(msg);
		}
		
		@Override
		public boolean equals(Object obj) {
			return getAttachment().equals(((Command<?>)obj).getAttachment());
		}
	}
}
