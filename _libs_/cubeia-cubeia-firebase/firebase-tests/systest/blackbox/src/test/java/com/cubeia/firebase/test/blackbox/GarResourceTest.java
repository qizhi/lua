package com.cubeia.firebase.test.blackbox;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.GarResourceProcessor;
import com.cubeia.test.systest.io.protocol.GarResourceRequestPacket;
import com.cubeia.test.systest.io.protocol.GarResourceResponsePacket;
import com.cubeia.test.systest.io.protocol.Enums.ResponseStatus;

public class GarResourceTest extends LoginTest {

	private int seq = 0;
	
	@Test
	public void testResourceExists() throws Exception {
		GameTable table = super.createTable(2, GarResourceProcessor.class);
		
		table.join(client, true);
	
		GarResourceRequestPacket req = new GarResourceRequestPacket(client.getPlayerId(), seq++);
		client.sendGamePacket(req, table.getTableId());
		
		GarResourceResponsePacket pack = (GarResourceResponsePacket) client.expect(new FluidBuilder()
				.expect(GarResourceResponsePacket.class, serializer)
				.where("status").is(ResponseStatus.OK), 10000);
		
		Assert.assertEquals(pack.seq, req.seq);
	}
}
