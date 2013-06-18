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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple configuration source based on a file.
 * 
 * @author lars.j.nilsson
 */
public class FileConfigSource implements ConfigSource {
	
	private final String name;
	private final File file;

	/**
	 * @param name Config name, must not be null
	 * @param file Config file, must exist, must not be null
	 * @throws IOException If the file does not exist, or is a directory
	 */
	public FileConfigSource(String name, File file) throws IOException {
		Arguments.notNull(name, "name");
		Arguments.notNull(file, "file");
		this.name = name;
		if(!file.exists()) throw new FileNotFoundException("File '" + file + "' does not exist.");
		if(file.isDirectory()) throw new IOException("Path '" + file + "' is a directory and not a file.");
		this.file = file;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * @return A file input stream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}
	
	@Override
	public String toString() {
		return "file://" + file.getAbsolutePath() + " [" + name + "]";
	}
}
