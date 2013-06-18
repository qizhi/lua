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
package com.cubeia.firebase.service.pubclientreg;

import java.net.SocketAddress;
import java.util.Map;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.clientregistry.ClientSessionState;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;

/**
 * See the {@link PublicClientRegistryService} for a description of this service
 * implementation. This is a simple wrapper on top of the real registry.
 * 
 * @author Lars J. Nilsson
 * @see PublicClientRegistryService
 */
public class PublicClientRegistryImpl implements Service, PublicClientRegistryService {
	
	/** The internal registry */
	private ClientRegistry clientRegistry;
	private ServiceContext context;



	/*------------------------------------------------
	
	 SERVICE BEAN Methods
	
	-------------------------------------------------*/
	
	public void init(ServiceContext con) throws SystemException {
		this.context = con;
	}

	public void start() {
		clientRegistry = context.getParentRegistry().getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
	}

	public void stop() {}
	
	public void destroy() {
		clientRegistry = null;
	}

	
	
	/*------------------------------------------------
	
	 SERVICE CONTRACT Methods
	
	-------------------------------------------------*/
	
	public int[] getAllLoggedIn() {
		return clientRegistry.getAllLoggedIn();
	}
	
	public boolean isLocal(int clientId) {
		return clientRegistry.isLocal(clientId);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getPlayerData(int playerId) {
		return clientRegistry.getPlayerData(playerId);
	}

	public String getScreenname(int playerId) {
		return clientRegistry.getScreenname(playerId);
	}
	
	public int getOperatorId(int playerId) {
		return clientRegistry.getOperatorId(playerId);
	}

	public boolean isLoggedIn(int playerId) {
		return clientRegistry.exists(playerId);
	}
	
	public ClientSessionState getClientStatus(int clientId) {
        return clientRegistry.getClientStatus(clientId);
    }
	
	public Map<Integer, Integer> getSeatedTables(int playerId) {
		return clientRegistry.getSeatedTables(playerId);
	}

	public void registerPlayerToTable(int tableid, int playerid, int seat, boolean remove) {
		registerPlayerToTable(tableid, playerid, seat, -1, remove);
	}
	
	public void registerPlayerToTable(int tableid, int playerid, int seat, int mttId, boolean remove) {
		if (!remove) {
	    	clientRegistry.addClientTable(playerid, tableid, seat, mttId);
	    	clientRegistry.removeWatchingTable(playerid, tableid);
		} else {
			clientRegistry.removeClientTable(playerid, tableid);
		}
    }

	
	public void registerWatcherToTable(int tableid, int playerid, boolean remove) {
		if (!remove) {
	    	clientRegistry.addWatchingTable(playerid, tableid);
		} else {
			clientRegistry.removeWatchingTable(playerid, tableid);
		}
	}
	
	
	/**
	 * @see com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService#getRemoteAddress(int)
	 */
	public SocketAddress getRemoteAddress(int clientId) {
		return clientRegistry.getRemoteAddress(clientId);
	}

    public int getGlobalClientCount() {
        return clientRegistry.getNumberOfClients();
    }

    public int getLocalClientCount() {
        return clientRegistry.getNumberOfGlobalClients();
    }

    
}
