package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.ServiceClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.io.protocol.ServiceBroadcastRequestPacket;
import com.cubeia.test.systest.io.protocol.ServiceBroadcastResponsePacket;

public class ServiceBroadcastTest extends MultiClientTest {

	private int seq = 0;
	
	public ServiceBroadcastTest() {
		setNumberOfClients(5);
	}

	@Test
	public void testBroadcast() throws Exception {
		String payload = String.valueOf(System.currentTimeMillis());
		
		/*
		 * Use client one to request a test from the systest service
		 */
		ServiceBroadcastRequestPacket req = new ServiceBroadcastRequestPacket(payload);
		client(0).sendServicePacket(SystestService.class, seq++, req);
		
		for (int i = 0; i < clients.size(); i++) {
			
			/*
			 * Expect a response for all connected clients
			 */
			client(i).expect(new FluidBuilder()
						.expect(new ServiceClassExpect(ServiceBroadcastResponsePacket.class, serializer))
						.where("payload").is(payload));
			
		}
	}
}
