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
package com.cubeia.firebase.server.deployment.resources;

import java.io.File;

import com.cubeia.firebase.server.deployment.DeploymentType;

import se.xec.commons.resource.Resource;

public interface DeploymentResource extends Resource {

	public File getFile();
	
	public String getName();
	
	public boolean isExploded();
	
	public File getExplodedBase();

	public int getDeploymentVersion();
	
	public FileSetResource getLatestClone();
	
	public FileSetResource clone(int version, boolean exploded);
	
	public DeploymentType getDeploymentType();
	
	public long getLastModificationTime();
	
	public long getVisitedTime();
	
	public boolean exists();

	public void updateVisitedTime();
	
}
