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
package com.cubeia.firebase.server.deployment;

import java.io.File;
import java.io.IOException;

import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.resources.DeploymentFileSet;
import com.cubeia.firebase.server.instance.ServerInstance;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.util.Files;

public class ResourceManager implements Startable {
	
	private static final String DEPLOY_FOLDER_NAME = "deploy/";
	private static final String WORK_FOLDER_NAME = "work/";
	private static final String LIB_FOLDER_NAME = "lib/";

	private ClassLoader depLoader;
	private DeploymentFileSet depSet;
	
	// private final File gameDir;
	private File deployFolder;
	private File workFolder;
	private File libFolder;

	public ResourceManager(File gameDir, ClassLoader depLoader) throws SystemException {
		Arguments.notNull(gameDir, "game dir");
		// this.gameDir = gameDir;
		this.depLoader = depLoader;
		setupFolders(gameDir);
		setupClassLoader();
		setupFileSet();
	}
	
	/**
	 * Test-only constructor!
	 */
	ResourceManager(File deployFolder, File workFolder, ClassLoader depLoader) throws SystemException {
		this.depLoader = depLoader;
		this.deployFolder = deployFolder;
		this.workFolder = workFolder;
		setupClassLoader();
		setupFileSet();
	}

	public ClassLoader getDeploymentClassLoader() {
		return depLoader;
	}
	
	public DeploymentFileSet getDeploymentFileSet() {
		return depSet;
	}
	
	public void start() {
		depSet.start();
	}
	
	public void stop() {
		depSet.stop();
		cleanup();
	}

	
	
	
	// --- PRIVATE METHODS --- //
	
	private void cleanup() {
		Files.recursiveDelete(workFolder);
	}

	private void setupFileSet() throws SystemCoreException {
		try {
			depSet = new DeploymentFileSet(deployFolder, workFolder);
		} catch (IOException e) {
			throw new SystemCoreException("Failed to create deployment file set; Given message: " + e.getMessage(), e);
		}
	}
	
	private void setupClassLoader() {
		/*
		 * We know that the server instance is loaded by the server loader,
		 * we also know, from the bootstrap server, that it's parent is the
		 * the shared space loader. /LJN
		 */
		ClassLoader serverLoader = ServerInstance.class.getClassLoader().getParent();
		if(depLoader == null) {
			/*
			 * We need to create a new deployment class loader if we're
			 * not given one in the constructor
			 */
			if(!Constants.IN_ECLIPSE) {
				/*
				 * We're outside eclipse, so we're setting up a real loader which
				 * use the lib folder (which may be null).
				 */
				depLoader = new DeploymentClassLoader(libFolder, serverLoader);
			} else {
				/*
				 * Hack attack: We're in eclipse, so use the main class loader as parent
				 */
				depLoader = new DeploymentClassLoader(libFolder, ServerInstance.class.getClassLoader());
			}
		}
	}
	
	private void setupFolders(File gameDir) throws SystemCoreException {
		deployFolder = new File(gameDir, DEPLOY_FOLDER_NAME);
		workFolder = new File(gameDir, WORK_FOLDER_NAME);
		libFolder = new File(gameDir, LIB_FOLDER_NAME);
		checkFolders();
	}
	
	private void checkFolders() throws SystemCoreException {
		// Check that deploy is valid
		if (!deployFolder.exists() || !deployFolder.isDirectory()) {
			throw new SystemCoreException("The deploy folder ["+deployFolder+"] does not exists.");
		}
		// Delete the folder and everything in it
		if (workFolder.exists()){
			Files.recursiveDelete(workFolder);
		}
		// Create new work folder
		Files.safeMkdir(workFolder);
		if (!workFolder.exists()) {
			throw new SystemCoreException("Could not create work folder: "+workFolder);
		}
		// Check lib folder
		if (!libFolder.exists()) {
			libFolder = null; // don't use
		}
	}
}
