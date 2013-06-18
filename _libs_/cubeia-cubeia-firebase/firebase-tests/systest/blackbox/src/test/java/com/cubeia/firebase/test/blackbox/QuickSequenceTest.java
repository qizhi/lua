package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.BeforeClass; 
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.QuickSequenceCheckProcessor;
import com.cubeia.test.systest.io.protocol.SequenceCheckPacket;

/*
 * See Trac ticket [ #592 ]
 */
public class QuickSequenceTest extends LoginTest {
	
	private int packets;

	
	// --- SETTERS --- //
	
	@BeforeClass
	@Parameters({ "packets" })
	public void setTestLength(@Optional("500") int packets) {
		this.packets = packets;
	}
	
	
	// --- TESTS --- //
	
	@Test
	public void testSequence() throws Exception {

		GameTable table = super.createTable(4, QuickSequenceCheckProcessor.class);
		table.join(client, true);
		
		client.sendGamePacket(new SequenceCheckPacket(packets, 0), table.getTableId());
		
		for (int i = 1; i <= packets; i++) {
			client.expect(new FluidBuilder()
					.expect(SequenceCheckPacket.class, serializer)
					.where("seq").is(i));
		}
	}
}
