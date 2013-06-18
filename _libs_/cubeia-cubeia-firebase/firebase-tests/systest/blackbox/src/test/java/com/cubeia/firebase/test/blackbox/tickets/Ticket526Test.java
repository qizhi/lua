package com.cubeia.firebase.test.blackbox.tickets;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.test.blackbox.MultiClientTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

/*
 * Ticket #526 
 * Notify leave sent regardless if the player is seated
 *
 * If you send a leave table request to a table, then all players will get a leave 
 * table notification regardless if the player is actually seated at the table. 
 *
 */
public class Ticket526Test extends MultiClientTest {
	
	
	public Ticket526Test() {
		setNumberOfClients(2);
	}

	@Test
	public void testLeaveNotification() throws Exception {
		// Create a table and join two clients
	    GameTable table = super.createTable(4);
	    
	    for (Client client : clients) {
	        table.join(client, true);
	    }
	    
	    Client client1 = clients.get(0);
	    Client client2 = clients.get(1);
	    
	    // Client 2 leaves the table and client 1 should be notified
	    table.leave(client2, true);
	    
	    client1.expect(new FluidBuilder()
	        .expect(NotifyLeavePacket.class)
	        .where("pid").is(client2.getPlayerId()));
		    
	    // Re-send leave request (without expecting any response, we will handle that below)
	    table.leave(client2, false);
	    
	    client2.expect(new FluidBuilder()
	        .expect(LeaveResponsePacket.class)
	        .where("tableid").is(table.getTableId())
	        .andWhere("status").is(ResponseStatus.FAILED));
	    
	    boolean fail = false;
	    try {
	    	// We should *not* get this...
		    client1.expect(new FluidBuilder()
		        .expect(NotifyLeavePacket.class)
		        .where("pid").is(client2.getPlayerId()));
		    // Oooops...
		    fail = true;
	    } catch(Throwable th) { 
	    	// This is correct as the expect above should fail
	    }
	    if(fail) {
	    	Assert.fail("Got a leave notification from a non-sitting player.");
	    }
	}
}
