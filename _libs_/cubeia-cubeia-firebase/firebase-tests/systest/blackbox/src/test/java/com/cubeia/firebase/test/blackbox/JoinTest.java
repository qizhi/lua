package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.JoinResponseStatus;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class JoinTest extends LoginTest {

	@Test
	public void testBadJoin() throws Exception {
		/*
		 * Create table and try to join a non-existing seat
		 */
		GameTable table = super.createTable(4);
		table.join(client, 66, false);

		/*
		 * Expect to be denied
		 */
		client.expect(new FluidBuilder()
			.expect(JoinResponsePacket.class)
			.where("status").is(JoinResponseStatus.DENIED));

	}
	
	@Test
	public void testJoin() throws Exception {
		/*
		 * Create table and try to join a seat
		 */
		GameTable table = super.createTable(4);
		table.join(client, 0, false);

		/*
		 * Expect OK
		 */
		client.expect(new FluidBuilder()
			.expect(JoinResponsePacket.class)
			.where("status").is(JoinResponseStatus.OK));

	}
}
