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
package com.cubeia.firebase.server.service.crypto;

import java.io.File;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;

@Configurated(inheritance=Inheritance.ALLOW)
public interface ServerConfig extends Configurable {

	/**
	 * @return True if SSL is enabled, false otherwise
	 */
	@Property(defaultValue="false")
	public boolean isSslEnabled();
	
	/**
	 * @return The key store to use for SSL
	 */
	public File getSslKeyStore();
	
	/**
	 * @return The key store type, "jks" or "pkcs12"
	 */
	@Property(defaultValue="jks")
	public KeyStoreType getSslKeyStoreType();
	
	/**
	 * @return The password for the SSL key store
	 */
	public String getSslKeyStorePassword();

}
