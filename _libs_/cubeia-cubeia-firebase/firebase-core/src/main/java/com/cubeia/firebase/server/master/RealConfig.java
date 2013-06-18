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
package com.cubeia.firebase.server.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.commands.Config;
import com.cubeia.firebase.server.commands.Config.Type;
import com.cubeia.firebase.server.conf.Configuration;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.conn.ClusterException;


public class RealConfig implements Configuration, Initializable<MasterComponentContext>{

	private static final long FILE_CHECK_INTERVAL = 2000;

	
	/// --- INSTANCE MEMBERS --- ///
	
	protected final String id;
	private final Logger log = Logger.getLogger(getClass());
	
	private File file;
	protected Map<PropertyKey, String> props;
	private ScheduledFuture<?> changeFuture;
	private MasterComponentContext con;
	
	public RealConfig(String id, File conf) throws IOException {
		Arguments.notNull(id, "id");
		Arguments.notNull(conf, "conf");
		this.id = id;
		this.file = conf;
		props = loadConf();
	}
	
	protected RealConfig(String id, Configuration conf) {
		Arguments.notNull(id, "id");
		this.id = id;
		props = new ConcurrentHashMap<PropertyKey, String>();
		for (PropertyKey key : conf.getKeys()) {
			if(accept(key)) {
				props.put(key, conf.getProperty(key.getNamespace(), key.getProperty()));
			}
		}
	}
	
	public void destroy() {
		changeFuture.cancel(false);
	}
	
	public void init(MasterComponentContext con) throws SystemCoreException {
		ScheduledExecutorService serv = con.getSystemScheduler();
		changeFuture = serv.scheduleAtFixedRate(new FileChecker(), FILE_CHECK_INTERVAL, FILE_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
		this.con = con;
	}
	
	public int size() {
		return props.size();
	}
	
	public HashMap<PropertyKey, String> cloneProperties() {
		return new HashMap<PropertyKey, String>(props);
	}
	
	public String getId() {
		return id;
	}

	public PropertyKey[] getKeys() {
		return props.keySet().toArray(new PropertyKey[props.size()]);
	}

	public String[] getProperties(Namespace ns) {
		Arguments.notNull(ns, "namespace");
		List<String> tmp = new LinkedList<String>();
		for(PropertyKey key : props.keySet()) {
			if(key.getNamespace().equals(ns)) {
				tmp.add(key.getProperty());
			}
		}
		return tmp.toArray(new String[tmp.size()]);
	}

	public String getProperty(Namespace ns, String prop) {
		return getProperty(new PropertyKey(ns, prop));
	}
	
	public String getProperty(PropertyKey key) {
		Arguments.notNull(key, "key");
		return props.get(key);
	}

	public boolean hasProperty(Namespace ns, String prop) {
		return props.containsKey(new PropertyKey(ns, prop));
	}
	
	
	/// --- PROTECTED METHODS --- ///
	
	protected boolean accept(PropertyKey key) {
		return true;
	}
	
	
	/// --- PRIVATE MEMBERS --- ///
	
	public void applyDelta(ConfigDelta delta) {
		for (ConfigProperty prop : delta.getRemovedProperties()) {
			props.remove(prop.getKey());
		}
		for (ConfigProperty prop : delta.getAddedProperties()) {
			props.put(prop.getKey(), prop.getValue());
		}
		for (ConfigProperty prop : delta.getModifiedProperties()) {
			props.put(prop.getKey(), prop.getValue());
		}
	}

	public void notifyDelta(ConfigDelta delta) {
		notifyDelta(delta.getRemovedProperties(), Config.Type.DELTA_REM);
		notifyDelta(delta.getAddedProperties(), Config.Type.DELTA_ADD);
		notifyDelta(delta.getModifiedProperties(), Config.Type.DELTA_MOD);
	}
	
	private void notifyDelta(ConfigProperty[] props, Type type) {
		if(props == null || props.length == 0) return; // SANITY CHECK
		try {
			con.getCluster().getCommandDispatcher().dispatch(new Config(type, props));
		} catch (ClusterException e) {
			log.error("failed to dispatch delta change command", e);
		}
	}

	private Map<PropertyKey, String> loadConf() throws IOException {
		Map<PropertyKey, String> map = new ConcurrentHashMap<PropertyKey, String>();
		InputStream in = new FileInputStream(file);
		try {
			Properties tmp = new Properties();
			tmp.load(in);
			for (Object o : tmp.keySet()) {
				String sKey = o.toString();
				PropertyKey key = toPropertyKey(sKey);
				if(key != null && accept(key)) {
					map.put(key, tmp.getProperty(sKey));
				}
			}
		} finally {
			try { in.close(); } catch(IOException e) { }
		}
		return map;
	}
	
	private PropertyKey toPropertyKey(String key) {
		if(key.trim().startsWith("#")) return null; // SANITYC CHECK
		String[] parts = key.split("\\.");
		if(parts.length == 0) return null; // SANITY CHECK
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < parts.length - 1; i++) {
			if(i != 0) b.append(".");
			b.append(parts[i]);
		}
		Namespace ns = new Namespace(b.toString());
		return new PropertyKey(ns, parts[parts.length - 1]);
	}
	
	
	/// --- INNER CLASSES --- ///
	
	private class FileChecker implements Runnable {
		
		private long lastChange = -1;
		
		public void run() {
			if(lastChange == -1) setInit();
			else checkChange();
		}

		private void checkChange() {
			long tmp = file.lastModified();
			if(tmp > lastChange) {
				lastChange = tmp;
				reload();
			}
		}

		private void reload() {
			try {
				Map<PropertyKey, String> next = loadConf();
				ConfigDelta delta = ConfigDelta.calculate(props, next);
				if(!delta.isEmpty()) {
					applyDelta(delta);
					notifyDelta(delta);
				}
			} catch(IOException e) {
				log.error("failed to check config changes", e);
			}
		}

		private void setInit() {
			lastChange = file.lastModified();
		}
	}
}
