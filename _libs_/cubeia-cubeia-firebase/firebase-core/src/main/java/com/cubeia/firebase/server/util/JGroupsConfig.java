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
package com.cubeia.firebase.server.util;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;

/**
 * This interface contains common jgroups 2.5 configuration 
 * parameters. It should be used by all XML based jgroups configurable
 * modules. The {@link #getMcastAddress()} method must be overridden
 * with default values.
 * 
 * <p>This interface is largely undocumented. Please only change if you
 * understand jgroups and its configuration, in which case the methods will
 * make sense.
 * 
 * @author Larsan
 */
public interface JGroupsConfig extends Configurable {
	
	public SocketAddress getMcastAddress();
	
	
	// --- METHODS WITH DEF VALUES --- //
	
	@Property(defaultValue="500")
	public long getMpingTimeout(); // TESTING...
	
	// @Property(defaultValue="true") 
	// public boolean getUseMcast();
	
	@Property(defaultValue="tcp")
	public TransportType getTpType();
	
	@Property(defaultValue="7800")
	public int getTcpStartPort();
	
	@Property(defaultValue="7900")
	public int getTcpEndPort();
	
	//@Property(defaultValue="true")
	//public boolean getUseTcpUnicast();
	
	@Property(defaultValue="600,1200,2400,3600")
	public StringList getTcpUnicastTimeouts();
	
	@Property(defaultValue="3")
	public int getTcpReaderThreads();
	
	@Property(defaultValue="3")
	public int getTcpWriterThreads();
	
	@Property(defaultValue="8")
	public int getTcpProcessorThreads();
	
	@Property(defaultValue="8")
	public int getTcpProcessorMinThreads();
	
	@Property(defaultValue="8")
	public int getTcpProcessorMaxThreads();
	
	@Property(defaultValue="100")
	public int getTcpProcessorQueueSize();
	
	@Property(defaultValue="false") 
	public boolean getUseBundling();
	
	@Property(defaultValue="64000") 
	public long getMaxBundleSize();
	
	@Property(defaultValue="true") 
	public boolean getLoopback();
	
	@Property(defaultValue="1,4,120000,true,2000") 
	public ThreadPoolProperties getOobPoolProperties();
	
	@Property(defaultValue="1,4,120000,true,2000") 
	public ThreadPoolProperties getMainPoolProperties();

	@Property(defaultValue="true") 
	public boolean getUseFd();
	
	@Property(defaultValue="false") 
	public boolean getFdShun();

	@Property(defaultValue="60000") 
	public long getFdTimeout();
	
	@Property(defaultValue="3") 
	public int getFdRetries();
	
	@Property(defaultValue="true") 
	public boolean getUseFdSock();
	
	@Property(defaultValue="false") 
	public boolean getPrintGmsAddress();
	
	@Property(defaultValue="true") 
	public boolean getDiscardDeliveredNakAcks();
	
	@Property(defaultValue="60000") 
	public int getFragSize();
	
	@Property(defaultValue="true") 
	public boolean getUseStateTransfer();
	
	@Property(defaultValue="false") 
	public boolean getUseCompress();
	
	@Property(defaultValue="1024") 
	public int getCompressMinSize();
	
	@Property(defaultValue="3") 
	public int getCompressLevel();
	
	@Property(defaultValue="1000000") 
	public long getStableMaxBytes();
	
	@Property(defaultValue="1000") 
	public long getStableDelay();

	@Property(defaultValue="5000") 
	public long getStableAvarageGossip();
	
	@Property(defaultValue="false")
	public boolean getUseTcpUnicast();
	
	@Property(defaultValue="false")
	public boolean getUseConcurrentStack();
	
	@Property(defaultValue="discard")
	public RejectionPolicy getOobRejectionPolicy();
	
	@Property(defaultValue="discard")
	public RejectionPolicy getMainRejectionPolicy();


	// -- UNDOCUMENTED BELOW --- //

	@Property(defaultValue="2000000") 
	public int getFcMaxCredits();
		
}
