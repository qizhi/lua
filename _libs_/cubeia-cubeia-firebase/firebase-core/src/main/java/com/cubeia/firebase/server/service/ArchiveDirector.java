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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import se.xec.commons.path.Path;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.ResourceManager;
import com.cubeia.firebase.server.deployment.resources.DeploymentFileSet;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.deployment.resources.FileSetResource;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveClassLoader;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.instance.ServerInstance;
import com.cubeia.firebase.util.Files;
import com.cubeia.util.IoUtil;
import com.game.server.bootstrap.SharedClassLoader;

public class ArchiveDirector {
	
	private static final String WORK_PREFIX = "_services";
	
	private static final int DEPLOYED = 1;
	private static final int ISOLATED = 2;
	private static final int TRUSTED = 3;

	private static final FileFilter SAR_FILTER = new FileFilter() {
	
		public boolean accept(File file) {
			return file.isFile() && file.getName().toLowerCase().endsWith(".sar");
		}
	};

	private final Logger log = Logger.getLogger(getClass());
	
	private final File workDir;
	private final List<ServiceArchive> services;
	private final SharedClassLoader space;
	
	private ClassLoader serverLoader;

	private final ClassLoader deploymentLoader;

	public ArchiveDirector(File workDir, SharedClassLoader space, ClassLoader deploymentLoader) throws IOException {
		Arguments.notNull(workDir, "work directory");
		Arguments.notNull(deploymentLoader, "deploymentLoader");
		Arguments.notNull(space, "space");
		this.deploymentLoader = deploymentLoader;
		services = new LinkedList<ServiceArchive>();
		this.workDir = checkWorkDirectory(workDir);
		this.space = space;
		setupClassLoader();
	}

	public void init(SystemLocations locations) throws IOException, IllegalArchiveException {
		Arguments.notNull(locations, "locations");
		Sar[] files = findSarFiles(locations, InternalComponentAccess.getDeploymentResources());
		if(log.isDebugEnabled()) {
			for (Sar s : files) {
				log.trace("Found Service Archive file: " + s.file.getAbsolutePath());
			}
		}
		explodeArchives(files);
	}

	public void destroy() {
		if(Constants.FORCE_SAR_REDEPLOY) {
			Files.recursiveDelete(workDir);
		}
		services.clear();
	}
	
