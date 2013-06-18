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
package com.cubeia.firebase.service.mcastgen;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import com.cubeia.firebase.api.util.SocketAddress;

/**
 * This class is responsible for generating addresses for 
 * services that are not configured.
 * 
 * @author Larsan
 */
class NaGenerator {
	
	private final AtomicInteger portCount = new AtomicInteger(0);
	private final AtomicInteger addressCount = new AtomicInteger(0);
	
	private String addressPrefix; // first 3 bytes, ending with a '.'
	private int leastByteBase; // to increment
	
	private int portBase; // to increment

	NaGenerator(SocketAddress base) {
		generatePrefix(base);
		assignLeastByte(base);
		assignPortBase(base);
	}

	public SocketAddress generate() throws UnknownHostException {
		int port = portBase + portCount.incrementAndGet();
		int leastByte = leastByteBase + addressCount.incrementAndGet();
		String address = addressPrefix + (leastByte & 0xFF);
		return new SocketAddress(address + ":" + port);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void assignPortBase(SocketAddress base) {
		portBase = base.getPort();
	}

	private void assignLeastByte(SocketAddress base) {
		leastByteBase = base.getHost().getAddress()[3];
	}

	private void generatePrefix(SocketAddress base) {
		byte[] src = base.getHost().getAddress();
		addressPrefix = (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + ".";
	}
}