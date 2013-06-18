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
package com.cubeia.firebase.server.jmx;

/**
 * An interface for getting performance data
 * from system. (man 5 proc for further info).
 * @author mikael.lowenadler
 *
 */
public interface SystemMonitorMBean {

	// /proc/cpuifo
	public String[] getSystemInfo();
	
	// /proc/meminfo
	public long getMemoryFree();
	public long getMemoryBuffered();	
	public long getMemoryCached();	
	public long getMemorySwapped();	
	
	// /proc/loadavg
	public double getLoadAverage1Min();
	public double getLoadAverage5Min();
	public double getLoadAverage15Min();
	
	// /proc/stat
	public double getCPUUser();
	public double getCPUUserNice();
	public double getCPUSystem();
	public double getCPUIdle();
	public double getCPUIOWait();
	public double getCPUIRQ();
	public double getCPUSoftIRQ();
	
	// gameserver stats
	/*public long getPokerAllLoggedIn();
	public long getPokerLocallyLoggedIn();	
	public long getPokerAllSeated();
	public long getPokerLocallySeated();
	public long getPokerLocalTables();	
	public long getPokerHandsPerHour();		*/
    //public double getAvarageGotPingReplyTime();
}
