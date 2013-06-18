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
package com.cubeia.firebase.server.service;

import java.io.File;
import java.io.IOException;

public interface ServiceArchiveLayout {

	public File[] getLibraryFiles(ServiceArchive arch) throws IllegalArchiveException;
	
	public InternalServiceInfo getServiceInfo(ServiceArchive arch) throws IOException, IllegalArchiveException;
	
	/**
	 * The path supplied should be relative to the archive root,
	 * and use '/' as separator.
	 * 
	 * @param arch Service archive, never null
	 * @param path Resource path, never null
	 * @return The file for the path, or null if not found
	 */
	public File getResource(ServiceArchive arch, String path);
}
