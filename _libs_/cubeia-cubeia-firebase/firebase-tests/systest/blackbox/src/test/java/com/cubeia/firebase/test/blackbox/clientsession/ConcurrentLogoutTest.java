package com.cubeia.firebase.test.blackbox.clientsession;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.test.blackbox.LoginTest;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.rules.FluidBuilder;

public class ConcurrentLogoutTest extends LoginTest {

    private static final int COUNT = 10;

    @Test
    public void testConcurrentLogout() throws Exception {
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
        client.logout(true);
        client.disconnect();
        
        Thread.sleep(10);

        // Login a client with same pid 
        Client client2 = super.newClient();
        connectAndLoginClient(client2, "user_B");

        try {
            client.expect(new FluidBuilder().expect(ForcedLogoutPacket.class), 100);
            Assert.fail("We should not get any message here");
        } catch (Exception e) {
            // Expected
        }

        client2.logout(false);
        client2.disconnect();
        
        connectAndLoginClient(client);
    }

}
