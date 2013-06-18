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
package com.cubeia.firebase.service.mcastgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.util.IoUtil;

public class MCastGenerationServiceImpl implements MCastGenerationService, Service {

	private static final int DEFAULT_NA_PORT_BASE = 8900;
	private static final String DEFAULT_NA_ADDRESS_BASE = "224.224.50.1";
	private static final String DEFAULT_CONFIG_FILE = "mcastgen.conf";
	
	private static final String NA_PORT_BASE_KEY = "_baseNaPort";
	private static final String NA_ADDRESS_BASE_KEY = "_baseNaAddress";
	private static final String ADDRESS_BASE_KEY = "_baseAddress";
	private static final String PORT_BASE_KEY = "_basePort";
	
	private NaGenerator naGenerator;
	private MCastGeneratorConfig config;
	private Generator generator;
	private ServiceContext con;
	private Map<String,String> props;
	
	public void destroy() { }

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		initConf();
		readPropertyMap();
		initNaGenerator();
		initGenerator();
	}

	public void start() { }

	public void stop() { }

	
	// --- PUBLIC INTERFACE --- //

	private void initGenerator() throws SystemCoreException {
		SocketAddress genBase = getBaseAddress();
		generator = new Generator(genBase, props);
	}
	
	private SocketAddress getBaseAddress() throws SystemCoreException {
		int portBase = getPortBase();
		String addressBase = getAddressBase();
		try {
			return new SocketAddress(addressBase + ":" + portBase);
		} catch (UnknownHostException e) {
			throw new SystemCoreException("Failed to create base address from config.", e);
		}
	}
	
	private String getAddressBase() {
		if(props.containsKey(ADDRESS_BASE_KEY)) {
			return props.get(ADDRESS_BASE_KEY);
		} else {
			return getClusterConnAddress();
		}
	}

	private String getClusterConnAddress() {
		ServiceRegistry reg = con.getParentRegistry();
		ConnectionServiceContract serv = reg.getServiceInstance(ConnectionServiceContract.class);
		SocketAddress address = serv.getSharedConnection().getMCastAddress();
		return address.getHost().getHostAddress();
	}

	private int getPortBase() throws SystemCoreException {
		if(props.containsKey(PORT_BASE_KEY)) {
			return Integer.parseInt(props.get(PORT_BASE_KEY));
		} else {
			return getClusterConnPort();
		}
	}
	
	private int getClusterConnPort() {
		ServiceRegistry reg = con.getParentRegistry();
		ConnectionServiceContract serv = reg.getServiceInstance(ConnectionServiceContract.class);
		SocketAddress address = serv.getSharedConnection().getMCastAddress();
		return address.getPort();
	}

	/*private int getClusterConnAddressEnd() throws SystemCoreException {
		ServiceRegistry reg = con.getParentRegistry();
		try {
			ServerConfigProviderContract contr = reg.getServiceInstance(ServerConfigProviderContract.class);
			if(contr == null) throw new SystemCoreException("Failed service dependencies; Could not find server configuration service '" + Constants.SERVER_CONFIG_SERVICE_NS + "'.");
			ServerConfig conf = contr.getConfiguration(ServerConfig.class, null);
			return conf.getClusterBindAddressEndPort();
		} catch(ConfigurationException e) {
			throw new SystemCoreException("Failed to access server configuration; Given error message: " + e.getMessage(), e);
		}
	}*/

	private void initNaGenerator() throws SystemCoreException {
		int portBase = getNaPortBase();
		String addressBase = getNaAddressBase();
		try {
			naGenerator = new NaGenerator(new SocketAddress(addressBase + ":" + portBase));
		} catch (UnknownHostException e) {
			throw new SystemCoreException("Failed to create base address from config.", e);
		}
	}

	private String getNaAddressBase() {
		if(props.containsKey(NA_ADDRESS_BASE_KEY)) {
			return props.get(NA_ADDRESS_BASE_KEY);
		} else {
			return DEFAULT_NA_ADDRESS_BASE;
		}
	}

	private int getNaPortBase() {
		if(props.containsKey(NA_PORT_BASE_KEY)) {
			return Integer.parseInt(props.get(NA_PORT_BASE_KEY));
		} else {
			return DEFAULT_NA_PORT_BASE;
		}
	}

	private void initConf() throws SystemCoreException {
		ServiceRegistry reg = con.getParentRegistry();
		ClusterConfigProviderContract cc = reg.getServiceInstance(ClusterConfigProviderContract.class);
		config = cc.getConfiguration(MCastGeneratorConfig.class, null);
	}
	
	private void readPropertyMap() throws SystemCoreException {
		InputStream in = findProperties();
		Properties p = new Properties();
		try {
			p.load(in);
			props = new TreeMap<String, String>();
			for (Object key : p.keySet()) {
				props.put(key.toString(), p.get(key).toString());
			}
		} catch (IOException e) {
			throw new SystemCoreException("Failed to read config file!", e);
		} finally {
			IoUtil.safeClose(in);
		}
	}
	
	private InputStream findProperties() throws SystemCoreException {
		InputStream in = tryFindConfiguredConf();
		if(in != null) return in; // EARLY RETURN
		in = tryFindConfigDirFile();
		if(in != null) return in; // EARLY RETURN
		return openDefaultConfig();
	}

	private InputStream openDefaultConfig() throws SystemCoreException {
		String name = "com/cubeia/firebase/service/mcastgen/" + DEFAULT_CONFIG_FILE;
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		if(stream == null) throw new SystemCoreException("Failed to read default config! Missing file '" + name + "'");
		return stream;
	}

	private InputStream tryFindConfigDirFile() throws SystemCoreException {
		File dir = con.getServerConfigDirectory();
		File file = new File(dir, DEFAULT_CONFIG_FILE);
		return checkReturnFile(file, true);
	}

	private InputStream tryFindConfiguredConf() throws SystemCoreException {
		File file = config.getServiceConfiguration();
		return checkReturnFile(file, false);
	}

	private InputStream checkReturnFile(File file, boolean allowNoExist) throws SystemCoreException {
		if(file == null) {
			return null;
		} else {
			if(file.exists()) {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new SystemCoreException("Failed to find configured MCast Address Generation config file '" + file.getAbsolutePath() + "'", e);
				}
			} else if(!allowNoExist){
				throw new SystemCoreException("Failed to find configured MCast Address Generation config file '" + file.getAbsolutePath() + "'");
			} else {
				return null;
			}
		}
	}

	public SocketAddress getGeneratedAddress(String serviceId) throws UnknownHostException {
		Arguments.notNull(serviceId, "service id");
		if(!generator.isKnown(serviceId)) {
			return naGenerator.generate();
		} else {
			return generator.generate(serviceId);
		}
	}
}
