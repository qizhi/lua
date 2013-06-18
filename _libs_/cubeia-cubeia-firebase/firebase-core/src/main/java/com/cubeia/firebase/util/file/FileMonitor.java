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
package com.cubeia.firebase.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Monitor a file.
 * 
 * So, why do I implement it as a singleton?
 * Mainly to keep threads down. I don't want to start a new thread
 * for each and every file we want to monitor.
 * 
 * Arguably, we could make this class a non-singleton and then
 * stick some instances into a registry (JNDI) if we want to 
 * split up responsibility.
 * 
 * If You deem this important, then feel free to implement it.
 * 
 * @author Fredrik
 *
 */
public class FileMonitor {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** 
	 * One Instance to rule them all, 
	 * One Instance to find them,
     * One Instance to bring them all and in the darkness bind them
     */
    private static final FileMonitor instance = new FileMonitor();
    
    /** The time will keep track of time **/
    private Timer timer;
    
    /** We want to monitor multiple files **/
    private Hashtable<String,FileMonitorTask> timerEntries;

    /**
     * Get the singleton instance.
     * If you have a problem with this being a singleton,
     * please read the class header documentation.
     * 
     * @return FileMonitor singleton
     */
    public static FileMonitor getInstance() {
        return instance;
    }

    /**
     * Create a FileMonitor.
     * 
     * The Time thread will run as daemon. (Evaulate this perhaps)
     */
    protected FileMonitor() { 
    	timer = new Timer("FileMonitor", true);
    	timerEntries = new Hashtable<String,FileMonitorTask>();
    }
    
    /** 
     * Add a monitored file with a FileChangeListener.
     * 
     * @param listener listener to notify when the file changed.
     * @param fileName name of the file to monitor.
     * @param period polling period in milliseconds.
     */
    public void addFileChangeListener(FileChangeListener listener, String fileName, long period) throws FileNotFoundException {
    	removeFileChangeListener(listener, fileName);
    	FileMonitorTask task = new FileMonitorTask(listener, fileName);
    	// Do we allow multiple listeners on a single file? Yes we do!
    	timerEntries.put(fileName + listener.hashCode(), task);
    	timer.schedule(task, period, period);
    }

    /** 
     * Remove the listener from the notification list.
     * 
     * @param listener the listener to be removed.
     */
    public void removeFileChangeListener(FileChangeListener listener, String fileName) {
        FileMonitorTask task = timerEntries.remove(fileName + listener.hashCode());
		if (task != null) {
		    task.cancel();
		}
    }

    /**
     * Notify the interested part
     * 
     * @param listener
     * @param fileName
     * @throws IOException 
     */
    protected void fireFileChangeEvent(FileChangeListener listener, String fileName) throws IOException {
    	listener.fileChanged(fileName);
    }

    /**
     * This is the timed task that checks the designated file
     * for change.
     * 
     * @author Fredrik
     */
    class FileMonitorTask extends TimerTask { 
    	FileChangeListener listener;
        String fileName;
        File monitoredFile;
        long lastModified;

        /**
         * Create a new FileMonitorTask
         * 
         * @param listener
         * @param fileName
         * @throws FileNotFoundException
         */
        public FileMonitorTask(FileChangeListener listener, String fileName) throws FileNotFoundException {
		    this.listener = listener;
		    this.fileName = fileName;
		    this.lastModified = 0;
	
		    monitoredFile = new File(fileName);
		    if (!monitoredFile.exists()) {  // but is it on CLASSPATH?
		        URL fileURL = listener.getClass().getClassLoader().getResource(fileName);
				if (fileURL != null) {
				    monitoredFile = new File(fileURL.getFile());
				} else {
				    throw new FileNotFoundException("File Not Found: " + fileName);
				}
		    }
		    this.lastModified = monitoredFile.lastModified();
		}

        /**
         * Run Forrest, run!
         */
        public void run() {
		    long lastModified = monitoredFile.lastModified();
		    if (lastModified != this.lastModified) {
		        this.lastModified = lastModified;
		        try {
		        	fireFileChangeEvent(this.listener, this.fileName);
		        } catch(Exception e) {
		        	log.error("FileMonitor caught an exception: "+e+". Do not throw exceptions to the FileMonitor!", e);
		        }
		    }
		}
    }
}
