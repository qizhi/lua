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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import se.xec.commons.fileset.RegexpFileSet;
import se.xec.commons.path.Path;
import se.xec.commons.resource.Resource;

import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.util.PathUtil;

public class DeploymentFolderSet extends RegexpFileSet implements FileSetResource, DeploymentResource {

	private static ThreadLocal<DeploymentFileSet> HACK_PARENT = new ThreadLocal<DeploymentFileSet>();

	private static String setHackParent(DeploymentFileSet parent) {
		HACK_PARENT.set(parent);
		return null;
	}
	
	
	// --- INSTANCE MEMBERS --- //
	
	private final DeploymentFileSet parent;
	private final int version;
	
	private ZipFileResource original;
	private File originalDirectory;
	
	private final AtomicReference<FileSetResource> lastClone = new AtomicReference<FileSetResource>();
	private final AtomicReference<Object> attachment = new AtomicReference<Object>();

	DeploymentFolderSet(File root, ZipFileResource original, int version, DeploymentFileSet parent) throws IOException {
		super(root, setHackParent(parent));
		this.original = original;
		this.version = version;
		this.parent = parent;
		setHackParent(null);
	}

	public DeploymentFolderSet(File root, File originalDirectory, int version, DeploymentFileSet parent) throws IOException {
		super(root, setHackParent(parent));
		this.originalDirectory = originalDirectory;
		this.version = version;
		this.parent = parent;
		setHackParent(null);
	}

	ZipFileResource getOriginal() {
		return original;
	}
	
	public File getOriginalDirectory() {
		return originalDirectory;
	}
	
	public Object getAttachment() {
		return attachment.get();
	}
	
	public void setAttachment(Object o) {
		attachment.set(o);
	}

	public FileSetResource clone(int version, boolean exploded) {
		 FileSetResource clone = parent.getResourceCopy(this, version, exploded);
		 lastClone.set(clone);
		 return clone;
	}
	
	public FileSetResource getLatestClone() {
		return lastClone.get();
	}

	public boolean exists() {
		return root.exists();
	}

	public DeploymentType getDeploymentType() {
		return (original == null ? DeploymentType.parse(originalDirectory) : original.getDeploymentType());
	}

	public int getDeploymentVersion() {
		return version;
	}

	public File getExplodedBase() {
		return root;
	}
	
	public File getRoot() {
		return root;
	}

	public File getFile() {
		return (original == null ? originalDirectory : original.getFile());
	}

	public long getLastModificationTime() {
		return root.lastModified();
	}

	public String getName() {
		return (original == null ? originalDirectory.getName() : original.getName());
	}

	public long getVisitedTime() {
		return -1;
	}
	
	public void updateVisitedTime() { }
	
	public boolean isExploded() {
		return true;
	}

	public Certificate[] getCertificates() {
		return null;
	}

	public String getContentType() {
		return null;
	}

	public InputStream getInputStream() {
		return null;
	}

	public Path getPath() {
		return PathUtil.createAbsoluteFilePath(root);
	}

	@SuppressWarnings("deprecation")
	public URL getURL() {
		try {
			return root.toURL();
		} catch (MalformedURLException e) {
			Logger.getLogger(getClass()).error("Malformed URL: " + e.getMessage(), e);
			return null;
		}
	}
	
	
	// -- PROTECTED METHODS --- //
	
	@Override
	protected Resource createFileResource(File file, Path path) {
		DeploymentFileSet fs = (parent == null ? HACK_PARENT.get() : parent);
		return fs.createFileResource(file, path);
	}
}
