package com.cubeia.firebase.test.blackbox.old;

//import java.util.Random;
//
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//import com.cubeia.firebase.io.protocol.Enums;
//import com.cubeia.firebase.io.protocol.JoinRequestPacket;
//import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
//import com.cubeia.firebase.io.protocol.LoginResponsePacket;
//import com.cubeia.firebase.io.protocol.LogoutPacket;
//import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
//
///**
// * Test for ticket #500 - Failed to remove client from subscribers.
// * 
// */
//public class Ticket500Test extends FirebaseTest {
//
//	@BeforeMethod
//	public void connect() throws Exception {
////		c = new Client();
////		c.connect(hostname, port);
//	}
//
//	public void testLoginAfterSubscribing() throws Exception {
//		c.login(username, password);
//
//		// Find a table to sit at
//		c.send(new LobbySubscribePacket(Enums.LobbyType.REGULAR, 99, "/"));
//		TableSnapshotPacket table = c.expect(TableSnapshotPacket.class);
//
//		// Join the table
//		c.send(new JoinRequestPacket(table.tableid, (byte) -1, null));
//
//		Client c2 = new Client();
//		c2.connect(hostname, port);
//		c2.login(username, password);
//
//		// Logout and close the connection.
//		c.send(new LogoutPacket(true));
//		Thread.sleep(500);
//		c2.disconnect();
//
//		Thread.sleep(6000);
//	}
//
//	public void testPacketsAfterLogout() throws Exception {
//		// c.send(new VersionPacket());
//		// c.expect(VersionPacket.class);
//		Client local = new Client();
//		local.connect(hostname, port);
//		local.login(username, password);
//		Thread.sleep(50);
//		local.expect(LoginResponsePacket.class);
//		Thread.sleep(new Random().nextInt(500));
//
//		// Find a table to sit at
//		local.send(new LobbySubscribePacket(Enums.LobbyType.REGULAR, 99, "/"));
//		Thread.sleep(50);
//		TableSnapshotPacket table = local.expect(TableSnapshotPacket.class);
//
//		// Join the table
//		local.send(new JoinRequestPacket(table.tableid, (byte) -1, null));
//		Thread.sleep(50);
//		local.send(new LogoutPacket(true));
//		Thread.sleep(1000);
//	}
//
//}