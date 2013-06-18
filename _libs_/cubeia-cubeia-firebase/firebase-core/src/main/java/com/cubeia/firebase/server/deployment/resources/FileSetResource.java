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

import se.xec.commons.fileset.FileSet;
import se.xec.commons.resource.Resource;

public interface FileSetResource extends FileSet, Resource { 
	
	public File getRoot();

	/**
	 * This method is a major hack. The attachment is used only when
	 * SAR files are picked up during server start. It should be refactored.
	 */
	public void setAttachment(Object o);
	
	/**
	 * This method is a major hack. The attachment is used only when
	 * SAR files are picked up during server start. It should be refactored.
	 */
	public Object getAttachment();
	
	public int getDeploymentVersion();

}
