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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import se.xec.commons.fileset.BaseFileSet;
import se.xec.commons.fileset.FileSetEvent;
import se.xec.commons.fileset.FileSetListener;
import se.xec.commons.path.Path;
import se.xec.commons.path.StringPath;
import se.xec.commons.resource.Resource;

import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.util.PathUtil;
import com.cubeia.firebase.util.Files;
import com.cubeia.util.IoUtil;
import com.cubeia.util.Lists;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This file set only pick up known deployment {@link DeploymentType types}. It scans
 * for changes every {@link Constants#DEPLOY_SCAN_REFRESH_PERIOD} milliseconds.
 * 
 * @author Larsan
 */
public class DeploymentFileSet extends BaseFileSet implements Startable, FileSetResource {

	private final File tempDir;
	private final List<FileSetListener> listeners;
	private ScheduledExecutorService exec;

	public DeploymentFileSet(File root, File tempDir) throws IOException {
		super(root);
		Arguments.notNull(tempDir, "tempDir");
		listeners = new CopyOnWriteArrayList<FileSetListener>();
		super.setIsRecursive(false);
		this.tempDir = tempDir;
		scan();
	}
	
	public Object getAttachment() {
		throw new UnsupportedOperationException();
	}
	
	public void setAttachment(Object o) {
		throw new UnsupportedOperationException();
	}
	
	public File getRoot() {
		return root;
	}

	public File getDeploymentFolder() {
		return root;
	}
	
	public int getDeploymentVersion() {
		return -1;
	}
	
	public InputStream getInputStream() {
		return null;
	}
	
	public Certificate[] getCertificates() {
		return null;
	}

	public String getContentType() {
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

    public Certificate[] getCertificates(Resource resource) {
        return null;
    }
	
    public synchronized void scan() throws IOException {
    	resources.clear();
    	manualScan(root);
    }

    /**
     * Get a versioned copy of the given resource. This returns a clone but with another underlying
     * source (ie another file or folder). If the "explode" argument is true and the source is a zip file
     * resource, it will be expanded and returned as a simple file set. 
     * 
     * @param rec
     * @param version
     * @param explode
     * @return
     */
	public FileSetResource getResourceCopy(DeploymentResource rec, int version, boolean explode) {
		if(!explode && !(rec instanceof DeploymentFolderSet)) {
			return cloneSingleResource(rec, version);
		} else if(rec instanceof DeploymentFolderSet) {
			return cloneFolderSet((DeploymentFolderSet)rec, version);
		} else {
			if(!(rec instanceof ZipFileResource)) throw new IllegalArgumentException("Unable to explode resource of type: " + rec.getClass().getName());
			else return cloneAndExplode((ZipFileResource)rec, version);
		}
	}

	public DeploymentResource[] getResourcesByType(DeploymentType type) {
		List<DeploymentResource> list = new LinkedList<DeploymentResource>();
		for (Path p : getResourcePaths()) {
			if(type.equals(DeploymentType.parse(p.getName()))) {
				DeploymentResource r = (DeploymentResource)getResource(p);
				if(r != null) {
					list.add(r);
				}
			}
		}
		return Lists.toArray(list, DeploymentResource.class);
	}

	public File getTemporaryDeploymentFolder() {
		return tempDir;
	}

	public void addFileSetListener(FileSetListener list) {
		listeners.add(list);
	}

	public void removeFileSetListener(FileSetListener list) {
		listeners.remove(list);
	}
	
	public void start() {
		exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleWithFixedDelay(new SafeRunnable() {
		
			@Override
			protected void innerRun() {
				checkScan();
			}
		}, Constants.DEPLOY_SCAN_REFRESH_PERIOD, Constants.DEPLOY_SCAN_REFRESH_PERIOD, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		exec.shutdownNow();
	}
	
	// --- PROTECTED METHODS --- //
	
	protected boolean matches(String filePath) {
		return DeploymentType.parse(filePath) != null;
	}
	
	
	// --- PACKAGE METHODS --- //
	
	Resource createFileResource(File file, Path path) {
		return createFileResource(super.root, file, path, null);
	}
	
    Resource createFileResource(File root, File file, Path path, DeploymentType knownType) {
		DeploymentType type = (knownType == null ? DeploymentType.parse(file) : knownType);
		if(file.isDirectory()) {
			try {
				return new DeploymentFolderSet(root, file, -1, this);
			} catch (IOException e) {
				Logger.getLogger(getClass()).error("Failed to scan deployment folder", e);
				return null;
			}
		} else if(type != null && type.isCompressed()) {
			try {
				return new ZipFileResource(file, type, true, -1, this);
			} catch (IOException e) {
				Logger.getLogger(getClass()).error("Failed to scan deployment folder", e);
				return null;
			}
		} else {
			return new XmlFileResource(root, path, file, type, -1, this);
		}
    }
	
	
	// --- PRIVATE METHODS --- //
    
    private boolean isUarDir(File file) {
		return (DeploymentType.parse(file) == DeploymentType.UNIFIED_ARCHIVE) && file.isDirectory();
	}
    
	private FileSetResource cloneFolderSet(DeploymentFolderSet rec, int version) {
		ZipFileResource org = rec.getOriginal();
		if(org != null) {
			return cloneAndExplode(org, version);
		} else {
			return cloneAndCopy(rec, version);
		}
	}
	
	private FileSetResource cloneAndCopy(DeploymentFolderSet rec, int version) {
		Path path = rec.getPath();
		String name = path.getName() + "_" + version;
		File file = new File(tempDir, name);
		if(file.exists()) throw new IllegalStateException("Version '" + version + "' already exists");
		File root = rec.getFile();
		try {
			file.mkdir();
			Files.copyDirectory(root, file, false);
			return new DeploymentFolderSet(file, rec.getOriginalDirectory(), version, this);
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to copy directory", e);
			return null;
		}
	}

	private FileSetResource cloneAndExplode(ZipFileResource rec, int version) {
		Path path = rec.getPath();
		String name = path.getName() + "_" + version;
		File file = new File(tempDir, name);
		if(file.exists()) throw new IllegalStateException("Version '" + version + "' already exists");
		File root = rec.getFile();
		try {
			Files.explode(new ZipFile(root), file);
			return new DeploymentFolderSet(file, rec, version, this);
		} catch (ZipException e) {
			Logger.getLogger(getClass()).error("Failed to unzip file", e);
			return null;
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to unzip file", e);
			return null;
		}
	}

	private FileSetResource cloneSingleResource(DeploymentResource rec, int version) {
		Path path = rec.getPath();
		String name = path.getName() + "_" + version;
		File file = new File(tempDir, name);
		if(file.exists()) throw new IllegalStateException("Version '" + version + "' already exists");
		OutputStream out = null;
		InputStream in = null;
		try {
			if(!file.createNewFile()) throw new IllegalStateException("Failed to create new temporary copy");
			out = new BufferedOutputStream(new FileOutputStream(file));
			in = rec.getInputStream();
			IoUtil.transfer(in, out);
			file.deleteOnExit();
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to create temporary file", e);
			return null;
		} finally {
			IoUtil.safeClose(out);
			IoUtil.safeClose(in);
		}
		return (FileSetResource)createFileResource(tempDir, file, new StringPath((StringPath)path.getParent(), name), rec.getDeploymentType());
	}
    
	/*
     * Perform re-scan to check for changes.
     */
    private void checkScan() {
        // don't waste time if there is no listeners
        if(listeners.size() > 0) {   
            List<FileSetEvent> events = new LinkedList<FileSetEvent>();
            int count = 0;
            try {
                scanImpl(root, events, count);
                checkRemoved(count, events);
                fireEvents(events);
            } catch(IOException e) {
                Logger.getLogger(getClass()).error("Failed to scan deployment folder", e);
            }
        }
    }
    
    /*
     * Do a complete scan for changes and place eventual change events
     * in the provided list
     */
     private void scanImpl(File folder, List<FileSetEvent> eventList, int count) throws IOException {
         File[] files = folder.listFiles();   
         for(int i = 0; (files != null && i < files.length); i++) {
        	 // FIXME: Enable resource changes for exploded UARs
             if(files[i].isFile()) {
                 String path = resolvePath(files[i]);
                 if(matches(path)) {
                 	 Path next = super.createResourcePath(path);
                 	 DeploymentResource rec = (DeploymentResource)getResource(next);
                     if(rec == null) {
                    	 eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_ADDED, this));
                     } else if(rec.getVisitedTime() < files[i].lastModified()) {
                    	 eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_CHANGED, this, rec));
                    	 rec.updateVisitedTime();
                     }
                     count++;
                 }
             }
         }
     }
     

	/*
      * One or more resources may have been removed. Check all available resources if they still ares
      * valid and note those that are not.
      */
     private synchronized void checkRemoved(int count, List<FileSetEvent> eventList) {
         if(count < resources.size()) {
             for(List<Resource> l : resources.values()) {
                 for (Resource r : l) {
	            	 DeploymentResource rec = (DeploymentResource)r;
	                 if(!rec.exists()) {
	                	 eventList.add(new FileSetEvent(FileSetEvent.RESOURCE_REMOVED, this, rec));
	                 }
                 }
             }
         }
     }
     
     /*
      * Fire a list of <i>FileSetEvent</i>s to the listeners. 
      */
     private void fireEvents(List<FileSetEvent> events) { 
         if(listeners.size() > 0) { 
        	 for (FileSetListener l : listeners) {
        		 for (FileSetEvent e : events) {
        			 l.receiveFileSetEvent(e);
        		 }
        	 }
         }
     }
     
      /*
       * Recursive scan method using a folder and a map to garther resources in.
       */
	 private void manualScan(File folder) throws IOException {
          File[] files = folder.listFiles();   
          for(int i = 0; (files != null && i < files.length); i++) {
              if(files[i].isFile() || isUarDir(files[i])) {
                  String path = resolvePath(files[i]);
                  if(matches(path)) {
                      path = path.replace(File.separatorChar, '/');
                      Path next = createResourcePath(path);
                      Resource res = createFileResource(files[i], next);
                      doPut(next, res);
                  }
              }
          }
      }
}
