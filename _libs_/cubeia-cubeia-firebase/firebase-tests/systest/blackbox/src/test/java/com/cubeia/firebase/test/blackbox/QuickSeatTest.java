package com.cubeia.firebase.test.blackbox;

import java.io.IOException;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.Enums.JoinResponseStatus;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.game.tests.ActivatorRouteProcessor;
import com.cubeia.test.systest.io.protocol.ActivatorTestGameResponsePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;
import com.cubeia.test.systest.io.protocol.QuickSeatRequest;

public class QuickSeatTest extends LoginTest {
	
	private int seq = 0;

	@Test
	public void testQuickSeat() throws IOException {
		/*
		 * Create table, we'll use the activator route test as a 
		 * 'ping' to make sure we can access the table
		 */
		GameTable table = super.createTable(4, ActivatorRouteProcessor.class);
		
		/*
		 * Send request to service
		 */
		QuickSeatRequest req = new QuickSeatRequest(client.getPlayerId(), table.getTableId());
		client.sendServicePacket(SystestService.class, seq++, req);
		
		/*
		 * Expect a join reponse
		 */
		client.expect(new FluidBuilder()
				.expect(JoinResponsePacket.class)
				.where("tableid").is(table.getTableId())
				.andWhere("status").is(JoinResponseStatus.OK)
		);
		
		/*
		 * Double check by sending a test packet to table
		 */
		ActivatorTestRequestPacket req1 = new ActivatorTestRequestPacket(-1, table.getTableId(), client.getPlayerId());
		client.sendGamePacket(req1, table.getTableId()); //, Collections.singletonList(new Attribute("kk", "kk")));
		
		/*
		 * Expect test packet response
		 */
		client.expect(new FluidBuilder()
			.expect(new GameClassExpect(ActivatorTestGameResponsePacket.class, serializer))
			.where("tableId").is(table.getTableId())
		);
	}
}
