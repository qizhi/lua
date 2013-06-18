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
 * This interface is used by games or services to locate resources within their 
 * GAR/SAR archives. It uses a string path which should be given relative
 * to the archive root, and use forward slashes, '/', as path separator.
 * 
 * @author Larsan
 */
public interface ResourceLocator {

	/**
	 * @param path Path of the resource in the archive, must not be null
	 * @return An open input stream from the resource, null if not found
	 * @throws IOException If the resource cannot be opened
	 */
	public InputStream openResource(String path) throws IOException;

}
