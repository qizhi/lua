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
package com.game.server.bootstrap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import se.xec.commons.path.Path;
import se.xec.commons.resource.Resource;
import se.xec.commons.resource.ResourceLocator;

public class SharedClassLoader extends CubeiaResourceClassLoader implements SharedClasses {

    private ResourceExporter exporter;
    private Resources resources;
    
    public SharedClassLoader(ClassLoader parent) {
        super(parent, null);
        resources = new Resources();
        super.locator = resources;
        exporter = new Export();
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        /*SecurityManager man = System.getSecurityManager();
        if(man != null) {
            int mark = name.lastIndexOf('.');
            if(mark != -1) man.checkPermission(new PackagePermission("load", name.substring(0, mark)));
        }*/
        return super.loadClass(name);
    }

    public ClassLoader getLoader() { 
        return this;
    }

    public ResourceExporter getExporter() { 
        return exporter;
    }
    
    
    
    /// --- INNER CLASSES --- ///
	
    private class Export implements ResourceExporter {
    
        /**
        * Export resource. Failed exports will be silently swallowed
        * and logged.
        *
        * @param resource Resource to export
        */
        
        public void export(Resource resource) { 
            SharedClassLoader.this.resources.addResource(resource);
        }
    }
    
    private class Resources extends HashMap<Path, List<Resource>> implements ResourceLocator {
    
		private static final long serialVersionUID = 3978985479184397360L;

        public synchronized Resource getResource(Path path) {
            List<Resource> l = get(path);
            return (l == null || l.size() == 0 ? null : l.get(0));
        }
        
        @Override
        public synchronized Resource[] getResources(Path path) {
        	List<Resource> l = get(path);
        	if(l == null || l.size() == 0) {
        		return new Resource[0];
        	} else {
        		return l.toArray(new Resource[l.size()]);
        	}
        }
        
        
        /// --- PRIVATE HELPER METHODS --- ///
        
        /**
        * Synchronized set method, return true if the set succeeded
        */
        
        private synchronized void addResource(Resource resource) {
        	List<Resource> l = get(resource.getPath());
        	if(l == null) {
        		l = new LinkedList<Resource>();
        		put(resource.getPath(), l);
        	}
        	l.add(resource);
        }
    }
}