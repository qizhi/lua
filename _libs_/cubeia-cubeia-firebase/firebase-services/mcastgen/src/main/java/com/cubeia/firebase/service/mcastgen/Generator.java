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
import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.util.SocketAddress;

class Generator {

	private Map<String, AddressPattern> patterns;
	private SocketAddress base;
	
	Generator(SocketAddress base, Map<String, String> props) {
		this.base = base;
		patterns = new HashMap<String, AddressPattern>(props.size());
		for (String key : props.keySet()) {
			if(isPatternKey(key)) {
				int i = key.indexOf('$');
				if(i == -1) throw new IllegalArgumentException("Configuration key '" + key + "' is missing port");
				String pattern = key.substring(0, i);
				String port = key.substring(i + 1);
				String id = props.get(key);
				AddressPattern p = new AddressPattern(pattern, Integer.parseInt(port));
				patterns.put(id, p);
			}
		}
	}
	
	private boolean isPatternKey(String key) {
		return (!key.startsWith("_") && !key.trim().startsWith("#") && key.trim().length() > 0);
	}

	public boolean isKnown(String id) {
		return patterns.containsKey(id);
	}
	
	public SocketAddress generate(String id) throws UnknownHostException {
		AddressPattern pattern = patterns.get(id);
		if(pattern == null) {
			return null;
		} else {
			return pattern.generate(base);
		}
	}
}
