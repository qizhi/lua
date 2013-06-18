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
package com.cubeia.firebase.server.gateway.client;

import java.net.InetSocketAddress;
import java.util.List;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.event.ClientGameActionHandler;
import com.cubeia.firebase.server.gateway.event.local.ClientLocalActionHandler;
import com.cubeia.firebase.server.gateway.util.CompilationCache;

/**
 * Model for a connected player/user/client
 * 
 * @author Fredrik
 *
 */
public interface Client {

	/**
	 * Get the global id of the client.
	 * -1 if not logged in.
	 * 
	 * @return
	 */
	public int getId();
	
	/**
	 * Get current state for the client.
	 * This is the state for the Client session only, 
	 * and should not be confused with the player status wich
	 * is much more fine grained.
	 * 
	 * A Client session can be:
	 * Connected, logged in or disconnected.
	 * 
	 * @return
	 */
	public void setState(ClientState logged_in);
	public ClientState getState();

	public void sendClientPacket(ProtocolObject packet);

	public void sendClientPackets(List<ProtocolObject> packet);

	public void sendClientPackets(CompilationCache packets);
	
	public void disconnected();
	
	/*
	 * Handle incoming data from client.
	 * 
	 * TODO:
	 * This declaration is deprecated and only needed for
	 * the NIO implementation. We should probably
	 * clean this up or remove the NIO implementation
	 * all together.
	 * 
	 * @param data
	 */
	// public void handleIncomingData(byte[] data);

	/**
	 * Get the stateful player data information class.
	 * @return
	 */
	public IPlayerData getClientData();
    
    /**
     * Get the action handler for the client.
     * @return
     */
    public ClientGameActionHandler getActionHandler();
    
    /**
     * Get the handler for local actions
     * @return
     */
    public ClientLocalActionHandler getLocalActionHandler();
    
    public ClientLocal getLocalData();
    
    public boolean closeForced();
    
    public void close();

	public String getSessionId();
	
	public void setSessionId(String sessionId);
	
	/**
	 * @return The node info for the client
	 */
	public ClientNodeInfo getNodeInfo();
	
	/**
	 * Get the remote socket address. 
	 * 
	 * @return
	 */
	public InetSocketAddress getRemoteAddress();
	public int getLocalClientId();
	
	/**
	 * Get the number of packets in the write queue (outgoing)
	 * @return
	 */
	public int getWriteQueueSize();
	
	/**
     * Get the number of bytes in the write queue (outgoing)
     * @return
     */
    public int getWriteQueueBytes();

	public void sendPing(int id);

	public void pingDisconnect();

}
