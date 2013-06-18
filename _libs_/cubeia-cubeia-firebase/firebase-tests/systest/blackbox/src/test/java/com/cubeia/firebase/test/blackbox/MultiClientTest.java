package com.cubeia.firebase.test.blackbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.cubeia.firebase.test.common.GameClient;

public abstract class MultiClientTest extends FirebaseTest {
	
	protected final static AtomicInteger ID_GENERATOR = new AtomicInteger();

	protected List<GameClient> clients;
	
	private boolean expectResponse = true;
	
	protected String namePrefix;
	protected int noClients;
	
	
	// --- SETTERS --- ///
	
	@BeforeClass
	@Parameters({ "namePrefix" })
	public void setNamePrefix(@Optional("user_") String namePrefix) {
		this.namePrefix = namePrefix;
	}
	
	public void setNumberOfClients(int clients) {
		this.noClients = clients;
	}
	
	public void setExpectLoginResponse(boolean expect) {
		this.expectResponse = expect;
	}
	
	
	// --- LIFETIME --- //

	@BeforeMethod
	public void loginAll() throws Exception {
		createClients();
		for (int i = 0; i < clients.size(); i++) {
			int id = ID_GENERATOR.incrementAndGet();
			super.connectClient(clients.get(i));
			clients.get(i).login(namePrefix + id, String.valueOf(id), expectResponse);
		}
	}
	
	private void createClients() {
		this.clients = new ArrayList<GameClient>(noClients);
		for (int i = 0; i < noClients; i++) {
			this.clients.add(newGameClient(serializer));
		}	
	}

	@AfterMethod
	public void logoutAll() throws IOException {
		for (int i = 0; i < clients.size(); i++) {
			try {
				logoutClient(i);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void logoutClient(int i) {
		try {
			clients.get(i).logout(true);
		} finally {
			clients.get(i).disconnect();
		}
	}
	
	
	// --- PROTECTED METHODS --- //
	
	protected GameClient client(int i) {
		return clients.get(i);
	}
	
	protected void loginClient(GameClient client) {
	    client.login(namePrefix + client.getPlayerId(), String.valueOf(client.getPlayerId()), true);
	}
}
