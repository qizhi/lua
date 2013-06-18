package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.test.common.GameTable;

public class NotifySeatedTest extends LoginTest {
	
	@Test
	public void testNotifySeated() throws IOException {
		GameTable table = super.createTable(6);
		table.join(client, true);
	}
	
	@Test
	public void testNotifySeatedReconnect() throws Exception {
		GameTable table = super.createTable(6);
		table.join(client, true);
		
		// Close without leaving
		client.disconnect();
		
		// Login again
		login();
		
		// Check that we got a notify seated on reconnect
		NotifySeatedPacket packet = client.expectFirebasePacket(NotifySeatedPacket.class);
		assertEquals(table.getTableId(), packet.tableid);	
	}
}
