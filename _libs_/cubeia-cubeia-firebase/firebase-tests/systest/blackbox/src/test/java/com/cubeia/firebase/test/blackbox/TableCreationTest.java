package com.cubeia.firebase.test.blackbox;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.CreateTableRequestPacket;
import com.cubeia.firebase.io.protocol.CreateTableResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyInvitedPacket;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.util.Parameters;

public class TableCreationTest extends MultiClientTest {

	public int seq = 0;
	
	public TableCreationTest() {
		setNumberOfClients(2);
	}

	@Test
	public void testCreateWithInvite() throws Exception {
		client(0).sendFirebasePacket(new CreateTableRequestPacket(seq++, GAME_ID, (byte)4, Parameters.emptyParams(), new int[] { client(1).getPlayerId() }));

		CreateTableResponsePacket resp = (CreateTableResponsePacket) client(0).expect(new FluidBuilder()
					.expect(CreateTableResponsePacket.class)
					.where("status").is(ResponseStatus.OK));
	
		// Thread.sleep(200);
		// System.out.println("KKKK: " + getLobby().getTableInfo(resp.tableid));
		
		GameTable table = new GameTable(resp.tableid);
		
		NotifyInvitedPacket inv = (NotifyInvitedPacket) client(1).expect(new FluidBuilder()
			.expect(NotifyInvitedPacket.class)
			.where("tableid").is(table.getTableId()));
		
		table.join(client(0), resp.seat, true);
		
		table.join(client(1), inv.seat, true);
		
		if(!tableProxy.destroyTable(table.getTableId())) {
            Assert.fail("Failed to destroy private table " + table.getTableId());
        }
	}
}
