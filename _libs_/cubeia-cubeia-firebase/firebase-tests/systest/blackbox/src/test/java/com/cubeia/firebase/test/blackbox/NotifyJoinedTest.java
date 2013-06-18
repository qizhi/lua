package com.cubeia.firebase.test.blackbox;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.Builder;
import com.cubeia.firebase.test.common.rules.impl.MemberAssertFilter;

public class NotifyJoinedTest extends FirebaseTest {

	private Client client1;
	private Client client2;
	
	private String password1;
	private String username1;
	private String password2;
	private String username2;
	
	@BeforeClass
	@Parameters({ "username", "password"})
	public void setUserOne(
			@Optional("dummyUser1") String username,
			@Optional("666") int password) {

		this.password1 = String.valueOf(password);
		this.username1 = username;
	}
	
	@BeforeClass
	@Parameters({ "username", "password"})
	public void setUserTwo(
			@Optional("dummyUser2") String username,
			@Optional("667") int password) {

		this.password2 = String.valueOf(password);
		this.username2 = username;
	}

	@BeforeMethod
	public void login() throws Exception {
		client1 = super.newClient();
		super.connectClient(client1);
		client1.login(username1, password1, true);	
		
		client2 = super.newClient();
		super.connectClient(client2);
		client2.login(username2, password2, true);
	}
	
	@AfterMethod
	public void logout() throws IOException {
		closeClient(client1);
		closeClient(client2);
	}

	private void closeClient(Client cl) {
		try {
			cl.logout(true);
		} finally {
			cl.disconnect();
		}
	}
	
	@Test
	public void testJoinNotification() throws Exception {
		GameTable table = super.createTable(4);
	
		// Join client one
		table.join(client1, true);
		
		// Join client two
		table.join(client2, true);
		
		// Make sure client one got a notify join 
		client1.expect(Builder.expect(
					NotifyJoinPacket.class, 
					new MemberAssertFilter("pid", client2.getPlayerId())
				));
		
	}
}
