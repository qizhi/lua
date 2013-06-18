package com.cubeia.firebase.test.blackbox.tickets;

import java.io.IOException;

import org.testng.annotations.Test;

import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.game.tests.GameObjectRouteProcessor;
import com.cubeia.test.systest.io.protocol.ActivatorGameRoutePacket;

public class Ticket685Test extends LoginTest {

	private int seq = 0;
	
	@Test
	public void testSendToGame() throws IOException {
		
		/*
		 * Create a table
		 */
		GameTable table = super.createTable(4, GameObjectRouteProcessor.class);
		
		/*
		 * We must join, otherwise we wont get the message
		 */
		table.join(client, true);
		
		/*
		 * This is what we'll do: Send a request to the systest service, the service will
		 * forward the request to the game table
		 */
		ActivatorGameRoutePacket req = new ActivatorGameRoutePacket(client.getPlayerId(), table.getTableId());
		client.sendServicePacket(SystestService.class, seq++, req);
		
		/*
		 * Expect the same packet back
		 */
		client.expect(new FluidBuilder()
				.expect(new GameClassExpect(ActivatorGameRoutePacket.class, serializer))
				.where("tableId").is(table.getTableId()).andWhere("pid").is(client.getPlayerId()));
	}
}
