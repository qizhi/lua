package com.cubeia.firebase.test.blackbox;

import static com.cubeia.firebase.test.common.Constants.COMETD_PATH;
import static com.cubeia.firebase.test.common.Constants.SOCKET_PATH;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

import javax.management.ObjectName;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.cubeia.firebase.clients.java.connector.CometdConnectorFactory;
import com.cubeia.firebase.clients.java.connector.ConnectorFactory;
import com.cubeia.firebase.clients.java.connector.Encryption;
import com.cubeia.firebase.clients.java.connector.SecurityConfig;
import com.cubeia.firebase.clients.java.connector.SocketConnectorFactory;
import com.cubeia.firebase.clients.java.connector.WebSocketConnectorFactory;
import com.cubeia.firebase.server.gateway.GatewayNodeMBean;
import com.cubeia.firebase.server.lobby.systemstate.StateLobbyMBean;
import com.cubeia.firebase.service.clientreg.state.StateClientRegistryMBean;
import com.cubeia.firebase.test.blackbox.util.LobbyInterrogatorImpl;
import com.cubeia.firebase.test.blackbox.util.TableCreatorImpl;
import com.cubeia.firebase.test.blackbox.util.TournamentCreatorImpl;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.ConnectorType;
import com.cubeia.firebase.test.common.Constants;
import com.cubeia.firebase.test.common.GameClient;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.GameTournament;
import com.cubeia.firebase.test.common.Lobby;
import com.cubeia.firebase.test.common.util.Jmx;
import com.cubeia.firebase.test.common.util.ProtocolObjectSerializer;
import com.cubeia.firebase.test.common.util.Serializer;
import com.cubeia.test.systest.game.TestProcessor;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;
import com.cubeia.test.systest.tournament.TournamentTestProcessor;
import com.cubeia.test.systest.tournament.tests.StandardProcessor;

public class FirebaseTest {

	public static final int GAME_ID = 98;
	public static final int MTT_ID = 998;
	
	public static final int HANDSHAKE_SIG = Integer.valueOf(System.getProperty("firebase.systest.handshakeSignature", "-1"));
	
	protected String clientHost;
	protected int clientPort;
	protected int activatorJmxPort;
	protected String activatorHost;
	protected String lobbyHost;
	protected int lobbyJmxPort;
	protected ConnectorType connectorType;
	
	
	protected com.cubeia.test.systest.game.ActivatorMBean tableProxy;
	protected com.cubeia.test.systest.tournament.ActivatorMBean mttProxy;
	protected StateLobbyMBean lobbyProxy;
	protected StateClientRegistryMBean registryProxy;
	
	protected final List<GameTable> tables = new LinkedList<GameTable>();
	protected final List<GameTournament> mtts = new LinkedList<GameTournament>();
	
	protected Serializer serializer = new ProtocolObjectSerializer(new ProtocolObjectFactory());
	
	protected ConnectorFactory connectorFactory;
	protected GatewayNodeMBean clientNode;


	@BeforeClass
	@Parameters({
		"clientHost", 
		"lobbyHost", 
		"activatorHost", 
		"clientPort", 
		"lobbyJmxPort", 
		"activatorJmxPort",
		"connectorType"})
	public void setUp(
			@Optional("localhost") String clientHost, 
			@Optional("localhost") String lobbyHost, 
			@Optional("localhost") String activatorHost, 
			@Optional("4123") int clientPort,
			@Optional("8999") int lobbyJmxPort,
			@Optional("8999") int activatorJmxPort,
			@Optional("SOCKET") String connectorType
			) throws Exception {
		
		this.clientHost = clientHost;
		this.lobbyHost = lobbyHost;
		this.activatorHost = activatorHost;
		this.clientPort = clientPort;
		this.lobbyJmxPort = lobbyJmxPort;
		this.activatorJmxPort = activatorJmxPort;
		this.connectorType = ConnectorType.valueOf(connectorType);
		
		this.connectorFactory = createConnectorFactory(clientHost, clientPort);
		this.connectorFactory.start();
		
		initProxies();
	}


	@AfterClass
	public void tearDown() { 
		this.connectorFactory.stop();
		for (GameTable t : tables) {
			tableProxy.destroyTable(t.getTableId());
		}
		tables.clear();
		for (GameTournament t : mtts) {
			mttProxy.destroy(GAME_ID, t.getTournamentId());
		}
		mtts.clear();
	}

	// --- HELPER METHODS --- //
	
