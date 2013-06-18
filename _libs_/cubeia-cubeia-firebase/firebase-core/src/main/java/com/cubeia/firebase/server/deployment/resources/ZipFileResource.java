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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import se.xec.commons.fileset.BaseResource;
import se.xec.commons.fileset.FileSetErrorHandler;
import se.xec.commons.fileset.TrivialFileSet;
import se.xec.commons.path.Path;
import se.xec.commons.resource.Resource;
import se.xec.commons.util.MimeTypes;

import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.util.PathUtil;
import com.cubeia.util.IoUtil;

public class ZipFileResource extends TrivialFileSet implements DeploymentResource, FileSetResource {

    private final DeploymentType type;
	private final int version;
	
	private long lastVisited;
	private final DeploymentFileSet parent;

	private final AtomicReference<FileSetResource> lastClone = new AtomicReference<FileSetResource>();
	private final AtomicReference<Object> attachment = new AtomicReference<Object>();
	
    ZipFileResource(File file, DeploymentType type, boolean doInitialScan, int version, DeploymentFileSet parent) throws IOException {
        super(file, null, doInitialScan);
		this.parent = parent;
        lastVisited = System.currentTimeMillis();
		this.type = type;
		this.version = version;
    }
    
	public Object getAttachment() {
		return attachment.get();
	}
	
	public void setAttachment(Object o) {
		attachment.set(o);
	}
    
    public void updateVisitedTime() {
    	lastVisited = System.currentTimeMillis();
    	if(lastVisited < file.lastModified()) {
			Logger.getLogger(getClass()).warn("Clock mismatch, system clock appears to lag behind the file system, or the file is set to future: " + file.getAbsolutePath());
			lastVisited = file.lastModified();
		}
    }
    
	public FileSetResource clone(int version, boolean exploded) {
		FileSetResource clone = parent.getResourceCopy(this, version, exploded);
		lastClone.set(clone);
		return clone;
	}
	
	public FileSetResource getLatestClone() {
		return lastClone.get();
	}
    
    public File getExplodedBase() {
    	return null;
    }
    
    public boolean isExploded() {
    	return false;
    }
    
    public File getRoot() {
    	return file;
    }
    
    public File getFile() {
    	return file;
    }
    
    public String getName() {
    	return file.getName();
    }
    
    public long getVisitedTime() {
    	return lastVisited;
    }
    
    public boolean exists() {
    	return file.exists();
    }
    
    public int getDeploymentVersion() {
    	return version;
    }
    
    public DeploymentType getDeploymentType() {
    	return type;
    }
    
    public long getLastModificationTime() {
    	return file.lastModified();
    }
    
    public Certificate[] getCertificates() {
    	return null;
    }
    
    public String getContentType() {
    	return type.getContentType();
    }
    
    public InputStream getInputStream() {
        if(file.exists()) {
            try {
                return new FileInputStream(file);
            } catch(IOException e) { 
                Logger.getLogger(getClass()).error("Failed to open zip file stream", e);
                return null;
            }
        } else return null;
    }
    
    @SuppressWarnings("deprecation")
	public URL getURL() {
        try {
            return (file.exists() ? file.toURL() : null);
        } catch(MalformedURLException e) {
            throw new RuntimeException("malformed file URL: " + e.getMessage());
        }
    }
    
    public Path getPath() {
    	return PathUtil.createAbsoluteFilePath(file);
    }
    
    
    /// --- PROTECTED HELPER METHODS --- ///
    
    /**
     * Perform scan. This method should populate the resource map
     * with new resources.
     */
    @SuppressWarnings({ "rawtypes" })
	protected void doScan() throws IOException {
        resources.clear();
        ZipFile zipFile = new ZipFile(file);
        try {
            for(Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry)e.nextElement();
                String name = entry.getName(); //.replace('/', File.separatorChar);
                Path next = super.createResourcePath(name);
                Resource res = new ZipResource(file, zipFile, next, System.currentTimeMillis(), super.getErrorHandler());
                doPut(next, res);
            }
        } finally {
            zipFile.close();   
        }
    }
    
    
    /// --- INNER CLASSES --- //
    
    public final class ZipResource extends BaseResource {
        
        private final File file;
		private final ZipFile zipFile;
        
        /*
         * Zip file path, this file path may differ from the super class file path
         * since all jar archives will store their entries using a "/" separator.
         */
        private final String zipPath;


        private ZipResource(File file, ZipFile zipFile, Path filePath, long visited, FileSetErrorHandler handler) {
            super(filePath, visited, handler);
			this.zipFile = zipFile;
            this.zipPath = filePath.toString();
            this.file = file;
        } 
        
        
        /// --- PUBLIC ACCESSORS --- ///
        
        public String getContentType() {
        	int mark = zipPath.lastIndexOf('/');
        	String tmp = (mark == -1 ? zipPath : zipPath.substring(mark + 1));
        	mark = tmp.lastIndexOf(".");
        	tmp = (mark == -1 ? tmp : tmp.substring(mark + 1));
    		return MimeTypes.getExtensionType(tmp);
    	}
        
        
        /**
         * @return A file input stream from the resource, or null if disabled
         * @throws IOException On IO errors
         */
        public InputStream getInputStream() {
            ZipEntry entr = getEntry();
            if(entr == null || entr.isDirectory()) return null;
            else {
                try {
                    return zipFile.getInputStream(entr);
                } catch(IOException e) { 
                    return (InputStream)super.report(e);
                }
            }
        }
        
        
        /**
         * @return A byte array from the resource, or null if disabled
         * @throws IOException On IO errors
         */
        public byte[] getBytes() {
            ZipEntry entr = getEntry();
            if(entr == null || entr.isDirectory()) return null;
            else {
                try {
                    return IoUtil.toByteArray(zipFile.getInputStream(entr));
                } catch(IOException e) { 
                    return (byte[])super.report(e);
                }
            }
        }
        
        
        /**
         * @return The resource URL, or null if disabled
         */
        @SuppressWarnings("deprecation")
		public URL getURL() {
             try {
            	 StringBuffer buff = new StringBuffer("jar:");
                 buff.append(file.toURL().toString()).append("!/");
                 buff.append(zipPath);
                 return new URL(buff.toString());
             } catch(MalformedURLException e) {
            	 throw new RuntimeException("malformed jar URL: " + e.getMessage());
             }   
        }
        
        public boolean exists() {
            ZipEntry entr = getEntry();
            return (entr != null && !entr.isDirectory());
        }
        
        public Certificate[] getCertificates() {
        	return null;
        }

        
        
        // --- PRIVATE METHODS --- //

        private ZipEntry getEntry() {
            if(file.exists()) return zipFile.getEntry(zipPath);
            else return null;
        }
    }
}
