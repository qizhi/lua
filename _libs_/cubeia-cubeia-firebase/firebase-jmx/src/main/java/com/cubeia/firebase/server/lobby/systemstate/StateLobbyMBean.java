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
package com.cubeia.firebase.server.lobby.systemstate;


/**
 * This MBean is attached to the lobby in a Firebase system. It 
 * contains method for changing the broadcast period of the lobby.
 * 
 * @author Larsan
 */
public interface StateLobbyMBean { 
	
	/**
	 * This method prints information of the lobby subscription to
	 * the server log. This subscription contains paths and object information. 
	 * Use with care as this method is fairly resource demanding.
	 */
	public void dumpSubscriptionInfoToLog();
	
	/**
	 * This method returns information of the lobby subscription.
	 *  This subscription contains paths and object information. 
	 * Use with care as this method is fairly resource demanding.
	 */
	public String printSubscriptionInfo();
	
	/**
	 * This method returns the number of subscriptions for a 
	 * particular path.
	 */
	public int countSubscribersForPath(String type, int area, String domain);
	
	/**
	 * This method return the broadcast period in milliseconds. The
	 * broadcast period is the interval in which Firebase sends lobby
	 * information to the clients.
	 * 
	 * @return The broadcast period in milliseconds
	 */
	public long getBroadcastPeriod();
	
	/**
	 * This method changes the broadcast period, the interval between lobby
	 * broadcasts. 
	 * 
	 * @param millis The interval in milliseconds
	 */
	public void setBroadcastPeriod(long millis);
	
	/**
	 * This method returns the approximate number of change that have been recorded
	 * for tables in the lobby per second.
	 * 
	 * @return The number of table changes per second
	 */
	public int getTableUpdatesPerSecond();
	
	/**
	 * This method temporarily halts the broadcast of lobby changes to all clients. In
	 * effect the lobby will stop updating for all connected clients.
	 */
	public void pause();
	
	/**
	 * If the lobby is {@link #pause() paused}, this method un-pauses it.
	 */
	public void unpause();
	
	/**
	 * Checks if the lobby is paused or not.
	 * 
	 * @return True if the lobby is paused, false if not
	 */
	public boolean isPaused();
	
	/**
	 * Get a debug string that contains total packet count and packets per path
	 * 
	 * @return
	 */
	public String printPacketCounts();
	
	/**
	 * Get a count for how many lobby paths that are stored in memory
	 * 
	 * @return
	 */
	public int getSnapshotNodeCount();
    
	/**
	 * Get a count for how many lobby paths that are stored in memory
	 * 
	 * @return
	 */
    public int getDeltaSnapshotNodeCount();
    
    /**
     * Get total subscription count. This is usually higher than the actual
     * number of connected clients since a client can have many subscriptions running.
     * @return
     */
    public int getSubscriptionCount();
    
    /**
     * Returns all known paths in the lobby tree (including non-leaves)
     * @return
     */
    public String printPaths();
    
    public String printTableData(int tableId);
    
    public String printTournamentData(int mttId);
	
}
