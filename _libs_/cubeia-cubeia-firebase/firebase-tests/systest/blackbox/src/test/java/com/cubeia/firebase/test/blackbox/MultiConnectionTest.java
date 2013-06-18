package com.cubeia.firebase.test.blackbox;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameClient;

/*
 * This test is slightly ugly, if for example the server has max connection set
 * below the number tested here, the test will fail, not on the test method, but on
 * configuration when the login fails... Well, at least it will fail... /LJN
 */
public class MultiConnectionTest extends MultiClientTest {
	
	public MultiConnectionTest() {
		setExpectLoginResponse(true);
	}

	@BeforeClass
	@Parameters({ "numberOfConnections" })
	public void setNumberConnections(@Optional("20") int conns) {
		super.setNumberOfClients(conns);
	}
	
	
	// --- TESTS --- //
	
	@Test
	public void testConnecting() throws Exception {
		for (GameClient cl : super.clients) {
			Assert.assertTrue(cl.isLoggedIn());
		}
	}
}
