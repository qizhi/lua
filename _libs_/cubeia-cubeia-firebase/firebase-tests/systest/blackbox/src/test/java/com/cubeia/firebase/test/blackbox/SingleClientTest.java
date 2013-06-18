package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.cubeia.firebase.test.common.Client;

public abstract class SingleClientTest extends FirebaseTest {

	protected Client client;
	
	@BeforeMethod
	public void createClient() throws Exception {
		client = super.newClient();
		assertFalse(client.isConnected());
		super.connectClient(client);
	}
	
	@AfterMethod
	public void destroyClient() {
		client.disconnect();
	}
}
