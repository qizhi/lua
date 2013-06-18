package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.TableListenerProcessor;
import com.cubeia.test.systest.io.protocol.GeneralResponsePacket;

public class TableListenerTest extends MultiClientTest {
	
	private static final long TIMEOUT = 2000;

	public TableListenerTest() {
		setNumberOfClients(2);
	}

	@Test
	public void testLeaveWithDenial() throws Exception {
		/*
		 * Create table and try to join a seat
		 */
		GameTable table = super.createTable(4, TableListenerProcessor.class);
		table.join(client(0), 0, true);
		table.join(client(1), 1, true);

		/*
		 * Send leave and check that it is denied
		 */
		table.leave(client(0), false);
		
		/*
		 * Expect a general response (from the table listener) followed by 
		 * a DENIED
		 */
		client(0).expect(new FluidBuilder()
		    .expect(GeneralResponsePacket.class, serializer), TIMEOUT);
		
		client(0).expect(new FluidBuilder()
			.expect(LeaveResponsePacket.class)
			.where("status").is(ResponseStatus.DENIED).andWhere("code").is(666), TIMEOUT);
		
		client(0).expect(new FluidBuilder()
	    	.expect(GeneralResponsePacket.class, serializer), TIMEOUT);
				
		/*
		 * Expect a general packet client 1
		 */
		client(1).expect(new FluidBuilder()
			.expect(GeneralResponsePacket.class, serializer), TIMEOUT);

	}
}
