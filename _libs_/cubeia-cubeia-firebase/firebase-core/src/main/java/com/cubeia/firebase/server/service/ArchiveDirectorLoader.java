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

import java.io.IOException;

import org.apache.log4j.Logger;

public class ArchiveDirectorLoader implements ArchiveLoader {

	private ArchiveDirector director;
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Override
	public void init(ArchiveLoaderContext con) throws IOException, IllegalArchiveException {
		log.debug("Service registry inits archive director on work directory: " + con.getWorkDirectory());
		director = new ArchiveDirector(con.getWorkDirectory(), con.getSharedSpace(), con.getDeploymentClassLoader());
		director.init(con.getLocations());
	}
	
	@Override
	public ServiceArchive[] getServices() {
		return director.getServices();
	}

	@Override
	public void destroy() {
		if(director != null) {
			director.destroy();
		}
	}
}
