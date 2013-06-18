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
package com.cubeia.firebase.server.service.crypto;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoFilter;
import com.cubeia.firebase.server.instance.SystemCoreException;

public class DefaultCryptoService implements SystemCryptoProvider, Service {

	private static final String MY_NS = "service.crypto";
	
	private ServerConfig sConf;
	private ServiceContext con;
	private ClusterConfig cConf;
	
	private Class<CryptoFilter> minaFilter;
	private SSLKeyStore keyStore;
	
	@Override
	public Class<CryptoFilter> getMinaEncryptionFilter() {
		return minaFilter;
	}

	@Override
	public SystemKeyStore getSystemKeyStore() {
		return keyStore;
	}
	
	@Override
	public boolean isEncryptionMandatory() {
		return cConf.isEncryptionMandatory();
	}

	
	// --- SERVICE METHODS --- //
	
	@Override
	public void destroy() { }

	@Override
	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		setupConfig();
		checkMinaCrypto();
		checkSsl();
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }

	
	// --- PRIVATE METHODS --- //
	
	@SuppressWarnings("unchecked")
	private void initMinaFilter() throws SystemException {
		String cl = cConf.getEncryptionFilter();
		try {
			minaFilter = (Class<CryptoFilter>) getClass().getClassLoader().loadClass(cl);
		} catch(Exception e) {
			throw new SystemCoreException("Failed to load mina crypto filter '" + cl + "'", e);
		} 
	}
	
	private void setupConfig() throws SystemException {
		ServiceRegistry reg = con.getParentRegistry();
		ServerConfigProviderContract server = reg.getServiceInstance(ServerConfigProviderContract.class);
		sConf = server.getConfiguration(ServerConfig.class, new Namespace(MY_NS));
		ClusterConfigProviderContract cluster = reg.getServiceInstance(ClusterConfigProviderContract.class);
		cConf = cluster.getConfiguration(ClusterConfig.class, new Namespace(MY_NS));
	}
	
	private void checkSsl() throws SystemException {
		if(sConf.isSslEnabled()) {
			initKeyStore();
		}
	}

	private void initKeyStore() throws SystemException {
		keyStore = new SSLKeyStore(sConf);
	}

	private void checkMinaCrypto() throws SystemException {
		if(cConf.isEncryptionEnabled()) {
			initMinaFilter();
		}
	}
}
