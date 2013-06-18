package com.cubeia.firebase.test.blackbox.old;

//import static org.testng.AssertJUnit.assertEquals;
//
//import java.io.IOException;
//
//import org.testng.annotations.Test;
//
//import com.cubeia.firebase.io.protocol.Enums;
//import com.cubeia.firebase.io.protocol.JoinResponsePacket;
//import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
//import com.cubeia.firebase.io.protocol.LoginResponsePacket;
//import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
//import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
//import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
//import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
//import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
//import com.cubeia.firebase.io.protocol.WatchRequestPacket;
//import com.cubeia.firebase.io.protocol.WatchResponsePacket;
//import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;
//import com.cubeia.firebase.test.blackbox.util.QueryVerification;
//import com.cubeia.firebase.test.common.Client;
//import com.cubeia.testgame.io.protocol.ArithmeticPacket;
//import com.cubeia.testgame.io.protocol.ResourcePacket;
//import com.cubeia.testgame.io.protocol.StatePacket;
//
//@Test( sequential = true)
//public class SystemTest extends FirebaseTest {
//
//	private int tableId = 1;
//	private String state = "";
//	private String reconnectId = "22";
//	private String reconnectPassword = "22";
//
//	@Test
//	public void login() throws IOException {
//		c = new Client();
//		c.connect(hostname, port);
//		c.login(username, password);
//		LoginResponsePacket response = c.expect(LoginResponsePacket.class);
//		assertEquals(Enums.ResponseStatus.OK, response.status);
//		c.setPlayerId(response.pid);
//	}
//
//	@Test(dependsOnMethods = { "login" })
//	public void joinTable() throws Exception {
//		tableId = c.joinTable().tableid;
//	}
//
//	@Test(dependsOnMethods = { "joinTable" })
//	public void sequence() throws IOException {
//		int iterations = 5;
//		int currentSequenceId = 0;
//
//		for (int i = 0; i < iterations; i++) {
//			c.send(new ArithmeticPacket(tableId, c.getPlayerId(), currentSequenceId, 0), tableId);
//			ArithmeticPacket response = c.expectTestGamePacket(ArithmeticPacket.class);
//			assertEquals(currentSequenceId, response.seq);
//			currentSequenceId++;
//		}
//	}
//	
//	@Test(dependsOnMethods = { "joinTable" })
//	public void state() throws Exception {
//		clearState();
//		appendState("Test");
//		appendState("Is");
//		appendState("Not", true); // Fail
//		appendState("Working");		
//
//		StatePacket response = null;
//		for (int i = 0; i < 4; i++) {
//			response = c.expectTestGamePacket(StatePacket.class);
//			System.err.println("response: " + response);
//		}
//		assertEquals(state, response.append);
//		clearState();
//	}
//	
//	@Test(dependsOnMethods = { "joinTable" })
//	public void tableQuery() throws Exception {
//		c.send(new TableQueryRequestPacket(tableId));
//		QueryVerification.verifyResponse(c.expect(TableQueryResponsePacket.class));
//	}
//	
//	@Test(dependsOnMethods = { "joinTable" })
//	public void resource() throws Exception {
//		c.send(new ResourcePacket(tableId, c.getPlayerId(), false), tableId);
//		ResourcePacket result = c.expectTestGamePacket(ResourcePacket.class);
//		assertEquals(false, result.failed);
//	}
//	
//	@Test(dependsOnMethods = { "joinTable" })
//	public void reconnect() throws Exception {
//		// Join, sit and watch
//		Client client = new Client();
//		client.connect(hostname, port);
//		client.login(reconnectId, reconnectPassword);
//		JoinResponsePacket join = client.joinTable();
//		client.send(new WatchRequestPacket(join.tableid + 1));
//		WatchResponsePacket watchResponse = client.expect(WatchResponsePacket.class);
//		assertEquals(WatchResponseStatus.OK, watchResponse.status);
//		
//		// Disconnect
//		client.disconnect();
//		
//		// Come back
//		client = new Client();
//		client.connect(hostname, port);
//		client.login(reconnectId, reconnectPassword);
//		
//		// Check that we were notified about sitting and watching 
//		NotifySeatedPacket seated = client.expect(NotifySeatedPacket.class);
//		NotifyWatchingPacket watching = client.expect(NotifyWatchingPacket.class);
//		
//		assertEquals(join.tableid, seated.tableid);
//		assertEquals(join.seat, seated.seat);
//		assertEquals(watchResponse.tableid, watching.tableid);
//		
//		// Clean up
//		client.send(new LeaveRequestPacket(join.tableid));
//		client.send(new UnwatchRequestPacket(watchResponse.tableid));
//		client.disconnect();
//	}
//	
//	@Test(dependsOnMethods = { "joinTable" })
//	public void testWaitingList() throws Exception {
//		new WaitingList(c);
//	}
//	
//	private void appendState(String string, boolean fail) throws IOException {
//		c.send(new StatePacket(tableId, c.getPlayerId(), string, false, fail), tableId);
//		if (!fail) state += string; 
//	}
//
//	private void appendState(String string) throws IOException {
//		appendState(string, false);
//	}
//
//	private void clearState() throws IOException {
//		c.send(new StatePacket(tableId, c.getPlayerId(), "", true, false), tableId);
//	}
//}