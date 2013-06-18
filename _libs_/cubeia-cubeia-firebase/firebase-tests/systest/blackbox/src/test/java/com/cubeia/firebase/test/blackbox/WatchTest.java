package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class WatchTest extends MultiClientTest {

	public WatchTest() {
		setNumberOfClients(2);
	}

	@Test
	public void testWatch() throws Exception {
		/*
		 * Create table and watch with client 1
		 */
		GameTable table = super.createTable(2);
		table.watch(client(0), true);
		
		/*
		 * Join with client 2
		 */
		table.join(client(1), true);
		
		/*
		 * We should expect a notify with client 1 id
		 */
		client(0).expect(new FluidBuilder()
					.expect(NotifyJoinPacket.class)
					.where("pid").is(client(1).getPlayerId()));
		
		/*
		 * Unwatch
		 */
		table.unwatch(client(0), true);	
	}
}
