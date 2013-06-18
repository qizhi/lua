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
package com.cubeia.firebase.server.deployment.mtt;

import java.io.File;
import java.io.IOException;

import com.cubeia.firebase.api.mtt.TournamentDefinition;

public interface TournamentDeploymentLayout {

	public TournamentDefinition getTournamentDefinition(TournamentRevision rev) throws IOException;
	
	public File[] getUtilityLibraries(TournamentRevision rev);
	
	public File[] getGameLibraries(TournamentRevision rev);
	
	public File getOpenClassFolder(TournamentRevision rev);

	/**
	 * The path supplied should be relative to the archive root,
	 * and use '/' as separator.
	 * 
	 * @param revision Current revision, never null
	 * @param path Resource path, never null
	 * @return The file for the path, or null if not found
	 */
	public File getResource(TournamentRevision revision, String path);
	
}