	public ServiceArchive[] getServices() {
		return services.toArray(new ServiceArchive[services.size()]);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupClassLoader() {
		/*
		 * We know that the bootstrap server is loaded by the server loader,
		 * we also know, from the bootstrap server, that its parent is the
		 * system class loader. /LJN
		 */
		serverLoader = ServerInstance.class.getClassLoader();
	}
	
	private void explodeArchives(Sar[] files) throws IOException, IllegalArchiveException {
		int counter = 0;
		ServiceArchiveLayout lay = DefaultServiceArchiveLayout.getInstance();
		if(doExplode(files)) {
			log.debug("Archive director forces SAR exploding [workDir=" + workDir +"]");
			Files.recursiveDelete(workDir); // clean old
			Files.safeMkdir(workDir);
			
			// Check if we have a directory now and throw an exception if not 
			if(!workDir.exists()) throw new IOException("Failed to create temporary work directory for services '" + workDir + "'");
			
			for (int i = 0; i < files.length; i++) {
				Sar sar = files[i];
				File archive = sar.file;
				String id = newArchiveId(archive.getName(), counter++);
				File file = new File(workDir, id);
				file.mkdir();
				Files.explode(new ZipFile(archive), file);
				String name = archive.getName();
				if(name.toLowerCase().endsWith(".sar")) {
					name = name.substring(0, name.length() - 4);
				}
				ServiceArchive a = null;
				if (sar.type == TRUSTED) {
					a = newTrustedArchive(lay, file, name);
				} else if(sar.type == ISOLATED) {
					a = newIsolatedArchive(lay, file, name);
				} else {
					a = newDeployedArchive(lay, file, name, sar.parentLoader);
				}
				log.trace("Created archive for service '" + a.getPublicId() + " from file: " + archive.getAbsolutePath());
				services.add(a);
			}
		} else {
			counter = files.length;
			log.debug("Archive director attempts SAR non-exploding [workDir=" + workDir +"]");
			for (int i = 0; i < files.length; i++) {
				Sar sar = files[i];
				File archive = sar.file;
				File existing = existsTmpDir(archive.getName());
				ClassLoader parent = sar.parentLoader;
				if(existing == null) {
					/*
					 * Oops, should get here, re-do...
					 */
					Constants.FORCE_SAR_REDEPLOY = true;
					explodeArchives(files);
					return; 
				} else {
					String name = archive.getName();
					if(name.toLowerCase().endsWith(".sar")) {
						name = name.substring(0, name.length() - 4);
					}
					if (sar.type == TRUSTED) {
						services.add(newTrustedArchive(lay, existing, name));
					} else if(sar.type == ISOLATED) {
						services.add(newIsolatedArchive(lay, existing, name));
					} else {
						services.add(newDeployedArchive(lay, existing, name, parent));
					}
				}
			}
		}
	}

	/*
	 * Return true if Constants.FORCE_SAR_REDEPLOY = true, or if any of the sars does
	 * not have a directory or any of the directories appears to be out of date
	 */
	private boolean doExplode(Sar[] files) {
		if(Constants.FORCE_SAR_REDEPLOY) return true;
		else {
			for (Sar sar : files) {
				File archive = sar.file;
				File old = existsTmpDir(archive.getName());
				if(old == null) return true;
				if(appearsOutdated(old, sar)) return true;
			}
			// we have all servs and they seem ok
			return false;
		}
	}

	// return true, if the existing dir seems to be older than the ser file
	private boolean appearsOutdated(File existing, Sar sar) {
		ZipInputStream in = null;
		try {
			in = new ZipInputStream(new FileInputStream(sar.file));
			return Files.needsUpdating(in, existing);
		} catch(IOException e) {
			// Ignore...
			return true;
		} finally {
			IoUtil.safeClose(in);
		}
	}

	// return the dir if there's a subdir starting with the name.hashCode + "_"
	private File existsTmpDir(String name) {
		String check = name.hashCode() + "_";
		for (File file : workDir.listFiles()) {
			if(file.getName().startsWith(check)) return file; 
		}
		return null;
	}

	private ServiceArchive newIsolatedArchive(ServiceArchiveLayout lay, File file, String name) throws IOException, IllegalArchiveException {
		return new ServiceArchive(name, file, lay, space, space, false, false);
	}
	
	private ServiceArchive newDeployedArchive(ServiceArchiveLayout lay, File file, String name, ClassLoader parent) throws IOException, IllegalArchiveException {
		return new ServiceArchive(name, file, lay, parent, space, false, true);
	}

	private ServiceArchive newTrustedArchive(ServiceArchiveLayout lay, File file, String name) throws IOException, IllegalArchiveException {
		return new ServiceArchive(name, file, lay, serverLoader, space, true, false);
	}
	
	private String newArchiveId(String name, int i) {
		return name.hashCode() + "_" + i;
	}

	private Sar[] findSarFiles(SystemLocations loc, ResourceManager resources) {
		List<Sar> files = new LinkedList<Sar>();
		doAddLocations(files, loc.trustedLocation(), TRUSTED, null);
		doAddLocations(files, loc.isolatedLocations(), ISOLATED, null);
		doAddLocations(files, loc.deployedLocations(), DEPLOYED, deploymentLoader);
		checkAddUarLocations(files, resources);
		return files.toArray(new Sar[files.size()]);
	}
		
	private void checkAddUarLocations(List<Sar> files, ResourceManager manager) {
		DeploymentFileSet deps = manager.getDeploymentFileSet();
		DeploymentResource[] recs = deps.getResourcesByType(DeploymentType.UNIFIED_ARCHIVE);
		doAddUarLocations(files, recs);
	}

	private void doAddUarLocations(List<Sar> files, DeploymentResource[] recs) {
		for (DeploymentResource rec : recs) {
			doAddUarLocation(files, rec);
		}
	}

	private void doAddUarLocation(List<Sar> files, DeploymentResource rec) {
		FileSetResource clone = rec.clone(0, true);
		UnifiedArchiveClassLoader classes = checkGetClassLoader(clone);
		if(classes != null) {
			clone.setAttachment(classes);
			for (Path p : clone.getResourcePaths()) {
				if(DeploymentType.SERVICE_ARHIVE.matches(p.getName())) {
					File file = new File(clone.getRoot(), p.getName());
					files.add(new Sar(file, DEPLOYED, classes));
				}
			}
		}
	}

	private UnifiedArchiveClassLoader checkGetClassLoader(FileSetResource clone) {
		if(!haveSars(clone)) {
			return null;
		} else {
			return new UnifiedArchiveClassLoader(deploymentLoader, clone);
		}
	}

	private boolean haveSars(FileSetResource clone) {
		for (Path p : clone.getResourcePaths()) {
			if(DeploymentType.SERVICE_ARHIVE.matches(p.getName())) {
				return true;
			}
		}
		return false;
	}

	private void doAddLocations(List<Sar> files, File[] locations, int type, ClassLoader parent) {
		for (File file : locations) {
			File[] next = file.listFiles(SAR_FILTER);
			if (next != null) {
				for (File sar : next) {
					files.add(new Sar(sar, type, parent));
				}
			}
		}
	}

	private File checkWorkDirectory(File workDir) throws IOException {
		File file = null;
		try {
			file = new File(workDir, WORK_PREFIX);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// if(file.exists() && Constants.FORCE_SAR_REDEPLOY) Files.recursiveDelete(file);
		if(!file.exists() && !file.mkdirs()) throw new IOException("Failed to create temporary work directory for services from check '" + file + "'");
		return file;
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private final class Sar {
		
		private File file;
		private int type;
		
		private final ClassLoader parentLoader;
		
		private Sar(File file, int type, ClassLoader parentLoader) {
			this.parentLoader = parentLoader;
			this.file = file;
			this.type = type;
		}
	}
}
