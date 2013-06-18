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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.util.IoUtil;

public class SSLKeyStore implements SystemKeyStore {
	
	// private final static String keyStoreType = "jks";
	
	private final static String sslAlgorithm = "SSL";
	
	private final static String keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

	
	// --- INSTANCE METHODS --- //

	private final File store;
	private final char[] passwd;

	private final KeyStoreType type;

	public SSLKeyStore(File store, String passwd, KeyStoreType type) throws SystemException {
		if(type == null) type = KeyStoreType.JKS;
		if(store == null) throw new SystemCoreException("SSL is enabled in config but not key store is specified.");
		if(passwd == null) throw new SystemCoreException("SSL is enabled in config but not key store password is specified.");
		if(!store.exists()) throw new SystemCoreException("SSL is enabled in config but key store '" + store + "' does not exist.");
		if(!store.canRead()) throw new SystemCoreException("SSL is enabled in config but key store '" + store + "' can not be read.");
		Logger.getLogger(getClass()).info("Using SSL from keystore: " + store + " (" + type.getName() + ")");
		this.type = type;
		this.passwd = passwd.toCharArray();	
		this.store = store;
	}
	
	public SSLKeyStore(ServerConfig conf) throws SystemException {
		this(conf.getSslKeyStore(), conf.getSslKeyStorePassword(), conf.getSslKeyStoreType());
	}

	public SSLContext createSSLContext() throws SystemException {
		KeyManager[] kms = getKeyManagers();
		try {
			SSLContext context = SSLContext.getInstance(sslAlgorithm);
			context.init(kms, null, null);
			return context;
		} catch(Exception e) {
			throw new SystemCoreException("Failed to initialize SSL context", e);
		}
	}
	
	private KeyManager[] getKeyManagers() throws SystemException {
		InputStream in = null;
		try {
			KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
			in = new BufferedInputStream(new FileInputStream(store));
			KeyStore ks = KeyStore.getInstance(type.getName());
			ks.load(in, passwd);
			kmFactory.init(ks, passwd);
			return kmFactory.getKeyManagers();
		} catch(Exception e) {
			throw new SystemCoreException("Failed to initialize keys from key store '" + store + "'", e);
		} finally {
			IoUtil.safeClose(in);
		}
	}
}
