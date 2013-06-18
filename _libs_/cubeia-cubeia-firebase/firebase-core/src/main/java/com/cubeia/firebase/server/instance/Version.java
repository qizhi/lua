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
package com.cubeia.firebase.server.instance;

import java.io.InputStream;
import java.util.Properties;

public class Version {

	public static final String FULL_VERSION;
	/*public static final String MAJOR_VERSION;
	public static final String MINOR_VERSION;*/
	
	public static final String VM_NAME =  System.getProperty("java.vm.name");
	public static final String VM_VERSION = System.getProperty("java.vm.version");
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String OS_ARCH = System.getProperty("os.arch");
	public static final String OS_VERSION = System.getProperty("os.name");
	
	
	static {
		//String one = null;
		//String two = null;
		String full = null;
		try {
			InputStream in = Version.class.getClassLoader().getResourceAsStream("version.properties");
			Properties props = new Properties();
			props.load(in);
			in.close();
			//one = props.getProperty("major.version");
			//two = props.getProperty("minor.version");
			//full = one + "." + two;
			full = props.getProperty("full-version");
		} catch(Exception e) {
			System.err.println("Failed to read VERSION from 'version.properties' which should exist in the class path");
			//one = "N/A";
			//two = "N/A";
			full = "N/A";
		}
		FULL_VERSION = full;
		//MAJOR_VERSION = one;
		//MINOR_VERSION = two;
	}
}
