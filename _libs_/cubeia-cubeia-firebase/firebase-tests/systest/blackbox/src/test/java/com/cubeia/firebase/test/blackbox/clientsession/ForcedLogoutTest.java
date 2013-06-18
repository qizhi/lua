package com.cubeia.firebase.test.blackbox.clientsession;

import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class ForcedLogoutTest extends LoginTest {

    private static final int COUNT = 10;

    @Test
    public void testForcedLogout() throws Exception {
        for (int i = 0; i < COUNT; i++) {
            try {
                doTestRun();
            } catch (Exception e) {
                System.out.println("Exception occurred at iteration: "+i);
                e.printStackTrace();
                throw e;
            }
        } 
    }


    private void doTestRun() throws Exception {
        // Relogin a client with same pid 
        Client client2 = super.newClient();
        connectAndLoginClient(client2, "user_B");

        client.expect(new FluidBuilder().expect(ForcedLogoutPacket.class));
        client.disconnect();
        
        client2.logout(false);
        client2.disconnect();
        
        connectAndLoginClient(client);
    }
}