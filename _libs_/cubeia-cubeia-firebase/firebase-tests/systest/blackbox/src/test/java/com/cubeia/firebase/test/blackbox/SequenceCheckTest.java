package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.SequenceCheckProcessor;
import com.cubeia.test.systest.io.protocol.SequenceCheckPacket;

public class SequenceCheckTest extends LoginTest {
	
	private int packets;

	
	// --- SETTERS --- //
	
	@BeforeClass
	@Parameters({ "packets" })
	public void setTestLength(
			@Optional("5") int packets) {
				this.packets = packets;
	}
	
	
	// --- TESTS --- //
	
	@Test
	public void testSequence() throws Exception {

		GameTable table = super.createTable(4, SequenceCheckProcessor.class);
		table.join(client, true);
		
		for (int i = 0; i < packets; i++) {
			client.sendGamePacket(new SequenceCheckPacket(i, i), table.getTableId());
		}
		
		for (int i = 0; i < packets; i++) {
			client.expect(new FluidBuilder()
					.expect(SequenceCheckPacket.class, serializer)
					.where("seq").is(i));
		}
	}
}
