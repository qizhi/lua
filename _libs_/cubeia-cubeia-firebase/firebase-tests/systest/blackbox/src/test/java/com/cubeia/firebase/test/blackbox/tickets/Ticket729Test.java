package com.cubeia.firebase.test.blackbox.tickets;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.ExpectFailure;
import com.cubeia.test.systest.game.tests.TablePingProcessor;
import com.cubeia.test.systest.io.protocol.TablePingPacket;

public class Ticket729Test extends LoginTest {
	
	@Test
	public void testForceLogoutOnDisconnect() throws Exception {
		Client client2 = connectClientTwo();
	
		client.expect(new FluidBuilder().expect(ForcedLogoutPacket.class));
		
		client2.disconnect();
	}
	
	@Test
	public void testPingTableAfterForcedLogout() throws Exception {
		GameTable table = super.createTable(4, TablePingProcessor.class);
		table.join(client, true);
		
		Client client2 = connectClientTwo();
		table.join(client2, true);
		
		client.sendGamePacket(new TablePingPacket(client.getPlayerId()), table.getTableId());
		
		try {
			client2.expect(new FluidBuilder()
						.expect(TablePingPacket.class, serializer)
						.where("pid").is(client.getPlayerId()), 1000);
			fail("Client 2 incorrectly received a table ping packet");
		} catch(ExpectFailure e) {
			// This is correct...
		}
	}

	
	// --- PRIVATE METHODS --- //
	
	private Client connectClientTwo() throws IOException, GeneralSecurityException {
		Client client2 = super.newClient();
		super.connectClient(client2);
		client2.login(username, password, true);
		return client2;
	}
}
