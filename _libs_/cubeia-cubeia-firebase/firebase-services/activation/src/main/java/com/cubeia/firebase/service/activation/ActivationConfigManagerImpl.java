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
package com.cubeia.firebase.service.activation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;

/**
 * Simple implementation of a config manager. This class keeps all sources a map keyed to
 * name and config type. It is thread safe and exposes available configuration sources as
 * an {@link ActivationConfigMBean mbean}.
 * 
 * @see ActivationConfigManager
 * @author Lars J. Nilsson
 */
public class ActivationConfigManagerImpl implements ActivationConfigManager, Service {

	private static final String JMX_NAME = "com.cubeia.firebase.service:type=ActivationConfigManager";

	private final Logger log = Logger.getLogger(getClass());
	
	private final Map<Key, ConfigSource> sources = new ConcurrentHashMap<Key, ConfigSource>();
	private final Set<ConfigSourceListener> lists = new CopyOnWriteArraySet<ConfigSourceListener>();

	private final ActivationConfig stats = new ActivationConfig(this);
	
	private ServiceContext con;
	
	
	// --- SERVICE METHODS --- //
	
	@Override
	public void destroy() { 
		destroyJmx();
	}
	
	@Override
	public void init(ServiceContext con) throws SystemException { 
		this.con = con;
		initJmx();
	}
	
	@Override
	public void start() { }
	
	@Override
	public void stop() { }
	
	
	// --- CONFIG MANAGER --- //
	
	@Override
	public void addConfigSourceListener(ConfigSourceListener list) {
		Arguments.notNull(list, "list");
		lists.add(list);
	}

	@Override
	public ActivationConfigSource getConfigSourceFor(String gameName, ActivationType type) {
		Arguments.notNull(type, "type");
		Arguments.notNull(gameName, "gameName");
		ConfigSource s = sources.get(new Key(gameName.toLowerCase(), type));
		log.debug("Config manager for game '" + gameName + "' is returning source: " + s);
		return (s == null ? null : new ActivationSource(s, type));
	}

	@Override
	public void registerConfigSource(ConfigSource src, ActivationType type) {
		Arguments.notNull(src, "src");
		Arguments.notNull(type, "type");
		log.info("Registering config source: " + src);
		boolean isMod = sources.containsKey(src.getName());
		sources.put(new Key(src.getName().toLowerCase(), type), src);
		if(isMod) {
			fireModified(new ActivationSource(src, type));
		} else {
			fireAdded(new ActivationSource(src, type));
		}
	}
	
	@Override
	public void unregisterConfigSource(String gameName, ActivationType type) {
		Arguments.notNull(type, "type");
		Arguments.notNull(gameName, "gameName");
		ConfigSource src = null;
		if((src = sources.remove(new Key(gameName.toLowerCase(), type))) != null) {
			log.info("Unregistering config source: " + src);
			fireRemoved(new ActivationSource(src, type));
		}
	}

	@Override
	public void removeConfigSourceListener(ConfigSourceListener list) {
		Arguments.notNull(list, "list");
		lists.remove(list);
	}
	
	
	
	// -- PACKAGE METHODS --- //
	
	/**
	 * @return The number of configurations registered
	 */
	int countConfigs() {
		return sources.size();
	}
	
	/**
	 * @return All configurations in a concurrent map, never null
	 */
	Map<Key, ConfigSource> getSources() {
		return sources;
	}
	

	
	// --- PRIVATE METHODS --- //
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName(JMX_NAME);
			if (mbs != null) {
			    mbs.registerMBean(stats, monitorName);
			}
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName(JMX_NAME);
	        if(mbs != null && mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to stop mbean", e);
		}
	}
	
	private void fireAdded(ActivationSource src) { 
		for (ConfigSourceListener l : lists) {
			l.sourceAdded(src);
		}
	}
	
	private void fireRemoved(ActivationSource src) { 
		for (ConfigSourceListener l : lists) {
			l.sourceRemoved(src);
		}
	}
	
	private void fireModified(ActivationSource src) { 
		for (ConfigSourceListener l : lists) {
			l.sourceModified(src);
		}
	}
	
	
	// --- PACKAGE CLASSES --- //
	
	/**
	 * This class is used internally for mapping sources to a compound key 
	 * consisting of name and activation type.
	 */
	static class Key {
		
		final String name;
		final ActivationType type;
		
		public Key(String name, ActivationType type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public boolean equals(Object o) {
			return ((Key)o).name.equals(name) && ((Key)o).type == type;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() ^ type.hashCode();
		}
	}
}
