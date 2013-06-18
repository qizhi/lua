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
package com.cubeia.firebase.api.mtt.support.registry;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttPlayerStatus;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;

/**
 * <pDefault implementation of a player registry.</p>
 * 
 * <p>This class should be included in the tournament state since it holds
 * all players and their current state.</p>
 * 
 * <p>This class automatically updates the system player registry.
 *
 * @author Fredrik
 */
public class PlayerRegistryImpl implements PlayerRegistry, PlayerRegistryImplMBean, Externalizable {
	
	/** Version id */
	private static final long serialVersionUID = 1L;

	private static transient Logger log = Logger.getLogger(PlayerRegistryImpl.class);
	
	/** Set of listeners. Transient */
	private transient Set<PlayerListener> listeners = Collections.synchronizedSet(new HashSet<PlayerListener>());
	
	/** Set of interceptors. Transient */
	private transient Set<PlayerInterceptor> interceptors = Collections.synchronizedSet(new HashSet<PlayerInterceptor>());
	
	/** Maps playerId to tournament player object. */
	private ConcurrentMap<Integer, MttPlayer> players = new ConcurrentHashMap<Integer, MttPlayer>();

	private int mttId;
	
	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/
	
   public PlayerRegistryImpl() {}
	   
	public PlayerRegistryImpl(int mttId) {
		this.mttId = mttId;
	}
	
	
	/*------------------------------------------------
	 
		REGISTRY HANDLING

	 ------------------------------------------------*/
	
	/**
	 * @see com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry#register(MttPlayer)
	 */
	public MttRegisterResponse register(MttInstance instance, MttRegistrationRequest request) {
		MttRegisterResponse response = MttRegisterResponse.ALLOWED;
		MttPlayer player = request.getPlayer();
		
		// Check interceptors
		for (PlayerInterceptor intercept : interceptors) {
			MttRegisterResponse interceptResponse = intercept.register(instance, request);
			if (!interceptResponse.equals(MttRegisterResponse.ALLOWED)) {
				// Interceptor said NO. Break and use return value
				response =  interceptResponse;
				break;
			}
		}
		
		// Check if tournament is full
		boolean full = false;
		if (instance.getState().getCapacity() == instance.getState().getRegisteredPlayersCount()) {
		    response = MttRegisterResponse.DENIED_MTT_FULL;
		    full = true;
		}
		
		// Only register if interceptors say OK
		if (response.equals(MttRegisterResponse.ALLOWED) && !full) {
		    if (players.containsKey(player.getPlayerId())) {
		        log.debug("registering an already registered player to tournament, tournament = " +
		            mttId + ", playerId = " + player.getPlayerId());
		    }
		    
			player.setStatus(MttPlayerStatus.REGISTERED);
			players.put(player.getPlayerId(), player);
			// log.debug("Added player["+player.getPlayerId()+"] to " +"tournament["+mttId+"]. Registry (current size: "+players.size()+")");
			
	        // Notify listeners 
	        for (PlayerListener listen : listeners) {
	            listen.playerRegistered(instance, request);
	        }
	        
	        // Notify system
	        TournamentPlayerRegistry reg = instance.getSystemPlayerRegistry();
	        reg.register(player.getPlayerId(), instance.getId());
		}
		
		return response;
	}

	public boolean isRegistered(int pid) {
		return players.containsKey(pid);
	}
	
	/**
	 * @see com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry#unregister(int)
	 */
	public MttRegisterResponse unregister(MttInstance instance, int pid) {
		MttRegisterResponse response = MttRegisterResponse.ALLOWED;
		
		// Check interceptors
		for (PlayerInterceptor intercept : interceptors) {
			MttRegisterResponse interceptResponse = intercept.unregister(instance, pid);
			if (!interceptResponse.equals(MttRegisterResponse.ALLOWED)) {
				// Interceptor said NO. Break and use return value
				response = interceptResponse;
			}
		}
		
		// Only unregister if interceptors says OK
		if (response.equals(MttRegisterResponse.ALLOWED)) {
			players.remove(pid);
			
			// Notify listeners 
			for (PlayerListener listen : listeners) {
			    listen.playerUnregistered(instance, pid);
			}
			
			// Notify system
	        TournamentPlayerRegistry reg = instance.getSystemPlayerRegistry();
	        reg.unregister(pid, instance.getId());
		}
		
		
		return response;
	}
	
	
	public int size() {
		return players.size();
	}
	
