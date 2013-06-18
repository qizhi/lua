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

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;

public class JGroupsConfigAdapter implements JGroupsConfig {

	private final JGroupsConfig parent;
	
	public JGroupsConfigAdapter(JGroupsConfig parent) {
		Arguments.notNull(parent, "parent");
		this.parent = parent;
	}
	
	@Override
	public long getMpingTimeout() {
		return parent.getMpingTimeout();
	}
	
	public int getCompressLevel() {
		return parent.getCompressLevel();
	}

	public int getCompressMinSize() {
		return parent.getCompressMinSize();
	}

	public boolean getDiscardDeliveredNakAcks() {
		return parent.getDiscardDeliveredNakAcks();
	}

	public int getFcMaxCredits() {
		return parent.getFcMaxCredits();
	}

	public int getFdRetries() {
		return parent.getFdRetries();
	}

	public boolean getFdShun() {
		return parent.getFdShun();
	}

	public long getFdTimeout() {
		return parent.getFdTimeout();
	}

	public int getFragSize() {
		return parent.getFragSize();
	}

	public boolean getLoopback() {
		return parent.getLoopback();
	}

	public ThreadPoolProperties getMainPoolProperties() {
		return parent.getMainPoolProperties();
	}

	public long getMaxBundleSize() {
		return parent.getMaxBundleSize();
	}

	public SocketAddress getMcastAddress() {
		return parent.getMcastAddress();
	}

	public ThreadPoolProperties getOobPoolProperties() {
		return parent.getOobPoolProperties();
	}

	public boolean getPrintGmsAddress() {
		return parent.getPrintGmsAddress();
	}

	public long getStableAvarageGossip() {
		return parent.getStableAvarageGossip();
	}

	public long getStableDelay() {
		return parent.getStableDelay();
	}

	public long getStableMaxBytes() {
		return parent.getStableMaxBytes();
	}

	public int getTcpEndPort() {
		return parent.getTcpEndPort();
	}

	public int getTcpProcessorMaxThreads() {
		return parent.getTcpProcessorMaxThreads();
	}

	public int getTcpProcessorMinThreads() {
		return parent.getTcpProcessorMinThreads();
	}

	public int getTcpProcessorQueueSize() {
		return parent.getTcpProcessorQueueSize();
	}

	public int getTcpProcessorThreads() {
		return parent.getTcpProcessorThreads();
	}

	public int getTcpReaderThreads() {
		return parent.getTcpReaderThreads();
	}

	public int getTcpStartPort() {
		return parent.getTcpStartPort();
	}

	public int getTcpWriterThreads() {
		return parent.getTcpWriterThreads();
	}

	public TransportType getTpType() {
		return parent.getTpType();
	}

	public boolean getUseBundling() {
		return parent.getUseBundling();
	}

	public boolean getUseCompress() {
		return parent.getUseCompress();
	}

	public boolean getUseFd() {
		return parent.getUseFd();
	}

	public boolean getUseFdSock() {
		return parent.getUseFdSock();
	}

	public boolean getUseStateTransfer() {
		return parent.getUseStateTransfer();
	}

	public StringList getTcpUnicastTimeouts() {
		return parent.getTcpUnicastTimeouts();
	}
	
	public RejectionPolicy getMainRejectionPolicy() {
		return parent.getMainRejectionPolicy();
	}
	
	public RejectionPolicy getOobRejectionPolicy() {
		return parent.getOobRejectionPolicy();
	}
	
	public boolean getUseTcpUnicast() {
		return parent.getUseTcpUnicast();
	}
	
	public boolean getUseConcurrentStack() {
		return parent.getUseConcurrentStack();
	}
}
