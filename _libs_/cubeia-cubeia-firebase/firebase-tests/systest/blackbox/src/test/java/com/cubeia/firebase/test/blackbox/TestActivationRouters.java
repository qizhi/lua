package com.cubeia.firebase.test.blackbox;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.GameTournament;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.Order;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.firebase.test.common.rules.impl.MttClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.game.tests.ActivatorRouteProcessor;
import com.cubeia.test.systest.io.protocol.ActivatorTestGameResponsePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestMttResponsePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;

public class TestActivationRouters extends LoginTest {

	private int seq = 0;
	
	@Test
	public void testRouters() throws Exception {
		
		/*
		 * Create a table an and mtt
		 */
		GameTable table = super.createTable(4, ActivatorRouteProcessor.class);
		GameTournament mtt = super.createTournament(5, 10, 10);
		
		/*
		 * We must join, otherwise we wont get the message
		 */
		table.join(client, true);
		
		/*
		 * This is what we'll do: Send a request to the systest service, the service will
		 * forward the request to the tournament and game activators, and both of the activators
		 * will forward to the table and the tournament. and finally the table and the tournament 
		 * will answer the request.... Pew...
		 */
		ActivatorTestRequestPacket req = new ActivatorTestRequestPacket(mtt.getTournamentId(), table.getTableId(), client.getPlayerId());
		List<Attribute> atts = Collections.singletonList(new Attribute("test1", "test2"));
		client.sendServicePacket(SystestService.class, seq++, req, atts);
		
		/*
		 * Expect two responses, in any order
		 */
		client.expect(new FluidBuilder()
				.expect(new MttClassExpect(ActivatorTestMttResponsePacket.class, serializer, atts))
				.where("mttId").is(mtt.getTournamentId())
				.and(new GameClassExpect(ActivatorTestGameResponsePacket.class, serializer, atts))
				.where("tableId").is(table.getTableId())
				.in(Order.RANDOM));
		
	}
}
