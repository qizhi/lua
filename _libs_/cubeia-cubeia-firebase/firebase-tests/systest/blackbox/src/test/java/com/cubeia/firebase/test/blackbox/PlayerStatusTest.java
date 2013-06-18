package com.cubeia.firebase.test.blackbox;

import static com.cubeia.firebase.api.game.player.PlayerStatus.CONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.DISCONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.WAITING_REJOIN;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.test.common.GameClient;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.test.systest.game.tests.PlayerStatusProcessor;
import com.cubeia.test.systest.io.protocol.ForceCleanupPacket;
import com.cubeia.test.systest.io.protocol.PlayerStatusRequestPacket;
import com.cubeia.test.systest.io.protocol.PlayerStatusResponsePacket;

public class PlayerStatusTest extends MultiClientTest {

	private GameTable table;
	
	public PlayerStatusTest() {
		setNumberOfClients(2);
	}
	
	@BeforeMethod
	public void setupTable() {
		table = super.createTable(2, PlayerStatusProcessor.class);
	}
	
	@AfterMethod
	public void destroyTable() {
		super.destroyTable(table);
	}
	
	@Test
	public void testSimpleConnect() throws Exception {
		// join 0
		table.join(client(0), true);
		
		// check 1 is not sitting
		checkPlayerStatus(client(0), client(1).getPlayerId(), null);
		
		// join 1
		table.join(client(1), true);
		
		// check 1 is connected
		checkPlayerStatus(client(0), client(1).getPlayerId(), CONNECTED);
		
		// leave 1
		table.leave(client(1), true);
		
		// check 1 is not sitting
		checkPlayerStatus(client(0), client(1).getPlayerId(), null);
	}
	
	@Test
	public void testReConnect() throws Exception {
		// join 0 + 1
		table.join(client(0), true);
		table.join(client(1), true);
		
		// disconnect 1
		client(1).disconnect();
		
		/*
		 * Disconnect is asynchronous, so here we have to do a very
		 * ugly wait... We could possibly query the client registry over 
		 * JMX instead.
		 */
		Thread.sleep(200);
		
		// check 1 is waiting for a rejoin
		checkPlayerStatus(client(0), client(1).getPlayerId(), WAITING_REJOIN);
		
		// connect and login 1 again
		super.connectClient(client(1));
		relogin(client(1));
	
		// still waiting
		checkPlayerStatus(client(0), client(1).getPlayerId(), WAITING_REJOIN);
		
		// join 1
		table.join(client(1), true);
		
		// check connected
		checkPlayerStatus(client(0), client(1).getPlayerId(), CONNECTED);
		
	}
	
	@Test
	public void testDisconnect() throws Exception {
		// join 0 + 1
		table.join(client(0), true);
		table.join(client(1), true);
		
		// disconnect 1
		client(1).disconnect();
		
		/*
		 * Disconnect is asynchronous, so here we have to do a very
		 * ugly wait... We could possibly query the client registry over 
		 * JMX instead.
		 */
		Thread.sleep(200);
		
		// check 1 is waiting for a rejoin
		checkPlayerStatus(client(0), client(1).getPlayerId(), WAITING_REJOIN);
		
		// force client registry disconnect
		super.clientNode.forceCleanupDisconnects();
		
		// check disconnected
		checkPlayerStatus(client(0), client(1).getPlayerId(), null);
		
	}
	
	@Test
	public void testCleanup() throws Exception {
		// join 0 + 1
		table.join(client(0), true);
		table.join(client(1), true);
		
		// disconnect 1
		client(1).disconnect();
		
		/*
		 * Disconnect is asynchronous, so here we have to do a very
		 * ugly wait... We could possibly query the client registry over 
		 * JMX instead.
		 */
		Thread.sleep(200);
		
		// check 1 is waiting for a rejoin
		checkPlayerStatus(client(0), client(1).getPlayerId(), WAITING_REJOIN);
		
		// force table cleanup
		client(0).sendGamePacket(new ForceCleanupPacket(client(1).getPlayerId()), table.getTableId());
		
		/*
		 * The above force is also asynchronous, so do yet another ugly 
		 * wait here...
		 */
		Thread.sleep(200);
		
		// check disconnected
		checkPlayerStatus(client(0), client(1).getPlayerId(), DISCONNECTED);
		
		// force client registry disconnect
		super.clientNode.forceCleanupDisconnects();
				
		// check player is STILL disconnect (?)
		checkPlayerStatus(client(0), client(1).getPlayerId(), DISCONNECTED);
		
	}

	private void relogin(GameClient client) {
		client.login(namePrefix + client.getPlayerId(), String.valueOf(client.getPlayerId()), true);
	}

	private void checkPlayerStatus(GameClient sitting, int playerId, PlayerStatus status) throws IOException {
		String stat = (status == null ? "" : status.name());
		sitting.sendGamePacket(new PlayerStatusRequestPacket(playerId), table.getTableId());
		sitting.expect(new FluidBuilder()
			.expect(new GameClassExpect(PlayerStatusResponsePacket.class, serializer))
			.where("pid").is(playerId).andWhere("status").is(stat)
		);
	}
}
