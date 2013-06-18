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
package com.cubeia.firebase.api.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A configuration source represents a single source of configuration
 * for an artifact. Normally this interface hides a file system object but may
 * be used for other sources as well.
 * 
 * @author lars.j.nilsson
 */
public interface ConfigSource {
	
	/**
	 * Get the name of the configuration. For files this will normally be
	 * the file name less the extension. 
	 * 
	 * @return The name of this configuration, never null
	 */
	public String getName();

	/**
	 * Get an input stream for reading the configuration. The caller is 
	 * responsible for closing the input stream.
	 * 
	 * @return The configuration source as a stream, never null
	 */
	public InputStream getInputStream() throws IOException;
	
}