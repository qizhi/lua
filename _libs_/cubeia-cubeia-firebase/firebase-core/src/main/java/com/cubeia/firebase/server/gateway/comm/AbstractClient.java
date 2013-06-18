/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.server.gateway.comm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.PacketVisitor;
import com.cubeia.firebase.io.protocol.PingPacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.client.ClientLocal;
import com.cubeia.firebase.server.gateway.client.ClientNodeInfo;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.server.gateway.client.IPlayerData;
import com.cubeia.firebase.server.gateway.comm.mina.PlayerData;
import com.cubeia.firebase.server.gateway.event.ClientGameActionHandler;
import com.cubeia.firebase.server.gateway.event.local.ClientLocalActionHandler;
import com.cubeia.firebase.server.gateway.packet.GamePacketHandler;
import com.cubeia.firebase.server.gateway.packet.InitialPacketHandler;
import com.cubeia.firebase.server.gateway.packet.LocalPacketHandler;
import com.cubeia.firebase.server.gateway.packet.MTTPacketHandler;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusDetails;
import com.cubeia.firebase.service.ping.PingManager;
import com.cubeia.firebase.service.ping.PingSession;
import com.cubeia.firebase.util.FirebaseLockFactory;

/**
 * This is a base class with common functionality shared
 * by all clients.
 * 
 * @author Lars J. Nilsson
 */
public abstract class AbstractClient implements Client {

	protected final static AtomicInteger ID_COUNTER = new AtomicInteger();
	protected final int localId = ID_COUNTER.incrementAndGet();
	
	protected final Logger log = Logger.getLogger(this.getClass());
	protected final Logger clientLog = Logger.getLogger("CLIENTS");
	
    protected Collection<PacketVisitor> handlers;   
    protected ClientGameActionHandler actionHandler;
    protected ClientLocalActionHandler localActionHandler;

    protected String sessionId;

    protected ReadWriteLock handlerLock = FirebaseLockFactory.createLock();
    
    protected ClientState state = ClientState.CONNECTED;
    
    protected PlayerData playerData = new PlayerData();
    
    protected ClientLocal clientLocalData = new ClientLocal();

    protected boolean closeForced = false;

    protected final PingSession pingSession;
    protected final ClientNodeContext context;
    protected final ClientNodeInfo nodeInfo;
	protected final PingManager pingManager;

    protected AbstractClient(ClientNodeContext context) {
		this.context = context;
		pingManager = context.getServices().getServiceInstance(PingManager.class);
        this.pingSession = pingManager.register(this);
		MBusDetails mbus = context.getServices().getServiceInstance(MBusContract.class).getMBusDetails();
		this.nodeInfo = new ClientNodeInfo(getNode(), mbus);
		handlers = new LinkedList<PacketVisitor>();
		setInitialPacketHandlerOnly();
		actionHandler = new ClientGameActionHandler(this, context.getServices());
        localActionHandler = new ClientLocalActionHandler(this, context.getServices());
    }
    
	public void disconnected() {
		Lobby lobby = context.getServices().getServiceInstance(LobbyServiceContract.class).getLobby();
		ClientRegistryServiceContract clientReg = context.getServices().getServiceInstance(ClientRegistryServiceContract.class);
        ClientRegistry clientRegistry = clientReg.getClientRegistry();
        PostLoginProcessor postLogin = context.getServices().getServiceInstance(PostLoginProcessor.class);
		try {
			// stop pinging
			pingManager.unregister(this);
			// Ticket #527, we need to unregister the object directly
			lobby.unsubscribeAll(this); 
			// Check if client has been forcibly closed 
			if  ( !closeForced() ) {
				// Remove player from client registry
				setState(ClientState.DISCONNECTED);
				boolean known = clientRegistry.clientLostConnection(getId());
				// if the client is known and we have a post login service
				if(known && postLogin != null) {
					postLogin.clientDisconnected(getId());
			    }
			} 
		} catch (Exception e) {
			log.error("Error caught when unregistrering client: " + this, e);
		}
	}
    
