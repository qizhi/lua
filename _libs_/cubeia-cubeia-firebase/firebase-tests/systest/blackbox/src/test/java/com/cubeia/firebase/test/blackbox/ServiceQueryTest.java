package com.cubeia.firebase.test.blackbox;

import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.ServiceClassExpect;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.io.protocol.ServiceQueryRequestPacket;
import com.cubeia.test.systest.io.protocol.ServiceQueryResponsePacket;

public class ServiceQueryTest extends LoginTest {

    private int PACKETS = 10;

    public ServiceQueryTest() {
        super();
    }

    @Test
    public void testBroadcast() throws Exception {
        String payload = String.valueOf(System.currentTimeMillis());
        long start = System.currentTimeMillis();

        for (int i = 0; i < PACKETS; i++) {
            ServiceQueryRequestPacket req = new ServiceQueryRequestPacket(i, client.getPlayerId(), payload);
            client.sendServicePacket(SystestService.class, i, req);
            
            client.expect(new FluidBuilder()
            .expect(new ServiceClassExpect(ServiceQueryResponsePacket.class, serializer))
            .where("seq").is(i));
        }
        
        long elapsed = System.currentTimeMillis()-start;
        System.out.println("Query Test took "+elapsed+"ms");
    }
}
