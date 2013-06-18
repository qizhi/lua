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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.cubeia.firebase.api.util.TaskRepeater;
import com.cubeia.util.IoUtil;

public class Files {

	private Files() { }
	
	/**
	 * <p>Work around method for creating directories on flaky OS's (i.e. Windows).
	 * Apparently Java will sometimes fail on mkdir on Windows when the directory
	 * has recently been deleted.</p>
	 * 
	 * <p>NOTE: This method will block the calling Thread for up to 10 seconds while waiting for 
	 * the operative file system to accept mkdir().</p> 
	 * 
	 * @param dir, the directory to create
	 */
	public static void safeMkdir(final File dir) {
		TaskRepeater r = new TaskRepeater("safe mkdir", 20, 500);
		r.safeExecute(new Callable<Boolean>() {
			
			@Override
			public Boolean call() {
				if(!dir.exists()) {
					return dir.mkdir();
				} else {
					return true;
				}
			}
		});
	}
	
	/**
	 * Attempt to find an entry in the zip file which exists in the target
	 * directory, check that the file has the same or a new timestamp than the
	 * zip entry.
	 * 
	 * @param in zip file input stream
	 * @param dir Target directory to compary with
	 * @return False if the target directory appear to be newer or the same age as the zip arhive
	 * @throws IOException
	 */
	public static boolean needsUpdating(ZipInputStream in, File dir) throws IOException {
		Set<String> existing = new TreeSet<String>();
		recursiveAbsolutePath(dir, existing);
		ZipEntry entry = null;
		while((entry = in.getNextEntry()) != null) {
			File next = new File(dir, entry.getName());
			if(!entry.isDirectory() && existing.contains(next.getAbsolutePath())) {
				long test = entry.getTime();
				if (test == -1) return true;
				else return (test > next.lastModified()); // EARLY RETURN !!
			}
		}
		return true;
	}
	
	public static void explode(ZipFile file, File dir) throws IOException {
		for(Enumeration<? extends ZipEntry> en = file.entries(); en.hasMoreElements(); ) {
			ZipEntry entry = en.nextElement();
			File next = new File(dir, entry.getName());
			if(entry.isDirectory()) {
				next.mkdirs();
			} else {
				if (next.getParentFile() != null) next.getParentFile().mkdirs();
				InputStream in = file.getInputStream(entry);
				try {
					writeBytes(in, next);
				} finally {
					IoUtil.safeClose(in);
				}
			}
		}
	}
	
	/**
	 * @param root Directory to copy from, must not be null
	 * @param target Directory to copy to, must not be null
	 * @param recursive True if the copy should traverse sub-directories, false otherwise
	 * @throws IOException On Io errors
	 */
	public static void copyDirectory(File root, File target, boolean recursive) throws IOException {
		for (File file : root.listFiles()) {
			File next = new File(target, file.getName());
			if(file.isDirectory() && recursive) {
				next.mkdir();
				copyDirectory(file, next, recursive);
			} else if(file.isFile()){
				copyFile(file, next);
			}
		}
	}	
	
	public static void writeBytes(InputStream in, File file) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			IoUtil.transfer(in, out);
		} finally {
			IoUtil.safeClose(out);
		}
	}
	
	public static void copyFile(File in, File out) throws IOException {
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
	}

	public static void recursiveDelete(File path) {
	    if(path.exists()) {
	    	File[] files = path.listFiles();
	    	for(int i=0; i < files.length; i++) {
	    		if(files[i].isDirectory()) {
	    			recursiveDelete(files[i]);
	    		} else {
	    			files[i].delete();
	    		}
	    	}
	    	path.delete();
	    }
	}
	
	/**
	 * @param folder directory to start searching in, not null
	 * @param regex regular expression which will be matched against the file names, not null
	 * @param recursive True if the search should descend into sub-directories, false otherwise
	 */
	public static List<File> getResources(File folder, String regex, boolean recursive) throws IOException {
		List<File> found = new ArrayList<File>();
		File[] files = folder.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.getPath().matches(regex)) {
					found.add(files[i]);
				}
				if (file.isDirectory() && recursive) {
					getResources(file, regex, true);
				}
			}
		}
		return found;
	}
	
	public static URL[] toUrls(File[] files) {
		if(files == null) return null;
		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				urls[i] = files[i].toURI().toURL();
			} catch(MalformedURLException e) {
				// FIXME: Exception?
				throw new IllegalStateException("malformed URL in file name?", e);
			}
		}
		return urls;
	}

	
	public static URL toUrls(File file) {
		try {
			return (file == null ? null : file.toURI().toURL());
		} catch (MalformedURLException e) {
			// FIXME: Exception?
			throw new IllegalStateException("malformed URL in file name?", e);
		}
	}
	
	public static void writeString(String content, File file) throws IOException {
		writeString(content, file, null);
	}
	
	public static void writeString(String content, File file, String charset) throws IOException {
		byte[] bytes = (charset == null ? content.getBytes() : content.getBytes(charset));
		writeBytes(new ByteArrayInputStream(bytes), file);
	}
	
	public static String readAsString(File file) throws IOException {
		return readAsString(file, null);
	}
	
	public static String readAsString(File file, String charset) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			return IoUtil.readAsString(in, charset);
		} finally {
			IoUtil.safeClose(in);
		}
	}
	
	public static byte[] getBytes(File file) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			IoUtil.transfer(in, out);
		} finally {
			IoUtil.safeClose(in);
		}
		return out.toByteArray();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private static void recursiveAbsolutePath(File dir, Set<String> set) {
		File[] arr = dir.listFiles();
		if (arr != null) {
			for (File f : arr) {
				if (f == null) continue;
				if (f.isDirectory()) recursiveAbsolutePath(f, set);
				else set.add(f.getAbsolutePath());
			}
		}
	}
}
