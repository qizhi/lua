package com.cubeia.firebase.test.blackbox.mtt;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.MttSeatedPacket;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.GameTournament;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class SeatingTest extends LoginTest {

	@Test
	public void testSeating() throws Exception {
		GameTournament tour = super.createTournament(2, 1, 1);
		
		tour.register(client, true);
		
		MttSeatedPacket seated = (MttSeatedPacket)client.expect(new FluidBuilder()
				.expect(MttSeatedPacket.class)
				.where("mttid").is(tour.getTournamentId()));
		
		GameTable table = new GameTable(seated.tableid);
		
		table.join(client, seated.seat, true);
		
	}
}
