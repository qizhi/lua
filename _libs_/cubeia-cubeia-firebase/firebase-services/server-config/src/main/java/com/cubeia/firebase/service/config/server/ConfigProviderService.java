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
package com.cubeia.firebase.service.config.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.server.conf.ConfigurationAdapter;
import com.cubeia.firebase.server.conf.MapConfiguration;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.service.config.ConfigurationDetails;
import com.cubeia.util.IoUtil;

/**
 * Service implementation for the local server config provider. This service
 * relies on a default configuration and as such, has no pre-requisites. However, if
 * a local config file cannot be found, a worning will be printed in the logs.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
public class ConfigProviderService implements Service, ServerConfigProviderContract {
	
	private static final String PROPERTY_PATTERN = "\\$\\{[^}]+}";
	private static final String SERVER_CONF = "server.props";
	private static final String SERVER_DEF_CONF_NAME = "server.default.props";
	private static final String SERVER_DEF_CONF = "com/cubeia/firebase/service/config/server/" + SERVER_DEF_CONF_NAME;

	
	// --- INSTANCE MEMBERS --- //
	
	private final Logger log = Logger.getLogger(getClass());
	
	private File configDir;
	private ConfigurationAdapter adapter;
	private MapConfiguration configuration;
	private ServiceContext con;
	
	public void destroy() {
		destroyJmx();
		configDir = null;
		adapter = null;
	}

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		this.configDir = con.getServerConfigDirectory();
		setupServerConf();
		initJmx();
	}

	public void start() { }

	public void stop() { }

	public <T extends Configurable> T getConfiguration(Class<T> cl, Namespace ns) {
		return adapter.implement(cl, ns);
	}
	
	public ConfigProperty[] getAllProperties() {
		return cloneProperties();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.service:type=ServerConfigProvider");
	        mbs.registerMBean(new ConfigurationDetails(this), monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.service:type=ServerConfigProvider");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to stop mbean", e);
		}
	}
	
	private void setupServerConf() throws SystemCoreException {
		Map<PropertyKey, String> def = readConf(true);
		Map<PropertyKey, String> map = readConf(false);
		// override props...
		if(map != null) def.putAll(map);
		// create conf
		
		/*for (Entry<PropertyKey, String> e : def.entrySet()) {
			System.out.println(" *** " + e.getKey().toString() + " = " + e.getValue());
		}*/
		
		configuration = new MapConfiguration(def);
		adapter = new ConfigurationAdapter(configuration);
	}
	
	private ConfigProperty[] cloneProperties() {
		int count = 0;
		Map<PropertyKey, String> map = configuration.cloneProperties();
		ConfigProperty[] arr = new ConfigProperty[map.size()];
		for (Entry<PropertyKey, String> e : map.entrySet()) {
			arr[count++] = new ConfigProperty(e.getKey(), e.getValue());
		}
		return arr;
	}
	
	private Map<PropertyKey, String> readConf(boolean isDef) throws SystemCoreException {
		InputStream in = null;
		try {
			if(isDef) in = findNodeDefConfStream();
			else in = findNodeConfStream();
			if(in == null) {
				if(!isDef) SystemLogger.info("Server configuration provider could not find any '" + SERVER_CONF + "' file inconfiguration directory or in class path; Using default only.");
				return null;
			} else {
				Properties props = new Properties();
				props.load(in);
				loadSystemProperties(props);
				return MapConfiguration.convert(props);
			}
		} catch(IOException e) {
			throw new SystemCoreException("Server configuration provider failed to read configuration; Received message: " + e.getMessage(), e);
		} finally {
			IoUtil.safeClose(in);
		}
	}
	
	private void loadSystemProperties(Properties props) {
		Pattern p = Pattern.compile(PROPERTY_PATTERN);
		for (Entry<Object, Object> e : props.entrySet()) {
			String tmp = e.getValue().toString();
			Matcher m = p.matcher(tmp);
			while(m.find()) {
				String group = m.group();
				// System.out.println(group);
				String key = group.substring(2, group.length() - 1);
				String val = System.getProperty(key, "");
				tmp = tmp.replace(group, val);
			}
			e.setValue(tmp);
		}
	}

	private InputStream findNodeConfStream() throws IOException {
		File file = new File(configDir, SERVER_CONF);
		if(file.exists()) return new FileInputStream(file); // EARLY RETURN
		else return getClass().getClassLoader().getResourceAsStream(SERVER_CONF);
	}
	
	
	private InputStream findNodeDefConfStream() throws IOException {
		File file = new File(configDir, SERVER_DEF_CONF_NAME);
		if(file.exists()) return new FileInputStream(file); // EARLY RETURN
		else return getClass().getClassLoader().getResourceAsStream(SERVER_DEF_CONF);
	}
}
