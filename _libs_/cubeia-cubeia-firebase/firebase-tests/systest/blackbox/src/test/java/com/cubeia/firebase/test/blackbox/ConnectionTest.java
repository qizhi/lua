package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class ConnectionTest extends SingleClientTest {

	@Test	
	public void testConnection() throws Exception {		
		assertTrue(client.isConnected());
	}
}
