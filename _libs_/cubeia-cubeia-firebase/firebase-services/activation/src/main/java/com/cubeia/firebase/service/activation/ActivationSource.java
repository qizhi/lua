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
package com.cubeia.firebase.service.activation;

import java.io.IOException;
import java.io.InputStream;

import com.cubeia.firebase.api.util.ConfigSource;

/**
 * Trivial {@link ActivationConfigSource config source} implementation. It 
 * acts as an adapter keeping wrapped fields for type and parent config. 
 * 
 * @see ConfigSource
 * @author Lars J. Nilsson
 */
public class ActivationSource implements ActivationConfigSource {

	private final ConfigSource parent;
	private final ActivationType type;

	/**
	 * @param parent Wrapped config, must not be null
	 * @param type Type of the config, must not be null
	 */
	ActivationSource(ConfigSource parent, ActivationType type) {
		this.parent = parent;
		this.type = type;
	}
	

	// --- PACKAGE METHODS --- //
	
	ConfigSource getWrappedSource() {
		return parent;
	}
	
	
	// --- CONFIG SOURCE --- //
	
	@Override
	public ActivationType getType() {
		return type;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return parent.getInputStream();
	}

	@Override
	public String getName() {
		return parent.getName();
	}
}
