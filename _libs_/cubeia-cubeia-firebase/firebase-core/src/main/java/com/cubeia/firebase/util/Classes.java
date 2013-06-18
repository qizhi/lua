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
package com.cubeia.firebase.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

public class Classes {

	private static final Logger log = Logger.getLogger(Classes.class);

	private Classes() { }
	
	
	/**
	 * This method calls the given invocation facade, but switches
	 * the thread context class loader before doing so to the argument class loader. It
	 * also switches back the the previous class loader when execution is finished.
	 * 
	 * @param target Facade to invike method on, must not be null
	 * @param args Method arguments, may be null
	 * @param loader Class loader to switch to, may be null
	 * @return The facade contract return value
	 */
	public static <T extends Throwable> Object switchContextClassLoaderForInvocation(InvocationFacade<T> facade, ClassLoader loader) throws T {
		Thread th = Thread.currentThread();
		ClassLoader old = th.getContextClassLoader();
		try {
			th.setContextClassLoader(loader);
			return facade.invoke();
		} finally {
			th.setContextClassLoader(old);
		}
	}
	
	/**
	 * This is a somewhat hacky method that tries to verify that the 
	 * bootstrap server class loader is set as the thread context class 
	 * loader. This method should be called in select places in the 
	 * server code to check that calls from game code is reset before 
	 * executing on the platform. See Trac issue #20 and #664.
	 */
	public static void verifyServerClassLoaderInContext() {
		/*
		 * This is a hack, but we want to warn if there's anything else than the 
		 * main server class loader in the context at this point
		 */
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
    	if(!cl.getClass().getName().contains("bootstrap.ServerClassLoader")) {
    		log.warn("Access with class a non-standard class loader: " + cl, new IllegalStateException());
    	}
	} 
	
	
	/**
	 * @param pckgname Package to find classes for, must not be null
	 * @param cld Class loader to use, if null, the utility object class loader will be used
	 * @return All classes found in the package, never null
	 * @throws ClassNotFoundException
	 */
	/*
	 * Nicked from some old code by Fred /LJN
	 */
	@SuppressWarnings({ "deprecation" })
	public static Class<?>[] getClasses(String pckgname, ClassLoader cld) throws ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        // Get a File object for the package
        File directory = null;
        
        try {
            //ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if(cld == null) {
            	cld = Classes.class.getClassLoader();
            }
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            
            String path = pckgname.replace('.', '/');
            Enumeration<URL> resources = cld.getResources(path);
            if (resources == null) {
                throw new ClassNotFoundException("No resource for " + path);
            }
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                // HACK!! /LJN
                String file = resource.getPath();
                file = URLDecoder.decode(file);
                if(file.startsWith("file:")) directory = new File(new URL(file).toURI());
                else directory = new File(file);
                
            }
        } catch (NullPointerException x) {
        	if(log.isDebugEnabled()) log.debug(x);
            throw new ClassNotFoundException(pckgname + " (" + directory
                    + ") does not appear to be a valid package");
            
        } catch (IOException x) {
        	if(log.isDebugEnabled()) log.debug(x);
        	throw new ClassNotFoundException(pckgname + " (" + directory
                    + ") does not appear to be a valid package");
        } catch (URISyntaxException x) {
        	if(log.isDebugEnabled()) log.debug(x);
        	throw new ClassNotFoundException(pckgname + " (" + directory
                    + ") does not appear to be a valid package");
		}
        
        // if(log.isDebugEnabled()) log.debug("searching file object directory: " + directory.getAbsolutePath());
        
        if(directory == null) throw new ClassNotFoundException("no directory could be found for package name: " + pckgname);
        
        if (directory.getAbsolutePath().indexOf(".jar!") != -1) {
        	List<String> files = listJarClasses(pckgname, directory.toString());
        	for(String s : files) {
        		if(s.endsWith(".class")) {
        			s = s.substring(0, s.length() - 6);
        			classes.add(Class.forName(pckgname + '.' + s));
        		}
        	}
        } else if (directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // removes the .class extension
                    classes.add(Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6)));
                }
            }
        } else {
            throw new ClassNotFoundException(pckgname+" does not appear to be a valid package");
        }
        
        Class<?>[] classesA = new Class[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }

	private static List<String> listJarClasses(String pckgname, String fullpath) throws ClassNotFoundException {
		int i = fullpath.indexOf(".jar!");
		String file = fullpath.substring(0, i + 4);
		String path = fullpath.substring(i + 5).replace(File.separatorChar, '/');
		if(!path.endsWith("/")) path += "/";
		if(path.startsWith("/")) path = path.substring(1);
		List<String> list = new LinkedList<String>();
		try {
			JarFile jarFile = new JarFile(file);
			for(Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
				JarEntry entry = e.nextElement();
				String name = entry.getName();
				if(name.startsWith(path) && !entry.isDirectory()) {
					int tmp = name.lastIndexOf("/");
					if(tmp > path.length()) continue; // SUBDIRECTORY
					else {
						name = name.substring(tmp + 1);
						list.add(name);
					}
				}
			}
			return list;
		} catch (IOException e) {
			throw new ClassNotFoundException("could not read classes", e);
		}
	}
}