	protected Lobby getLobby() {
		return new Lobby(new LobbyInterrogatorImpl(lobbyProxy));
	}
	
	protected void connectClient(Client cl) throws IOException, GeneralSecurityException {
		cl.connect(connectorFactory.createConnector(newSecurityConfig(HANDSHAKE_SIG)));	
	}

	protected GameTable createTable(int seats) {
		return createTable(seats, null);
	}
	
	protected boolean destroyTable(GameTable table) {
		boolean b = tableProxy.destroyTable(table.getTableId());
		if(b) {
			tables.remove(table);
		} 
		return b;
	}
	
	protected GameTable createTable(int seats, Class<? extends TestProcessor> processor) {
		GameTable t = new GameTable(new TableCreatorImpl(tableProxy, seats, null, (processor == null ? null : processor.getName())));
		tables.add(t);
		return t;
	}
	
	protected GameTable[] createTables(int num, int seats) {
		GameTable[] arr = new GameTable[num];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = createTable(seats);
		}
		return arr;
	}
	
	protected GameTournament createTournament(int seats, int capacity, int minPlayers) {
		return createTournament(seats, capacity, minPlayers, StandardProcessor.class);
	}
	
	protected GameTournament createTournament(int seats, int capacity, int minPlayers, Class<? extends TournamentTestProcessor> processor) {
		return createTournament(seats, "", capacity, minPlayers, StandardProcessor.class);
	}
	
	protected GameTournament createTournament(int seats, String domain, int capacity, int minPlayers, Class<? extends TournamentTestProcessor> processor) {
		GameTournament t = new GameTournament(new TournamentCreatorImpl(mttProxy, domain, seats, capacity, minPlayers, (processor == null ? null : processor.getName())));
		mtts.add(t);
		return t;
	}
	
	protected Client newClient() {
		return new Client(this.connectorType, this.clientHost, this.clientPort);
	}
	
	protected GameClient newGameClient(Serializer serializer) {
		return new GameClient(serializer, this.connectorType, this.clientHost, this.clientPort);
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private SecurityConfig newSecurityConfig(int handshakeSig) {
		return new SecurityConfig(handshakeSig != -1, handshakeSig, getEncryption());
	}
	
	private Encryption getEncryption() {
		if(Constants.USE_NAIVE_SSL) {
			return Encryption.NAIVE_SSL;
		} else if(Constants.USE_SSL) {
			return Encryption.SSL;
		} else if(Constants.USE_NATIVE) {
			return Encryption.FIREBASE_NATIVE;
		} else {
			return Encryption.NONE;
		}
	}
	
	private ConnectorFactory createConnectorFactory(String host, int port) {
		switch(connectorType) {
			case SOCKET : return new SocketConnectorFactory(host, port);
			case WEB_SOCKET : return new WebSocketConnectorFactory(host, port, SOCKET_PATH);
			case COMETD: return new CometdConnectorFactory(host, port, COMETD_PATH) {
				
				@Override
				protected HttpClient createClient() {
					HttpClient cl = super.createClient();
					/*
					 * Limit the number of threads as responses from the server will come out of
					 * order otherwise... :-/
					 */
					cl.setThreadPool(new QueuedThreadPool(3));
					return cl;
				}
			};
			default : {
				throw new RuntimeException("Missing implementation for connector " + this);
			}
		}
	}
	
	private void initProxies() throws Exception {
		tableProxy = Jmx.proxy(activatorHost, activatorJmxPort, new ObjectName(com.cubeia.test.systest.game.Activator.ACTIVATOR_OBJECTNAME), com.cubeia.test.systest.game.ActivatorMBean.class);
		mttProxy = Jmx.proxy(activatorHost, activatorJmxPort, new ObjectName(com.cubeia.test.systest.tournament.Activator.ACTIVATOR_OBJECTNAME), com.cubeia.test.systest.tournament.ActivatorMBean.class);
		lobbyProxy = Jmx.proxy(lobbyHost, lobbyJmxPort, new ObjectName("com.cubeia.firebase.lobby:type=SysLobby"), StateLobbyMBean.class);
		clientNode = Jmx.proxy(lobbyHost, lobbyJmxPort, new ObjectName("com.cubeia.firebase:type=ClientNode"), GatewayNodeMBean.class);
		registryProxy = Jmx.proxy(lobbyHost, lobbyJmxPort, new ObjectName("com.cubeia.firebase.clients:type=ClientRegistry"), StateClientRegistryMBean.class);
	}
}