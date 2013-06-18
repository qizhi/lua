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
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import se.xec.commons.path.Path;
import se.xec.commons.resource.Resource;
import se.xec.commons.resource.ResourceLocator;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveClassLoader;
import com.game.server.bootstrap.CubeiaResourceClassLoader;
import com.game.server.bootstrap.SharedClasses;

public class ServiceClassLoader extends CubeiaResourceClassLoader {

    private SharedClasses sharedSpace;
    private PackageSet sharedSet;
	private ServiceArchive archive;
    
    private final Logger log = Logger.getLogger(getClass());

    public ServiceClassLoader(ServiceArchive archive, ClassLoader parent, boolean doExport) throws IOException {
        super(parent, new NullLocator());
		Arguments.notNull(archive, "archive");
		this.archive = archive;
		setupClassLocation(archive, parent);
		setupSharedSpace(archive, doExport);
        setupSharedSet(archive);
    }
    
	
    /**
    * Load class from the frameworks. This method delegates to the shared class
    * space if the class is exported/imported be the current service.
    *
    * @param name Fully qualified class name, must not be null
    */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(!sharedSet.impliesResource(name)) return super.loadClass(name);
        else return sharedSpace.getLoader().loadClass(name);
    }
    
    
    /// --- PROTECTED INTERFACE --- ///
    
    /**
    * Find resource from the frameworks. This method delegates to the shared class
    * space if the class is exported/imported be the current service.
    *
    * @param name Resource name, must not be null
    * @return The resource URL, or null if not found
    */ 
    protected URL findResource(String name) {
        if(!sharedSet.impliesResource(name)) return super.findResource(name);
        else return sharedSpace.getLoader().getResource(name);
    }
	
    /**
     * Find resources from the frameworks. This method delegates to the shared class
     * space if the class is exported/imported be the current service.
     *
     * @param name Resource name, must not be null
     * @return The resource URL enumeration, never null
     */ 
    protected Enumeration<URL> findResources(String name) {
    	if(!sharedSet.impliesResource(name)) return super.findResources(name);
		else {
			try {
				return sharedSpace.getLoader().getResources(name);
			} catch (IOException e) {
				log.error("failed to read resource from shared space", e);
				return null;
			}
		}
    }
    
    /**
    * Get the permissions for this archive. This will take the superclass permissions
    * and add package "load" permissions for all imported and exported packages. It will
    * alse add a thread scope permission for the class.
    *
    * @param src Code source, ignored in his loader
    */
    
    protected PermissionCollection getPermissions(CodeSource src) {
        /*PermissionCollection col = super.getPermissions(src);
		File archiveFile = archive.getArchive().getFile();
        col.add(new FilePermission(archiveFile.getPath(), "read"));
		col.add(new PackagePermission("load", "*"));
        col.add(new PropertyPermission("se.xec.norna.*", "read"));
        return col;*/
    	return super.getPermissions(src);
    }
    
    
    /// --- PRIVATE HELPER METHODS --- ///
    
    /*
    * Given a class name, calculate the package name
    */
    
    /*private String parsePackage(String cl) {
        if(cl.indexOf('.') != -1) return cl.substring(0, cl.lastIndexOf('.'));
        else return "";
    }*/
    
	private void setupSharedSet(ServiceArchive archive) {
		this.sharedSet = new PackageCombiner(archive);
	}

	private void setupSharedSpace(ServiceArchive archive, boolean doExport) {
		this.sharedSpace = archive.getSharedSpace();
        if(doExport) {
        	archive.export(this.sharedSpace.getExporter());
        }
	}

	@SuppressWarnings("deprecation")
	private void setupClassLocation(ServiceArchive archive, ClassLoader parent) throws IOException {
		if(parent instanceof UnifiedArchiveClassLoader) {
			/*
			 * This is a service contained within a unified archive, so
			 * push all jars and classes to the parent class loader.
			 */
			UnifiedArchiveClassLoader ua = (UnifiedArchiveClassLoader)parent;
			ServiceArchiveLayout layout = archive.getLayout();
			try {
				File[] files = layout.getLibraryFiles(archive);
				for (File file : files) {
					ua.addURL(file.toURL());
				}
			} catch (IllegalArchiveException e) {
				/*
				 * Shouldn't get here...
				 */
				Logger.getLogger(getClass()).error("Failed to read archive", e);
			}
			
		} else {
			super.locator = archive.getResources();
		}
	}
    
	@Override
	public String toString() {
		return getClass().getName() + "#" + archive.getPublicId();
	}
	
    
    /// --- INNNER CLASSES --- ///
    
    /*
    * A package combiner is simply the combined set of to package
    * sets, this is to avoid double searching for classes that should be
    * loaded from the shared space.
    */
    private static class PackageCombiner extends PackageSetImpl {

        public PackageCombiner(ServiceArchive archive) {
        	super(archive.getName());
            addAll(archive.getExportedPackages());
            //addAll(archive.getImportedPackages());
        }
        
        private void addAll(PackageSet set) {
            if(!(set instanceof PackageSetImpl)) {
            	for(Iterator<String> it = set.iterator(); it.hasNext(); ) {
            		String str = (String)it.next();
            		super.addResource(str);
            	}
            } else {
            	Set<Path> my = super.getPathObjectSet();
            	Set<Path> his = ((PackageSetImpl)set).getPathObjectSet();
            	my.addAll(his);
            }
        }
    }
    
    /**
     * A null locator which does no contain any resources.
     */
    private static class NullLocator implements ResourceLocator {
    	
    	public Resource getResource(Path arg0) {
    		return null;
    	}
    	
    	public Resource[] getResources(Path path) {
    		return new Resource[0];
    	}
    }
}
