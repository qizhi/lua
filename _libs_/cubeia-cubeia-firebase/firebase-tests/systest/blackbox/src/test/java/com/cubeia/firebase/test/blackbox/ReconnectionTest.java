package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.Order;

public class ReconnectionTest extends LoginTest {

	@Test
	public void testReconnectNotification() throws Exception {
		/*
		 * Create two tables to use
		 */
		GameTable[] tables = super.createTables(2, 4);
	
		/*
		 * Join the first and watch the second
		 */
		tables[0].join(client, true);
		tables[1].watch(client, true);
		
		/*
		 * Logout without leaving the tables, 
		 * give the server a grace sleep and login
		 */
		logout(false);
		Thread.sleep(200);
		login();
		
		/*
		 * Expect a notify seated and a notify watching
		 */
		client.expect(new FluidBuilder()
				.expect(NotifySeatedPacket.class)
				.where("tableid").is(tables[0].getTableId())
				.and(NotifyWatchingPacket.class)
				.where("tableid").is(tables[1].getTableId())
				.in(Order.RANDOM));

		/*
		 * Unwatch
		 */
		tables[1].unwatch(client, true);		
	}
}