    public Collection<MttPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

	
	/*------------------------------------------------
	 
		LISTENERS AND INTERCEPTORS

	 ------------------------------------------------*/
	
	public void registerInterceptor(PlayerInterceptor interceptor) {
		checkInterceptors();
		interceptors.add(interceptor);
	}

	public void registerListener(PlayerListener listener) {
		checkListeners();
		listeners.add(listener);
	}
	
	public void unregisterListener(PlayerListener listener) {
		checkListeners();
		listeners.remove(listener);
	}

	public void unregisterInterceptor(PlayerInterceptor interceptor) {
		checkInterceptors();
		interceptors.remove(interceptor);
	}
	
	public void clearInterceptors() {
		checkInterceptors();
		interceptors.clear();
	}


	public void clearListeners() {
		checkListeners();
		listeners.clear();
	}

	/**
	 * Since the interceptor set is transient we cannot guarantee that they are 
	 * initialized for a new event.
	 */
	private void checkInterceptors() {
		if (interceptors == null) {
			interceptors = Collections.synchronizedSet(new HashSet<PlayerInterceptor>());
		}
	}
	
	/**
	 * Since the listener set is transient we cannot guarantee that they are 
	 * initialized for a new event.
	 */
	private void checkListeners() {
		if (listeners == null) {
			listeners = Collections.synchronizedSet(new HashSet<PlayerListener>());
		}
	}
	
	/*------------------------------------------------
	 
		MBEAN (JMX) METHODS 
	
	 ------------------------------------------------*/
	public Integer[] getRegisteredPlayerIds() {
		Set<Integer> set = players.keySet();
		Integer[] pids = set.toArray(new Integer[0]);
		return pids;
	}
	
	public void printPlayerCountToLog() {
		String nfo = "Tournament ["+mttId+"] Player registry size: "+players.size();
		log.info(nfo);
	}
	
	public void printPlayersToLog() {
		String nfo = "Tournament ["+mttId+"] Player registry size("+players.size()+"). Data: \n";
		nfo += players.toString();
		log.info(nfo);
	}

	public void removePlayer(int pid) {
		players.remove(pid);
	}

	/*------------------------------------------------
	 
		PRIVATE METHODS
	
	 ------------------------------------------------*/
	
	

	/*------------------------------------------------

        SERIALIZING METHODS

     ------------------------------------------------*/
	
	/**
     * Externalizable implementation.
     * NOTE: This method will be used instead of regular java serialization.
     * We have implemented this for more efficient serializing, i.e. less data.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        mttId = in.readInt();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            MttPlayer player = readMttPlayer(in);
            players.put(player.getPlayerId(), player);
        }
    }

    /**
     * Externalizable implementation.
     * NOTE: This method will be used instead of regular java serialization.
     * We have implemented this for more efficient serializing, i.e. less data.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(mttId);
        out.writeInt(players.size());
        for (MttPlayer p : players.values()) {
            writeMttPlayer(p, out);
        }
    }
    
    
    public MttPlayer readMttPlayer(ObjectInput in) throws IOException, ClassNotFoundException {
        MttPlayer player = new MttPlayer(in.readInt(), in.readUTF());   // Id & Screenname
        player.setPosition(in.readInt());                               // Position
        player.setStatus(MttPlayerStatus.values()[in.readByte()]);      // Status
        return player;
    }

    /**
     * Externalizable implementation.
     * NOTE: This method will be used instead of regular java serialization.
     * We have implemented this for more efficient serializing, i.e. less data.
     */
    public void writeMttPlayer(MttPlayer player, ObjectOutput out) throws IOException {
        out.writeInt(player.getPlayerId());
        out.writeUTF(player.getScreenname());
        out.writeInt(player.getPosition());
        out.writeByte((byte)player.getStatus().ordinal());
    }
	
}
