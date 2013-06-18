package com.cubeia.firebase.test.blackbox.tickets;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.test.blackbox.MultiClientTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameClient;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.ExpectFailure;
import com.cubeia.test.systest.game.tests.ModifyPlayerSetProcessor;
import com.cubeia.test.systest.io.protocol.ModifyPlayersPacket;

/*
 * Ticket #483
 * Seat and Unseat methods
 *
 * Check so that seat and unseat methods propagate correct
 * packets to the other players.
 * 
 * NOTE: We do not assert any notifications to the client that
 * is seated/unseated since the method does not specify any 
 * return packets.
 *
 */
public class Ticket483Test extends MultiClientTest {
	
	public Ticket483Test() {
		setNumberOfClients(2);
	}

	@Test
	public void testUnseatNotification() throws Exception {
		// Create a table and join two clients
	    GameTable table = super.createTable(4, ModifyPlayerSetProcessor.class);
	    
	    for (Client client : clients) {
	        table.join(client, true);
	    }
	    
	    GameClient client1 = clients.get(0);
	    GameClient client2 = clients.get(1);
	    
	    client2.sendGamePacket(new ModifyPlayersPacket(false, client2.getPlayerId(), -1), table.getTableId());
	    
	    client1.expect(new FluidBuilder()
        .expect(NotifyLeavePacket.class)
        .where("pid").is(client2.getPlayerId()));
	    
	    
	    client2.logout(false);
	    client2.disconnect();
	    super.connectClient(client2);
	    loginClient(client2);
	    
	    try {
    	    client2.expect(new FluidBuilder()
            .expect(NotifySeatedPacket.class)
            .where("tableid").is(table.getTableId()), 500);
    	    Assert.fail("Received unexpected SeatedNotification");
	    } catch (ExpectFailure e) {
	        // Correct behavior
	    }
	}
	
//	Test removed since we removed seatPlayer functionality
//	@Test
//    public void testSeatNotification() throws Exception {
//        // Create a table and join two clients
//        GameTable table = super.createTable(4, ModifyPlayerSetProcessor.class);
//        
//        GameClient client1 = clients.get(0);
//        GameClient client2 = clients.get(1);
//        
//        table.join(client1, true);
//        
//        client1.sendGamePacket(new ModifyPlayersPacket(true, client2.getPlayerId(), 3), table.getTableId());
//        
//        // Client 1 is already seated and should be notified of a new player joining in
//        client1.expect(new FluidBuilder()
//        .expect(NotifyJoinPacket.class)
//        .where("pid").is(client2.getPlayerId()));
//        
//        // Client 2 should not receive any notification(s)
//        try {
//            client2.expect(new FluidBuilder()
//            .expect(JoinResponseAction.class)
//            .where("pid").is(client2.getPlayerId()));
//            Assert.fail("Received unexpected JoinResponseAction");
//        } catch (ExpectFailure e) {
//            // Correct behavior
//        }
//        
//        
//        client2.logout(false);
//        client2.disconnect();
//        client2.connect(hostname, port);
//        loginClient(client2);
//        
//        // Client 2's previous session was seated at table and the new session should be notified about it
//        client2.expect(new FluidBuilder()
//        .expect(NotifySeatedPacket.class)
//        .where("tableid").is(table.getTableId()), 500);
//    }
	
	
	@Test
    public void testUnseatSeatedPlayer() throws Exception {
        // Create a table and join two clients
        GameTable table = super.createTable(4, ModifyPlayerSetProcessor.class);
        
        for (Client client : clients) {
            table.join(client, true);
        }
        
        GameClient client1 = clients.get(0);
        GameClient client2 = clients.get(1);
        
        client2.sendGamePacket(new ModifyPlayersPacket(false, client2.getPlayerId(), -1), table.getTableId());
        
        client1.expect(new FluidBuilder()
        .expect(NotifyLeavePacket.class)
        .where("pid").is(client2.getPlayerId()));
        
        
        client2.logout(false);
        client2.disconnect();
        super.connectClient(client2);
        loginClient(client2);
        
        try {
            client2.expect(new FluidBuilder()
            .expect(NotifySeatedPacket.class)
            .where("tableid").is(table.getTableId()), 500);
            Assert.fail("Received unexpected SeatedNotification");
        } catch (ExpectFailure e) {
            // Correct behavior
        }
    }
}
