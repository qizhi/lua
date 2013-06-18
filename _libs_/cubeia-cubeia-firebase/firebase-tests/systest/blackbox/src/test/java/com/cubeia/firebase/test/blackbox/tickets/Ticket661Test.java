package com.cubeia.firebase.test.blackbox.tickets;

import static com.cubeia.firebase.io.protocol.Enums.LobbyType.MTT;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.test.systest.tournament.tests.StandardProcessor;

/*
 * Ticket #661
 * 
 * The test is to check that a subscription to two child
 * nodes ("a/b" and "a/c") does not imply a subscription to
 * parent node ("a").
 */
public class Ticket661Test extends LoginTest {

	@Test
	public void testSubscription() throws Exception {

		/*
		 * We'll use a UUID in order to make the test
		 * lobby path unique
		 */
		String id = UUID.randomUUID().toString();
		String parent = "Tournaments/" + id;
		String all = parent + "/All";
		String partner = parent + "/Partner-1";
		
		/*
		 * Create tournament for child one ("all") 
		 */
		super.createTournament(2, all, 1, 1, StandardProcessor.class);
		
		/*
		 * Subscribe...
		 */
		super.client.sendLobbySubscriptionPacket(MTT, MTT_ID, all);
		super.client.sendLobbySubscriptionPacket(MTT, MTT_ID, all);
		
		/*
		 * Hack: Wait for loby update
		 */
		Thread.sleep(200); //HACK!
		
		/*
		 * Count subscriptions per lobby path
		 */
		assertCount(1, all);
		assertCount(0, partner);
		assertCount(0, parent);
		
		/*
		 * Logout, wait for lobby and check again
		 */
		super.logout();
		Thread.sleep(500); // HACK!
		assertCount(0, all);
		assertCount(0, partner);
		assertCount(0, parent);
		
		/*
		 * Create tournament for child two ("partner")
		 */
		super.createTournament(2, partner, 1, 1, StandardProcessor.class);
		
		/*
		 * Login, subscribe again and wait for lobby
		 */
		super.login();
		super.client.sendLobbySubscriptionPacket(MTT, MTT_ID, all);
		super.client.sendLobbySubscriptionPacket(MTT, MTT_ID, partner);
		Thread.sleep(500); // HACK!
		
		/*
		 * Count subscriptions per lobby path
		 */
		assertCount(1, all);
		assertCount(1, partner);
		assertCount(0, parent);
		
	}
	
	
	// --- PRIVATE METHODS --- //

	private void assertCount(int i, String domain) {
		int count = getLobby().getSubscribersForPath(new LobbyPath(LobbyPathType.MTT, MTT_ID, domain, -1));
		Assert.assertEquals(count, i);
	}
}
