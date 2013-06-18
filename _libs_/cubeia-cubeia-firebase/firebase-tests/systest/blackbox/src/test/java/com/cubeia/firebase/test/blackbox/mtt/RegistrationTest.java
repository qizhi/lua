package com.cubeia.firebase.test.blackbox.mtt;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.TournamentRegisterResponseStatus;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.GameTournament;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class RegistrationTest extends LoginTest {

	@Test
	public void testRegistration() throws Exception {
		GameTournament tour = super.createTournament(2, 10, 2);
		
		tour.register(client, false);
		
		client.expect(new FluidBuilder()	
				.expect(MttRegisterResponsePacket.class)
				.where("status").is(TournamentRegisterResponseStatus.OK));
	
		tour.unregister(client, false);
		
		client.expect(new FluidBuilder()	
				.expect(MttUnregisterResponsePacket.class)
				.where("status").is(TournamentRegisterResponseStatus.OK));
		
	}
}
