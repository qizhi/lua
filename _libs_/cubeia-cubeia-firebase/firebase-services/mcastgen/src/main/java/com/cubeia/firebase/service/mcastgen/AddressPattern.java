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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;

class AddressPattern {
	
	private int[] addressOffsets;
	private int portOffset;
	
	AddressPattern(String pattern, int portOffset) {
		this.portOffset = portOffset;
		parsePattern(pattern);
	}

	public SocketAddress generate(SocketAddress base) throws UnknownHostException {
		Arguments.notNull(base, "base");
		String address = generateAddress(base.getHost());
		int port = base.getPort() + portOffset;
		return new SocketAddress(address + ":" + port);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private String generateAddress(InetAddress host) {
		byte[] src = host.getAddress();
		return (combine(src, 0) & 0xFF) + "." + (combine(src, 1) & 0xFF) + "." + (combine(src, 2) & 0xFF) + "." + (combine(src, 3) & 0xFF);
	}

	private int combine(byte[] src, int index) {
		return src[index] + addressOffsets[index];
	}

	private void parsePattern(String str) {
		String[] arr = str.split("\\.");
		if(arr.length != 4) throw new IllegalArgumentException("Patterns string '" + str + "' does not contain 4 tokens");
		addressOffsets = new int[4];
		addressOffsets[0] = parsePart(arr, 0);
		addressOffsets[1] = parsePart(arr, 1);
		addressOffsets[2] = parsePart(arr, 2);
		addressOffsets[3] = parsePart(arr, 3);
	}

	private int parsePart(String[] arr, int index) {
		String s = arr[index];
		if(s.equals("*")) {
			return 0;
		}
		else {
			return Integer.parseInt(s);
		}
	}
}
