package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.JoinResponseStatus;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class LeaveTest extends LoginTest {

	@Test
	public void testLeave() throws Exception {
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

		
		/*
		 * Leave and expect OK
		 */
		table.leave(client, true);
	}
}
