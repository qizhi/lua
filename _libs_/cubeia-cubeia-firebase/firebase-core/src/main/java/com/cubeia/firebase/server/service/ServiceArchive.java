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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import se.xec.commons.fileset.JarFile;
import se.xec.commons.path.Path;
import se.xec.commons.resource.Resource;
import se.xec.commons.resource.ResourceLocator;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.instance.ServerInstance;
import com.game.server.bootstrap.ResourceExporter;
import com.game.server.bootstrap.ServerClassLoader;
import com.game.server.bootstrap.SharedClassLoader;

/**
 * This class represents an exploded service archive, located at a file
 * directory. It controls the class loaders and the service information.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
public class ServiceArchive {

	private static final int SLOW_ALLOCATION_DELAY = Integer.parseInt(System.getProperty("com.cubeia.firebase.server.deploy.forceAllocationDelay", "-1"));
	private static final boolean FORCE_ALLOCATION_GC = System.getProperty("com.cubeia.firebase.server.deploy.forceAllocationGC", "false").equals(true);

	private final Logger log = Logger.getLogger(getClass());
	
	private final File archive;
	private final ServiceArchiveLayout layout;
	private final InternalServiceInfo info;
	private final String name;
	private final SharedClassLoader space;
	private final ClassLoader parentLoader;
	
	private Locator locator;
	private ClassLoader loader;
	private final boolean isTrusted;
	private boolean isDeployed;
	
	/**
	 * @param name Service deployment name, must not be null
	 * @param dir Exploded service directory, must not be null
	 * @param layout Layout to use, must not be null
	 * @param parentLoader Parent class loader, must not be null
	 * @param space Shared space to use, must not be null
	 * @param isTrusted True if the service is trusted
	 */
	public ServiceArchive(String name, File dir, ServiceArchiveLayout layout, ClassLoader parentLoader, SharedClassLoader space, boolean isTrusted, boolean isDeployed) throws IOException, IllegalArchiveException {
		this.name = name;
		this.layout = layout;
		this.archive = dir;
		this.parentLoader = parentLoader;
		this.space = space;
		this.isTrusted = isTrusted;
		this.isDeployed = isDeployed;
		this.info = layout.getServiceInfo(this);
		initLocator();
		initLoader();
	}
	
	public boolean isDeployed() {
		return isDeployed;
	}
	
	public ServiceArchiveLayout getLayout() {
		return DefaultServiceArchiveLayout.getInstance();
	}
	
	@Override
	public String toString() {
		return "service://" + name;
	}
	
	public InternalServiceInfo getServiceInfo() {
		return info;
	}
	
	/*public ServiceClassLoader getServiceClassLoader() {
		return loader;
	}*/
	
	public boolean isTrusted() {
		return isTrusted;
	}

	public String getName() {
		return name;
	}

	public File getRootFolder() {
		return archive;
	}
	
	public Locator getResources() {
		return locator;
	}

	public void export(ResourceExporter exporter) {
		Arguments.notNull(exporter, "exporter");
		/*
		 * At this point, we should only export it (1) we're 
		 * not trusted (ie "isolated"); or (2) we're trusted but public.
		 */
		if(!isExporting()) return; // SANITY CHECK
		PackageSet set = getExportedPackages();
        for (Path tmp : locator.resources.keySet()) {
			String path = tmp.toString();
			// WARNING! 'path.endsWith' is a bodge to not export empty folders
			if(path.endsWith("/")) continue; // EARLY LOOP RETURN
			/*
			 * Trac #74, remove all file extensions before
			 * checking if the resource is implied.
			 */
			int lastDot = path.lastIndexOf('.');
            if(lastDot != -1) {
                path = path.substring(0, lastDot);
            }
            String dotted = path.replace('/', '.');
            if(set.impliesResource(dotted)) { 
            	Resource[] arr = locator.getResources(tmp);
            	log.debug("Service archive '" + name + "' exports " + arr.length + " implied resources: " + dotted);
            	for (Resource o : arr) {
                	exporter.export(o);
                }
            }
        }
	}
	
	public boolean isPublic() {
		return isExporting();
	}

	public String getPublicId() {
		return getServiceInfo().getPublicId();
	}

	public PackageSet getExportedPackages() {
		return info.getExportedPackages();
	}
	
	public ClassLoader getServiceClassLoader() {
		return loader;
	}
	
	
	// --- PACKAGE METHODS --- //
	
	Class<?>[] instantiateContracts()  throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String[] classes = info.getContractClasses();
		Class<?>[] arr = new Class<?>[classes.length];
		for(int i = 0; i < classes.length; i++) {
			arr[i] = loader.loadClass(classes[i]);
		}
		return arr;
	}
	
	Object instantiateService() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			return loader.loadClass(info.getServiceClass()).newInstance();
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	ClassLoader getParentClassLoader() {
		return parentLoader;
	}

	SharedClassLoader getSharedSpace() {
		return space;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	// Only export if isolated, or public
	private boolean isExporting() {
		return !isTrusted || info.isPublic();
	}
	
	private void initLocator() throws IllegalArchiveException, IOException {
		JarFile[] files = toJarFiles(layout.getLibraryFiles(this));
		locator = new Locator(files);
	}
	
	private void initLoader() throws IOException, IllegalArchiveException {
		// TODO: Add support for flat class loaders to avoid hacks like this...
		if(Constants.IN_ECLIPSE) {
			log.trace("Service archive '" + name + "' create non-exporting class loader [inEclipse=true]");
			loader = new ServiceClassLoader(this, ServerInstance.class.getClassLoader(), false);
		} else if(!isTrusted) {
			log.trace("Service archive '" + name + "' create exporting class loader [isTrusted=false]");
			loader = new ServiceClassLoader(this, getParentClassLoader(), true);
		} else  {
			log.trace("Service archive '" + name + "' pushes JAR files to parent class loader [isExporting=" + isExporting() + "]");
			ServerClassLoader sl = (ServerClassLoader)getParentClassLoader();
			File[] files = layout.getLibraryFiles(this);
			for (File file : files) {
				sl.addURL(file.toURI().toURL());
			}
			loader = sl;
		} 
		
		/*if("ns://www.cubeia.com/firebase/space/tc-space/service".equals(info.getPublicId())) {
			ClassProcessorHelper.registerGlobalLoader(new CL());
		}*/
	}
	
    /*public class CL extends URLClassLoader implements NamedClassLoader {
    	
    	private String name = "tcServerLoader";
    	
    	public CL() {
    		super(new URL[0], loader);
    	}
    	
    	public String __tc_getClassLoaderName() {
    		return name;
    	}
    	
    	public void __tc_setClassLoaderName(String name) {
    		this.name = name;
    	}
    }*/

	private JarFile[] toJarFiles(File[] libs) throws IOException {
		JarFile[] arr = new JarFile[libs.length];
		boolean doScan = !Constants.IN_ECLIPSE && isExporting();
		log.trace("Service archive '" + name + "' loads JAR files [scanning=" + doScan + "]");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new JarFile(libs[i], null, false);
			if(doScan) {
				// Ticket #473: If system property is set, force a delay
				if(SLOW_ALLOCATION_DELAY > 0) {
				    try {
				        Thread.sleep(SLOW_ALLOCATION_DELAY); 
				    } catch(InterruptedException e) { }
				}
				// Ticket #473: If system property is set, force gc
				if(FORCE_ALLOCATION_GC) {
				    System.gc();
				}
				arr[i].scan();
			}
		}
		return arr;
	}
	
	
	// --- INNER CLASSES --- //

	public class Locator implements ResourceLocator, Iterable<Resource> {
		
		private Map<Path, List<Resource>> resources = new HashMap<Path, List<Resource>>();
		
		private Locator(JarFile[] files) {
			for (JarFile file : files) {
				addResources(file);
			}
		}
		
		public Iterator<Resource> iterator() {
			List<Resource> l = new ArrayList<Resource>(resources.size());
			for (List<Resource> inner : resources.values()) {
				l.addAll(inner);
			}
			return l.iterator();
		}

		public Resource getResource(Path path) {
			List<Resource> l = resources.get(path);
			return (l == null || l.size() == 0 ? null : l.get(0));
		}
		
		public Resource[] getResources(Path path) {
			List<Resource> l = resources.get(path);
			if(l == null || l.size() == 0) {
				return new Resource[0];
			} else {
				return l.toArray(new Resource[l.size()]);
			}
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private void addResources(JarFile file) {
			Path[] paths = file.getResourcePaths();
			for (Path path : paths) {
				doPut(path, file.getResource(path));
			}
		}

		private void doPut(Path path, Resource resource) {
			List<Resource> l = resources.get(path);
			if(l == null) {
				l = new LinkedList<Resource>();
				resources.put(path, l);
			}
			l.add(resource);
		}
	}
}
