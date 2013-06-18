package com.cubeia.firebase.test.blackbox;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.rules.Expect;
import com.cubeia.firebase.test.common.rules.impl.FilteredExpect;
import com.cubeia.firebase.test.common.rules.impl.MemberAssertFilter;
import com.cubeia.firebase.test.common.rules.impl.ServiceClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.io.protocol.GarResourceRequestPacket;
import com.cubeia.test.systest.io.protocol.GarResourceResponsePacket;
import com.cubeia.test.systest.io.protocol.Enums.ResponseStatus;

public class SarResourceTest extends LoginTest {

	private int seq = 0;
	
	@Test
	public void testResourceExists() throws Exception {
		GarResourceRequestPacket req = new GarResourceRequestPacket(client.getPlayerId(), seq++);
		client.sendServicePacket(SystestService.class, req.seq, req);
		
		Expect e = new ServiceClassExpect(GarResourceResponsePacket.class, serializer);
		FilteredExpect filt = new FilteredExpect(e, new MemberAssertFilter("status", ResponseStatus.OK));
		
		GarResourceResponsePacket pack = (GarResourceResponsePacket) client.expect(filt);
		
		Assert.assertEquals(pack.seq, req.seq);
	}
}
