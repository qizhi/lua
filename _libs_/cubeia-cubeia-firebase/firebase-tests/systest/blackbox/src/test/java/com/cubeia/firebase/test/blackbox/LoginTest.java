package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameClient;

public class LoginTest extends FirebaseTest {
	
	protected GameClient client;
	
	protected String password;
	protected String username;
	
	// --- SETTERS --- //
	
	@BeforeClass
	@Parameters({ "username", "password"})
	public void setUser(
			@Optional("dummyUser") String username,
			@Optional("666") int password) {

		this.password = String.valueOf(password);
		this.username = username;
	}
	
	
	// --- LIFETIME --- //
	
	@BeforeMethod
	public void login() throws Exception {
		client = newGameClient(serializer);
		connectAndLoginClient(client);		
	}

	@AfterMethod
	public void logout() throws IOException {
		try {
			logout(true);
		} finally {
			client.disconnect();
		}
	}
	
	
	// --- TESTS --- //
	
	// @Test
	public void testLoginSuccess() throws Exception {
		assertTrue(client.getPlayerId() != -1);
	}
	
	
	
	// --- PROTECTED METHODS --- //
	
	public void logout(boolean leaveTables) throws IOException {
		client.logout(leaveTables);
	}
	
	protected void connectAndLoginClient(Client target) throws UnknownHostException, IOException, GeneralSecurityException {
	    connectAndLoginClient(target, username);
    }
	
	protected void connectAndLoginClient(Client target, String username) throws UnknownHostException, IOException, GeneralSecurityException {
        super.connectClient(target);
        target.login(username, password, true);
    }
	
	/*@Test
	public void testLoginFailure() throws Exception {
		c.login(username, wrongPassword);
		LoginResponsePacket failed = c.expect(LoginResponsePacket.class);
		assertEquals(Enums.ResponseStatus.DENIED, failed.status);
	}

	@Test
	public void testLoginAgainForcesLogoutOfOldClient() throws Exception {
		c.login(username, password);

		// Another client connects and logs in with the same user name.
		Client c2 = new Client();
		c2.connect(hostname, port);
		c2.login(username, password);
		LoginResponsePacket response = c2.expect(LoginResponsePacket.class);
		assertEquals(Enums.ResponseStatus.OK, response.status);
		
		// Old client should get a "forced logout packet"
		c.expect(ForcedLogoutPacket.class);
		
		// Note, the first client is now dead, it will receive no more packets.
		c.send(new VersionPacket());
		try {
			c.expect(VersionPacket.class, 100);
			assert false;
		} catch (Exception expected) {}
		
		// Housekeeping.
		c2.disconnect();
	}*/
}