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
package com.cubeia.firebase.service.deploy.tournament.impl;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.cubeia.firebase.util.Files;

/**
 * Models a Game Archive.
 * 
 * @author Fredrik
 *
 */
public class TournamentArchive {
	
	/** The underlying archive. This archive is not guaranteed to remain */
	private ZipFile archive;
	
	private String name;
	
	public TournamentArchive(File file) {
		try {
			archive = new ZipFile(file);
		} catch (ZipException e) {
			throw new RuntimeException("Could not load Zipped archive: "+file);
		} catch (IOException e) {
			throw new RuntimeException("Could not load Zipped archive: "+file);
		}
		name = archive.getName();
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Extract to supplied folder.
	 * 
	 * @param folder
	 * @return true if success
	 */
	public boolean extract(File folder) {
		boolean ok = false;
		try {
		    Files.explode(archive, folder);
        } catch(Exception e) {
            System.err.println(e);
        }
        
		return ok;
	}
}
