/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.backoffice.accounting.perf;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/*
 * This class loads a set of properties from the class path. Default path
 * is "bots.properties", to change this use system property "bot.properties.path".
 */
public class PropertiesLoader {

	private static final String PATH = System.getProperty("bot.properties.path", "bots.properties");
	
	private static Logger log = Logger.getLogger(PropertiesLoader.class);
	
	private PropertiesLoader() { }
	
	public static Properties load() {
		InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream(PATH);
		try {
			if(in == null) throw new FileNotFoundException(PATH);
			in = new BufferedInputStream(in);
			Properties p = new Properties();
			p.load(in);
			in.close();
			return p;
		} catch(IOException e) {
			log.error("Failed to load config properties: " + e.getMessage(), e);
			return new Properties();
		}
	}
}
