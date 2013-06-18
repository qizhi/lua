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
package com.cubeia.firebase.server.gateway.packet;

import java.util.List;

import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.SystemInfoRequestAction;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Enums.LobbyType;
import com.cubeia.firebase.io.protocol.GameVersionPacket;
import com.cubeia.firebase.io.protocol.LobbyObjectSubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyObjectUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyQueryPacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.io.protocol.SystemInfoRequestPacket;
import com.cubeia.firebase.io.protocol.VersionPacket;
import com.cubeia.firebase.server.gateway.GatewayNode;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.LobbyQueryRequest;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;
import com.cubeia.firebase.server.login.LoginManager;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.firebase.server.service.login.LoginServiceContract;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.localservice.LocalServiceManagerContract;
import com.cubeia.firebase.service.sysinfo.SystemInfoServiceContract;




/**
 * Handles packets that are allowed to be handled pre-login.
 * I.e. packets handled here can be sent before a client has
 * logged in so we cannot rely on player id or send anything
 * over the cluster.
 * 
 * 
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class InitialPacketHandler extends AbstractPacketHandler {
	  
	/**
	 * Lobby services
	 */
	private Lobby lobby;
	/**
	 * We need the service registry to be able to lookup
	 * services in the system as needed.
	 */
	private ServiceRegistry serviceRegistry;
	/**
	 * Access protector
	 */
	private DosProtector dosProtect;
	/**
	 * Id for dos protection.
	 */
	private Object dosId;
	private boolean isDosWarned;
	
    /**
     * Constructor with injected client
     * 
     * @param client
     * @param route  
     */
    public InitialPacketHandler(Client client, ServiceRegistry reg) {
        super(client, getIdGenerator(reg));
        dosId = Integer.valueOf(client.getLocalClientId());
        // Logger.getLogger(getClass()).debug("Dos id: " + dosId);
        setServiceRegistry(reg);
    }
    
    /*
     * Ijnect a service registry.
     * Will lookup lobby services and chat services.
     * 
     * @param serviceRegistry
     */
    private void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		lobby = serviceRegistry.getServiceInstance(LobbyServiceContract.class).getLobby();
		dosProtect = serviceRegistry.getServiceInstance(DosProtector.class);
	}

    /* (non-Javadoc)
     * @see com.cubeia.firebase.server.gateway.packet.AbstractPacketHandler#toString()
     */
    @Override
    public String toString() {
        return "InitialPacketHandler";
    }
    
    /**
     * Handle login
     * We use a special login for now =)
     * 
     * User = screenname
     * Password = id (integer)
     */
    @Override
    public void visit(LoginRequestPacket packet) {
    	if (serviceRegistry == null) {
    		log.fatal("No service registry defined for login.");
    	} else {
    		if(!checkDos(false, "Login request")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		// Create Action object
    		LoginRequestAction request = new LoginRequestAction(packet.user, packet.password, packet.operatorid);
    		request.setData(packet.credentials); 
    		request.setRemoteAddress(client.getRemoteAddress());
    		
    		// Set operator id
    		client.getClientData().setOperatorId(packet.operatorid);
    		
    		LoginServiceContract serviceInstance = serviceRegistry.getServiceInstance(LoginServiceContract.class, null);    		
    		LoginManager loginManager = serviceInstance.getLoginManager();
    		loginManager.handleLoginRequest(request, client.getLocalActionHandler());
    	}
    }
    

    @Override
    public void visit(LobbyQueryPacket packet) {
    	if (lobby != null) {
    		if(!checkDos(true, "Lobby query")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		LobbyPathType type = (packet.type == LobbyType.MTT ? LobbyPathType.MTT : LobbyPathType.TABLES);
    		LobbyPath path = new LobbyPath(type, packet.gameid, packet.address);
    		
    		LobbyQueryRequest request = new LobbyQueryRequest(getPlayerid(), path);
        	request.setType(path.getType());
    		
    		List<ProtocolObject> packets = lobby.getLobbyQuery(request);
    		sendPackets(packets);
    		
    	} else {
    		log.warn("Lobby service not started");
    	}
    }
    

	@Override
    public void visit(LobbySubscribePacket packet) {
    	if (lobby != null) {
    		if(!checkDos(true, "Lobby subsribe")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		LobbyPathType type = (packet.type == LobbyType.MTT ? LobbyPathType.MTT : LobbyPathType.TABLES);

    		LobbyPath path = new LobbyPath(type, packet.gameid, packet.address);
    		
    		LobbySubscriptionRequest request = new LobbySubscriptionRequest(getPlayerid(), path);
        	request.setType(path.getType());
    		
    		lobby.subscribe(request, client);
    		
    	} else {
    		log.warn("Lobby service not started");
    	}
    }

    @Override
    public void visit(LobbyUnsubscribePacket packet) {
    	if (lobby != null) {
    		if(!checkDos(false, "Lobby unsubsribe")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		LobbyPathType type = (packet.type == LobbyType.MTT ? LobbyPathType.MTT : LobbyPathType.TABLES);
    		LobbyPath path = new LobbyPath(type, packet.gameid, packet.address);
    		
    		LobbyUnsubscriptionRequest request = new LobbyUnsubscriptionRequest(getPlayerid(), path);
        	request.setType(path.getType());
    		
    		lobby.unsubscribe(request, client);
    		
    	} else {
    		log.warn("Lobby service not started");
    	}
    }

    
    @Override
    public void visit(LobbyObjectSubscribePacket packet) {
    	if (lobby != null) {
    		if(!checkDos(true, "Lobby object subsribe")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		LobbyPathType type = (packet.type == LobbyType.MTT ? LobbyPathType.MTT : LobbyPathType.TABLES);
    		LobbyPath path = new LobbyPath(type, packet.gameid, packet.address, packet.objectid);
    		
    		LobbySubscriptionRequest request = new LobbySubscriptionRequest(getPlayerid(), path);
        	request.setType(path.getType());
    		lobby.subscribeToLobbyObject(request, client);
    		
    	} else {
    		log.warn("Lobby service not started");
    	}
    }
    
    @Override
    public void visit(LobbyObjectUnsubscribePacket packet) {
    	if (lobby != null) {
    		if(!checkDos(false, "Lobby object unsubsribe")) {
    			return; // EARLY RETURN, DOS PROTECTION
    		}
    		
    		LobbyPathType type = (packet.type == LobbyType.MTT ? LobbyPathType.MTT : LobbyPathType.TABLES);
    		LobbyPath path = new LobbyPath(type, packet.gameid, packet.address, packet.objectid);
    		
    		LobbyUnsubscriptionRequest request = new LobbyUnsubscriptionRequest(getPlayerid(), path);
        	request.setType(path.getType());
    		lobby.unsubscribeToLobbyObject(request, client);
    		
    	} else {
    		log.warn("Lobby service not started");
    	}
    }
    
	@Override
	public void visit(VersionPacket packet) {
		if(!checkDos(false, "Version request")) {
			return; // EARLY RETURN, DOS PROTECTION
		}
		
		VersionPacket response = new VersionPacket();
		response.game = 0;
		response.operatorid = 0;
		response.protocol = new ProtocolObjectFactory().version();
		
		if (response.protocol != packet.protocol) {
			log.warn("Client ["+getPlayerid()+"] has wrong version id. Server: "+response.protocol+" Client: "+packet.protocol);
		}
		
		sendPacket(response);
	}
	
	@Override
	public void visit(GameVersionPacket packet) {
		if(!checkDos(false, "Game Version request")) {
			return; // EARLY RETURN, DOS PROTECTION
		}
		
		GameVersionPacket response = new GameVersionPacket();
		response.game = packet.game;
		response.operatorid = packet.operatorid;
		 
		// Lookup game version using gid and system state
		SystemStateServiceContract serviceInstance = serviceRegistry.getServiceInstance(SystemStateServiceContract.class);
		String version = String.valueOf(serviceInstance.getAttribute(SystemStateConstants.GAME_ROOT_FQN+packet.game, "version"));
		
		response.version = version != null ? version : "";
		
		sendPacket(response);
	}

	@Override
	public void visit(LocalServiceTransportPacket packet) {
		LocalServiceManagerContract manager = serviceRegistry.getServiceInstance(LocalServiceManagerContract.class);
		if (manager != null) {
			LocalServiceAction action = new LocalServiceAction(packet.seq);
			action.setData(packet.servicedata);
			action.setRemoteAddress(client.getRemoteAddress());
			manager.handleAction(action, client.getLocalActionHandler());
		} else {
			log.error("No service for contract LocalServiceManagerContract. Ignoring packet: "+packet);
		}
	}
	
    
    @Override
    public void visit(SystemInfoRequestPacket packet) {
        log.debug("Request system info from player: "+client.getId()+" : "+client.getSessionId());
        if(!checkDos(false, "System info request")) {
            return; // EARLY RETURN, DOS PROTECTION
        }
        SystemInfoServiceContract infoService = serviceRegistry.getServiceInstance(SystemInfoServiceContract.class);
        if (infoService != null) {
            SystemInfoRequestAction request = new SystemInfoRequestAction();
            infoService.handleSystemInfoRequest(request, client.getLocalActionHandler());
        }
    }
	
	
	// --- PRIVATE METHODS --- //
	
	private boolean checkDos(boolean isLobby, String prefix) {
		if(dosAllow(isLobby)) {
			isDosWarned = false;
			return true;
		} else {
			if(!isDosWarned) {
				log.warn(prefix + " dropped for client due to access rules; Remote address: " + super.client.getRemoteAddress() + "; Local client id: " + dosId);
				isDosWarned = true;
			}
			return false;
		}
	}
	
	private boolean dosAllow(boolean isLobby) {
		return dosProtect.allow(isLobby ? LobbyServiceContract.LOBBY_DOS_KEY : GatewayNode.GATEWAY_DOS_KEY, dosId);
	}
}   

