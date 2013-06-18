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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.deployment.DeploymentType;

import se.xec.commons.fileset.FileResource;
import se.xec.commons.path.Path;

public class XmlFileResource extends FileResource implements DeploymentResource {

	private final DeploymentType type;
	private final File file;
	private final int version;
	private final DeploymentFileSet parent;
	
	private final AtomicReference<FileSetResource> lastClone = new AtomicReference<FileSetResource>();

	XmlFileResource(File root, Path filePath, File file, DeploymentType type, int version, DeploymentFileSet parent) {
		super(root, filePath, file);
		this.file = file;
		this.type = type;
		this.version = version;
		this.parent = parent;
	}
	
	public FileSetResource clone(int version, boolean exploded) {
		 FileSetResource clone = parent.getResourceCopy(this, version, false);
		 lastClone.set(clone);
		 return clone;
	}
	
	public FileSetResource getLatestClone() {
		return lastClone.get();
	}
	
	public void updateVisitedTime() {
		super.visited = System.currentTimeMillis();
		if(super.visited < file.lastModified()) {
			Logger.getLogger(getClass()).warn("Clock mismatch, system clock appears to lag behind the file system, or the file is set to future: " + file.getAbsolutePath());
			super.visited = file.lastModified();
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public File getExplodedBase() {
		return null;
	}
	
	public boolean isExploded() {
		return false;
	}
	
	public int getDeploymentVersion() {
		return version;
	}

	public String getName() {
		return file.getName();
	}
	
	@Override
	public String getContentType() {
		return type.getContentType();
	}
	
	public DeploymentType getDeploymentType() {
		return type;
	}
	
	public long getLastModificationTime() {
		return file.lastModified();
	}
}
