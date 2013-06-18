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

package com.cubeia.backoffice.users;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;


public class Configuration {

	private static Logger log = LoggerFactory.getLogger(Configuration.class);
	
	private String defaultPropertiesPath;
	private String overridePropertiesPath;
	private Properties defaultProperties;	
	private Properties overrideProperties;
	
	public void init() throws IOException{
		defaultProperties = new Properties();
		defaultProperties.load(new ClassPathResource(defaultPropertiesPath).getInputStream());
		
		try{
			overrideProperties = new Properties();
			overrideProperties.load(new ClassPathResource(overridePropertiesPath).getInputStream());			
		}catch (Exception e) {
			log.warn("No user-service.properties configuration file found. Using default.");
		}
	}
	
	public Properties getProperties() {
		Properties mergedProperties = new Properties();
		if(defaultProperties != null) {
			mergedProperties.putAll(defaultProperties);
		}
		if(overrideProperties != null) {
			mergedProperties.putAll(overrideProperties);
		}
		return mergedProperties;
	}

	public String getProperty(String key){
		Properties props = overrideProperties != null && overrideProperties.containsKey(key) 
				? overrideProperties : defaultProperties;
		return props.getProperty(key);
	}
	
	public String getOperatorProperty(String key, Long operatorId) {
		String value = getProperty(key + ".operator." + operatorId);
		if(value == null){
			value = getProperty(key);
		}
		return value;
	}

	public boolean isPasswordEncryptionEnabled(){
		return Boolean.parseBoolean(getProperty("user.service.password.encryption.enabled"));
	}
	
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public void setOverrideProperties(Properties overrideProperties) {
		this.overrideProperties = overrideProperties;
	}

	public void setDefaultPropertiesPath(String defaultPropertiesPath) {
		this.defaultPropertiesPath = defaultPropertiesPath;
	}

	public void setOverridePropertiesPath(String overridePropertiesPath) {
		this.overridePropertiesPath = overridePropertiesPath;
	}
	
}
