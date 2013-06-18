package com.cubeia.firebase.test.blackbox.mtt;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.TournamentRegisterResponseStatus;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTournament;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class ReconnectRegistrationTest extends LoginTest {

	private static final int COUNT = 10;

    @Test
	public void testRegistration() throws Exception {
	    
	    long start = System.currentTimeMillis();
	    
	    for (int i = 0; i < COUNT; i++) {
	        try {
	            doRegistrationTestRun();
	        } catch (Exception e) {
	            System.out.println("Exception occurred at iteration: "+i);
	            e.printStackTrace();
	            throw e;
	        }
	    } 
	    
	    long elapsed = System.currentTimeMillis() - start;
	    System.out.println("ReconnectRegistrationTest "+COUNT+" runs took: "+elapsed+"ms");
	    
	}
    
    private void doRegistrationTestRun() throws Exception {
        GameTournament tour = super.createTournament(2, 10, 2);
   
        // Relogin a client with same pid 
        Client client2 = super.newClient();
        connectAndLoginClient(client2, "user_B");
        
        client.expect(new FluidBuilder().expect(ForcedLogoutPacket.class));
        client.disconnect();
        
        tour.register(client2, false);

        client2.expect(new FluidBuilder()	
        		.expect(MttRegisterResponsePacket.class)
        		.where("status").is(TournamentRegisterResponseStatus.OK));
  	
        tour.unregister(client2, false);
        
        client2.expect(new FluidBuilder()
        		.expect(MttUnregisterResponsePacket.class)
        		.where("status").is(TournamentRegisterResponseStatus.OK));
                
        client2.logout(false);
        client2.disconnect();
        
        connectAndLoginClient(client);
    }
}