	public PingSession getPingSession() {
		return pingSession;
	}
	
	public void sendPing(int id) {
		sendClientPacket(new PingPacket(id));
	}
	
	public void pingDisconnect() {
		log.warn("Client has lost too many pings, will be disconnected; Client: " + getId() + "; Session: " + getSessionId() + "; Remote address: " + getRemoteAddress());
	}

	public Collection<PacketVisitor> getHandlers() {
		return handlers;
	}

	public void handleIncomingData(byte[] data) {
		throw new RuntimeException("Not implemented!");
	}

	public ClientState getState() {
		return state;
	}

	public void setState(ClientState state) {
		this.state = state;
		switch (state) {
			case LOGGED_IN: 
				addPacketHandlers();
				break;
			default:
				setInitialPacketHandlerOnly();
		}
	}
	
	public int getLocalClientId() {
		return localId;
	}
	
	public ClientGameActionHandler getActionHandler() {
		return actionHandler;
	}

	public IPlayerData getClientData() {
		return playerData;
	}

	public int getId() {
		return getClientData().getId();
	}
	
	public ClientLocalActionHandler getLocalActionHandler() {
		return localActionHandler;
	}

	public ClientNodeInfo getNodeInfo() {
		return nodeInfo;
	}

	public String toString() {
		return "id["+getId()+"] sid["+sessionId+"] "+playerData;
	}

	public ClientLocal getLocalData() {
		return clientLocalData;
	}
	
	public void close() {
		clientLog.info("Closing client session: " + getId() + " Session: "+sessionId);
		closeForced = true;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean closeForced() {
		return closeForced;
	}
    
	
	// --- PROTECTED METHODS --- //
	
	protected void dispatch(List<ProtocolObject> packets) {  
    	handlerLock.readLock().lock();
		try {
	    	for (ProtocolObject packet : packets) {
	    		if(packet instanceof PingPacket) {
	    			handlePing((PingPacket)packet);
	    		} else {
			    	lockedDispatchToHandlers(packet);
	    		}
	    	}	
		} finally {
			handlerLock.readLock().unlock();
		}
    }
	
	// --- PRIVATE METHODS --- //
	
	private void handlePing(PingPacket packet) {
		pingSession.pingReceived(packet.id);
	}
    
    // lock elsewere
	private void lockedDispatchToHandlers(ProtocolObject packet) {
		pingSession.dataReceived();
		for (PacketVisitor handler : handlers) {
		    packet.accept(handler);
		}
	}
	
	private void setInitialPacketHandlerOnly() {
		handlerLock.writeLock().lock();
		try {
			handlers.clear();
			InitialPacketHandler initialHandler = new InitialPacketHandler(this, context.getServices());
			handlers.add(initialHandler);
		} finally {
			handlerLock.writeLock().unlock();
		}
	}
    
	private String getNode() {
		return context.getNodeRouter().getId();
	}
    
    private void addPacketHandlers() {
		try {
			ClientNodeRouter route = context.getNodeRouter();
	
			// Handler for locally executed packets (login etc)
			LocalPacketHandler localHandler = new LocalPacketHandler(this, route, context.getServices());
			// localHandler.setServiceRegistry(context.getServices());
	
			ConnectionServiceContract connectionService = context.getServices().getServiceInstance(ConnectionServiceContract.class);
			CommandDispatcher commandDispatcher = connectionService.getSharedConnection().getCommandDispatcher();
			
			// Handler for game(Table) packets 
			GamePacketHandler gameHandler = new GamePacketHandler(this, route, commandDispatcher, context.getServices());
	
			// Handler for tournament packets
			MTTPacketHandler mttHandler = new MTTPacketHandler(this, route.getMttSender(), context.getServices());
	
			handlerLock.writeLock().lock();
			try {
				handlers.add(localHandler);
				handlers.add(gameHandler);
				handlers.add(mttHandler);
			} finally {
				handlerLock.writeLock().unlock();
			}
		} catch (Exception e) {
			log.error("Error when setting action handlers", e);
		}
	}
}
