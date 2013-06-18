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

import org.apache.log4j.Logger;

public class FileUtil {
	
	private static Logger log = Logger.getLogger(FileUtil.class);
	
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
	public static void safeMkdir(File dir) {
		long start = System.currentTimeMillis();
		if(!dir.exists() && !dir.mkdir()) {
			while(!dir.mkdir()) {
				log.info("Waiting for your file system to allow mkdir operation (Windows hack)");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					log.error(e,e);
				}
				if ((System.currentTimeMillis() - start) > 10000) {
					log.warn("Windows hack timed out (10 seconds), could not create temp work folder ["+dir+"]");
					break;
				}
			}
		}
	}
	
}
